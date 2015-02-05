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

import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.CaptureResult.Key;
import android.hardware.camera2.impl.PublicKey;
import android.util.Rational;

/**
 * <p>The subset of the results of a single image capture from the depth image sensor.</p>
 *
 * <p>Contains a subset of the final configuration for the capture hardware (sensor, lens,
 * flash), the processing pipeline, the control algorithms, and the output
 * buffers.</p>
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
    public static final Key<Integer> R200_MEDIAN_THRESHOLD =
            new Key<Integer>("intel.r200.medianThreshold", int.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Integer> R200_SCORE_MIN_THRESHOLD =
            new Key<Integer>("intel.r200.scoreMinThreshold", int.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Integer> R200_SCORE_MAX_THRESHOLD =
            new Key<Integer>("intel.r200.scoreMaxThreshold", int.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Integer> R200_NEIGHBOR_THRESHOLD =
            new Key<Integer>("intel.r200.neighborThreshold", int.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Integer> R200_LR_AGREE_THRESHOLD =
            new Key<Integer>("intel.r200.lrAgreeThreshold", int.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Integer> R200_TEXTURE_COUNT_THRESHOLD =
            new Key<Integer>("intel.r200.textureCountThreshold", int.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Integer> R200_TEXTURE_DIFFERENCE_THRESHOLD =
            new Key<Integer>("intel.r200.textureDifferenceThreshold", int.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Integer> R200_SECOND_PEAK_THRESHOLD =
            new Key<Integer>("intel.r200.secondPeakThreshold", int.class);

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
     * <p>1/10000 - 30 sec range. No bulb mode</p>
     */
    @PublicKey
    public static final Key<Long> DEPTHCOMMON_EXPOSURE_TIME =
            new Key<Long>("intel.depthcommon.exposureTime", long.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Rational> DEPTHCOMMON_GAIN_FACTOR =
            new Key<Rational>("intel.depthcommon.gainFactor", Rational.class);

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
