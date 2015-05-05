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
     * <p>List of enums from com.intel.device.info.availableExtensions</p>
     * <p>The empty list means no camera extension support</p>
     * @see #INTEL_EXTENSIONS_AVAILABLE_GROUPS_STATISTICS
     * @see #INTEL_EXTENSIONS_AVAILABLE_GROUPS_CV
     * @see #INTEL_EXTENSIONS_AVAILABLE_GROUPS_ENHANCEMENT
     * @see #INTEL_EXTENSIONS_AVAILABLE_GROUPS_DEVICE
     */
    public static final Key<int[]> INTEL_EXTENSIONS_AVAILABLE_GROUPS =
            new Key<int[]>("com.intel.extensions.availableGroups", int[].class);

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
     * <p>List of enums from com.intel.device.dualCameraMode</p>
     * <p>OFF Dual camera mode is off. ON Dual camera mode is on.</p>
     */
    public static final Key<int[]> INTEL_DEVICE_INFO_AVAILABLE_DUAL_CAMERA_MODE =
            new Key<int[]>("com.intel.device.info.availableDualCameraMode", int[].class);

    /**
     * <p>List of color effect modes for com.intel.imageEnhance.colorEffect that can be applied to images.</p>
     * <p>Android standard color effect modes and Intel color effect modes can't be applied at same time,
     * Android color effect should have higher priority</p>
     */
    public static final Key<int[]> INTEL_IMAGE_ENHANCE_INFO_AVAILABLECOLOR_EFFECTS =
            new Key<int[]>("com.intel.imageEnhance.info.availablecolorEffects", int[].class);

    /**
     * <p>List of available manual controls.</p>
     * <p>This static tag can be used to signal that certain devices do not support
     * some of the manual controls. If this tag is not available it is assumed
     * that ALL the controls are available</p>
     * @see #INTEL_IMAGE_ENHANCE_INFO_AVAILABLE_MANUAL_CONTROLS_BRIGHTNESS
     * @see #INTEL_IMAGE_ENHANCE_INFO_AVAILABLE_MANUAL_CONTROLS_CONTRAST
     * @see #INTEL_IMAGE_ENHANCE_INFO_AVAILABLE_MANUAL_CONTROLS_SATURATION
     * @see #INTEL_IMAGE_ENHANCE_INFO_AVAILABLE_MANUAL_CONTROLS_SHARPNESS
     * @see #INTEL_IMAGE_ENHANCE_INFO_AVAILABLE_MANUAL_CONTROLS_HUE
     */
    public static final Key<int[]> INTEL_IMAGE_ENHANCE_INFO_AVAILABLE_MANUAL_CONTROLS =
            new Key<int[]>("com.intel.imageEnhance.info.availableManualControls", int[].class);

    /*~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * End generated code
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~O@*/

}
