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
#include <android_runtime/AndroidRuntime.h>
#include <jni.h>
#include <JNIHelp.h>
#include <stdint.h>
#include <inttypes.h>

#include "ICameraHAL.h"
// ----------------------------------------------------------------------------

using namespace android;
static void DepthSurfaceConfiguration_configureUsageBits(JNIEnv* env, jobject thiz, jintArray jconfigurations)
{
  ALOGV("%s: ", __FUNCTION__);

  sp<IServiceManager> sm = defaultServiceManager();
  sp<IBinder> binder = sm->getService(String16(CAMERA_HAL_SERVICE_NAME));
  if(binder == 0)
    jniThrowRuntimeException(env, "Camera HAL Service Binder not found");

  const sp<ICameraHAL>& bts = interface_cast<ICameraHAL>(binder);

  jsize len = env->GetArrayLength(jconfigurations);
  int i=0;
  // Get the elements
  jint* jintConfigurations = env->GetIntArrayElements(jconfigurations, 0);
  for ( i =0; i < len; i++ )
    bts->setStreamFlags(i, jintConfigurations[i]);
  env->ReleaseIntArrayElements(jconfigurations, jintConfigurations, 0);

}

static JNINativeMethod gDepthSurfaceConfiguration[] = {
    {"nativeConfigureUsageBits", "([I)V", (void*)DepthSurfaceConfiguration_configureUsageBits },
};

int register_intel_camera2_extensions_depthcamera_DepthSurfaceConfiguration(JNIEnv *env) {

    return AndroidRuntime::registerNativeMethods(env,
                   "com/intel/camera2/extensions/depthcamera/DepthCameraCaptureSessionConfiguration$ConfigureDepthSurface", gDepthSurfaceConfiguration, NELEM(gDepthSurfaceConfiguration));

}

