package com.intel.camera2.extensions.depthcamera;

import android.view.Surface;
import android.util.Pair;
import java.util.List;
import java.util.ArrayList;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.content.Context;
import android.os.Handler;

public class DepthCameraCaptureSessionConfiguration
{
    private static class ConfigureDepthSurface
    {
        public List<Surface> configureDepthSurfaces(List<Pair<Surface, Integer> > targetsSourceIdMap, DepthCameraStreamConfigurationMap depthConfigMap)
        {
            int[] usages = new int[targetsSourceIdMap.size()]; //to send to depth camera binder
            List<Surface> targets  = new ArrayList<Surface>(); //to be returned to createCapture
            Pair<Surface, Integer> item;
            Surface surface;
            int sourceId;
            int format;

            for (int i = 0; i < targetsSourceIdMap.size(); i++)
            {
                item = targetsSourceIdMap.get(i);
                surface = item.first;
                sourceId = item.second;

                format = nativeGetSurfaceFormat(surface);
                usages[i] = depthConfigMap.getUsageMask(sourceId,format);
                targets.add(surface);
                Log.d("DepthCameraCaptureSessionConfiguration", "format 0x" + Integer.toHexString(format) +  "usageMask 0x" + Integer.toHexString(usages[i]));
            }

            nativeConfigureUsageBits(usages);

            return targets;
        }
        private synchronized native int nativeGetSurfaceFormat(Surface s);
        private synchronized native void nativeConfigureUsageBits(int[] usageMask);

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

        device.createCaptureSession(configUtil.configureDepthSurfaces(targetsSourceIdMap, depthConfigMap) , callback, handler);
    }
}
