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

package com.intel.camera2.extensions;

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

public class CameraMetadataIntel {

    /*@O~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * The enum values below this point are generated from metadata
     * definitions in /system/media/camera/docs. Do not modify by hand or
     * modify the comment blocks at the start or end.
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~*/

    //
    // Enumeration values for IntelCameraCharacteristics#INTEL_DEVICE_INFO_AVAILABLE_EXTENSIONS
    //

    /**
     * <p>camera extension supports statistics analisys </p>
     * @see IntelCameraCharacteristics#INTEL_DEVICE_INFO_AVAILABLE_EXTENSIONS
     */
    public static final int INTEL_DEVICE_INFO_AVAILABLE_EXTENSIONS_STATISTICS = 0;

    /**
     * <p>camera extension supports computer vision </p>
     * @see IntelCameraCharacteristics#INTEL_DEVICE_INFO_AVAILABLE_EXTENSIONS
     */
    public static final int INTEL_DEVICE_INFO_AVAILABLE_EXTENSIONS_CV = 1;

    /**
     * <p>camera extension supports image enhancement, eg saturation </p>
     * @see IntelCameraCharacteristics#INTEL_DEVICE_INFO_AVAILABLE_EXTENSIONS
     */
    public static final int INTEL_DEVICE_INFO_AVAILABLE_EXTENSIONS_ENHANCEMENT = 2;

    /**
     * <p>camera extension supports special features in device,eg dual video </p>
     * @see IntelCameraCharacteristics#INTEL_DEVICE_INFO_AVAILABLE_EXTENSIONS
     */
    public static final int INTEL_DEVICE_INFO_AVAILABLE_EXTENSIONS_DEVICE = 3;

    //
    // Enumeration values for IntelCaptureRequest#INTEL_STATISTICS_ANALYSIS_MODE
    //

    /**
     * <p>(default) Do not include AIQ extended results in capture result metadata</p>
     * @see IntelCaptureRequest#INTEL_STATISTICS_ANALYSIS_MODE
     */
    public static final int INTEL_STATISTICS_ANALYSIS_MODE_OFF = 0;

    /**
     * <p>Include AIQ extended result in capture result metadata</p>
     * @see IntelCaptureRequest#INTEL_STATISTICS_ANALYSIS_MODE
     */
    public static final int INTEL_STATISTICS_ANALYSIS_MODE_ON = 1;

    //
    // Enumeration values for IntelCaptureRequest#INTEL_CV_SMILE_DETECT_MODE
    //

    /**
     * <p>Do not include smile detection statistics in capture results</p>
     * @see IntelCaptureRequest#INTEL_CV_SMILE_DETECT_MODE
     */
    public static final int INTEL_CV_SMILE_DETECT_MODE_OFF = 0;

    /**
     * <p>Return smile status and score values</p>
     * @see IntelCaptureRequest#INTEL_CV_SMILE_DETECT_MODE
     */
    public static final int INTEL_CV_SMILE_DETECT_MODE_ON = 1;

    //
    // Enumeration values for IntelCaptureRequest#INTEL_CV_BLINK_DETECT_MODE
    //

    /**
     * <p>Do not include blink detection statistics in capture results</p>
     * @see IntelCaptureRequest#INTEL_CV_BLINK_DETECT_MODE
     */
    public static final int INTEL_CV_BLINK_DETECT_MODE_OFF = 0;

    /**
     * <p>Return blink status and score values</p>
     * @see IntelCaptureRequest#INTEL_CV_BLINK_DETECT_MODE
     */
    public static final int INTEL_CV_BLINK_DETECT_MODE_ON = 1;

    //
    // Enumeration values for IntelCaptureRequest#INTEL_CV_FACE_RECOGNIZE_MODE
    //

    /**
     * <p>Do not include face recognition statistics in capture results</p>
     * @see IntelCaptureRequest#INTEL_CV_FACE_RECOGNIZE_MODE
     */
    public static final int INTEL_CV_FACE_RECOGNIZE_MODE_OFF = 0;

