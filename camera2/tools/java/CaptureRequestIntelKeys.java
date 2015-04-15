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

import android.hardware.camera2.CaptureRequest.Key;

/**
 * <p>An immutable package of settings and outputs needed to capture a single
 * image from the camera device.</p>
 *
 * <p>Contains the configuration for the capture hardware (sensor, lens, flash),
 * the processing pipeline, the control algorithms, and the output buffers. Also
 * contains the list of target Surfaces to send image data to for this
 * capture.</p>
 *
 * <p>CaptureRequests can be created by using a {@link Builder} instance,
 * obtained by calling {@link CameraDevice#createCaptureRequest}</p>
 *
 * <p>CaptureRequests are given to {@link CameraCaptureSession#capture} or
 * {@link CameraCaptureSession#setRepeatingRequest} to capture images from a camera.</p>
 *
 * <p>Each request can specify a different subset of target Surfaces for the
 * camera to send the captured data to. All the surfaces used in a request must
 * be part of the surface list given to the last call to
 * {@link CameraDevice#createCaptureSession}, when the request is submitted to the
 * session.</p>
 *
 * <p>For example, a request meant for repeating preview might only include the
 * Surface for the preview SurfaceView or SurfaceTexture, while a
 * high-resolution still capture would also include a Surface from a ImageReader
 * configured for high-resolution JPEG images.</p>
 *
 * @see CameraDevice#capture
 * @see CameraDevice#setRepeatingRequest
 * @see CameraDevice#createCaptureRequest
 */

public final class CaptureRequestIntelKeys extends CameraMetadataIntel
{

    /*@O~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * The key entries below this point are generated from metadata
     * definitions in /system/media/camera/docs. Do not modify by hand or
     * modify the comment blocks at the start or end.
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~*/


    /**
     * <p>Whether the extended AIQ analysis results are provided for each capture request.</p>
     * <p>This is the list of extra tags that will be present in the result:</p>
     * <ul>
     * <li>multiframeHint</li>
     * <li>sceneDected</li>
     * <li>hdr.preferedExposures</li>
     * </ul>
     * @see #INTEL_STATISTICS_ANALYSIS_MODE_OFF
     * @see #INTEL_STATISTICS_ANALYSIS_MODE_ON
     */
    public static final Key<Integer> INTEL_STATISTICS_ANALYSIS_MODE =
            new Key<Integer>("com.intel.statistics.analysisMode", int.class);

    /**
     * <p>Control of Smile detection.</p>
     * <p>When ON, com.intel.cv.smileDetectStatus (dynamic) and com.intel.cv.smileDetectScores (dynamic) outputs are valid.</p>
     * @see #INTEL_CV_SMILE_DETECT_MODE_OFF
     * @see #INTEL_CV_SMILE_DETECT_MODE_ON
     */
    public static final Key<Integer> INTEL_CV_SMILE_DETECT_MODE =
            new Key<Integer>("com.intel.cv.smileDetectMode", int.class);

    /**
     * <p>Control of Blink detection.</p>
     * <p>When ON, com.intel.cv.blinkDetectStatus (dynamic) and com.intel.cv.blinkDetectScores (dynamic) outputs are valid.</p>
     * @see #INTEL_CV_BLINK_DETECT_MODE_OFF
     * @see #INTEL_CV_BLINK_DETECT_MODE_ON
     */
    public static final Key<Integer> INTEL_CV_BLINK_DETECT_MODE =
            new Key<Integer>("com.intel.cv.blinkDetectMode", int.class);

    /**
     * <p>Control of Face recognition.</p>
     * <p>When ON, com.intel.cv.faceRecognizePersonIds (dynamic) and com.intel.cv.faceRecognizePersonSimilarities (dynamic) outputs are valid.</p>
     * @see #INTEL_CV_FACE_RECOGNIZE_MODE_OFF
     * @see #INTEL_CV_FACE_RECOGNIZE_MODE_ON
     */
    public static final Key<Integer> INTEL_CV_FACE_RECOGNIZE_MODE =
            new Key<Integer>("com.intel.cv.faceRecognizeMode", int.class);

    /**
     * <p>Control of Dual camera.</p>
     * <p>When ON, the camera HAL can support dual camera mode.  </p>
     * @see #INTEL_DEVICE_DUAL_CAMERA_MODE_OFF
     * @see #INTEL_DEVICE_DUAL_CAMERA_MODE_ON
     */
    public static final Key<Integer> INTEL_DEVICE_DUAL_CAMERA_MODE =
            new Key<Integer>("com.intel.device.dualCameraMode", int.class);

    /**
     * <p>This contains the color effect modes supported by Intel 3A that can be applied to images.</p>
     * <p>Intel color effect can be used like as an extension to the standard control(android.control.effectMode).
     * But due to range checks in android standard API, those keys can't be sent by android.control.effectMode,
     * it's right way to send by separate control tag.
     * If a request with both tags arrives at the HAL, the Android standard metadata should take precedence
     * over the Intel's.</p>
     * @see #INTEL_IMAGE_ENHANCE_COLOR_EFFECT_OFF
     * @see #INTEL_IMAGE_ENHANCE_COLOR_EFFECT_SKY_BLUE
     * @see #INTEL_IMAGE_ENHANCE_COLOR_EFFECT_GRASS_GREEN
     * @see #INTEL_IMAGE_ENHANCE_COLOR_EFFECT_SKIN_WHITEN
     * @see #INTEL_IMAGE_ENHANCE_COLOR_EFFECT_SKIN_WHITEN_LOW
     * @see #INTEL_IMAGE_ENHANCE_COLOR_EFFECT_SKIN_WHITEN_HIGH
     * @see #INTEL_IMAGE_ENHANCE_COLOR_EFFECT_VIVID
     */
    public static final Key<Integer> INTEL_IMAGE_ENHANCE_COLOR_EFFECT =
            new Key<Integer>("com.intel.imageEnhance.colorEffect", int.class);

    /**
     * <p>value of brightness, its range is [-10, 10]; 10 is maximum brightness, 0 is neutral. </p>
     */
    public static final Key<Integer> INTEL_IMAGE_ENHANCE_BRIGHTNESS =
            new Key<Integer>("com.intel.imageEnhance.brightness", int.class);

    /**
     * <p>value of contrast, its range is [-10, 10]; 10 is maximum contrast, 0 is neutral. </p>
     */
    public static final Key<Integer> INTEL_IMAGE_ENHANCE_CONTRAST =
            new Key<Integer>("com.intel.imageEnhance.contrast", int.class);

    /**
     * <p>value of saturation, its range is [-10, 10]; 10 is maximum saturation, 0 is neutral. </p>
     */
    public static final Key<Integer> INTEL_IMAGE_ENHANCE_SATURATION =
            new Key<Integer>("com.intel.imageEnhance.saturation", int.class);

    /**
     * <p>value of hue, its range is [-10, 10]; 10 is maximum hue, 0 is neutral. </p>
     */
    public static final Key<Integer> INTEL_IMAGE_ENHANCE_HUE =
            new Key<Integer>("com.intel.imageEnhance.hue", int.class);

    /**
     * <p>value of sharpness, its range is [-10, 10]; 10 is maximum sharpness, 0 is neutral. </p>
     */
    public static final Key<Integer> INTEL_IMAGE_ENHANCE_SHARPNESS =
            new Key<Integer>("com.intel.imageEnhance.sharpness", int.class);

    /*~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * End generated code
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~O@*/

}
