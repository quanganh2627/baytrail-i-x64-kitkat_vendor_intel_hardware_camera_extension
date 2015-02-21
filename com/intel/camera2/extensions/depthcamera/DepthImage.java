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
import android.graphics.Point;
import android.graphics.PointF;
import android.media.Image;

public abstract class DepthImage extends Image {

    protected DepthImage() {
		/* Empty */
    }
    // Row , column , depth (get from image) => point cloud in world coordinates (camera coordinates)
    public abstract Point3DF projectImageToWorldCoordinates(DepthCameraCalibrationDataMap.IntrinsicParams zIntrincs ,Point pos2d);

    // Whether the color is rectified or not can be retrieved from calibration data
    public abstract PointF mapDepthToColorCoordinates(DepthCameraCalibrationDataMap.DepthCameraCalibrationData calibrationData, Point depthCoordinates);

    // Map part of the depth image (region)- might be used for optimization
    public abstract PointF[][]  mapDepthToColorCoordinates(DepthCameraCalibrationDataMap.DepthCameraCalibrationData calibrationData, Point origin, int width, int height);

    // Get z value at coordinate
    public abstract int getZ(int x, int y);
}