    /**
     * <p>Return person id and person similarity </p>
     * @see IntelCaptureRequest#INTEL_CV_FACE_RECOGNIZE_MODE
     */
    public static final int INTEL_CV_FACE_RECOGNIZE_MODE_ON = 1;

    //
    // Enumeration values for IntelCaptureRequest#INTEL_DEVICE_DUAL_CAMERA_MODE
    //

    /**
     * <p>(default) Dual camera mode is off.</p>
     * @see IntelCaptureRequest#INTEL_DEVICE_DUAL_CAMERA_MODE
     */
    public static final int INTEL_DEVICE_DUAL_CAMERA_MODE_OFF = 0;

    /**
     * <p>Dual camera mode is on</p>
     * @see IntelCaptureRequest#INTEL_DEVICE_DUAL_CAMERA_MODE
     */
    public static final int INTEL_DEVICE_DUAL_CAMERA_MODE_ON = 1;

    //
    // Enumeration values for IntelCaptureRequest#INTEL_IMAGE_ENHANCE_COLOR_EFFECT
    //

    /**
     * <p>ia_aiq_color_effect_sky_blue</p>
     * @see IntelCaptureRequest#INTEL_IMAGE_ENHANCE_COLOR_EFFECT
     */
    public static final int INTEL_IMAGE_ENHANCE_COLOR_EFFECT_SKY_BLUE = 0;

    /**
     * <p>ia_aiq_color_effect_grass_green</p>
     * @see IntelCaptureRequest#INTEL_IMAGE_ENHANCE_COLOR_EFFECT
     */
    public static final int INTEL_IMAGE_ENHANCE_COLOR_EFFECT_GRASS_GREEN = 1;

    /**
     * <p>ia_aiq_color_effect_skin_whiten</p>
     * @see IntelCaptureRequest#INTEL_IMAGE_ENHANCE_COLOR_EFFECT
     */
    public static final int INTEL_IMAGE_ENHANCE_COLOR_EFFECT_SKIN_WHITEN = 2;

    /**
     * <p>ia_aiq_color_effect_skin_whiten_low</p>
     * @see IntelCaptureRequest#INTEL_IMAGE_ENHANCE_COLOR_EFFECT
     */
    public static final int INTEL_IMAGE_ENHANCE_COLOR_EFFECT_SKIN_WHITEN_LOW = 3;

    /**
     * <p>ia_aiq_color_effect_skin_whiten_high</p>
     * @see IntelCaptureRequest#INTEL_IMAGE_ENHANCE_COLOR_EFFECT
     */
    public static final int INTEL_IMAGE_ENHANCE_COLOR_EFFECT_SKIN_WHITEN_HIGH = 4;

    /**
     * <p>ia_aiq_color_effect_vivid</p>
     * @see IntelCaptureRequest#INTEL_IMAGE_ENHANCE_COLOR_EFFECT
     */
    public static final int INTEL_IMAGE_ENHANCE_COLOR_EFFECT_VIVID = 5;

    //
    // Enumeration values for IntelCaptureResult#INTEL_STATISTICS_MULTI_FRAME_HINT
    //

    /**
     * <p>(default) No multiframe processing is required</p>
     * @see IntelCaptureResult#INTEL_STATISTICS_MULTI_FRAME_HINT
     */
    public static final int INTEL_STATISTICS_MULTI_FRAME_HINT_NONE = 0;

    /**
     * <p>Capture would benefit of using Ultra Low Light post processing</p>
     * @see IntelCaptureResult#INTEL_STATISTICS_MULTI_FRAME_HINT
     */
    public static final int INTEL_STATISTICS_MULTI_FRAME_HINT_ULL = 1;

    /**
     * <p>Capture would benefit of using High Dynamic Range post processing</p>
     * @see IntelCaptureResult#INTEL_STATISTICS_MULTI_FRAME_HINT
     */
    public static final int INTEL_STATISTICS_MULTI_FRAME_HINT_HDR = 2;

