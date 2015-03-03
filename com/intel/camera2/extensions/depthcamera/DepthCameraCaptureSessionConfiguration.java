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
import android.hardware.camera2.impl.CameraDeviceImpl;
import android.os.Handler;
import android.util.Pair;
import android.view.Surface;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DepthCameraCaptureSessionConfiguration
{
    //recieves List of <Surface, sourceId> pairs
    //The depthMap can be obtained from the device.getId() and then create a manager and getting the DepthConfigurationMap

    private static class ConfigureDepthSurface
    {
        public int  getSurfaceFormat(Surface s)
        {
           return  nativeGetSurfaceFormat(s);
        }
        public void  configureNextStreamsType(int type)
        {
            nativeConfigureSurfacesType(type);// TODO change it to use source ID
        }
        public void startStreamConfiguration()
        {
            nativeSendExtendedConfigurationCommand(true);
        }
        public void endStreamConfiguration()
        {
            nativeSendExtendedConfigurationCommand(false);
        }
        private synchronized native int nativeGetSurfaceFormat(Surface s);
        private synchronized native void nativeConfigureSurfacesType(int type);
        private synchronized native void nativeSendExtendedConfigurationCommand(boolean isStart);

        static {
            System.loadLibrary("inteldepthcamera_jni");
        }
    }
    private static HashSet<Surface> mConfiguredStreams = new HashSet<Surface>();
    private static ConfigureDepthSurface mConfigUtil = new ConfigureDepthSurface();

    private static boolean configureNewSpecialTargets(List<Surface> newSpecialTargets, List<Surface> oldTargets, CameraDevice device,
        CameraCaptureSession.StateCallback callback, Handler handler, int usage, int totalTargetsCount) throws CameraAccessException
    {
        oldTargets.addAll(newSpecialTargets);
        boolean isFinalConfigure = (oldTargets.size() == totalTargetsCount);

        if (isFinalConfigure)
        {
            mConfigUtil.configureNextStreamsType(usage | 0x100);
            device.createCaptureSession(oldTargets, callback, handler);
            mConfigUtil.endStreamConfiguration();
        }
        else
        {
            CameraDeviceImpl myDevice = (CameraDeviceImpl)device;
            mConfigUtil.configureNextStreamsType(usage);
            myDevice.configureOutputs(oldTargets);
        }
        return isFinalConfigure;
    }

    public static void createDepthCaptureSession(CameraDevice device, CameraCharacteristics camChars, List<Pair<Surface, Integer> > targetsSourceIdMap,
             CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException

    {
        if (device == null)
            throw new IllegalArgumentException(
                    "CameraDevice argument cannot be null");

        DepthCameraStreamConfigurationMap depthConfigMap = new DepthCameraStreamConfigurationMap(camChars);
        //for each surface in the list, configure

        List<Surface> targets  = new ArrayList<Surface>();
        List<Surface> oldTargets = new ArrayList<Surface>();
        List<Surface> leftTargets = new ArrayList<Surface>();
        List<Surface> rightTargets = new ArrayList<Surface>();
        List<Surface> depthPreviewTargets = new ArrayList<Surface>();
        int leftUsage = 0;
        int rightUsage = 0;
        int depthPreviewUsage = 0;

        for (int i=0; i < targetsSourceIdMap.size(); i++)
        {
            Pair<Surface, Integer> item = targetsSourceIdMap.get(i);
            Surface surface = item.first;
            int sourceId = item.second;
            targets.add(surface);
            if (mConfiguredStreams.contains(surface))
            {
                oldTargets.add(surface);
                continue;
            }
            //new surface, check if it has usage bits
            int format = mConfigUtil.getSurfaceFormat(surface);
            int usageMask = depthConfigMap.getUsageMask(sourceId,format);

            if (usageMask != 0)
            {
                //new special stream
                if ( sourceId == DepthCameraStreamConfigurationMap.LEFT_STREAM_SOURCE_ID)
                {
                    leftTargets.add(surface);
                    leftUsage = usageMask;
                }
                else if (sourceId == DepthCameraStreamConfigurationMap.RIGHT_STREAM_SOURCE_ID)
                {
                    rightTargets.add(surface);
                    rightUsage = usageMask;
                }
                else if (sourceId == DepthCameraStreamConfigurationMap.DEPTH_STREAM_SOURCE_ID)
                {
                    depthPreviewTargets.add(surface);
                    depthPreviewUsage = usageMask;
                }
                else
                {
                    Log.e("DepthCameraCaptureSessionConfiguration","non valide stream id for usage mask value");
                }
            }
        }
        mConfiguredStreams.clear();//reset configuredStreams
        mConfiguredStreams.addAll(targets);

        if (!leftTargets.isEmpty() ||
            !rightTargets.isEmpty() ||
            !depthPreviewTargets.isEmpty())
        {
            if (!(device instanceof CameraDeviceImpl))
            {
                throw new IllegalArgumentException(
                    "CameraDevice argument has to be instance of CameraDeviceImpl");
            }
            CameraDeviceImpl myDevice = (CameraDeviceImpl)device;

            mConfigUtil.startStreamConfiguration();

            if (!oldTargets.isEmpty())
                myDevice.configureOutputs(oldTargets);


            if (!leftTargets.isEmpty())
                if (configureNewSpecialTargets(leftTargets, oldTargets, myDevice, callback, handler, leftUsage, targets.size()))
                    return;

            if (!rightTargets.isEmpty())
                if (configureNewSpecialTargets(rightTargets, oldTargets, myDevice, callback, handler, rightUsage, targets.size()))
                    return;

            if (!depthPreviewTargets.isEmpty())
                if (configureNewSpecialTargets(depthPreviewTargets, oldTargets, myDevice, callback, handler, depthPreviewUsage, targets.size()))
                    return;

        }
        mConfigUtil.endStreamConfiguration();
        device.createCaptureSession(targets, callback, handler);
    }
}
