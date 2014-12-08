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
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraCharacteristics.Key;
import android.hardware.camera2.impl.PublicKey;
import android.hardware.camera2.impl.SyntheticKey;
import android.hardware.camera2.utils.TypeReference;
import android.util.Rational;

import java.util.Collections;
import java.util.List;

/**
 * <p>{@link DS4CameraCharacteristics} objects are immutable.</p>
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
     * @see #DEPTHCOMMON_MODULE_ID_DS
     * @see #DEPTHCOMMON_MODULE_ID_IVCAM
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
     * @see #DEPTHCOMMON_AVAILABLE_NODES_LEFT_RIGHT
     * @see #DEPTHCOMMON_AVAILABLE_NODES_CENTER
     */
    @PublicKey
    public static final Key<int[]> DEPTHCOMMON_AVAILABLE_NODES =
            new Key<int[]>("intel.depthcommon.availableNodes", int[].class);

    /**
     * <p>entry i is a bitmap that relates to entry i in availableProcessedSizes,
     * bit j in the bitmap relates to the nodeId in entry availableNodes[j].
     * If bit i is ON then the size is available for the relevant nodeId</p>
     */
    @PublicKey
    public static final Key<byte[]> DEPTHCOMMON_SIZE_NODES_MAPPING =
            new Key<byte[]>("intel.depthcommon.sizeNodesMapping", byte[].class);

    /**
     * <p>entry i is a bitmap that relates to entry i in availableFormats,
     * bit j in the bitmap relates to the nodeId in entry availableNodes[j]
     * If bit i is ON then the format is available for the relevant nodeId</p>
     */
    @PublicKey
    public static final Key<byte[]> DEPTHCOMMON_FORMAT_NODES_MAPPING =
            new Key<byte[]>("intel.depthcommon.formatNodesMapping", byte[].class);

    /**
     * <p>set of ranges of valid exposure
     * times. each entry has 3 values:  node Id, min and max </p>
     */
    @PublicKey
    public static final Key<long[]> DEPTHCOMMON_NODE_EXPOSURE_TIME_RANGE =
            new Key<long[]>("intel.depthcommon.nodeExposureTimeRange", long[].class);

    /**
     * <p>set of ranges of gain ranges.
     * each entry has 3 values:  node Id, min and max </p>
     */
    @PublicKey
    public static final Key<long[]> DEPTHCOMMON_NODE_GAIN_RANGE =
            new Key<long[]>("intel.depthcommon.nodeGainRange", long[].class);

    /*~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * End generated code
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~O@*/

}
