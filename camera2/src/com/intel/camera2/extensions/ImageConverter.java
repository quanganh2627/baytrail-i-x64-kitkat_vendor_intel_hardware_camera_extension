/*
 * Copyright 2015, Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intel.camera2.extensions;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.media.Image.Plane;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class ImageConverter {
    private static final String TAG = "ImageConverter";

    /**
     * It converts from Image to gray byte array.
     * @param yuvImage {@link android.media.Image}: It supports YUV_420_888 format only.
     * @return byte array: gray image.
     */
    public static byte[] getGrayImageData(Image yuvImage) {
        if (yuvImage.getFormat() != ImageFormat.YUV_420_888) {
            throw new IllegalArgumentException("not supported yuv format");
        }
        Plane[] planes = yuvImage.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        int ySize = yBuffer.capacity();
        byte[] imageData = new byte[ySize];
        yBuffer.get(imageData, 0, ySize);
        yBuffer.rewind();
        return imageData;
    }

    public static byte[] getGrayImageData(Bitmap bitmap, int stride, int width, int height) {
        byte[] gray = new byte[stride * height];
        int[] argb = new int[stride * height];

        Buffer dst = IntBuffer.wrap(argb);
        bitmap.copyPixelsToBuffer(dst);

        int yIndex = 0;
        int R, G, B, Y;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < stride; i++) {

//                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to Gray algorithm
                Y = ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16;

                gray[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));

                index ++;
            }
        }
        return gray;
    }

    public static byte[] getYUV420SPImageData(Bitmap bitmap, int stride, int width, int height) {
        byte[] yuv420sp = new byte[stride * height * 3 / 2];
        int[] argb = new int[stride * height];

        Buffer dst = IntBuffer.wrap(argb);
        bitmap.copyPixelsToBuffer(dst);

        int yIndex = 0;
        int uvIndex = stride * height;

        int R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < stride; i++) {

//                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16;
                U = ( ( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
                V = ( ( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) { 
                    yuv420sp[uvIndex++] = (byte)((U<0) ? 0 : ((U > 255) ? 255 : U));
                    yuv420sp[uvIndex++] = (byte)((V<0) ? 0 : ((V > 255) ? 255 : V));
                }

                index ++;
            }
        }
        return yuv420sp;
    }

    public static void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height, int strideWidth) {
        final int frameSize = strideWidth * height;
        final int skipWidth = strideWidth - width;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * strideWidth, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                if (uvp >= yuv420sp.length || yp >= yuv420sp.length) return;
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    u = (0xff & yuv420sp[uvp++]) - 128;
                    v = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * u);
                int g = (y1192 - 833 * u - 400 * v);
                int b = (y1192 + 2066 * v);
