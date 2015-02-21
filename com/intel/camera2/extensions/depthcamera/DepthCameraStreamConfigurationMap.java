/*
 * The source code contained or described herein and all documents related to the source code
 * ("Material") are owned by Intel Corporation or its suppliers or licensors. Title to the
 * Material remains with Intel Corporation or its suppliers and licensors. The Material may
 * contain trade secrets and proprietary and confidential information of Intel Corporation
 * and its suppliers and licensors, and is protected by worldwide copyright and trade secret
 * laws and treaty provisions. No part of the Material may be used, copied, reproduced,
 * modified, published, uploaded, posted, transmitted, distributed, or disclosed in any way
 * without Intel’s prior express written permission.
 * No license under any patent, copyright, trade secret or other intellectual property right
 * is granted to or conferred upon you by disclosure or delivery of the Materials, either
 * expressly, by implication, inducement, estoppel or otherwise. Any license under such
 * intellectual property rights must be express and approved by Intel in writing.
 * Copyright © 2015 Intel Corporation. All rights reserved.
 */

package com.intel.camera2.extensions.depthcamera;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.utils.HashCodeHelpers;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.lang.Integer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.android.internal.util.Preconditions.checkArgumentNonnegative;
import static com.android.internal.util.Preconditions.checkArgumentPositive;
import static com.android.internal.util.Preconditions.checkNotNull;

public class DepthCameraStreamConfigurationMap
{
    final public static int COLOR_STREAM_SOURCE_ID = 0x0;
    final public static int DEPTH_STREAM_SOURCE_ID = 0x1;
    final public static int LEFT_STREAM_SOURCE_ID  = 0x2;
    final public static int RIGHT_STREAM_SOURCE_ID = 0x3;

    // these values should match the ones defined in the HAL layer
    final private static int LEFT_USAGE_BIT_VAL           = 0x10000000;
    final private static int RIGHT_USAGE_BIT_VAL          = 0x20000000;
    final private static int DEPTH_PREVIEW_USAGE_BIT_VAL  = LEFT_USAGE_BIT_VAL | RIGHT_USAGE_BIT_VAL;

    public class DepthStreamConfiguration
    {
        /**
         * Create a new {@link DepthStreamConfiguration}.
         *
         * @param format image format
         * @param width image width, in pixels (positive)
         * @param height image height, in pixels (positive)
         * @param input true if this is an input configuration, false for output configurations
         * @param minDuration minimum duration for the configuration
         * @param usage bit or zero
         * @throws IllegalArgumentException
         *              if width/height were not positive
         *              or if the format was not user-defined in ImageFormat/PixelFormat
         *                  (IMPL_DEFINED is ok)
         *             or if min duration is negative
         *             or if usage value is not valid for this configuration
         *
         * @hide
         */
        public DepthStreamConfiguration(
                final int format, final int width, final int height, final boolean input, final int minDuration, final int usage) {
            mFormat = checkFormat(format);
            mWidth = checkArgumentPositive(width, "width must be positive");
            mHeight = checkArgumentPositive(height, "height must be positive");
            mInput = input;
            mMinDuration = checkArgumentNonnegative(minDuration, "minDuration must be non-negative");
            mUsageVal = checkUsage(usage);
            //Log.d(TAG,"New depth stream configuration: \n" + toString());
        }

        public String toString()
        {
            return "Format: " + Integer.toHexString(mFormat) + " Size: " + mWidth + "x" + mHeight + " input: " + mInput + " usage: " + mUsageVal;
        }

        private int checkUsage(int usage)
        {
            switch(usage){
                case LEFT_USAGE_BIT_VAL:
                case RIGHT_USAGE_BIT_VAL:
                case DEPTH_PREVIEW_USAGE_BIT_VAL:
                case 0:
                    return usage;
                default:
                    throw new IllegalArgumentException(String.format(
                            "non valid value for usage 0x%x", usage));
            }
        }
        private int checkFormat(int format)
        {
            switch (format) {
                case HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED:
                case HAL_PIXEL_FORMAT_BLOB:
                case HAL_PIXEL_FORMAT_RAW_OPAQUE:
                    return format;
                case ImageFormat.JPEG:
                    throw new IllegalArgumentException(
                            "ImageFormat.JPEG is an unknown internal format");
                default:
                    return checkArgumentFormat(format);
            }

        }
        private int checkArgumentFormat(int format) {
            if (!ImageFormat.isPublicFormat(format) && !PixelFormat.isPublicFormat(format) && !DepthImageFormat.isPublicFormat(format) &&
                    format != 0x20203859 /*Y8*/ && format != 0x20363159 /*Y16*/) { //TODO resolve this issue
                throw new IllegalArgumentException(String.format(
                        "format 0x%x was not defined in either ImageFormat or PixelFormat or DepthImageFormat", format));
            }

            return format;
        }

