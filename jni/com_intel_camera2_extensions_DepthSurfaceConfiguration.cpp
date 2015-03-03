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


//#define LOG_NDEBUG 0
#define LOG_TAG "DepthSurfaceConfiguration_JNI"
#include <utils/Log.h>
#include <utils/misc.h>
#include <utils/List.h>
#include <utils/String8.h>
#include <cstdio>
#include <gui/Surface.h>
#include <camera3.h>
#include <android_runtime/AndroidRuntime.h>
#include <android_runtime/android_view_Surface.h>

#include <jni.h>
#include <JNIHelp.h>
#include <stdint.h>
#include <inttypes.h>

#include "ufo/graphics.h"
#include "ICameraHAL.h"

// ----------------------------------------------------------------------------

using namespace android;
//TODO native init, and save binder isntance
static void DepthSurfaceConfiguration_configureNextSurfacesType(JNIEnv* env, jobject thiz, jint type)
{
    sp<IServiceManager> sm = defaultServiceManager();

    sp<IBinder> binder = sm->getService(String16(CAMERA_HAL_SERVICE_NAME));
    if(binder == 0)
    {
        jniThrowRuntimeException(env, "COuldn't find Camera HAL service binder");
        return;
    }
    const sp<ICameraHAL>& bts = interface_cast<ICameraHAL>(binder);

    bts->setStreamType(type); // Set flags=1 for stream id 0
}
static void DepthSurfaceConfiguration_sendExtendedConfigurationCommand(JNIEnv* env, jobject thiz, jboolean isStart)
{
     sp<IServiceManager> sm = defaultServiceManager();

    sp<IBinder> binder = sm->getService(String16(CAMERA_HAL_SERVICE_NAME));
    if(binder == 0)
    {
       jniThrowRuntimeException(env, "COuldn't find Camera HAL service binder");
       return;
    }
    const sp<ICameraHAL>& bts = interface_cast<ICameraHAL>(binder);
    if ( isStart)
        bts->startStreamConfig();
    else
        bts->endStreamConfig();
}

static JNINativeMethod gDepthSurfaceConfiguration[] = {
    {"nativeConfigureSurfacesType", "(I)V", (void*)DepthSurfaceConfiguration_configureNextSurfacesType },
    {"nativeSendExtendedConfigurationCommand", "(Z)V", (void*) DepthSurfaceConfiguration_sendExtendedConfigurationCommand},
};

int register_intel_camera2_extensions_depthcamera_DepthSurfaceConfiguration(JNIEnv *env)
{
    return AndroidRuntime::registerNativeMethods(env,
            "com/intel/camera2/extensions/depthcamera/DepthCameraCaptureSessionConfiguration$ConfigureDepthSurface", gDepthSurfaceConfiguration, NELEM(gDepthSurfaceConfiguration));

}