//                int b = (y1192 + 1634 * v);
//                int g = (y1192 - 833 * v - 400 * u);
//                int r = (y1192 + 2066 * u);

                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;

                rgb[j * width + i] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00)
                        | ((b >> 10) & 0xff);
            }

            yp += skipWidth;
        }
    }

    public static void decodeYUV420_888(int[] rgb, byte[] yPlane, byte[] uvPlane,/* byte[] vPlane,*/ int width, int height, int strideWidth) {
        final int skipWidth = strideWidth - width;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = (j>>1) * strideWidth;
            int u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & yPlane[yp]) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) 
                {
                    u = (0xff & uvPlane[uvp++]) - 128;
                    v = (0xff & uvPlane[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * u);
                int g = (y1192 - 833 * u - 400 * v);
                int b = (y1192 + 2066 * v);

                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;

                rgb[j * width + i] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00)
                        | ((b >> 10) & 0xff);
//                int r = clip0_255((298 * y           + 409 * v + 128) >> 8);
//                int g = clip0_255((298 * y - 100 * u - 208 * v + 128) >> 8);
//                int b = clip0_255((298 * y + 516 * u           + 128) >> 8);
//                rgb[j * width + i] = 0xff000000 | (r << 16) | (g << 8) | (b << 0);
            }

            yp += skipWidth;
        }
    }

    public static int clip0_255(int value) {
        if (value < 0) {
            value = 0;
        } else if (value > 255) {
            value = 255;
        }
        return value;
    }

    public static byte[] getYuvDataFromImage(Image image) {
        int format = image.getFormat();
        int width = image.getWidth();
        int height = image.getHeight();
        int rowStride, pixelStride;
        int bpp = ImageFormat.getBitsPerPixel(format) / 8;

        Log.v(TAG, "image size = " + width +" x " + height + ", bit per pixel = " + bpp);
        // Read image data
        Plane[] planes = image.getPlanes();

        // Check image validity
        if (planes.length != 3) {
            Log.e(TAG, "plane count mismatch");
            return null;
        }

        int offset = 0;
        byte[] data = new byte[width * height * bpp];
        byte[] rowData = new byte[planes[0].getRowStride()];
        Log.v(TAG, "get data from " + planes.length + " planes");
        for (int i = 0; i < planes.length; i++) {
            ByteBuffer buffer = planes[i].getBuffer();
            rowStride = planes[i].getRowStride();
            pixelStride = planes[i].getPixelStride();
            Log.v(TAG, "pixelStride " + pixelStride);
            Log.v(TAG, "rowStride " + rowStride);
            Log.v(TAG, "width " + width);
            Log.v(TAG, "height " + height);
            Log.v(TAG, "bpp " + bpp);
            Log.v(TAG, "capacity " + buffer.capacity());
            // For multi-planar yuv images, assuming yuv420 with 2x2 chroma subsampling.
            int w = (i == 0) ? width : width / 2;
            int h = (i == 0) ? height : height / 2;
            for (int row = 0; row < h; row++) {
                if (pixelStride == bpp) {
                    // Special case: optimized read of the entire row
                    int length = w * bpp;
                    buffer.get(data, offset, length);
                    // Advance buffer the remainder of the row stride
                    buffer.position(buffer.position() + rowStride - length);
                    offset += length;
                } else {
                    // Generic case: should work for any pixelStride but slower.
                    // Use intermediate buffer to avoid read byte-by-byte from
                    // DirectByteBuffer, which is very bad for performance
                    buffer.get(rowData, 0, rowStride);
                    for (int col = 0; col < w; col++) {
                        data[offset++] = rowData[col * pixelStride];
                    }
                }
            }
            Log.v(TAG, "Finished reading data from plane " + i);
            buffer.rewind();
        }
        return data;
    }

    public static byte[] getImageData(Image image) {
        int format = image.getFormat();
        int width = image.getWidth();
        int height = image.getHeight();
        int rowStride, pixelStride;
        int bpp = ImageFormat.getBitsPerPixel(format) / 8;

        Log.v(TAG, "image size = " + width +" x " + height + ", bit per pixel = " + bpp);
        // Read image data
        Plane[] planes = image.getPlanes();

        // Check image validity
        if (planes.length != 3) {
            Log.e(TAG, "plane count mismatch");
            return null;
        }

        int offset = 0;
        byte[] data = new byte[width * height * bpp];
        byte[] rowData = new byte[planes[0].getRowStride()];
        Log.v(TAG, "get data from " + planes.length + " planes");
        for (int i = 0; i < planes.length; i++) {
            ByteBuffer buffer = planes[i].getBuffer();
            if (buffer.remaining() != buffer.limit()) {
                buffer.rewind();
            }
            rowStride = planes[i].getRowStride();
            pixelStride = planes[i].getPixelStride();
            Log.v(TAG, "pixelStride " + pixelStride);
            Log.v(TAG, "rowStride " + rowStride);
            Log.v(TAG, "width " + width);
            Log.v(TAG, "height " + height);
            Log.v(TAG, "bpp " + bpp);
            Log.v(TAG, "capacity " + buffer.capacity());
            // For multi-planar yuv images, assuming yuv420 with 2x2 chroma subsampling.
            int w = (i == 0) ? width : width / 2;
            int h = (i == 0) ? height : height / 2;
            for (int row = 0; row < h; row++) {
                if (pixelStride == bpp) {
                    // Special case: optimized read of the entire row
                    int length = w * bpp;
                    buffer.get(data, offset, length);
                    // Advance buffer the remainder of the row stride
                    buffer.position(buffer.position() + rowStride - length);
                    offset += length;
                } else {
                    // Generic case: should work for any pixelStride but slower.
                    // Use intermediate buffer to avoid read byte-by-byte from
                    // DirectByteBuffer, which is very bad for performance
                    buffer.get(rowData, 0, rowStride);
                    for (int col = 0; col < w; col++) {
                        data[offset++] = rowData[col * pixelStride];
                    }
                }
            }
            Log.v(TAG, "Finished reading data from plane " + i);
            buffer.rewind();
        }
        return data;
    }

    /**
     * It converts from Image to yuv byte array.
     * @param yuvImage {@link android.media.Image}: It supports YUV_420_888 format only.
     * @return byte array: yuv image.
     */
    public static byte[] getYuvImageData(Image yuvImage) {
        if (yuvImage.getFormat() != ImageFormat.YUV_420_888) {
            throw new IllegalArgumentException("not supported yuv format");
        }
        byte[] imageData;
        Plane[] planes = yuvImage.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();
        int yBufferSize = yBuffer.capacity();
        int uBufferSize = uBuffer.capacity();
        int vBufferSize = vBuffer.capacity();

        if (yBuffer == null || uBuffer == null || vBuffer == null) {
            Log.e(TAG, "yuvBuffer is null");
            return null;
        }

        Log.d(TAG, "image y plane size = " + yBufferSize + ", row stride = " + planes[0].getRowStride() + ", pixel stride = " + planes[0].getPixelStride());
        Log.d(TAG, "image u plane size = " + uBufferSize + ", row stride = " + planes[1].getRowStride() + ", pixel stride = " + planes[1].getPixelStride());
        Log.d(TAG, "image v plane size = " + vBufferSize + ", row stride = " + planes[2].getRowStride() + ", pixel stride = " + planes[2].getPixelStride());

        int uPixelStride = planes[1].getPixelStride();
        int vPixelStride = planes[2].getPixelStride();
        if (uPixelStride != vPixelStride) {
            Log.e(TAG, "uv pixelStride is different");
            return null;
        }

        int height = yuvImage.getHeight();
        int yRowStride = planes[0].getRowStride();
        int ySize = yRowStride * height;
        int vaildYsize = Math.min(ySize, yBufferSize);

        if (uPixelStride == 1) {
            int uSize = ySize >> 2;
            int vSize = uSize;
            int imageDataSize = ySize + (uSize + vSize);
            int vaildUsize = Math.min(uSize, uBufferSize);
            int vaildVsize = Math.min(vSize, vBufferSize);

            imageData = new byte[imageDataSize];
            byte[] uData = new byte[uSize];
            byte[] vData = new byte[vSize];

            yBuffer.get(imageData, 0, vaildYsize);
            yBuffer.rewind();
            uBuffer.get(uData, 0, vaildUsize);
            uBuffer.rewind();
            vBuffer.get(vData, 0, vaildVsize);
            vBuffer.rewind();
            int offset = ySize;
            for (int i=0; i < vaildUsize; i++) {
                imageData[offset++] = uData[i];
                imageData[offset++] = vData[i];
            }
        } else if (uPixelStride == 2) {
            int uSize = ySize >> 1;
            int imageDataSize = ySize + uSize;
            int vaildUsize = Math.min(uSize, uBufferSize);

            imageData = new byte[imageDataSize];

            yBuffer.get(imageData, 0, vaildYsize);
            yBuffer.rewind();
            int offset = ySize;
            uBuffer.get(imageData, offset, vaildUsize);
            uBuffer.rewind();
        } else {
            imageData = null;
            Log.e(TAG, "yuv error!");
        }
        return imageData;
    }

    public static Bitmap convertYuvImageToBitmap(Image yuvImage) {
        if (yuvImage.getFormat() != ImageFormat.YUV_420_888) {
            throw new IllegalArgumentException("not supported yuv format");
        }
        int width = yuvImage.getWidth();
        int height = yuvImage.getHeight();
        int[] argbData = new int[width * height];
        Image.Plane[] planes = yuvImage.getPlanes();
        Log.d(TAG, "y plane : cap = " + planes[0].getBuffer().capacity() + ", row stride = " + planes[0].getRowStride() + ", pixel stride = " + planes[0].getPixelStride());
        Log.d(TAG, "u plane : cap = " + planes[1].getBuffer().capacity() + ", row stride = " + planes[1].getRowStride() + ", pixel stride = " + planes[1].getPixelStride());
        Log.d(TAG, "v plane : cap = " + planes[2].getBuffer().capacity() + ", row stride = " + planes[2].getRowStride() + ", pixel stride = " + planes[2].getPixelStride());
        long time = System.currentTimeMillis();
        byte[] yPlane = new byte[planes[0].getBuffer().capacity()];
        byte[] uvPlane = new byte[planes[1].getBuffer().capacity()];
//        byte[] vPlane = new byte[planes[2].getBuffer().capacity()];
        ByteBuffer buffer;
        buffer = planes[0].getBuffer();
        if (buffer.remaining() != buffer.limit()) {
            buffer.rewind();
        }
        buffer.get(yPlane, 0, yPlane.length);

        buffer = planes[1].getBuffer();
        if (buffer.remaining() != buffer.limit()) {
            buffer.rewind();
        }
        buffer.get(uvPlane, 0, uvPlane.length);
//        buffer = planes[2].getBuffer();
//        buffer.get(vPlane, 0, vPlane.length);
        decodeYUV420_888(argbData, yPlane, uvPlane, /*vPlane, */width, height, width);
        Log.d(TAG, "elapsed time to converting = " + (System.currentTimeMillis() - time) + "ms");
        Bitmap bitmap = Bitmap.createBitmap(argbData, width, height, Bitmap.Config.ARGB_8888);

        return bitmap;
    }

    public static Bitmap convertYuvImageToBitmapByRenderScript(Context context, Image yuvImage) {
        int imgWidth = yuvImage.getWidth();
        int imgHeight = yuvImage.getHeight();
        Log.d(TAG, "image size = " + imgWidth + " x " + imgHeight);
        byte[] yuvData = getYuvImageData(yuvImage);
        if (yuvData == null) {
            Log.w(TAG, "image data is null!!");
            return null;
        }

        RenderScript rs = RenderScript.create(context);

        ScriptIntrinsicYuvToRGB theIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

        Type.Builder typeBuilderIn = new Type.Builder(rs, Element.createPixel(rs, Element.DataType.UNSIGNED_8, Element.DataKind.PIXEL_YUV));
        typeBuilderIn.setX(imgWidth);
        typeBuilderIn.setY(imgHeight);
        typeBuilderIn.setYuvFormat(ImageFormat.NV21);

        Allocation inputAllocation = Allocation.createTyped(rs, typeBuilderIn.create(), Allocation.USAGE_SCRIPT);
        inputAllocation.copyFrom(yuvData);

        Type.Builder typeBuilderOut = new Type.Builder(rs, Element.createPixel(rs, Element.DataType.UNSIGNED_8, Element.DataKind.PIXEL_RGBA));
        typeBuilderOut.setX(imgWidth);
        typeBuilderOut.setY(imgHeight);
        Allocation outputAllocation = Allocation.createTyped(rs, typeBuilderOut.create(), Allocation.USAGE_SCRIPT);
        theIntrinsic.setInput(inputAllocation);
        theIntrinsic.forEach(outputAllocation);
        Bitmap outputBitmap = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
        Log.d(TAG,"created bitmap size = " + outputBitmap.getWidth() + "x" + outputBitmap.getHeight());
        outputAllocation.copyTo(outputBitmap);
        return outputBitmap;
    }

    public static byte[] encodeImageToJpegDataByRsIntrinsic(Context context, Image image, int jpegQuality) {
        long time = System.currentTimeMillis();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Bitmap bitmap = convertYuvImageToBitmapByRenderScript(context, image);
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality, out);
        }
        Log.d(TAG, "elapsed time to encode = " + (System.currentTimeMillis() - time) + "ms");
        return out.toByteArray();
    }

    public static byte[] encodeYuvImageToJpeg(Image image, int jpegQuality) {
        long time = System.currentTimeMillis();
        int width = image.getWidth();
        int height = image.getHeight();
        byte[] jpeg = null;
        if (image.getFormat() == ImageFormat.YUV_420_888) {
            byte[] yuvData = getYuvImageData(image);
            if (yuvData != null) {
                jpeg = encodeYuvImageToJpeg(yuvData, image.getFormat(), image.getPlanes()[0].getRowStride(), width, height, jpegQuality);
            }
        } else {
            jpeg = image.getPlanes()[0].getBuffer().array();
        }
        Log.d(TAG, "elapsed time to getYuvData from Image = " + (System.currentTimeMillis() - time) + "ms");
        return jpeg;
    }

    private static byte[] convertNV12toNV21(byte[] inData, int stride, int height) {
        byte[] outData = new byte[inData.length];
        System.arraycopy(inData, 0, outData, 0, stride * height);
        for (int i = stride * height; i < inData.length; i+=2) {
            outData[i  ] = inData[i+1];
            outData[i+1] = inData[i  ];
        }
        return outData;
    }

    public static YuvImage convertToYuvImage(byte[] imageData, int format, int stride, int width, int height) {
        if (ImageFormat.YUV_420_888 != format) {
            Log.e(TAG, "Only ImageFormat.YUV_420_888 could be supported!!!");
            return null;
        }

        int[] strides = null;
        if (stride != 0) {
            strides = new int[2];
            strides[0] = stride;
            strides[1] = stride;
        }
        byte[] vuData = convertNV12toNV21(imageData, stride, height);
        return new YuvImage(vuData, ImageFormat.NV21, width, height, strides);
    }

    //ImageFormat.YUV_420_888 To Jpeg
    public static byte[] encodeYuvImageToJpeg(byte[] imageData, int format, int stride, int width, int height, int jpegQaulity) {
        long time = System.currentTimeMillis();
        YuvImage yuv = convertToYuvImage(imageData, format, stride, width, height);
        if (yuv != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuv.compressToJpeg(new Rect(0, 0, width, height), jpegQaulity, out);
            Log.d(TAG, "elapsed time to encode = " + (System.currentTimeMillis() - time) + "ms");
            return out.toByteArray();
        } else {
            return null;
        }
    }

    public static byte[] encodeBitmapToJpeg(Bitmap bitmap, int jpegQuality) {
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality, byteOutStream);
        byte[] imageData = byteOutStream.toByteArray();
        return imageData;
    }
}
