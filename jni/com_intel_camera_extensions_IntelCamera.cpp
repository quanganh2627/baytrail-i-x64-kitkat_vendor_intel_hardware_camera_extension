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
#include "android_hardware_Camera.h"

#include "intel_camera_extensions.h"

using namespace android;

class IntelCameraListener: public CameraListener
{
public:
    IntelCameraListener(JNICameraContext* aRealListener, jobject weak_this, jclass clazz);
    ~IntelCameraListener() { release();}
    void notify(int32_t msgType, int32_t ext1, int32_t ext2);
    void postData(int32_t msgType, const sp<IMemory>& dataPtr,
                  camera_frame_metadata_t *metadata) ;
    void postDataTimestamp(nsecs_t timestamp, int32_t msgType, const sp<IMemory>& dataPtr);
    sp<Camera> getCamera() { return mRealListener->getCamera();}
    void release();

private:
    JNICameraContext* mRealListener;
    jobject mCameraJObjectWeak;
    jclass mCameraJClass;

};

struct fields_t {
    jfieldID intel_listener;
    jmethodID post_event;
};

static fields_t fields;

extern sp<Camera> get_native_camera(JNIEnv *env, jobject thiz, struct JNICameraContext** context);

static void com_intel_camera_extensions_IntelCamera_native_setup(JNIEnv *env, jobject thiz,
        jobject weak_this, jobject cameraDevice)
{
    LOGV("native setup");
    JNICameraContext* listener;
    sp<Camera> camera = get_native_camera(env, cameraDevice, &listener);
    if (camera == 0) return;

    jclass clazz = env->GetObjectClass(thiz);
    sp<IntelCameraListener> l = new IntelCameraListener(listener, weak_this, clazz);
    l->incStrong(thiz);
    camera->setListener(l);
    env->SetIntField(thiz, fields.intel_listener, (int)l.get());
}

static void com_intel_camera_extensions_IntelCamera_native_release(JNIEnv *env, jobject thiz)
{
    LOGV("native_release");
    IntelCameraListener* intel_listener = reinterpret_cast<IntelCameraListener*>(env->GetIntField(thiz, fields.intel_listener));
    // Make sure we do not attempt to callback on a deleted Java object.
    env->SetIntField(thiz, fields.intel_listener, 0);
    if (intel_listener != NULL)
        // remove context to prevent further Java access
        intel_listener->decStrong(thiz);
}

static bool com_intel_camera_extensions_IntelCamera_enableIntelCamera(JNIEnv *env, jobject thiz, jobject cameraDevice)
{
    LOGV("enableIntelCamera");
    IntelCameraListener* intel_listener = reinterpret_cast<IntelCameraListener*>(env->GetIntField(thiz, fields.intel_listener));
    sp<Camera> camera = intel_listener->getCamera();
    if (camera == NULL) {
        LOGE("get camera handle failed");
        return false;
    }

    return camera->sendCommand(CAMERA_CMD_ENABLE_INTEL_PARAMETERS, 0, 0);
}

static void com_intel_camera_extensions_IntelCamera_startSceneDetection(JNIEnv *env, jobject thiz)
{
    LOGV("startSceneDetection");
    IntelCameraListener* intel_listener = reinterpret_cast<IntelCameraListener*>(env->GetIntField(thiz, fields.intel_listener));
    sp<Camera> camera = intel_listener->getCamera();
    if (camera == NULL) {
        LOGE("get camera handle failed");
        return;
    }

    camera->sendCommand(CAMERA_CMD_START_SCENE_DETECTION, 0, 0);
}


static void com_intel_camera_extensions_IntelCamera_stopSceneDetection(JNIEnv *env, jobject thiz)
{
    LOGV("stopSceneDetection");
    IntelCameraListener* intel_listener = reinterpret_cast<IntelCameraListener*>(env->GetIntField(thiz, fields.intel_listener));
    sp<Camera> camera = intel_listener->getCamera();
    if (camera == NULL) {
        LOGE("get camera handle failed");
        return;
    }

    camera->sendCommand(CAMERA_CMD_STOP_SCENE_DETECTION, 0, 0);
}

IntelCameraListener::IntelCameraListener(JNICameraContext* aRealListener, jobject weak_this, jclass clazz)
{
    LOGV("new IntelCameraListener");

    mRealListener = aRealListener;
    JNIEnv *env = AndroidRuntime::getJNIEnv();
    mCameraJClass = (jclass)env->NewGlobalRef(clazz);
    mCameraJObjectWeak = env->NewGlobalRef(weak_this);
}

void IntelCameraListener::release()
{
    LOGV("release IntelCameraListener");
    JNIEnv *env = AndroidRuntime::getJNIEnv();

    if (mCameraJClass != NULL) {
        env->DeleteGlobalRef(mCameraJClass);
        mCameraJClass = NULL;
    }

    if (mCameraJObjectWeak != NULL) {
        env->DeleteGlobalRef(mCameraJObjectWeak);
        mCameraJObjectWeak = NULL;
    }

    mRealListener = NULL;
}

void IntelCameraListener::notify(int32_t msgType, int32_t ext1, int32_t ext2)
{
    LOGV("intel notification, msgType:0%d", msgType);
    JNIEnv *env = AndroidRuntime::getJNIEnv();

    switch (msgType) {
    case CAMERA_MSG_SCENE_DETECT:
        env->CallStaticVoidMethod(mCameraJClass, fields.post_event,
        mCameraJObjectWeak, msgType, ext1, ext2, NULL);
        break;;
    default:
        if (mRealListener != NULL)
            mRealListener->notify(msgType, ext1, ext2);
        break;
    }
}

void IntelCameraListener::postData(int32_t msgType, const sp<IMemory>& dataPtr,
                           camera_frame_metadata_t *metadata)
{
    if (mRealListener != NULL)
        mRealListener->postData(msgType,dataPtr,metadata);
}

void IntelCameraListener::postDataTimestamp(nsecs_t timestamp, int32_t msgType, const sp<IMemory>& dataPtr)
{
    if (mRealListener != NULL)
        mRealListener->postDataTimestamp(timestamp,  msgType, dataPtr);
}

static JNINativeMethod camMethods[] = {
    { "native_setup",
      "(Ljava/lang/Object;Landroid/hardware/Camera;)V",
      (void*)com_intel_camera_extensions_IntelCamera_native_setup },
    { "native_release",
      "()V",
      (void*)com_intel_camera_extensions_IntelCamera_native_release },
    { "native_enableIntelCamera",
      "()Z",
      (void *)com_intel_camera_extensions_IntelCamera_enableIntelCamera},
    { "native_startSceneDetection",
      "()V",
      (void *)com_intel_camera_extensions_IntelCamera_startSceneDetection },
    { "native_stopSceneDetection",
      "()V",
      (void *)com_intel_camera_extensions_IntelCamera_stopSceneDetection }
};

int register_com_intel_camera_extensions_IntelCamera(JNIEnv *env)
{
    LOGV("regist intel camera");
    jclass clazz = env->FindClass("com/intel/camera/extensions/IntelCamera");
    jfieldID field = env->GetFieldID(clazz, "mNativeContext", "I");
    if (field != NULL) fields.intel_listener = field;
    fields.post_event = env->GetStaticMethodID(clazz, "postEventFromNative",
                                        "(Ljava/lang/Object;IIILjava/lang/Object;)V");

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


