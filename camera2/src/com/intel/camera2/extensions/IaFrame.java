package com.intel.camera2.extensions;

import java.nio.ByteBuffer;

import android.graphics.ImageFormat;
import android.media.Image;
import android.media.Image.Plane;
import android.util.Log;

/**
 * common image object class to use across the engine library and classes of extensions packages.
 * all input images should be converted {@link IaFrame}
 */
public class IaFrame {
    private static final String TAG = "IaFrame";

    // IaFrame format.
    // Those definitions should be synced up with enum 'ia_frame_format' in 'ia_types.h'
    public static final int FRAME_FORMAT_NV12 = 0;
    public static final int FRAME_FORMAT_YUV420 = 1;
    public static final int FRAME_FORMAT_YUV444 = 2;
    public static final int FRAME_FORMAT_RAW = 3;
    public static final int FRAME_FORMAT_RGBA32 = 4;
    public static final int FRAME_FORMAT_YUY2 = 5;
    public static final int FRAME_FORMAT_RAW16 = 6;
    public static final int FRAME_FORMAT_RGB16P = 7;

    /*
     * @param format defined in pvl_types
     * enum pvl_image_format {
     *     pvl_image_format_nv12,      12 bit YUV 420, Y plane first followed by UV-interleaved plane. e.g. YYYYYYYY UVUV
     *     pvl_image_format_yv12,      12 bit YUV 420, Y plane first, U plane and then V plane. e.g. YYYYYYYY UU VV
     *     pvl_image_format_gray,      8 bit, Y plane only.
     *     pvl_image_format_rgba32,    32 bit RGBA, 8 bits per channel. e.g. RGBA RGBA RGBA
     *     pvl_image_format_yuy2,      16 bit YUV 422, YUYV interleaved. e.g. YUYV YUYV YUYV
     * };
     */
    public static class PvlFormat {
        public static final int NV12 = 0;
        public static final int YV12 = 1;
        public static final int GRAY = 2;
        public static final int RGBA32 = 3;
        public static final int YUY2 = 4;
    }

    public byte[] imageData;
    public int format;
    public int width;
    public int height;
    public int stride;
    public int size;
    public int rotation;
    public long timestamp;  // ms

    /**
     * Convert the input image to IaFrame. The input image is not released internally.
     * @param frameFormat IaFrame format.
     * @param image {@link android.media.Image}: It supports YUV_420_888 format only.
     */
    public IaFrame(int frameFormat, Image image) {
        this.imageData = getYuvImageData(image);
        this.format = frameFormat;
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.stride = image.getPlanes()[0].getRowStride();
        this.timestamp = System.currentTimeMillis();
        if (this.imageData != null) {
            this.size = this.imageData.length;
        } else {
            this.size = 0;
        }
    }

    /**
     * @param imageData image data array.
     * @param stride stride
     * @param width width
     * @param height height
     * @param format format
     * @param rotation supported degree: 0, 90, 180, 270.
     */
    public IaFrame(byte[] imageData, int stride, int width, int height, int format, int rotation) {
        this.imageData = imageData;
        this.width = width;
        this.height = height;
        this.format = format;
        this.rotation = rotation;
        this.stride = stride;
        this.size = stride * height;
        this.timestamp = System.currentTimeMillis();
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
        byte[] imageData = null;
        Plane[] planes = yuvImage.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();
        int ySize = yBuffer.capacity();
        int uSize = uBuffer.capacity();
        int vSize = vBuffer.capacity();
        if (vSize == (ySize / 2)) {
            imageData = new byte[ySize + vSize];
            yBuffer.get(imageData, 0, ySize);
            yBuffer.rewind();
            vBuffer.get(imageData, ySize, vSize);
            vBuffer.rewind();
        } else if (vSize == (ySize / 4)){
            imageData = new byte[ySize + uSize + vSize];
            byte[] uData = new byte[uSize];
            byte[] vData = new byte[vSize];
            yBuffer.get(imageData, 0, ySize);
            yBuffer.rewind();
            uBuffer.get(uData, 0, uSize);
            uBuffer.rewind();
            vBuffer.get(vData, 0, vSize);
            vBuffer.rewind();
            int offset = ySize;
            for (int i = 0 ; i < uSize ; i++) {
                imageData[offset++] = (byte) 0x80;//uData[i];
                imageData[offset++] = (byte) 0x80;//uData[i];
            }
        } else {
            int height = yuvImage.getHeight();
            int yStride = planes[0].getRowStride();
            int yBuffSize = yStride * height;
            imageData = new byte[yBuffSize + yBuffSize / 2];
            yBuffer.get(imageData, 0, ySize);
            yBuffer.rewind();
            vBuffer.get(imageData, yBuffSize, vSize);
            vBuffer.rewind();

            Log.i(TAG, "image size("+yuvImage.getWidth()+"x"+yuvImage.getHeight()+")");
            printPlane(planes[0]);
            printPlane(planes[1]);
            printPlane(planes[2]);
            Log.i(TAG, "ySize("+ySize+") uSize("+uSize+") vSize("+vSize+")");
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

    public String toString() {
        return "stride("+stride+") size("+width+"x"+height+") length("+size+", "+imageData.length+") format("+format+") rotation("+rotation+")";
    }
}
