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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Bitmap.Config;
import android.media.Image;
import java.nio.ByteBuffer;

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
                    this.imageData = ImageConverter.getGrayImageData(image);
                    break;
                case PvlFormat.NV12: // same with IaFormat.NV12
                    this.imageData = ImageConverter.getYuvImageData(image);
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
                this.imageData = ImageConverter.getGrayImageData(bitmap, this.stride, this.width, this.height);
                break;
            case PvlFormat.NV12:    // same with IaFormat.NV12
                this.imageData = ImageConverter.getYUV420SPImageData(bitmap, this.stride, this.width, this.height);
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

    public String toString() {
        return "stride("+stride+") size("+width+"x"+height+") length("+size+", "+imageData.length+") format("+format+") rotation("+degree+")";
    }
}