        /**
         * Get the internal image {@code format} in this stream configuration.
         *
         * @return an integer format
         *
         * @see ImageFormat
         * @see PixelFormat
         */
        public final int getFormat() {
            return mFormat;
        }


        /**
         * Return the width of the stream configuration.
         *
         * @return width > 0
         */
        public int getWidth() {
            return mWidth;
        }

        /**
         * Return the height of the stream configuration.
         *
         * @return height > 0
         */
        public int getHeight() {
            return mHeight;
        }

        /**
         * Convenience method to return the size of this stream configuration.
         *
         * @return a Size with positive width and height
         */
        public Size getSize() {
            return new Size(mWidth, mHeight);
        }

        /**
         * Determines if this configuration is usable for input streams.
         *
         * <p>Input and output stream configurations are not interchangeable;
         * input stream configurations must be used when configuring inputs.</p>
         *
         * @return {@code true} if input configuration, {@code false} otherwise
         */
        public boolean isInput() {
            return mInput;
        }

        /**
         * Determines if this configuration is usable for output streams.
         *
         * <p>Input and output stream configurations are not interchangeable;
         * out stream configurations must be used when configuring outputs.</p>
         *
         * @return {@code true} if output configuration, {@code false} otherwise
         *
         * @see CameraDevice#createCaptureSession
         */
        public boolean isOutput() {
            return !mInput;
        }
        public int getMinDuration()
        {
            return mMinDuration;
        }

        /*
         * @hide
         */
        public int getUsageVal()
        {
            return mUsageVal;
        }
        /**
         * Check if this {@link DepthStreamConfiguration} is equal to another {@link DepthStreamConfiguration}.
         *
         * <p>Two vectors are only equal if and only if each of the respective elements is equal.</p>
         *
         * @return {@code true} if the objects were equal, {@code false} otherwise
         */
        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            if (obj instanceof DepthStreamConfiguration) {
                final DepthStreamConfiguration other = (DepthStreamConfiguration) obj;
                return mFormat == other.mFormat &&
                        mWidth == other.mWidth &&
                        mHeight == other.mHeight &&
                        mInput == other.mInput &&
                        mMinDuration == other.mMinDuration &&
                        mUsageVal == other.mUsageVal;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            int p[] = new int[6];
            p[0] = mFormat;
            p[1] = mWidth;
            p[2] = mHeight;
            p[3] = mInput ?1 : 0;
            p[4] = mMinDuration;
            return HashCodeHelpers.hashCode(p);
        }

        private final int mFormat;
        private final int mWidth;
        private final int mHeight;
        private final boolean mInput;
        private final int mMinDuration;
        private final int mUsageVal;
    }
    private DepthStreamConfiguration unmarshalItem(int[] data, int base)
    {

        int format = data[base];
        int width  = data[base + 1];
        int height = data[base + 2];
        boolean input = data[base + 3] != 0;
        int minDuration = data[base + 4];
        int usage = data[base + 5];
        return new DepthStreamConfiguration(format,width,height,input,minDuration,usage);
    }
    private int getElementSize()
    {
        return 6;
    }

