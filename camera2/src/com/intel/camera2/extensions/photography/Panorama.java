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
    private static Config mConfig;
    private int mPicIndex;
    private Direction mSetDirection = Direction.Still;

    public static final int SUCCESS = 0;
    public static final int ERROR = -1;

    public static class Config {
        /** The version information. */
        public final String version;
        /** The maximum number of input images supported by this component. */
        public final int maxSupportedNumImages;
        /** The minimum configurable value of overlapping ratio. (0 ~ 1.0f) */
        public final float minOverlappingRatio;
        /** The maximum configurable value of overlapping ratio. (0 ~ 1.0f) */
        public final float maxOverlappingRatio;
        /** The recommend value of overlapping ratio. (0 ~ 1.0f) */
        public final float recommendOverlappingRatio;

        Config(PanoramaJNI.Config config) {
            this.version = config.version.toString();
            this.maxSupportedNumImages = config.max_supported_num_images;
            this.minOverlappingRatio = (float)config.min_overlapping_ratio / (float)100;
            this.maxOverlappingRatio = (float)config.max_overlapping_ratio / (float)100;
            this.recommendOverlappingRatio = (float)config.default_overlapping_ratio / (float)100;
        }

        @Override
        public String toString() {
            return "version("+version.toString()+") maxSupportedNumImages("+maxSupportedNumImages+") overlapping_ratio: min("+minOverlappingRatio+")" +
                   "max("+maxOverlappingRatio+") recommend("+recommendOverlappingRatio+")";
        }
    }

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
     * Get the configure info from Panorama library.
     * @return Panroama.Config
     */
    public static Config getConfig() {
        if (mConfig == null) {
            long instance = PanoramaJNI.create();
            if (instance != 0) {
                PanoramaJNI.Config config = PanoramaJNI.getConfig(instance);
                if (config != null) {
                    mConfig = new Panorama.Config(config);
                } else {
                    mConfig = null;
                }
                PanoramaJNI.destroy(instance);
            }
        }
        return mConfig;
    }

    /**
     * The recommend angle is returned.<br>
     * Two input arguments can be got from CameraCharateristics.<br>
     * - sensorLength: CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE<br>
     * - focalLength: CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS
     * @param sensorLength If the device is landscape, it should use a physical size width.
     *                     If the device is portrait, it should use a physical size height.
     * @param focalLength 
     * @return recommend angle.(Unit: degree)
     */
    public static float getRecommendAngle(float sensorLength, float focalLength) {
        Config config = getConfig();
        if (config == null) {
            Log.e(TAG, "Panorama is not supported.");
            return Float.NaN;
        }

        double result = 2 * Math.atan(sensorLength / (2 * focalLength));
        double degree = Math.toDegrees(result);
        return (float)degree * (1f - config.recommendOverlappingRatio);
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
        Config config = getConfig();
        if (config == null) {
            Log.e(TAG, "Panorama is not supported.");
            return false;
        }
        if (mPicIndex >= config.maxSupportedNumImages) {
            Log.e(TAG, "MAX_SUPPORTED_NUM_IMAGES is " + config.maxSupportedNumImages);
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
