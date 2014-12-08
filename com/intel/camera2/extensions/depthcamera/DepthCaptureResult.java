/*
 * Copyright (C) 2012 The Android Open Source Project
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
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.CaptureResult.Key;
import android.hardware.camera2.impl.CaptureResultExtras;
import android.hardware.camera2.impl.PublicKey;
import android.hardware.camera2.impl.SyntheticKey;
import android.hardware.camera2.utils.TypeReference;
import android.util.Log;
import android.util.Rational;

import java.util.List;

/**
 * <p>The subset of the results of a single image capture from the image sensor.</p>
 *
 * <p>Contains a subset of the final configuration for the capture hardware (sensor, lens,
 * flash), the processing pipeline, the control algorithms, and the output
 * buffers.</p>
 *
 * <p>CaptureResults are produced by a {@link CameraDevice} after processing a
 * {@link CaptureRequest}. All properties listed for capture requests can also
 * be queried on the capture result, to determine the final values used for
 * capture. The result also includes additional metadata about the state of the
 * camera device during the capture.</p>
 *
 * <p>Not all properties returned by {@link DS4CameraCharacteristics#getAvailableCaptureResultKeys()}
 * are necessarily available. Some results are {@link DS4CaptureResult partial} and will
 * not have every key set. Only {@link TotalCaptureResult total} results are guaranteed to have
 * every key available that was enabled by the request.</p>
 *
 * <p>{@link DS4CaptureResult} objects are immutable.</p>
 *
 */
public class DepthCaptureResult extends CameraMetadata<CaptureResult.Key<?>> {

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
     * <p>supported node Ids.
     * Left/Right relevant for stereo depth cameras, center is for non-stereo depth cameras</p>
     * <p>BUGBUG: Note</p>
     * @see #DEPTHCOMMON_AVAILABLE_NODES_COLOR
     * @see #DEPTHCOMMON_AVAILABLE_NODES_DEPTH
     * @see #DEPTHCOMMON_AVAILABLE_NODES_LEFT_RIGHT
     * @see #DEPTHCOMMON_AVAILABLE_NODES_CENTER
     */
    @PublicKey
    public static final Key<int[]> DEPTHCOMMON_AVAILABLE_NODES =
            new Key<int[]>("intel.depthcommon.availableNodes", int[].class);

    /**
     * <p>array of per-node frame counts. Node IDs are specified in
     * the availableNodes array, that also has to be repeated in the response
     * metadata exactly as it has been sent with static
     * metadata</p>
     */
    @PublicKey
    public static final Key<int[]> DEPTHCOMMON_NODE_FRAME_COUNTERS =
            new Key<int[]>("intel.depthcommon.nodeFrameCounters", int[].class);

    /*~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * End generated code
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~O@*/

}
