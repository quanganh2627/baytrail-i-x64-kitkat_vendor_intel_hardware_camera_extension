/*
 * The source code contained or described herein and all documents related to the source code
 * ("Material") are owned by Intel Corporation or its suppliers or licensors. Title to the
 * Material remains with Intel Corporation or its suppliers and licensors. The Material may
 * contain trade secrets and proprietary and confidential information of Intel Corporation
 * and its suppliers and licensors, and is protected by worldwide copyright and trade secret
 * laws and treaty provisions. No part of the Material may be used, copied, reproduced,
 * modified, published, uploaded, posted, transmitted, distributed, or disclosed in any way
 * without Intel’s prior express written permission.
 * No license under any patent, copyright, trade secret or other intellectual property right
 * is granted to or conferred upon you by disclosure or delivery of the Materials, either
 * expressly, by implication, inducement, estoppel or otherwise. Any license under such
 * intellectual property rights must be express and approved by Intel in writing.
 * Copyright © 2015 Intel Corporation. All rights reserved.
 */

package com.intel.camera2.extensions.depthcamera;

import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Key;
import android.hardware.camera2.impl.PublicKey;
import android.hardware.camera2.impl.SyntheticKey;
import java.util.Objects;
import android.util.Rational;

/**
 * <p>An immutable package of settings and outputs needed to capture a single
 * image from the depth camera device.</p>
 *
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
     * <p>Enable or disable Color rectification</p>
     * @see #R200_COLOR_RECTIFICATION_MODE_OFF
     * @see #R200_COLOR_RECTIFICATION_MODE_ON
     */
    @PublicKey
    public static final Key<Integer> R200_COLOR_RECTIFICATION_MODE =
            new Key<Integer>("intel.r200.colorRectificationMode", int.class);

    /**
     * <p>Resolution of color stream to perform uvmap for.</p>
     * <p>If more than one color stream is configured, and uvmap stream is also requested
     * This field must be configured to inform the HAL which color resolution is relevant for the
     * uvmap calculations.</p>
     */
    @PublicKey
    public static final Key<android.util.Size> R200_UVMAP_COLOR_SIZE =
            new Key<android.util.Size>("intel.r200.uvmapColorSize", android.util.Size.class);

    /**
     * <p>BUGBUG: Note</p>
     * @see #DEPTHCOMMON_DEPTH_UNITS_MICRON
     * @see #DEPTHCOMMON_DEPTH_UNITS_MM
     */
    @PublicKey
    public static final Key<Integer> DEPTHCOMMON_DEPTH_UNITS =
            new Key<Integer>("intel.depthcommon.depthUnits", int.class);

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
     * <p>1/10000 - 30 sec range. No bulb mode</p>
     */
    @PublicKey
    public static final Key<Long> DEPTHCOMMON_EXPOSURE_TIME =
            new Key<Long>("intel.depthcommon.exposureTime", long.class);

    /**
     * <p>Depth aeMode - Whether AE is currently updating the sensor
     * exposure and sensitivity fields</p>
     * @see #DEPTHCOMMON_AE_MODE_OFF
     * @see #DEPTHCOMMON_AE_MODE_ON
     */
    @PublicKey
    public static final Key<Integer> DEPTHCOMMON_AE_MODE =
            new Key<Integer>("intel.depthcommon.aeMode", int.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<Rational> DEPTHCOMMON_GAIN_FACTOR =
            new Key<Rational>("intel.depthcommon.gainFactor", Rational.class);

    /**
     * <p>frame counters should be included when on</p>
     * <p>Enable / disable boolean</p>
     */
    @PublicKey
    public static final Key<Boolean> DEPTHCOMMON_FRAME_COUNTERS_MODE =
            new Key<Boolean>("intel.depthcommon.frameCountersMode", boolean.class);

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
