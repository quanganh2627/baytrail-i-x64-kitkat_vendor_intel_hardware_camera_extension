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
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraCharacteristics.Key;
import android.hardware.camera2.impl.PublicKey;
import android.hardware.camera2.impl.SyntheticKey;
import android.hardware.camera2.utils.TypeReference;
import android.util.Rational;

import java.util.Collections;
import java.util.List;

/**
 * <p>{@link IntelCameraCharacteristics} objects are immutable.</p>
 */
public final class IntelCameraCharacteristics extends CameraMetadata<CameraCharacteristics.Key<?>> 
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
     * <p>List of enums from com.intel.faceEngine.smileDetectMode</p>
     * <p>NO is always supported.</p>
     * <p>YES means the device supports com.intel.faceEngine.smileDetectStatus and com.intel.faceEngine.smileDetectScores outputs.</p>
     */
    @PublicKey
    public static final Key<int[]> INTEL_FACE_ENGINE_INFO_AVAILABLE_SMILE_DETECT =
            new Key<int[]>("com.intel.faceEngine.info.availableSmileDetect", int[].class);

    /**
     * <p>List of enums from com.intel.faceEngine.blinkDetectMode</p>
     * <p>NO is always supported.</p>
     * <p>YES means the device supports com.intel.faceEngine.blinkDetectStatus and com.intel.faceEngine.blinkDetectScores outputs.</p>
     */
    @PublicKey
    public static final Key<int[]> INTEL_FACE_ENGINE_INFO_AVAILABLE_BLINK_DETECT =
            new Key<int[]>("com.intel.faceEngine.info.availableBlinkDetect", int[].class);

    /**
     * <p>List of enums from com.intel.faceEngine.faceRecognizeMode</p>
     * <p>NO is always supported.</p>
     * <p>Yes means the device supports com.intel.faceEngine.faceRecognizePersonIds and com.intel.faceEngine.faceRecognizePersonSimilarities outputs.</p>
     */
    @PublicKey
    public static final Key<int[]> INTEL_FACE_ENGINE_INFO_AVAILABLE_FACE_RECOGNIZE =
            new Key<int[]>("com.intel.faceEngine.info.availableFaceRecognize", int[].class);

    /**
     * <p>List of enums from com.intel.faceEngineObjectTracking.mode</p>
     * <p>NO is always supported.</p>
     * <p>Yes means the device supports com.intel.faceEngine.objectTrackingBoundingRectangles, com.intel.faceEngine.objectTrackingTrackingIds and com.intel.faceEngine.objectTrackingScores outputs.</p>
     */
    @PublicKey
    public static final Key<int[]> INTEL_FACE_ENGINE_INFO_AVAILABLE_OBJECT_TRACK =
            new Key<int[]>("com.intel.faceEngine.info.availableObjectTrack", int[].class);

    /**
     * <p>List of enums from com.intel.faceEngine.panoramaMode</p>
     * <p>NO is always supported.</p>
     * <p>Yes means the device supports (TO BE DEFINED)</p>
     */
    @PublicKey
    public static final Key<int[]> INTEL_FACE_ENGINE_INFO_AVAILABLE_PANORAMA =
            new Key<int[]>("com.intel.faceEngine.info.availablePanorama", int[].class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     * @see #INTEL_DEPTHCOMMON_MODULE_ID_DS
     * @see #INTEL_DEPTHCOMMON_MODULE_ID_IVCAM
     */
    @PublicKey
    public static final Key<Integer> INTEL_DEPTHCOMMON_MODULE_ID =
            new Key<Integer>("com.intel.depthcommon.moduleId", int.class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<byte[]> INTEL_DEPTHCOMMON_CALIBRATION_DATA =
            new Key<byte[]>("com.intel.depthcommon.calibrationData", byte[].class);

    /**
     * <p>BUGBUG:describe!</p>
     * <p>BUGBUG: Note</p>
     */
    @PublicKey
    public static final Key<byte[]> INTEL_DEPTHCOMMON_AVAILABLE_DEPTH_UNITS =
            new Key<byte[]>("com.intel.depthcommon.availableDepthUnits", byte[].class);

    /**
     * <p>supported node Ids.
     * Left/Right relevant for stereo depth cameras, center is for non-stereo depth cameras</p>
     * <p>BUGBUG: Note</p>
     * @see #INTEL_DEPTHCOMMON_AVAILABLE_NODES_COLOR
     * @see #INTEL_DEPTHCOMMON_AVAILABLE_NODES_DEPTH
     * @see #INTEL_DEPTHCOMMON_AVAILABLE_NODES_LEFT_RIGHT
     * @see #INTEL_DEPTHCOMMON_AVAILABLE_NODES_CENTER
     */
    @PublicKey
    public static final Key<int[]> INTEL_DEPTHCOMMON_AVAILABLE_NODES =
            new Key<int[]>("com.intel.depthcommon.availableNodes", int[].class);

    /**
     * <p>entry i is a bitmap that relates to entry i in availableProcessedSizes,
     * bit j in the bitmap relates to the nodeId in entry availableNodes[j].
     * If bit i is ON then the size is available for the relevant nodeId</p>
     */
    @PublicKey
    public static final Key<byte[]> INTEL_DEPTHCOMMON_SIZE_NODES_MAPPING =
            new Key<byte[]>("com.intel.depthcommon.sizeNodesMapping", byte[].class);

    /**
     * <p>entry i is a bitmap that relates to entry i in availableFormats,
     * bit j in the bitmap relates to the nodeId in entry availableNodes[j]
     * If bit i is ON then the format is available for the relevant nodeId</p>
     */
    @PublicKey
    public static final Key<byte[]> INTEL_DEPTHCOMMON_FORMAT_NODES_MAPPING =
            new Key<byte[]>("com.intel.depthcommon.formatNodesMapping", byte[].class);

    /**
     * <p>set of ranges of valid exposure
     * times. each entry has 3 values:  node Id, min and max </p>
     */
    @PublicKey
    public static final Key<long[]> INTEL_DEPTHCOMMON_NODE_EXPOSURE_TIME_RANGE =
            new Key<long[]>("com.intel.depthcommon.nodeExposureTimeRange", long[].class);

    /**
     * <p>set of ranges of gain ranges.
     * each entry has 3 values:  node Id, min and max </p>
     */
    @PublicKey
    public static final Key<long[]> INTEL_DEPTHCOMMON_NODE_GAIN_RANGE =
            new Key<long[]>("com.intel.depthcommon.nodeGainRange", long[].class);

    /*~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * End generated code
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~O@*/

}
