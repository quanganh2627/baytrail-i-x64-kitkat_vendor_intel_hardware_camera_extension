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

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraCharacteristics.Key;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.impl.PublicKey;
import android.hardware.camera2.utils.TypeReference;

/**
 * <p>{@link DepthCameraCharacteristics} objects are immutable.</p>
 */
public final class DepthCameraCharacteristics extends CameraMetadata<CameraCharacteristics.Key<?>> 
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
     * @see #DEPTHCOMMON_MODULE_ID_R200
     * @see #DEPTHCOMMON_MODULE_ID_F200
     */
    @PublicKey
    public static final Key<Integer> DEPTHCOMMON_MODULE_ID =
            new Key<Integer>("intel.depthcommon.moduleId", int.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<byte[]> DEPTHCOMMON_CALIBRATION_DATA =
            new Key<byte[]>("intel.depthcommon.calibrationData", byte[].class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<byte[]> DEPTHCOMMON_AVAILABLE_DEPTH_UNITS =
            new Key<byte[]>("intel.depthcommon.availableDepthUnits", byte[].class);

    /**
     * <p>supported node Ids.
     * Left/Right relevant for stereo depth cameras, center is for non-stereo depth cameras</p>
     * <p>BUGBUG: Note</p>
     * @see #DEPTHCOMMON_AVAILABLE_NODES_COLOR
     * @see #DEPTHCOMMON_AVAILABLE_NODES_DEPTH
     * @see #DEPTHCOMMON_AVAILABLE_NODES_LEFT
     * @see #DEPTHCOMMON_AVAILABLE_NODES_RIGHT
     * @see #DEPTHCOMMON_AVAILABLE_NODES_CENTER
     */
    @PublicKey
    public static final Key<int[]> DEPTHCOMMON_AVAILABLE_NODES =
            new Key<int[]>("intel.depthcommon.availableNodes", int[].class);

    /**
     * <p>Vallid depth (image) exposure time ranges </p>
     */
    @PublicKey
    public static final Key<android.util.Range<Long>> DEPTHCOMMON_EXPOSURE_TIME_RANGE =
            new Key<android.util.Range<Long>>("intel.depthcommon.exposureTimeRange", new TypeReference<android.util.Range<Long>>() {{ }});

    /**
     * <p>depth gain range </p>
     */
    @PublicKey
    public static final Key<android.util.Range<Long>> DEPTHCOMMON_GAIN_RANGE =
            new Key<android.util.Range<Long>>("intel.depthcommon.gainRange", new TypeReference<android.util.Range<Long>>() {{ }});

    /**
     * <p>Depth ctream configurations supported by the camera i.e. tuples of
     * (format, width, height, in/out, usage_flag).</p>
     * @hide
     */
    public static final Key<int[]> DEPTHCOMMON_AVAILABLE_STREAM_CONFIGURATIONS =
            new Key<int[]>("intel.depthcommon.availableStreamConfigurations", int[].class);

    /*~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * End generated code
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~O@*/

    public static boolean isDepthCamera(CameraCharacteristics c)
    {
        try
        {
            if (c.get(DepthCameraCharacteristics.DEPTHCOMMON_AVAILABLE_NODES) != null)
                return true;
        }
        catch (java.lang.IllegalArgumentException e)
        {
            return false;
        }
        return false;
    }

}
