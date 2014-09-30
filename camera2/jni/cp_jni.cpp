/*
 * Copyright 2015, Intel Corporation
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
#include <utils/RefBase.h>
#include <binder/IMemory.h>
#include <stdlib.h>
#include <string.h>

#include "cp_ipu_jni.h"
#include "cp_cpu_jni.h"

#define SUPPORT_TARGET_NUMBER 10

static ia_cp_target getTarget(jlong instance) {
    if (instance != 0) {
        return ((CPEngine*)instance)->target;
    } else {
        return ia_cp_tgt_ia;
    }
}

jintArray CP_getAvailableAccTarget(JNIEnv* env, jobject thiz)
{
    LOGI("%s", __FUNCTION__);
    jint err = ia_err_none;
    int cArray[SUPPORT_TARGET_NUMBER];
    int len = 0;
    memset(cArray, 0, SUPPORT_TARGET_NUMBER * sizeof(int));

    cArray[len ++] = ia_cp_tgt_ia;
    bool isIpuAccSupported = CP_ipu_getAvailableAccTarget();
    if (isIpuAccSupported)
        cArray[len ++] = ia_cp_tgt_ipu;

    jintArray jArray = env->NewIntArray(len);
    if (jArray != NULL) {
        env->SetIntArrayRegion(jArray, 0, len, cArray);
    }
    return jArray;
}

jint CP_getHdrAccMode(JNIEnv* env, jobject thiz, jint target)
{
    LOGI("%s", __FUNCTION__);
    if (target == ia_cp_tgt_ia)
        return ACC_EXTENSION;
    else if (target == ia_cp_tgt_ipu)
        return CP_ipu_getHdrAccMode();
    else {
        LOGE("this target can't be supported");
        return ERROR_MODE;
    }
}

jint CP_getUllAccMode(JNIEnv* env, jobject thiz, jint target)
{
    LOGI("%s", __FUNCTION__);
    if (target == ia_cp_tgt_ia)
        return ACC_EXTENSION;
    else if (target == ia_cp_tgt_ipu)
        return CP_ipu_getUllAccMode();
    else {
        LOGE("this target can't be supported");
        return ERROR_MODE;
    }
}

jlong CP_init(JNIEnv* env, jobject thiz, jint tgt)
{
    LOGI("%s", __FUNCTION__);
    if (tgt == ia_cp_tgt_ipu) {
        return CP_ipu_init(env, thiz);
    } else if (tgt == ia_cp_tgt_ia) {
        return CP_cpu_init(env,thiz);
    } else {
        LOGE("this target(%d) can't be supported", tgt);
        return 0;
    }
}

void CP_uninit(JNIEnv* env, jobject thiz, jlong instance)
{
    LOGI("%s", __FUNCTION__);
    ia_cp_target tgt = getTarget(instance);

    if (tgt == ia_cp_tgt_ipu)
        CP_ipu_uninit(env, thiz,instance);
    else if (tgt == ia_cp_tgt_ia)
        CP_cpu_uninit(env,thiz,instance);
    else {
        LOGE("this target can't be supported");
        return;
    }
}

jint CP_hdrInit(JNIEnv* env, jobject thiz, jlong instance, jint width, jint height, jobject jBlendOption)
{
    LOGI("%s", __FUNCTION__);
    ia_cp_target tgt = getTarget(instance);

    jint err = ia_err_none;
    if (tgt == ia_cp_tgt_ipu)
        err = CP_ipu_hdrInit(env, thiz, instance, width, height, jBlendOption);
    else if (tgt == ia_cp_tgt_ia)
        err = CP_cpu_hdrInit(env, thiz, instance, width, height, jBlendOption);
    else {
        LOGE("this target can't be supported");
        err = ia_err_argument;
    }

    LOGD("ret = %d", err);
    return err;
}

jint CP_hdrUninit(JNIEnv* env, jobject thiz, jlong instance)
{
    LOGI("%s", __FUNCTION__);
    ia_cp_target tgt = getTarget(instance);
    jint err = ia_err_none;
    if (tgt == ia_cp_tgt_ipu)
        err = CP_ipu_hdrUninit(env, thiz, instance);
    else if (tgt == ia_cp_tgt_ia)
        err = CP_cpu_hdrUninit(env, thiz, instance);
    else {
        LOGE("this target can't be supported");
        err = ia_err_argument;
    }
    return err;
}

jobject CP_hdrCompose(JNIEnv* env, jobject thiz, jlong instance, jobjectArray jInputIaFrames, jobject jHdrOption)
{
    LOGI("%s", __FUNCTION__);
    jobject jOutputIaFrame = NULL;

    if (jInputIaFrames == NULL) {
        ALOGE("input IaFrame is NULL!");
        return NULL;
    }
    ia_cp_target tgt = getTarget(instance);
    if (tgt == ia_cp_tgt_ipu)
        jOutputIaFrame = CP_ipu_hdrCompose(env, thiz, instance, jInputIaFrames, jHdrOption);
    else if (tgt == ia_cp_tgt_ia)
        jOutputIaFrame = CP_cpu_hdrCompose(env, thiz, instance, jInputIaFrames, jHdrOption);
    else {
        LOGE("this target can't be supported");
    }
    return jOutputIaFrame;
}

jint CP_ullInit(JNIEnv* env, jobject thiz, jlong instance, jint width, jint height, jobject jBlendOption)
    {
    LOGI("%s", __FUNCTION__);
    ia_cp_target tgt = getTarget(instance);
    jint err = ia_err_none;
    if (tgt == ia_cp_tgt_ipu)
        err = CP_ipu_ullInit(env, thiz, instance, width, height, jBlendOption);
    else if (tgt == ia_cp_tgt_ia)
        err = CP_cpu_ullInit(env, thiz, instance, width, height, jBlendOption);
    else {
        LOGE("this target can't be supported");
        err = ia_err_argument;
        }

    LOGD("ret = %d", err);
    return err;
        }

jint CP_ullUninit(JNIEnv* env, jobject thiz, jlong instance)
{
    LOGI("%s", __FUNCTION__);
    ia_cp_target tgt = getTarget(instance);
    jint err = ia_err_none;
    if (tgt == ia_cp_tgt_ipu)
        err = CP_ipu_ullUninit(env, thiz, instance);
    else if (tgt == ia_cp_tgt_ia)
        err = CP_cpu_ullUninit(env, thiz, instance);
    else {
        LOGE("this target can't be supported");
        err = ia_err_argument;
    }
    return err;
}
jobject CP_ullCompose(JNIEnv* env, jobject thiz, jlong instance, jobjectArray jInputIaFrames, jobject jUllOption)
{
    LOGI("%s", __FUNCTION__);
    jobject jOutputIaFrame = NULL;
    if (jInputIaFrames == NULL) {
        LOGE("input IaFrame is NULL!");
        return NULL;
    }

    ia_cp_target tgt = getTarget(instance);
    if (tgt == ia_cp_tgt_ipu)
        jOutputIaFrame = CP_ipu_ullCompose(env, thiz, instance, jInputIaFrames, jUllOption);
    else if (tgt == ia_cp_tgt_ia)
        jOutputIaFrame = CP_cpu_ullCompose(env, thiz, instance, jInputIaFrames, jUllOption);
    else {
         LOGE("this target can't be supported");
    }
    return jOutputIaFrame;
}

static JNINativeMethod gMethods[] = {
    { "getAvailableAccTarget",
      "()[I",
      (void*)CP_getAvailableAccTarget},

    { "getHdrAccMode",
      "(I)I",
      (void*)CP_getHdrAccMode},

    { "getUllAccMode",
      "(I)I",
      (void*)CP_getUllAccMode},

    { "init",
      "(I)J",
      (void*)CP_init },

    { "uninit",
      "(J)V",
      (void*)CP_uninit },

    { "hdrInit",
      "(JII" SIG_BLENDER_OPTION ")I",
      (void*)CP_hdrInit },

    { "hdrUninit",
      "(J)I",
      (void*)CP_hdrUninit },

    { "hdrCompose",
      "(J[" SIG_IAFRAME SIG_HDR_OPTION ")" SIG_IAFRAME,
      (void*)CP_hdrCompose },

    { "ullInit",
      "(JII" SIG_BLENDER_OPTION ")I",
      (void*)CP_ullInit },

    { "ullUninit",
      "(J)I",
      (void*)CP_ullUninit },

    { "ullCompose",
      "(J[" SIG_IAFRAME SIG_ULL_OPTION ")" SIG_IAFRAME,
      (void*)CP_ullCompose },
};

int register_jni_CP(JNIEnv *env)
{
    return jniRegisterNativeMethods(env, CLASS_CP, gMethods,
        sizeof(gMethods)/sizeof(JNINativeMethod));
}

