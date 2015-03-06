package com.intel.camera2.extensions;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.apache.http.entity.ByteArrayEntity;

import android.animation.IntArrayEvaluator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Bitmap.Config;
import android.media.Image;
import android.media.Image.Plane;
import android.util.Log;

/**
 * common image object class to use across the engine library and classes of extensions packages.
 * all input images should be converted {@link IaFrame}
 */
public class IaFrame {
    private static final String TAG = "IaFrame";

    /**
     * IaFrame format.
     * Those definitions should be synced up with enum 'ia_frame_format' in 'ia_types.h'
     */
    public static class IaFormat {
        /** 12 bit YUV 420, Y, UV plane */
        public static final int NV12 = 0;
        /** 12 bit YUV 420, Y, U, V plane */
        public static final int YUV420 = 1;
        /** 24 bit YUV 444, Y, U, V plane */
        public static final int YUV444 = 2;
        /** RAW, 1 plane */
        public static final int RAW = 3;
        /** RGBA 8 bits per channel */
        public static final int RGBA32 = 4;
        /** 16 bit YUV 422, YUYV plane */
        public static final int YUY2 = 5;
        /** 16 bit RAW, 1 plane */
        public static final int RAW16 = 6;
        /** 16 bits per channel, 3 planes */
        public static final int RGB16P = 7;
    }

    /**
     * IaFrame format.
     * Those definitions should be synced up with enum 'pvl_frame_format' in 'pvl_types.h'
     */
    public static class PvlFormat {
        /** 12 bit YUV 420, Y plane first followed by UV-interleaved plane. e.g. YYYYYYYY UVUV */
        public static final int NV12 = 0;
        /** 12 bit YUV 420, Y plane first, U plane and then V plane. e.g. YYYYYYYY UU VV */
        public static final int YV12 = 1;
        /** 8 bit, Y plane only. */
        public static final int GRAY = 2;
        /** 32 bit RGBA, 8 bits per channel. e.g. RGBA RGBA RGBA */
        public static final int RGBA32 = 3;
        /** 16 bit YUV 422, YUYV interleaved. e.g. YUYV YUYV YUYV */
        public static final int YUY2 = 4;
    }

    public byte[] imageData;
    public int format;
    public int width;
    public int height;
    public int stride;
    public int size;
    public int degree;
    public long timestamp;  // ms

    /**
     * Convert the input image to IaFrame. The input image is not released internally.
     * @param image {@link android.media.Image}: It supports YUV_420_888 format only.
     * @param frameFormat IaFrame format.
     */
    public IaFrame(Image image, int frameFormat, int degree) {
        updateFromImage(image, frameFormat);
        this.degree = degree;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * @param imageData image data array.
     * @param stride stride
     * @param width width
     * @param height height
     * @param format format
     * @param degree supported degree: 0, 90, 180, 270.
     */
    public IaFrame(byte[] imageData, int stride, int width, int height, int format, int degree) {
        this.imageData = imageData;
        this.width = width;
        this.height = height;
        this.format = format;
        this.degree = degree;
        this.stride = stride;
        this.size = stride * height;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Convert the input bitmap to IaFrame. The input image is not released internally.
     * @param bitmap
     * @param format
     * @param degree
     */
    public IaFrame(Bitmap bitmap, int format, int degree) {
        updateFromBitmap(bitmap, format);
        this.degree = degree;
        this.timestamp = System.currentTimeMillis();
    }

    private void updateFromImage(Image image, int format) {
        if (image.getFormat() == ImageFormat.YUV_420_888) {
            switch (format) {
                case PvlFormat.GRAY:
                case IaFormat.RAW:
                    this.imageData = getGrayImageData(image);
                    break;
                case PvlFormat.NV12: // same with IaFormat.NV12
                    this.imageData = getYuvImageData(image);
                    break;
                default:
                    throw new IllegalArgumentException("not support format("+format+") for IaFrame.");
            }
            this.format = format;
            this.width = image.getWidth();
            this.height = image.getHeight();
            this.stride = image.getPlanes()[0].getRowStride();
            if (this.imageData != null) {
                this.size = this.imageData.length;
            } else {
                this.size = 0;
            }
        } else if (image.getFormat() == ImageFormat.JPEG) {
            ByteBuffer jpegBuffer = image.getPlanes()[0].getBuffer();
            byte[] jpegData = new byte[jpegBuffer.capacity()];
            jpegBuffer.rewind();
            jpegBuffer.get(jpegData, 0, jpegData.length);

            Bitmap bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
            if (bitmap != null) {
                updateFromBitmap(bitmap, format);
            }
        } else {
            throw new IllegalArgumentException("not support format("+image.getFormat()+") of Image.");
        }
    }

    private void updateFromBitmap(Bitmap bitmap, int format) {
        if (bitmap == null || bitmap.getConfig() != Config.ARGB_8888) {
            throw new IllegalArgumentException("Only supported ARGB_8888 format for bitmap.");
        }

        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        this.format = format;
        this.stride = bitmap.getRowBytes() / 4;

        switch (format) {
            case PvlFormat.GRAY:
            case IaFormat.RAW:
                this.imageData = getGrayImageData(bitmap, this.stride, this.width, this.height);
                break;
            case PvlFormat.NV12:    // same with IaFormat.NV12
                this.imageData = getYUV420SPImageData(bitmap, this.stride, this.width, this.height);
                break;
            default:
                throw new IllegalArgumentException("not support format("+format+") for IaFrame.");
        }

        if (this.imageData != null) {
            this.size = this.imageData.length;
        } else {
            this.size = 0;
        }
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

    private static void printPlane(Plane plane) {
        Log.i(TAG, "PixelStride("+plane.getPixelStride()+") RowStride("+plane.getRowStride()+")");
    }

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

    private static byte[] getGrayImageData(Bitmap bitmap, int stride, int width, int height) {
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

    private static byte[] getYUV420SPImageData(Bitmap bitmap, int stride, int width, int height) {
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

    public String toString() {
        return "stride("+stride+") size("+width+"x"+height+") length("+size+", "+imageData.length+") format("+format+") rotation("+degree+")";
    }
}
