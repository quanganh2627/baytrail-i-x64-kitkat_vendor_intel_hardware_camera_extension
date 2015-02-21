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
    if (ret1 || ret2 || ret3 || ret4)
        ALOGV("%s - registering SurfaceQuery failed %d, %d , %d, %d", __FUNCTION__, ret1, ret2, ret3, ret4);

    return (ret1 || ret2 || ret3);

}

// jint JNI_OnLoad(JavaVM* vm, void* reserved) in com_intel_camera2_extensions_DepthCameraImageReader.cpp

