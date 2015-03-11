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

/**
    Enumeration values for Depth camera settings
 **/

public class DepthCameraMetadata {

    /*@O~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * The enum values below this point are generated from metadata
     * definitions in /system/media/camera/docs. Do not modify by hand or
     * modify the comment blocks at the start or end.
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~*/

    //
    // Enumeration values for DepthCameraCharacteristics#DEPTHCOMMON_MODULE_ID
    //

    /**
     * <p>Rear facing 3d Camera</p>
     * @see DepthCameraCharacteristics#DEPTHCOMMON_MODULE_ID
     */
    public static final int DEPTHCOMMON_MODULE_ID_R200 = 0;

    /**
     * <p>Front facing 3d Camera</p>
     * @see DepthCameraCharacteristics#DEPTHCOMMON_MODULE_ID
     */
    public static final int DEPTHCOMMON_MODULE_ID_F200 = 1;

    //
    // Enumeration values for DepthCameraCharacteristics#DEPTHCOMMON_AVAILABLE_NODES
    //

    /**
     * <p>COLOR Camera</p>
     * @see DepthCameraCharacteristics#DEPTHCOMMON_AVAILABLE_NODES
     */
    public static final int DEPTHCOMMON_AVAILABLE_NODES_COLOR = 0;

    /**
     * <p>depth Camera</p>
     * @see DepthCameraCharacteristics#DEPTHCOMMON_AVAILABLE_NODES
     */
    public static final int DEPTHCOMMON_AVAILABLE_NODES_DEPTH = 1;

    /**
     * <p>Left Camera</p>
     * @see DepthCameraCharacteristics#DEPTHCOMMON_AVAILABLE_NODES
     */
    public static final int DEPTHCOMMON_AVAILABLE_NODES_LEFT = 2;

    /**
     * <p>Right Camera</p>
     * @see DepthCameraCharacteristics#DEPTHCOMMON_AVAILABLE_NODES
     */
    public static final int DEPTHCOMMON_AVAILABLE_NODES_RIGHT = 3;

    /**
     * <p>Center Camera</p>
     * @see DepthCameraCharacteristics#DEPTHCOMMON_AVAILABLE_NODES
     */
    public static final int DEPTHCOMMON_AVAILABLE_NODES_CENTER = 4;

    //
    // Enumeration values for DepthCaptureRequest#DEPTHCOMMON_DEPTH_UNITS
    //

    /**
     * <p>MicroMeters</p>
     * @see DepthCaptureRequest#DEPTHCOMMON_DEPTH_UNITS
     */
    public static final int DEPTHCOMMON_DEPTH_UNITS_MICRON = 0;

    /**
     * <p>Millimeters</p>
     * @see DepthCaptureRequest#DEPTHCOMMON_DEPTH_UNITS
     */
    public static final int DEPTHCOMMON_DEPTH_UNITS_MM = 1;

    /*~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * End generated code
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~O@*/

}
