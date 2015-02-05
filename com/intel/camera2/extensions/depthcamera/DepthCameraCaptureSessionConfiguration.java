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
