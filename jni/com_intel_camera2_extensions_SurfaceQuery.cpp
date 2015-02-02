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
#define LOG_TAG "SurfaceQuery_JNI"
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
#define ALIGN(x, mask) ( ((x) + (mask) - 1) & ~((mask) - 1) )

// ----------------------------------------------------------------------------

using namespace android;
static jint SurfaceQuery_getSurfaceFormat(JNIEnv* env, jobject thiz, jobject surface)
{
    ALOGV("%s: ", __FUNCTION__);
    sp<ANativeWindow> anw = android_view_Surface_getNativeWindow(env, surface);
    int format;

    if ((anw->query(anw.get(), NATIVE_WINDOW_FORMAT, &format)) != OK) {
           ALOGE("%s: Failed to query Surface height", __FUNCTION__);
           return 0;
    }

    return format;
}
static jint SurfaceQuery_getSurfaceWidth(JNIEnv* env, jobject thiz, jobject surface)
{
    ALOGV("%s: ", __FUNCTION__);
    sp<ANativeWindow> anw = android_view_Surface_getNativeWindow(env, surface);
    int width;

    if ((anw->query(anw.get(), NATIVE_WINDOW_WIDTH, &width)) != OK) {
           ALOGE("%s: Failed to query Surface width", __FUNCTION__);
           return 0;
    }

    return width;
}

static jint SurfaceQuery_getSurfaceHeight(JNIEnv* env, jobject thiz, jobject surface)
{
    ALOGV("%s: ", __FUNCTION__);
    sp<ANativeWindow> anw = android_view_Surface_getNativeWindow(env, surface);
    int height;

    if ((anw->query(anw.get(), NATIVE_WINDOW_WIDTH, &height)) != OK) {
           ALOGE("%s: Failed to query Surface height", __FUNCTION__);
           return 0;
    }

    return height;
}


static JNINativeMethod gSurfaceQuery_1[] = {
    {"nativeGetSurfaceFormat", "(Landroid/view/Surface;)I", (void*)SurfaceQuery_getSurfaceFormat },


};
static JNINativeMethod gSurfaceQuery_2[] = {
    {"nativeGetSurfaceWidth", "(Landroid/view/Surface;)I", (void*)SurfaceQuery_getSurfaceWidth },
    {"nativeGetSurfaceHeight", "(Landroid/view/Surface;)I", (void*)SurfaceQuery_getSurfaceHeight },
};


int register_intel_camera2_extensions_depthcamera_SurfaceQuery(JNIEnv *env) {

    int ret1 = AndroidRuntime::registerNativeMethods(env,
                   "com/intel/camera2/extensions/depthcamera/DepthCameraCaptureSessionConfiguration$ConfigureDepthSurface", gSurfaceQuery_1, NELEM(gSurfaceQuery_1));
    int ret2 = AndroidRuntime::registerNativeMethods(env,
                   "com/intel/camera2/extensions/depthcamera/DepthCameraCalibrationDataMap", gSurfaceQuery_2, NELEM(gSurfaceQuery_2));
    int ret3 = AndroidRuntime::registerNativeMethods(env,
                   "com/intel/camera2/extensions/depthcamera/DepthCameraStreamConfigurationMap", gSurfaceQuery_2, NELEM(gSurfaceQuery_2));
    int ret4 = AndroidRuntime::registerNativeMethods(env,
                   "com/intel/camera2/extensions/depthcamera/DepthCameraStreamConfigurationMap", gSurfaceQuery_1, NELEM(gSurfaceQuery_1));
    if ( ret1 || ret2 || ret3 || ret4 )
      ALOGV("%s - registering SurfaceQuery failed %d, %d , %d, %d", __FUNCTION__, ret1, ret2, ret3, ret4);

    return (ret1 || ret2 || ret3);

}

// jint JNI_OnLoad(JavaVM* vm, void* reserved) in com_intel_camera2_extensions_DepthCameraImageReader.cpp

