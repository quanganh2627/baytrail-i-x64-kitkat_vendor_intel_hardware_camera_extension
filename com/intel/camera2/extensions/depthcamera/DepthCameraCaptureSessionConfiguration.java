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

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.os.Handler;
import android.util.Pair;
import android.view.Surface;

import java.util.ArrayList;
import java.util.List;

public class DepthCameraCaptureSessionConfiguration
{
    //recieves List of <Surface, sourceId> pairs
    //The depthMap can be obtained from the device.getId() and then create a manager and getting the DepthConfigurationMap

    private static class ConfigureDepthSurface
    {
        public Surface configureDepthSurface(Surface s, int sourceId, DepthCameraStreamConfigurationMap depthConfigMap)
        {
            int format = nativeGetSurfaceFormat(s);
            int usageMask = depthConfigMap.getUsageMask(sourceId,format);
            nativeConfigureSurface(s,usageMask);
            return s;
        }
        private synchronized native int nativeGetSurfaceFormat(Surface s);
        private synchronized native void nativeConfigureSurface(Surface s, int usageMask);

        static {
            System.loadLibrary("inteldepthcamera_jni");
        }
    }

    public static void createDepthCaptureSession(CameraDevice device, CameraCharacteristics camChars, List<Pair<Surface, Integer> > targetsSourceIdMap,
             CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException

    {
        if (device == null)
            throw new IllegalArgumentException(
                    "CameraDevice argument cannot be null");

        DepthCameraStreamConfigurationMap depthConfigMap = new DepthCameraStreamConfigurationMap(camChars);
        //for each surface in the list, configure
        ConfigureDepthSurface configUtil = new ConfigureDepthSurface();
        List<Surface> targets  = new ArrayList<Surface>();
        for (int i=0; i < targetsSourceIdMap.size(); i++)
        {
            Pair<Surface, Integer> item = targetsSourceIdMap.get(i);
            Surface surface = item.first;
            int sourceId = item.second;
            surface = configUtil.configureDepthSurface(surface, sourceId, depthConfigMap);
            targets.add(surface);
        }
        device.createCaptureSession(targets, callback, handler);
    }
}
