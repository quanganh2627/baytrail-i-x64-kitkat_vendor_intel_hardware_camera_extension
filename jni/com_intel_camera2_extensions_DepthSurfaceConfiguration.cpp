/*
 * Copyright 2013 The Android Open Source Project
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
    ALOGI("%s: usagebits 0x%x ", __FUNCTION__, usageBits);

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

