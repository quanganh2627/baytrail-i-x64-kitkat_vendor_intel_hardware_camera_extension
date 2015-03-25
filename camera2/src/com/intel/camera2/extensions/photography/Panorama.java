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

import android.util.Log;

import com.intel.camera2.extensions.IaFrame;

/**
 * 
 */
public class Panorama {
    private static final String TAG = "Panorama";
    private static Panorama mPanorama;
    private long mJNIInstance;
    private final int MAX_SUPPORTED_NUM_IMAGES;
    private int mPicIndex;
    private Direction mSetDirection = Direction.Still;

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
        /** The direction of panning is automatically detected in preview mode. */
        PreviewAuto,
    }

    private Panorama(long instance) {
        mJNIInstance = instance;
        PanoramaJNI.Config config = PanoramaJNI.getConfig(instance);
        if (config != null) {
            MAX_SUPPORTED_NUM_IMAGES = config.max_supported_num_images;
        } else {
            MAX_SUPPORTED_NUM_IMAGES = 0;
        }
    }

    /**
     * It checks the panorama library is available.
     */
    public static boolean isSupported() {
        return PanoramaJNI.isSupported();
    }

    /**
     * Return the panorama instance that is kept single instance.
     * @return Panorama singleton instance.
     */
    public static Panorama getInstance() {
        if (mPanorama == null && PanoramaJNI.isSupported()) {
            long jniInstance = PanoramaJNI.create();
            if (jniInstance != 0) {
                mPanorama = new Panorama(jniInstance);
            }
        }
        return mPanorama;
    }

    /**
     * It will release all resources are allocated internally.<br>
     * This method must be called after finishing panorama.
     */
    public static void release() {
        if (mPanorama != null) {
            mPanorama.releaseLibraryInstances();
            mPanorama = null;
        }
    }

    private void releaseLibraryInstances() {
        if (mJNIInstance != 0) {
            PanoramaJNI.destroy(mJNIInstance);
            mJNIInstance = 0;
        }
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
     * This method should be called before calling addImage or getStitchedImage.
     * @param direction {@link Direction}
     */
    public void setDirection(Direction direction) {
        mSetDirection = direction;
        PanoramaJNI.setParam(mJNIInstance, direction.ordinal());
    }

    /**
     * It adds the image for stitching.
     * @param image Only {@link IaFrame.PvlFormat}.NV12 is supported.
     */
    public void addImage(IaFrame image) {
        if (mPicIndex >= MAX_SUPPORTED_NUM_IMAGES) {
            Log.e(TAG, "MAX_SUPPORTED_NUM_IMAGES is " + MAX_SUPPORTED_NUM_IMAGES);
            return;
        }
        switch(mSetDirection) {
            case Left:
            case Right:
            case Up:
            case Down:
                break;
            default:
                Log.e(TAG, "Direction(" + mSetDirection + ") is not supported on addImage() method.");
                return;
        }

        if (mPicIndex == 0) {
            PanoramaJNI.Param param = PanoramaJNI.getParam(mJNIInstance);
            if (param != null && param.direction == PanoramaJNI.DIRECTION_STILL) {
                Log.e(TAG, "panorama's direction is not set.");
                return;
            }
        }
        PanoramaJNI.stitch(mJNIInstance, image, mPicIndex);
        mPicIndex++;
    }

    /**
     * It returns the stitched image that are added through addImage() method.
     * @return stitched image.
     */
    public IaFrame getStitchedImage() {
        if (mPicIndex > 0) {
            return PanoramaJNI.run(mJNIInstance);
        }
        return null;
    }

    public void setDebug(int debug) {
        PanoramaJNI.setDebug(mJNIInstance, debug);
    }
}
