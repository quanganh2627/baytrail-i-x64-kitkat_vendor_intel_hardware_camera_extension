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

// ----------------------------------------------------------------------------

using namespace android;
static void DepthSurfaceConfiguration_configureSurfaceUsageBits(JNIEnv* env, jobject thiz, jobject jsurface, jint usageBits)
{
    int res;
    int32_t currUsageBits;

    ALOGV("%s: ", __FUNCTION__);
    ALOGV("%s: usagebits 0x%x ", __FUNCTION__, usageBits);

    sp<IGraphicBufferProducer> gbp;
    sp<Surface> surface;
    if (jsurface) {
        surface = android_view_Surface_getSurface(env, jsurface);
        if (surface != NULL) {
            gbp = surface->getIGraphicBufferProducer();
        }
    }
    if ((res = gbp->query(NATIVE_WINDOW_CONSUMER_USAGE_BITS, &currUsageBits)) != OK)
    {
        ALOGE("%s Failed to query consumer usage bis", __FUNCTION__);
        return;
    }

    int32_t newConsumerBits = currUsageBits | usageBits;
    gbp->setConsumerUsageBits(newConsumerBits);
}

static JNINativeMethod gDepthSurfaceConfiguration[] = {
    {"nativeConfigureSurface", "(Landroid/view/Surface;I)V", (void*)DepthSurfaceConfiguration_configureSurfaceUsageBits },
};

int register_intel_camera2_extensions_depthcamera_DepthSurfaceConfiguration(JNIEnv *env)
{
    return AndroidRuntime::registerNativeMethods(env,
            "com/intel/camera2/extensions/depthcamera/DepthCameraCaptureSessionConfiguration$ConfigureDepthSurface", gDepthSurfaceConfiguration, NELEM(gDepthSurfaceConfiguration));

}