    private void mapItem(DepthStreamConfiguration item)
    {
        int sourceId = -1;
        if (item.getFormat() == DepthImageFormat.Z16 || item.getFormat() == DepthImageFormat.UVMAP
                || item.getUsageVal() == DEPTH_PREVIEW_USAGE_BIT_VAL)
            sourceId = DEPTH_STREAM_SOURCE_ID;
        else if (item.getFormat() == ImageFormat.Y8 || item.getFormat() == ImageFormat.Y16 || item.getFormat() == ImageFormat.YV12)
        {
            if (item.getUsageVal() == LEFT_USAGE_BIT_VAL)
                sourceId = LEFT_STREAM_SOURCE_ID;
            else if (item.getUsageVal() == RIGHT_USAGE_BIT_VAL)
                sourceId = RIGHT_STREAM_SOURCE_ID;
            else
                throw new IllegalArgumentException("Illegal usage bit value = "
                        + item.getUsageVal() + "!");
        }
        else
            throw new IllegalArgumentException("Illegal content of depth stream configuration! format "
                    + Integer.toHexString(item.getFormat()) + " usage " + Integer.toHexString(item.getUsageVal()));

        if (sourceId != -1)
        {
            ArrayList<DepthStreamConfiguration> configList;
            if (mDepthStreamConfigurations.containsKey(sourceId))
                configList = mDepthStreamConfigurations.get(sourceId);
            else
                configList = new ArrayList<DepthStreamConfiguration>();
            configList.add(item);
            mDepthStreamConfigurations.put(sourceId, configList);
        }
    }
    private void unmarshalArray(int[] data)
    {
        if (data == null)
        {
            Log.w(TAG, "metadata has no data for key DEPTHCOMMON_AVAILABLE_STREAM_CONFIGURATIONS ");
            return;
        }
        int elementSize = getElementSize();
        int arraySize = data.length/elementSize;
        if (data.length == 0) {
            throw new UnsupportedOperationException("data size is 0");
        }

        if (data.length % elementSize != 0){
            throw new UnsupportedOperationException("data has leftovers " + (data.length % elementSize) +
                    "(data.length = " + data.length + "elementSize = " + elementSize + ")");
        }

        for (int i = 0; i < arraySize; ++i) {
            mapItem(unmarshalItem(data, i*elementSize));
        }

    }
    //UVMAP is format in depth_tream_source_id
    public DepthCameraStreamConfigurationMap(CameraCharacteristics c)
    {
        //initialize the DepthConfigurationMap struct
        int[] data = c.get(DepthCameraCharacteristics.DEPTHCOMMON_AVAILABLE_STREAM_CONFIGURATIONS);
        mDepthStreamConfigurations = new HashMap<Integer, ArrayList< DepthStreamConfiguration> >();
        unmarshalArray(data);
    }