    //
    // Enumeration values for IntelCaptureResult#INTEL_STATISTICS_SCENE_DETECTED
    //

    /**
     * <p>ia_aiq_scene_mode_none</p>
     * @see IntelCaptureResult#INTEL_STATISTICS_SCENE_DETECTED
     */
    public static final int INTEL_STATISTICS_SCENE_DETECTED_NONE = 0;

    /**
     * <p>ia_aiq_scene_mode_close_up_portrait</p>
     * @see IntelCaptureResult#INTEL_STATISTICS_SCENE_DETECTED
     */
    public static final int INTEL_STATISTICS_SCENE_DETECTED_CLOSE_UP_PORTRAIT = 1;

    /**
     * <p>ia_aiq_scene_mode_portrait</p>
     * @see IntelCaptureResult#INTEL_STATISTICS_SCENE_DETECTED
     */
    public static final int INTEL_STATISTICS_SCENE_DETECTED_PORTRAIT = 2;

    /**
     * <p>ia_aiq_scene_mode_lowlight_portrait</p>
     * @see IntelCaptureResult#INTEL_STATISTICS_SCENE_DETECTED
     */
    public static final int INTEL_STATISTICS_SCENE_DETECTED_LOWLIGHT_PORTRAIT = 3;

    /**
     * <p>ia_aiq_scene_mode_low_light</p>
     * @see IntelCaptureResult#INTEL_STATISTICS_SCENE_DETECTED
     */
    public static final int INTEL_STATISTICS_SCENE_DETECTED_LOWLIGHT = 4;

    /**
     * <p>ia_aiq_scene_mode_action</p>
     * @see IntelCaptureResult#INTEL_STATISTICS_SCENE_DETECTED
     */
    public static final int INTEL_STATISTICS_SCENE_DETECTED_ACTION = 5;

    /**
     * <p>ia_aiq_scene_mode_backlight</p>
     * @see IntelCaptureResult#INTEL_STATISTICS_SCENE_DETECTED
     */
    public static final int INTEL_STATISTICS_SCENE_DETECTED_BACKLIGHT = 6;

    /**
     * <p>ia_aiq_scene_mode_landscape</p>
     * @see IntelCaptureResult#INTEL_STATISTICS_SCENE_DETECTED
     */
    public static final int INTEL_STATISTICS_SCENE_DETECTED_LANDSCAPE = 7;

    /**
     * <p>ia_aiq_scene_mode_document</p>
     * @see IntelCaptureResult#INTEL_STATISTICS_SCENE_DETECTED
     */
    public static final int INTEL_STATISTICS_SCENE_DETECTED_DOCUMENT = 8;

    /**
     * <p>ia_aiq_scene_mode_firework</p>
     * @see IntelCaptureResult#INTEL_STATISTICS_SCENE_DETECTED
     */
    public static final int INTEL_STATISTICS_SCENE_DETECTED_FIREWORK = 9;

    /**
     * <p>ia_aiq_scene_mode_lowlight_action</p>
     * @see IntelCaptureResult#INTEL_STATISTICS_SCENE_DETECTED
     */
    public static final int INTEL_STATISTICS_SCENE_DETECTED_LOWLIGHT_ACTION = 10;

    /**
     * <p>ia_aiq_scene_mode_baby</p>
     * @see IntelCaptureResult#INTEL_STATISTICS_SCENE_DETECTED
     */
    public static final int INTEL_STATISTICS_SCENE_DETECTED_BABY = 11;

    /**
     * <p>ia_aiq_scene_mode_barcode</p>
     * @see IntelCaptureResult#INTEL_STATISTICS_SCENE_DETECTED
     */
    public static final int INTEL_STATISTICS_SCENE_DETECTED_BARCODE = 12;

    /*~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * End generated code
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~O@*/

}
