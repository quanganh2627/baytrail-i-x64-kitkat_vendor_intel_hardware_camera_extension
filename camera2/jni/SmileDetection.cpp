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
#include <stdio.h>
#include <stdlib.h>
#include "PvlUtil.h"
#include "pvl_eye_detection.h"
#include "pvl_smile_detection.h"

struct SmileDetectionEngine {
    pvl_smile_detection *smiledetection;
    int debug;
};

static void getEDResult(JNIEnv* env, pvl_eye_detection_result *out, jobjectArray jEDResults, int index);
static jobjectArray createJResult(JNIEnv* env, pvl_smile_detection_result* results, int num);
static jobject createJConfig(JNIEnv *env, pvl_smile_detection *detection);
static jobject createJParam(JNIEnv *env, pvl_smile_detection_parameters *param);
static void getParam(JNIEnv *env, pvl_smile_detection_parameters *param, jobject jParam);

static pvl_smile_detection* getSmileDetection(jlong instance)
{
    if (instance != 0) {
        SmileDetectionEngine* fde = (SmileDetectionEngine*)instance;
        return fde->smiledetection;
    } else {
        return NULL;
    }
}

jlong SmileDetection_create(JNIEnv* env, jobject thiz)
{
    pvl_version version = {0, 0, 0, NULL};
    pvl_config config = {{0, 0, 0, NULL}, {pvl_false, pvl_false, pvl_false, pvl_false, NULL, NULL}, {NULL, NULL, NULL, NULL, NULL}};

    SmileDetectionEngine *fde = NULL;
    pvl_smile_detection *sd = NULL;//(pvl_smile_detection*)calloc(1, sizeof(pvl_smile_detection));

    pvl_smile_detection_get_default_config(&config);
    pvl_err ret = pvl_smile_detection_create(&config, &sd);
    if (ret == pvl_success) {
        fde = (SmileDetectionEngine*)calloc(1, sizeof(SmileDetectionEngine));
        fde->smiledetection = sd;
    }

    return (jlong)fde;
}

void SmileDetection_destroy(JNIEnv* env, jobject thiz, jlong instance)
{
    pvl_smile_detection* sd = getSmileDetection(instance);
    if (sd != NULL) {
        pvl_smile_detection_destroy(sd);
        free((void*)instance);
    }
}

jobjectArray SmileDetection_runInImage(JNIEnv* env, jobject thiz, jlong instance, jobject jIaFrame, jobjectArray jEDResults)
{
    jobjectArray jResult = NULL;
    pvl_smile_detection* sd = getSmileDetection(instance);
    if (sd != NULL) {
        pvl_image image;
        mapImage(env, &image, jIaFrame);

        jsize length = env->GetArrayLength(jEDResults);
        pvl_smile_detection_result results[length]; 
        memset(results, 0, sizeof(pvl_smile_detection_result) * length);

        for (int i = 0; i < length; i++) {
            pvl_eye_detection_result edResult;
            getEDResult(env, &edResult, jEDResults, i);

            pvl_err ret = pvl_smile_detection_run_in_image(sd, &image, &edResult.left_eye, &edResult.right_eye, &results[i]);
        }

        jResult = createJResult(env, results, length);
    }

    return jResult;
}

void SmileDetection_setParam(JNIEnv* env, jobject thiz, jlong instance, jobject jParam)
{
    pvl_smile_detection* sd = getSmileDetection(instance);
    if (sd != NULL) {
        pvl_smile_detection_parameters param;
        getParam(env, &param, jParam);
        pvl_smile_detection_set_parameters(sd, &param);
    }
}

jobject SmileDetection_getParam(JNIEnv* env, jobject thiz, jlong instance)
{
    pvl_smile_detection* sd = getSmileDetection(instance);
    if (sd != NULL) {
        pvl_smile_detection_parameters param;
        pvl_smile_detection_get_parameters(sd, &param);
        return createJParam(env, &param);
    }
    return NULL;
}

jobject SmileDetection_getConfig(JNIEnv* env, jobject thiz, jlong instance)
{
    pvl_smile_detection* sd = getSmileDetection(instance);
    if (sd != NULL) {
        return createJConfig(env, sd);
    }
    return NULL;
}

void getEDResult(JNIEnv* env, pvl_eye_detection_result *out, jobjectArray jEDResults, int index) {
    jobject edResult = env->GetObjectArrayElement(jEDResults, index);

    getValuePoint(env, &out->left_eye, edResult, "leftEye");
    getValuePoint(env, &out->right_eye, edResult, "rightEye");
    out->confidence = getValueInt(env, edResult, "confidence");
}

jobjectArray createJResult(JNIEnv* env, pvl_smile_detection_result* results, int num)
{
    jclass cls = env->FindClass(CLASS_SMILE_DETECTION_INFO);
    jobjectArray retArray = (jobjectArray)env->NewObjectArray(num, cls, NULL);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(II)V");

    for (int i = 0; i < num; i++) {
        pvl_smile_detection_result* result = &results[i];
        jobject jResult = env->NewObject(cls, constructor, result->score, result->state);
        env->SetObjectArrayElement(retArray, i, jResult);
    }

    return retArray;
}

jobject createJConfig(JNIEnv *env, pvl_smile_detection *detection)
{
    jclass cls = env->FindClass(CLASS_SMILE_DETECTION_CONFIG);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(" SIG_PVL_VERSION "II)V");
    return env->NewObject(cls, constructor,
                            createJVersion(env, &detection->version),
                            detection->default_threshold,
                            detection->rop_tolerance);
}

jobject createJParam(JNIEnv *env, pvl_smile_detection_parameters *param)
{
    jclass cls = env->FindClass(CLASS_SMILE_DETECTION_PARAM);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(I)V");
    return env->NewObject(cls, constructor,
                            param->threshold);
}

void getParam(JNIEnv *env, pvl_smile_detection_parameters *param, jobject jParam)
{
    param->threshold = getValueInt(env, jParam, "threshold");
}

static JNINativeMethod gMethods[] = {
    { "create",
      "()J",
      (void*)SmileDetection_create },

    { "destroy",
      "(J)V",
      (void*)SmileDetection_destroy },

    { "runInImage",
      "(J"SIG_IAFRAME "["SIG_EYE_DETECTION_INFO")["SIG_SMILE_DETECTION_INFO,
      (void*)SmileDetection_runInImage },

    { "setParam",
      "(J"SIG_SMILE_DETECTION_PARAM")V",
      (void*)SmileDetection_setParam },

    { "getParam",
      "(J)"SIG_SMILE_DETECTION_PARAM,
      (void*)SmileDetection_getParam },

    { "getConfig",
      "(J)"SIG_SMILE_DETECTION_CONFIG,
      (void*)SmileDetection_getConfig },
};

int register_jni_SmileDetection(JNIEnv *env)
{
    return jniRegisterNativeMethods(env, CLASS_SMILE_DETECTION, gMethods,
        sizeof(gMethods)/sizeof(JNINativeMethod));
}

