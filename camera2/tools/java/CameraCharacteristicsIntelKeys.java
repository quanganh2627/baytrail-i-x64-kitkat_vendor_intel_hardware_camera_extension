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

import android.hardware.camera2.CameraCharacteristics.Key;

/**
 * <p>{@link IntelCameraCharacteristics} objects are immutable.</p>
 */
public final class CameraCharacteristicsIntelKeys extends CameraMetadataIntel
{

    /*@O~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * The key entries below this point are generated from metadata
     * definitions in /system/media/camera/docs. Do not modify by hand or
     * modify the comment blocks at the start or end.
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~*/


    /**
     * <p>List of enums from com.intel.cv.faceRecognizeMode</p>
     * <p>NO is always supported.</p>
     * <p>Yes means the device supports com.intel.cv.faceRecognizePersonIds and com.intel.cv.faceRecognizePersonSimilarities outputs.</p>
     */
    public static final Key<int[]> INTEL_CV_INFO_AVAILABLE_FACE_RECOGNIZE =
            new Key<int[]>("com.intel.cv.info.availableFaceRecognize", int[].class);

    /**
     * <p>List of enums from com.intel.faceEngineObjectTracking.mode</p>
     * <p>NO is always supported.</p>
     * <p>Yes means the device supports com.intel.cv.objectTrackingBoundingRectangles, com.intel.cv.objectTrackingTrackingIds and com.intel.cv.objectTrackingScores outputs.</p>
     */
    public static final Key<int[]> INTEL_CV_INFO_AVAILABLE_OBJECT_TRACK =
            new Key<int[]>("com.intel.cv.info.availableObjectTrack", int[].class);

    /**
     * <p>List of enums from com.intel.cv.panoramaMode</p>
     * <p>NO is always supported.</p>
     * <p>Yes means the device supports (TO BE DEFINED)</p>
     */
    public static final Key<int[]> INTEL_CV_INFO_AVAILABLE_PANORAMA =
            new Key<int[]>("com.intel.cv.info.availablePanorama", int[].class);

    /**
     * <p>List of enums from com.intel.device.dualCameraMode</p>
     * <p>OFF Dual camera mode is off. ON Dual camera mode is on.</p>
     */
    public static final Key<int[]> INTEL_DEVICE_INFO_AVAILABLE_DUAL_CAMERA_MODE =
            new Key<int[]>("com.intel.device.info.availableDualCameraMode", int[].class);

    /**
     * <p>List of enums from com.intel.device.info.availableExtensions</p>
     * <p>The empty list means no camera extension support</p>
     * @see #INTEL_DEVICE_INFO_AVAILABLE_EXTENSIONS_STATISTICS
     * @see #INTEL_DEVICE_INFO_AVAILABLE_EXTENSIONS_CV
     * @see #INTEL_DEVICE_INFO_AVAILABLE_EXTENSIONS_ENHANCEMENT
     * @see #INTEL_DEVICE_INFO_AVAILABLE_EXTENSIONS_DEVICE
     */
    public static final Key<int[]> INTEL_DEVICE_INFO_AVAILABLE_EXTENSIONS =
            new Key<int[]>("com.intel.device.info.availableExtensions", int[].class);

    /*~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * End generated code
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~O@*/

}
