/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.intel.camera2.extensions.depthcamera;

/**
    Enumeration values for Depth camera settings
 **/

public class DepthCameraMetadata {

    /*@O~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * The enum values below this point are generated from metadata
     * definitions in /system/media/camera/docs. Do not modify by hand or
     * modify the comment blocks at the start or end.
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~*/

    //
    // Enumeration values for DepthCameraCharacteristics#DEPTHCOMMON_MODULE_ID
    //

    /**
     * <p>Rear facing 3d Camera</p>
     * @see DepthCameraCharacteristics#DEPTHCOMMON_MODULE_ID
     */
    public static final int DEPTHCOMMON_MODULE_ID_R200 = 0;

    /**
     * <p>Front facing 3d Camera</p>
     * @see DepthCameraCharacteristics#DEPTHCOMMON_MODULE_ID
     */
    public static final int DEPTHCOMMON_MODULE_ID_F200 = 1;

    //
    // Enumeration values for DepthCameraCharacteristics#DEPTHCOMMON_AVAILABLE_NODES
    //

    /**
     * <p>COLOR Camera</p>
     * @see DepthCameraCharacteristics#DEPTHCOMMON_AVAILABLE_NODES
     */
    public static final int DEPTHCOMMON_AVAILABLE_NODES_COLOR = 0;

    /**
     * <p>depth Camera</p>
     * @see DepthCameraCharacteristics#DEPTHCOMMON_AVAILABLE_NODES
     */
    public static final int DEPTHCOMMON_AVAILABLE_NODES_DEPTH = 1;

    /**
     * <p>Left Camera</p>
     * @see DepthCameraCharacteristics#DEPTHCOMMON_AVAILABLE_NODES
     */
    public static final int DEPTHCOMMON_AVAILABLE_NODES_LEFT = 2;

    /**
     * <p>Right Camera</p>
     * @see DepthCameraCharacteristics#DEPTHCOMMON_AVAILABLE_NODES
     */
    public static final int DEPTHCOMMON_AVAILABLE_NODES_RIGHT = 3;

    /**
     * <p>Center Camera</p>
     * @see DepthCameraCharacteristics#DEPTHCOMMON_AVAILABLE_NODES
     */
    public static final int DEPTHCOMMON_AVAILABLE_NODES_CENTER = 4;

    //
    // Enumeration values for DepthCaptureRequest#R200_COLOR_RECTIFICATION_MODE
    //

    /**
     * <p>Color rectification disabled</p>
     * @see DepthCaptureRequest#R200_COLOR_RECTIFICATION_MODE
     */
    public static final int R200_COLOR_RECTIFICATION_MODE_OFF = 0;

    /**
     * <p>Color rectification enabled</p>
     * @see DepthCaptureRequest#R200_COLOR_RECTIFICATION_MODE
     */
    public static final int R200_COLOR_RECTIFICATION_MODE_ON = 1;

    //
    // Enumeration values for DepthCaptureRequest#DEPTHCOMMON_DEPTH_UNITS
    //

    /**
     * <p>MicroMeters</p>
     * @see DepthCaptureRequest#DEPTHCOMMON_DEPTH_UNITS
     */
    public static final int DEPTHCOMMON_DEPTH_UNITS_MICRON = 0;

    /**
     * <p>Millimeters</p>
     * @see DepthCaptureRequest#DEPTHCOMMON_DEPTH_UNITS
     */
    public static final int DEPTHCOMMON_DEPTH_UNITS_MM = 1;

    //
    // Enumeration values for DepthCaptureRequest#DEPTHCOMMON_AE_MODE
    //

    /**
     * <p>Autoexposure is disabled;
     * sensor.sensitivity and sensor.frameDuration are used</p>
     * @see DepthCaptureRequest#DEPTHCOMMON_AE_MODE
     */
    public static final int DEPTHCOMMON_AE_MODE_OFF = 0;

    /**
     * <p>Autoexposure is active</p>
     * @see DepthCaptureRequest#DEPTHCOMMON_AE_MODE
     */
    public static final int DEPTHCOMMON_AE_MODE_ON = 1;

    /*~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * End generated code
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~O@*/

}
