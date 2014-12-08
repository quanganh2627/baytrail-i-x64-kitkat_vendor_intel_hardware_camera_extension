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

import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.impl.PublicKey;
import android.hardware.camera2.impl.SyntheticKey;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The base class for camera controls and information.
 *
 * <p>
 * This class defines the basic key/value map used for querying for camera
 * characteristics or capture results, and for setting camera request
 * parameters.
 * </p>
 *
 * <p>
 * All instances of CameraMetadata are immutable. The list of keys with {@link #getKeys()}
 * never changes, nor do the values returned by any key with {@code #get} throughout
 * the lifetime of the object.
 * </p>
 *
 * @see CameraDevice
 * @see CameraManager
 * @see CameraCharacteristics
 **/

public class DepthCameraMetadata {

    /*@O~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * The enum values below this point are generated from metadata
     * definitions in /system/media/camera/docs. Do not modify by hand or
     * modify the comment blocks at the start or end.
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~*/

    //
    // Enumeration values for CameraCharacteristics#DEPTHCOMMON_MODULE_ID
    //

    /**
     * <p>Deap Sea Camera</p>
     * @see CameraCharacteristics#DEPTHCOMMON_MODULE_ID
     */
    public static final int DEPTHCOMMON_MODULE_ID_DS = 0;

    /**
     * <p>InVision Camera</p>
     * @see CameraCharacteristics#DEPTHCOMMON_MODULE_ID
     */
    public static final int DEPTHCOMMON_MODULE_ID_IVCAM = 1;

    //
    // Enumeration values for CameraCharacteristics#DEPTHCOMMON_AVAILABLE_NODES
    //

    /**
     * <p>COLOR Camera</p>
     * @see CameraCharacteristics#DEPTHCOMMON_AVAILABLE_NODES
     */
    public static final int DEPTHCOMMON_AVAILABLE_NODES_COLOR = 0;

    /**
     * <p>depth Camera</p>
     * @see CameraCharacteristics#DEPTHCOMMON_AVAILABLE_NODES
     */
    public static final int DEPTHCOMMON_AVAILABLE_NODES_DEPTH = 1;

    /**
     * <p>Left-Right Camera</p>
     * @see CameraCharacteristics#DEPTHCOMMON_AVAILABLE_NODES
     */
    public static final int DEPTHCOMMON_AVAILABLE_NODES_LEFT_RIGHT = 2;

    /**
     * <p>Center Camera</p>
     * @see CameraCharacteristics#DEPTHCOMMON_AVAILABLE_NODES
     */
    public static final int DEPTHCOMMON_AVAILABLE_NODES_CENTER = 3;

    //
    // Enumeration values for CaptureRequest#DS4_RGB_RECTIFICATION_MODE
    //

    /**
     * <p>RGB rectification disabled</p>
     * @see CaptureRequest#DS4_RGB_RECTIFICATION_MODE
     */
    public static final int DS4_RGB_RECTIFICATION_MODE_OFF = 0;

    /**
     * <p>RGB rectification enabled</p>
     * @see CaptureRequest#DS4_RGB_RECTIFICATION_MODE
     */
    public static final int DS4_RGB_RECTIFICATION_MODE_ON = 1;

    //
    // Enumeration values for CaptureRequest#DEPTHCOMMON_DEPTH_UNITS
    //

    /**
     * <p>MicroMeters</p>
     * @see CaptureRequest#DEPTHCOMMON_DEPTH_UNITS
     */
    public static final int DEPTHCOMMON_DEPTH_UNITS_MICRON = 0;

    /**
     * <p>Millimeters</p>
     * @see CaptureRequest#DEPTHCOMMON_DEPTH_UNITS
     */
    public static final int DEPTHCOMMON_DEPTH_UNITS_MM = 1;

    //
    // Enumeration values for CaptureRequest#DEPTHCOMMON_NODE_AE_MODE
    //

    /**
     * <p>Autoexposure is disabled;
     * sensor.sensitivity and sensor.frameDuration are used</p>
     * @see CaptureRequest#DEPTHCOMMON_NODE_AE_MODE
     */
    public static final int DEPTHCOMMON_NODE_AE_MODE_OFF = 0;

    /**
     * <p>Autoexposure is active</p>
     * @see CaptureRequest#DEPTHCOMMON_NODE_AE_MODE
     */
    public static final int DEPTHCOMMON_NODE_AE_MODE_ON = 1;

    /*~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * End generated code
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~O@*/

}
