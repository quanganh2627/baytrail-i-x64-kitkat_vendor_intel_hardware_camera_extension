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

public final class DepthCaptureRequest extends CameraMetadata<CaptureRequest.Key<?>>
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
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Integer> DS4_MEDIAN_THRESHOLD =
            new Key<Integer>("intel.ds4.medianThreshold", int.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Integer> DS4_SCORE_MIN_THRESHOLD =
            new Key<Integer>("intel.ds4.scoreMinThreshold", int.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Integer> DS4_SCORE_MAX_THRESHOLD =
            new Key<Integer>("intel.ds4.scoreMaxThreshold", int.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Integer> DS4_NEIGHBOR_THRESHOLD =
            new Key<Integer>("intel.ds4.neighborThreshold", int.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Integer> DS4_LR_AGREE_THRESHOLD =
            new Key<Integer>("intel.ds4.lrAgreeThreshold", int.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Integer> DS4_TEXTURE_COUNT_THRESHOLD =
            new Key<Integer>("intel.ds4.textureCountThreshold", int.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Integer> DS4_TEXTURE_DIFFERENCE_THRESHOLD =
            new Key<Integer>("intel.ds4.textureDifferenceThreshold", int.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Integer> DS4_SECOND_PEAK_THRESHOLD =
            new Key<Integer>("intel.ds4.secondPeakThreshold", int.class);

    /**
     * <p>Enable or disable RGB rectification</p>
     * @see #DS4_RGB_RECTIFICATION_MODE_OFF
     * @see #DS4_RGB_RECTIFICATION_MODE_ON
     */
    @PublicKey
    public static final Key<Integer> DS4_RGB_RECTIFICATION_MODE =
            new Key<Integer>("intel.ds4.rgbRectificationMode", int.class);

    /**
     * <p>BUGBUG: Note</p>
     * @see #DEPTHCOMMON_DEPTH_UNITS_MICRON
     * @see #DEPTHCOMMON_DEPTH_UNITS_MM
     */
    @PublicKey
    public static final Key<int[]> DEPTHCOMMON_DEPTH_UNITS =
            new Key<int[]>("intel.depthcommon.depthUnits", int[].class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Boolean> DEPTHCOMMON_DISPARITY_OUTPUT_MODE =
            new Key<Boolean>("intel.depthcommon.disparityOutputMode", boolean.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Double> DEPTHCOMMON_DISPARITY_MULTIPLIER =
            new Key<Double>("intel.depthcommon.disparityMultiplier", double.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Integer> DEPTHCOMMON_DEPTH_MIN =
            new Key<Integer>("intel.depthcommon.depthMin", int.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Integer> DEPTHCOMMON_DEPTH_MAX =
            new Key<Integer>("intel.depthcommon.depthMax", int.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Boolean> DEPTHCOMMON_EMITTER =
            new Key<Boolean>("intel.depthcommon.emitter", boolean.class);

    /**
     * <p>Duration each pixel is exposed to
     * light.</p>
     * <p>If the sensor can't expose this exact duration, it should shorten the
     * duration exposed to the nearest possible value (rather than expose longer).</p>
     * <p>array - Each entry has a pair of node id and nodeExposure time</p>
     * <p>1/10000 - 30 sec range. No bulb mode</p>
     */
    @PublicKey
    public static final Key<long[]> DEPTHCOMMON_NODE_EXPOSURE_TIME =
            new Key<long[]>("intel.depthcommon.nodeExposureTime", long[].class);

    /**
     * <p>array of modes.
     * Each entry has a pair of node id and aeMode time
     * Whether AE is currently updating the sensor
     * exposure and sensitivity fields</p>
     * @see #DEPTHCOMMON_NODE_AE_MODE_OFF
     * @see #DEPTHCOMMON_NODE_AE_MODE_ON
     */
    @PublicKey
    public static final Key<long[]> DEPTHCOMMON_NODE_AE_MODE =
            new Key<long[]>("intel.depthcommon.nodeAeMode", long[].class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Rational[]> DEPTHCOMMON_NODE_GAIN_FACTOR =
            new Key<Rational[]>("intel.depthcommon.nodeGainFactor", Rational[].class);

    /**
     * <p>Per node frame counts should be included</p>
     * <p>Enable / disable boolean</p>
     */
    @PublicKey
    public static final Key<Boolean> DEPTHCOMMON_NODE_FRAME_COUNTERS_MODE =
            new Key<Boolean>("intel.depthcommon.nodeFrameCountersMode", boolean.class);

    /**
     * <p>Enable frame synchronisation, based on embedded frame counts</p>
     * <p>Enable / disable boolean</p>
     */
    @PublicKey
    public static final Key<Boolean> DEPTHCOMMON_FRAME_SYNC_MODE =
            new Key<Boolean>("intel.depthcommon.frameSyncMode", boolean.class);

    /*~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * End generated code
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~O@*/

}
