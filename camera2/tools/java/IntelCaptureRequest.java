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
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Key;
import android.hardware.camera2.impl.PublicKey;
import android.hardware.camera2.impl.SyntheticKey;
import android.hardware.camera2.utils.HashCodeHelpers;
import android.hardware.camera2.utils.TypeReference;
import java.util.Objects;
import android.util.Rational;

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

public final class IntelCaptureRequest extends CameraMetadata<CaptureRequest.Key<?>>
{
    /**
     * {@inheritDoc}
     * @hide
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <T> T getProtected(Key<?> key) {
        return null;
    }

    /**
     * {@inheritDoc}
     * @hide
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Key<?>> getKeyClass() {
        Object thisClass = Key.class;
        return (Class<Key<?>>)thisClass;
    }

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
     * @see #INTEL_AIQ_ANALYSIS_MODE_OFF
     * @see #INTEL_AIQ_ANALYSIS_MODE_ON
     */
    @PublicKey
    public static final Key<Integer> INTEL_AIQ_ANALYSIS_MODE =
            new Key<Integer>("com.intel.aiq.analysisMode", int.class);

    /**
     * <p>This reports the recommended color effects the user should select for optimal quality.</p>
     * <p>To be added...</p>
     * @see #INTEL_AIQ_COLOR_EFFECT_SKY_BLUE
     * @see #INTEL_AIQ_COLOR_EFFECT_GRASS_GREEN
     * @see #INTEL_AIQ_COLOR_EFFECT_SKIN_WHITEN
     * @see #INTEL_AIQ_COLOR_EFFECT_SKIN_WHITEN_LOW
     * @see #INTEL_AIQ_COLOR_EFFECT_SKIN_WHITEN_HIGH
     * @see #INTEL_AIQ_COLOR_EFFECT_VIVID
     */
    @PublicKey
    public static final Key<Integer> INTEL_AIQ_COLOR_EFFECT =
            new Key<Integer>("com.intel.aiq.colorEffect", int.class);

    /**
     * <p>Control of Smile detection.</p>
     * <p>When ON, com.intel.faceEngine.smileDetectStatus (dynamic) and com.intel.faceEngine.smileDetectScores (dynamic) outputs are valid.</p>
     * @see #INTEL_FACE_ENGINE_SMILE_DETECT_MODE_OFF
     * @see #INTEL_FACE_ENGINE_SMILE_DETECT_MODE_ON
     */
    @PublicKey
    public static final Key<Integer> INTEL_FACE_ENGINE_SMILE_DETECT_MODE =
            new Key<Integer>("com.intel.faceEngine.smileDetectMode", int.class);

    /**
     * <p>Control of Blink detection.</p>
     * <p>When ON, com.intel.faceEngine.blinkDetectStatus (dynamic) and com.intel.faceEngine.blinkDetectScores (dynamic) outputs are valid.</p>
     * @see #INTEL_FACE_ENGINE_BLINK_DETECT_MODE_OFF
     * @see #INTEL_FACE_ENGINE_BLINK_DETECT_MODE_ON
     */
    @PublicKey
    public static final Key<Integer> INTEL_FACE_ENGINE_BLINK_DETECT_MODE =
            new Key<Integer>("com.intel.faceEngine.blinkDetectMode", int.class);

    /**
     * <p>Control of Face recognition.</p>
     * <p>When ON, com.intel.faceEngine.faceRecognizePersonIds (dynamic) and com.intel.faceEngine.faceRecognizePersonSimilarities (dynamic) outputs are valid.</p>
     * @see #INTEL_FACE_ENGINE_FACE_RECOGNIZE_MODE_OFF
     * @see #INTEL_FACE_ENGINE_FACE_RECOGNIZE_MODE_ON
     */
    @PublicKey
    public static final Key<Integer> INTEL_FACE_ENGINE_FACE_RECOGNIZE_MODE =
            new Key<Integer>("com.intel.faceEngine.faceRecognizeMode", int.class);

    /**
     * <p>Control of Dual camera.</p>
     * <p>When ON, the camera HAL can support dual camera mode.<br />
     * </p>
     * @see #INTEL_DUAL_DUAL_CAMERA_MODE_OFF
     * @see #INTEL_DUAL_DUAL_CAMERA_MODE_ON
     */
    @PublicKey
    public static final Key<Integer> INTEL_DUAL_DUAL_CAMERA_MODE =
            new Key<Integer>("com.intel.dual.dualCameraMode", int.class);

    /*~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * End generated code
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~O@*/

}