    public boolean equals(Object obj)
    {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof DepthCameraStreamConfigurationMap) {
            final DepthCameraStreamConfigurationMap other = (DepthCameraStreamConfigurationMap) obj;

            return mDepthStreamConfigurations == other.mDepthStreamConfigurations;
        }
        return false;
    }


    public final int[] getOutputFormats(int sourceId)
    {
        if (mDepthStreamConfigurations.containsKey(sourceId))
        {

            ArrayList<DepthStreamConfiguration> configList = mDepthStreamConfigurations.get(sourceId);

            //hide IMPLEMENTATION_DEFINED from user
            HashSet<Integer> formats = new HashSet<Integer>();
            for (DepthStreamConfiguration item: configList)
            {
                if (item.getFormat() != HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED)
                {
                    formats.add(item.getFormat());
                }
            }

            int[] res = new int[formats.size()];
            int i=0;
            for (Integer format : formats)
            {
                res[i++] = format;
            }


            return res;
        }
        throw new IllegalArgumentException(String.format(
                "sourceId %d is not supported by this depth stream configuration map", sourceId));

    }


    public long getOutputMinFrameDuration(int sourceId, int format, Size size)
    {
        checkNotNull(size, "size must not be null");
        if (mDepthStreamConfigurations.containsKey(sourceId))
        {
            ArrayList<DepthStreamConfiguration> configList = mDepthStreamConfigurations.get(sourceId);

            for (DepthStreamConfiguration s : configList)
            {
                if (s.getWidth() == size.getWidth() && s.getHeight() == size.getHeight() && format == s.getFormat() && s.isOutput())
                    return s.getMinDuration();
            }
            throw new IllegalArgumentException(String.format(
                    "source Id %d, format %x and size (%dx%d) are not supported output by this depth stream configuration map", sourceId, format, size.getWidth(), size.getHeight()));
        }
        throw new IllegalArgumentException(String.format(
                "source Id %d is not supported by this depth stream configuration map", sourceId));
    }


    public Size[] getOutputSizes(int sourceId, int format)
    {
        if (mDepthStreamConfigurations.containsKey(sourceId))
        {
            ArrayList<DepthStreamConfiguration> configList = mDepthStreamConfigurations.get(sourceId);
            HashSet<Size> resSet  = new HashSet<Size>();
            for (DepthStreamConfiguration s : configList)
                resSet.add(new Size(s.getWidth(), s.getHeight()));
            Size[] res = new Size[resSet.size()];
            res = resSet.toArray(res);
            return res;
        }
        throw new IllegalArgumentException(String.format(
                "source Id %d and format %x are not supported by this depth stream configuration map", sourceId, format));
    }

    public long getOutputStallDuration(int sourceId, int format, Size size)
    {
        checkNotNull(size, "size must not be null");
        if (mDepthStreamConfigurations.containsKey(sourceId))
        {
            ArrayList<DepthStreamConfiguration> configList = mDepthStreamConfigurations.get(sourceId);

            for (DepthStreamConfiguration s : configList)
            {
                if (s.getWidth() == size.getWidth() && s.getHeight() == size.getHeight() && format == s.getFormat())
                    return 0; //stall is 0 zero for all our valid configurations
            }
            throw new IllegalArgumentException(String.format(
                    "source Id %d, format %x and size (%dx%d) are not supported by this depth stream configuration map", sourceId, format, size.getWidth(), size.getHeight()));
        }
        throw new IllegalArgumentException(String.format(
                "source Id %d is not supported by this depth stream configuration map", sourceId));
    }

    public boolean isOutputSupportedFor(int sourceId, int format)
    {
        if (mDepthStreamConfigurations.containsKey(sourceId))
        {
            ArrayList<DepthStreamConfiguration> configList = mDepthStreamConfigurations.get(sourceId);

            for (DepthStreamConfiguration s : configList)
                if (s.getFormat() == format)
                    return true;
            return false;
        }
        throw new IllegalArgumentException(String.format(
                "source Id %d is not supported by this depth stream configuration map", sourceId));
    }

    public int getUsageMask(int sourceId, int format)
    {
        if (mDepthStreamConfigurations.containsKey(sourceId))
        {
            ArrayList<DepthStreamConfiguration> configList = mDepthStreamConfigurations.get(sourceId);

            for (DepthStreamConfiguration s : configList)
                if (s.getFormat() == format)
                    return s.getUsageVal();
        }
        return 0;
    }

    public boolean isOutputSupportedFor(int sourceId, Surface surface)
    {
        checkNotNull(surface, "surface must not be null");
        if (mDepthStreamConfigurations.containsKey(sourceId))
        {
            int format = nativeGetSurfaceFormat(surface);
            int width = nativeGetSurfaceWidth(surface);
            int height = nativeGetSurfaceHeight(surface);

            ArrayList<DepthStreamConfiguration> configList = mDepthStreamConfigurations.get(sourceId);

            for (DepthStreamConfiguration s : configList)
                if (s.getFormat() == format && s.getWidth() == width && s.getHeight() == height)
                    return true;
            return false;
        }

        throw new IllegalArgumentException(String.format(
                "source Id %d is not supported by this depth stream configuration map", sourceId));
    }


    static {
        System.loadLibrary("inteldepthcamera_jni");
    }

    private synchronized native int nativeGetSurfaceFormat(Surface s);
    private synchronized native int nativeGetSurfaceWidth(Surface s);
    private synchronized native int nativeGetSurfaceHeight(Surface s);
    //////    PRIVATE /////
    private static final String TAG = "DepthCameraStreamConfigurationMap";
    private static final boolean VERBOSE = true;//Log.isLoggable(TAG, Log.VERBOSE);
    // from system/core/include/system/graphics.h
    private static final int HAL_PIXEL_FORMAT_BLOB = 0x21;
    private static final int HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED = 0x22;
    private static final int HAL_PIXEL_FORMAT_RAW_OPAQUE = 0x24;
    private HashMap<Integer, ArrayList<DepthStreamConfiguration> > mDepthStreamConfigurations;
}
