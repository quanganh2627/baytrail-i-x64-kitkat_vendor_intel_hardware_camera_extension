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
package com.intel.camera2.extensions.photography;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.media.Image;
import android.util.Log;

import com.intel.camera2.extensions.IaFrame;
import com.intel.camera2.extensions.ImageConverter;

/**
 * It receives some images that are received in one direction.
 * It returns one panoramic image from the stitching of the received images. 
 * 
 * This class needs some libraries what there are dependence on Intel specific platform.
 * The {@link #isSupported} method informs that your device can be supported.
 */
public class Panorama {
    private static final String TAG = "Panorama";
    private long mJNIInstance;
    private static int MAX_SUPPORTED_NUM_IMAGES = -1;
    private int mPicIndex;
    private Direction mSetDirection = Direction.Still;

    public static final int SUCCESS = 0;
    public static final int ERROR = -1;

    /**
     * The enumerated values to specify the panning direction of panoramic stitch.
     */
    public static enum Direction {
        /** No direction */
        Still,
        /** Left to right panning */
        Right,
        /** Right to left panning */
        Left,
        /** Downward panning */
        Down,
        /** Upward panning */
        Up,
    }

    /**
     * It checks for the availability of the panorama library.
     */
    public static boolean isSupported() {
        return PanoramaJNI.isSupported();
    }

    /**
     * Get a count that this class can stitch.
     * @return Supported image count
     */
    public static int getMaxCountSupported() {
        if (MAX_SUPPORTED_NUM_IMAGES == -1) {
            long instance = PanoramaJNI.create();
            if (instance != 0) {
                PanoramaJNI.Config config = PanoramaJNI.getConfig(instance);
                if (config != null) {
                    MAX_SUPPORTED_NUM_IMAGES = config.max_supported_num_images;
                }
                PanoramaJNI.destroy(instance);
            }
        }
        return MAX_SUPPORTED_NUM_IMAGES;
    }

    /**
     * It creates a new panorama instance. If it's not supported, it will return null.
     * @return Panorama instance
     */
    public static Panorama newInstance() {
        try {
            return new Panorama();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Panorama() {
        mJNIInstance = PanoramaJNI.create();
        getMaxCountSupported();
    }

    /**
     * It releases all resources that are allocated internally.
     * This method must be called after finishing panorama.
     */
    public void release() {
        if (mJNIInstance != 0) {
            PanoramaJNI.destroy(mJNIInstance);
            mJNIInstance = 0;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }

    /**
     * It initializes setting values on Panorama.
     */
    public void initialize() {
        mPicIndex = 0;
        mSetDirection = Direction.Still;
        PanoramaJNI.reset(mJNIInstance);
    }

    /**
     * Set direction for stitching.<br>
     * This method should be called before calling addImage.
     * @param direction {@link Direction}
     */
    public void setDirection(Direction direction) {
        mSetDirection = direction;
        PanoramaJNI.setParam(mJNIInstance, direction.ordinal());
    }

    /**
     * It adds the image for stitching.
     */
    public int addInputImage(Bitmap bitmap) {
        if (checkToAddInputImage()) {
            IaFrame iaFrame = new IaFrame(bitmap, IaFrame.PvlFormat.NV12, 0);
            addInputImage(iaFrame);
            return SUCCESS;
        } else {
            return ERROR;
        }
    }

    /**
     * It adds the image for stitching.
     */
    public int addInputImage(Image image) {
        if (checkToAddInputImage()) {
            IaFrame iaFrame = new IaFrame(image, IaFrame.PvlFormat.NV12, 0);
            addInputImage(iaFrame);
            return SUCCESS;
        } else {
            return ERROR;
        }
    }

    /**
     * It returns the stitched image that are added through addImage() method.
     * @return YuvImage panorama image.
     */
    public YuvImage getPanoramaImage() {
        if (mPicIndex > 0) {
            IaFrame frame = PanoramaJNI.run(mJNIInstance);
            if (frame != null) {
                return ImageConverter.convertToYuvImage(frame.imageData, ImageFormat.YUV_420_888, frame.stride, frame.width, frame.height);
            }
        }
        return null;
    }

    private boolean checkToAddInputImage() {
        if (mPicIndex >= MAX_SUPPORTED_NUM_IMAGES) {
            Log.e(TAG, "MAX_SUPPORTED_NUM_IMAGES is " + MAX_SUPPORTED_NUM_IMAGES);
            return false;
        }
        switch(mSetDirection) {
            case Left:
            case Right:
            case Up:
            case Down:
                break;
            default:
                Log.e(TAG, "Direction(" + mSetDirection + ") is not supported on addImage() method.");
                return false;
        }

        if (mPicIndex == 0) {
            PanoramaJNI.Param param = PanoramaJNI.getParam(mJNIInstance);
            if (param != null && param.direction == PanoramaJNI.DIRECTION_STILL) {
                Log.e(TAG, "panorama's direction is not set.");
                return false;
            }
        }
        return true;
    }

    private void addInputImage(IaFrame frame) {
        PanoramaJNI.stitch(mJNIInstance, frame, mPicIndex);
        mPicIndex++;
    }
}
