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
     * <p>List of enums from com.intel.faceEngine.dualCameraMode</p>
     * <p>OFF is always supported.</p>
     * @see #INTEL_DUAL_INFO_AVAILABLE_DUAL_CAMERA_MODE_OFF
     * @see #INTEL_DUAL_INFO_AVAILABLE_DUAL_CAMERA_MODE_ON
     */
    @PublicKey
    public static final Key<Integer> INTEL_DUAL_INFO_AVAILABLE_DUAL_CAMERA_MODE =
            new Key<Integer>("com.intel.dual.info.availableDualCameraMode", int.class);

    /*~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * End generated code
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~O@*/

}
