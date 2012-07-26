/*
**
** Copyright 2012, Intel Corporation
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

//#define LOG_NDEBUG 0
#define LOG_TAG "IntelCamera-JNI"

#include <camera/Camera.h>
#include <jni.h>
#include <JNIHelp.h>
#include <utils/Log.h>
#include <android_runtime/AndroidRuntime.h>

#include "../include/intel_camera_extensions.h"

using namespace android;

extern sp<Camera> get_native_camera(JNIEnv *env, jobject thiz, struct JNICameraContext** context);

static bool com_intel_camera_extensions_IntelCamera_enableIntelCamera(JNIEnv *env, jobject thiz, jobject cameraDevice)
{
    LOGV("enableIntelCamera");
    sp<Camera> camera = get_native_camera(env, cameraDevice, NULL);
    if (camera == 0) return false;

    return camera->sendCommand(CAMERA_CMD_ENABLE_INTEL_PARAMETERS, 0, 0);
}


static JNINativeMethod camMethods[] = {
    {"enableIntelCamera", "(Landroid/hardware/Camera;)Z", (void *)com_intel_camera_extensions_IntelCamera_enableIntelCamera}
};


int register_com_intel_camera_extensions_IntelCamera(JNIEnv *env)
{
    LOGV("regist intel camera");
    return AndroidRuntime::registerNativeMethods(env, "com/intel/camera/extensions/IntelCamera",
                                                camMethods, NELEM(camMethods));
}

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("ERROR: GetEnv failed\n");
        goto fail;
    }
    assert(env != NULL);

    if (register_com_intel_camera_extensions_IntelCamera(env) < 0) {
        LOGE("ERROR: native registration failed\n");
        goto fail;
    }
    result = JNI_VERSION_1_4;

fail:
    return result;

}


