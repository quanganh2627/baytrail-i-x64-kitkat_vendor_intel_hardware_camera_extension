/*
 * Copyright (C) 2014 Intel Corporation
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

package com.intel.camera2.extensions.intelcamera;

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

public class IntelCameraMetadata {

    /*@O~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * The enum values below this point are generated from metadata
     * definitions in /system/media/camera/docs. Do not modify by hand or
     * modify the comment blocks at the start or end.
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~*/

    //
    // Enumeration values for IntelCaptureRequest#INTEL_AIQ_ANALYSIS_MODE
    //

    /**
     * <p>(default) Do not include AIQ extended results in capture result metadata</p>
     * @see IntelCaptureRequest#INTEL_AIQ_ANALYSIS_MODE
     */
    public static final int INTEL_AIQ_ANALYSIS_MODE_OFF = 0;

    /**
     * <p>Include AIQ extended result in capture result metadata</p>
     * @see IntelCaptureRequest#INTEL_AIQ_ANALYSIS_MODE
     */
    public static final int INTEL_AIQ_ANALYSIS_MODE_ON = 1;

    //
    // Enumeration values for IntelCaptureRequest#INTEL_FACE_ENGINE_SMILE_DETECT_MODE
    //

    /**
     * <p>Do not include smile detection statistics in capture results</p>
     * @see IntelCaptureRequest#INTEL_FACE_ENGINE_SMILE_DETECT_MODE
     */
    public static final int INTEL_FACE_ENGINE_SMILE_DETECT_MODE_OFF = 0;

    /**
     * <p>Return smile status and score values</p>
     * @see IntelCaptureRequest#INTEL_FACE_ENGINE_SMILE_DETECT_MODE
     */
    public static final int INTEL_FACE_ENGINE_SMILE_DETECT_MODE_ON = 1;

    //
    // Enumeration values for IntelCaptureResult#INTEL_AIQ_MULTI_FRAME_HINT
    //

    /**
     * <p>(default) No multiframe processing is required</p>
     * @see IntelCaptureResult#INTEL_AIQ_MULTI_FRAME_HINT
     */
    public static final int INTEL_AIQ_MULTI_FRAME_HINT_NONE = 0;

    /**
     * <p>Capture would benefit of using Ultra Low Light post processing</p>
     * @see IntelCaptureResult#INTEL_AIQ_MULTI_FRAME_HINT
     */
    public static final int INTEL_AIQ_MULTI_FRAME_HINT_ULL = 1;

    /**
     * <p>Capture would benefit of using High Dynamic Range post processing</p>
     * @see IntelCaptureResult#INTEL_AIQ_MULTI_FRAME_HINT
     */
    public static final int INTEL_AIQ_MULTI_FRAME_HINT_HDR = 2;

    //
    // Enumeration values for IntelCaptureResult#INTEL_AIQ_SCENE_DETECTED
    //

    /**
     * <p>ia_aiq_scene_mode_none</p>
     * @see IntelCaptureResult#INTEL_AIQ_SCENE_DETECTED
     */
    public static final int INTEL_AIQ_SCENE_DETECTED_NONE = 0;

    /**
     * <p>ia_aiq_scene_mode_close_up_portrait</p>
     * @see IntelCaptureResult#INTEL_AIQ_SCENE_DETECTED
     */
    public static final int INTEL_AIQ_SCENE_DETECTED_CLOSE_UP_PORTRAIT = 1;

    /**
     * <p>ia_aiq_scene_mode_portrait</p>
     * @see IntelCaptureResult#INTEL_AIQ_SCENE_DETECTED
     */
    public static final int INTEL_AIQ_SCENE_DETECTED_PORTRAIT = 2;

    /**
     * <p>ia_aiq_scene_mode_lowlight_portrait</p>
     * @see IntelCaptureResult#INTEL_AIQ_SCENE_DETECTED
     */
    public static final int INTEL_AIQ_SCENE_DETECTED_LOWLIGHT_PORTRAIT = 3;

    /**
     * <p>ia_aiq_scene_mode_low_light</p>
     * @see IntelCaptureResult#INTEL_AIQ_SCENE_DETECTED
     */
    public static final int INTEL_AIQ_SCENE_DETECTED_LOWLIGHT = 4;

    /**
     * <p>ia_aiq_scene_mode_action</p>
     * @see IntelCaptureResult#INTEL_AIQ_SCENE_DETECTED
     */
    public static final int INTEL_AIQ_SCENE_DETECTED_ACTION = 5;

    /**
     * <p>ia_aiq_scene_mode_backlight</p>
     * @see IntelCaptureResult#INTEL_AIQ_SCENE_DETECTED
     */
    public static final int INTEL_AIQ_SCENE_DETECTED_BACKLIGHT = 6;

    /**
     * <p>ia_aiq_scene_mode_landscape</p>
     * @see IntelCaptureResult#INTEL_AIQ_SCENE_DETECTED
     */
    public static final int INTEL_AIQ_SCENE_DETECTED_LANDSCAPE = 7;

    /**
     * <p>ia_aiq_scene_mode_document</p>
     * @see IntelCaptureResult#INTEL_AIQ_SCENE_DETECTED
     */
    public static final int INTEL_AIQ_SCENE_DETECTED_DOCUMENT = 8;

    /**
     * <p>ia_aiq_scene_mode_firework</p>
     * @see IntelCaptureResult#INTEL_AIQ_SCENE_DETECTED
     */
    public static final int INTEL_AIQ_SCENE_DETECTED_FIREWORK = 9;

    /**
     * <p>ia_aiq_scene_mode_lowlight_action</p>
     * @see IntelCaptureResult#INTEL_AIQ_SCENE_DETECTED
     */
    public static final int INTEL_AIQ_SCENE_DETECTED_LOWLIGHT_ACTION = 10;

    /**
     * <p>ia_aiq_scene_mode_baby</p>
     * @see IntelCaptureResult#INTEL_AIQ_SCENE_DETECTED
     */
    public static final int INTEL_AIQ_SCENE_DETECTED_BABY = 11;

    /**
     * <p>ia_aiq_scene_mode_barcode</p>
     * @see IntelCaptureResult#INTEL_AIQ_SCENE_DETECTED
     */
    public static final int INTEL_AIQ_SCENE_DETECTED_BARCODE = 12;

    /*~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * End generated code
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~O@*/

}
