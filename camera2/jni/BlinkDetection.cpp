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
#include "pvl_blink_detection.h"

struct BlinkDetectionEngine {
    pvl_blink_detection *blinkdetection;
    int debug;
};

static void getEDResult(JNIEnv* env, pvl_eye_detection_result *out, jobjectArray jEDResults, int index);
static jobjectArray createJResult(JNIEnv* env, pvl_blink_detection_result* results, int num);
static jobject createJConfig(JNIEnv *env, pvl_blink_detection *detection);
static jobject createJParam(JNIEnv *env, pvl_blink_detection_parameters *param);
static void getParam(JNIEnv *env, pvl_blink_detection_parameters *param, jobject jParam);

static pvl_blink_detection* getBlinkDetection(jlong instance)
{
    if (instance != 0) {
        BlinkDetectionEngine* fde = (BlinkDetectionEngine*)instance;
        return fde->blinkdetection;
    } else {
        return NULL;
    }
}

jlong BlinkDetection_create(JNIEnv* env, jobject thiz)
{
    pvl_version version = {0, 0, 0, NULL};
    pvl_config config = {{0, 0, 0, NULL}, {pvl_false, pvl_false, pvl_false, pvl_false, NULL, NULL}, {NULL, NULL, NULL, NULL, NULL}};

    BlinkDetectionEngine *fde = NULL;
    pvl_blink_detection *bd = NULL;//(pvl_blink_detection*)calloc(1, sizeof(pvl_blink_detection));

    pvl_blink_detection_get_default_config(&config);
    pvl_err ret = pvl_blink_detection_create(&config, &bd);
    if (ret == pvl_success) {
        fde = (BlinkDetectionEngine*)calloc(1, sizeof(BlinkDetectionEngine));
        fde->blinkdetection = bd;
    }

    return (jlong)fde;
}

void BlinkDetection_destroy(JNIEnv* env, jobject thiz, jlong instance)
{
    pvl_blink_detection* bd = getBlinkDetection(instance);
    if (bd != NULL) {
        pvl_blink_detection_destroy(bd);
        free((void*)instance);
    }
}

jobjectArray BlinkDetection_runInImage(JNIEnv* env, jobject thiz, jlong instance, jobject jIaFrame, jobjectArray jEDResults)
{
    jobjectArray jResult = NULL;
    pvl_blink_detection* bd = getBlinkDetection(instance);
    if (bd != NULL) {
        pvl_image image;
        mapImage(env, &image, jIaFrame);

        jsize length = env->GetArrayLength(jEDResults);
        pvl_blink_detection_result results[length]; 
        memset(results, 0, sizeof(pvl_blink_detection_result) * length);

        for (int i = 0; i < length; i++) {
            pvl_eye_detection_result edResult;
            getEDResult(env, &edResult, jEDResults, i);

            pvl_err ret = pvl_blink_detection_run(bd, &image, &edResult.left_eye, &edResult.right_eye, &results[i]);
        }

        jResult = createJResult(env, results, length);
    }

    return jResult;
}

void BlinkDetection_setParam(JNIEnv* env, jobject thiz, jlong instance, jobject jParam)
{
    pvl_blink_detection* bd = getBlinkDetection(instance);
    if (bd != NULL) {
        pvl_blink_detection_parameters param;
        getParam(env, &param, jParam);
        pvl_blink_detection_set_parameters(bd, &param);
    }
}

jobject BlinkDetection_getParam(JNIEnv* env, jobject thiz, jlong instance)
{
    pvl_blink_detection* bd = getBlinkDetection(instance);
    if (bd != NULL) {
        pvl_blink_detection_parameters param;
        pvl_blink_detection_get_parameters(bd, &param);
        return createJParam(env, &param);
    }
    return NULL;
}

jobject BlinkDetection_getConfig(JNIEnv* env, jobject thiz, jlong instance)
{
    pvl_blink_detection* bd = getBlinkDetection(instance);
    if (bd != NULL) {
        return createJConfig(env, bd);
    }
    return NULL;
}

void getEDResult(JNIEnv* env, pvl_eye_detection_result *out, jobjectArray jEDResults, int index) {
    jobject edResult = env->GetObjectArrayElement(jEDResults, index);

    getValuePoint(env, &out->left_eye, edResult, STR_ED_left_eye);
    getValuePoint(env, &out->right_eye, edResult, STR_ED_right_eye);
    out->confidence = getValueInt(env, edResult, STR_ED_confidence);
}

jobject createJConfig(JNIEnv *env, pvl_blink_detection *detection)
{
    jclass cls = env->FindClass(CLASS_BLINK_DETECTION_CONFIG);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(" SIG_PVL_VERSION "II)V");
    return env->NewObject(cls, constructor,
                            createJVersion(env, &detection->version),
                            detection->default_threshold,
                            detection->rop_tolerance);
}

jobject createJParam(JNIEnv *env, pvl_blink_detection_parameters *param)
{
    jclass cls = env->FindClass(CLASS_BLINK_DETECTION_PARAM);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(I)V");
    return env->NewObject(cls, constructor,
                            param->threshold);
}

void getParam(JNIEnv *env, pvl_blink_detection_parameters *param, jobject jParam)
{
    param->threshold = getValueInt(env, jParam, "threshold");
}

jobjectArray createJResult(JNIEnv* env, pvl_blink_detection_result* results, int num)
{
    jclass cls = env->FindClass(CLASS_BLINK_DETECTION_INFO);
    jobjectArray retArray = (jobjectArray)env->NewObjectArray(num, cls, NULL);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(IIII)V");

    for (int i = 0; i < num; i++) {
        pvl_blink_detection_result* result = &results[i];
        jobject jResult = env->NewObject(cls, constructor,
                result->left_score,
                result->left_state,
                result->right_score,
                result->right_state);
        env->SetObjectArrayElement(retArray, i, jResult);
    }

    return retArray;
}

static JNINativeMethod gMethods[] = {
    { "create",
      "()J",
      (void*)BlinkDetection_create },

    { "destroy",
      "(J)V",
      (void*)BlinkDetection_destroy },

    { "runInImage",
      "(J"SIG_IAFRAME "["SIG_EYE_DETECTION_INFO")["SIG_BLINK_DETECTION_INFO,
      (void*)BlinkDetection_runInImage },

    { "setParam",
      "(J"SIG_BLINK_DETECTION_PARAM")V",
      (void*)BlinkDetection_setParam },

    { "getParam",
      "(J)"SIG_BLINK_DETECTION_PARAM,
      (void*)BlinkDetection_getParam },

    { "getConfig",
      "(J)"SIG_BLINK_DETECTION_CONFIG,
      (void*)BlinkDetection_getConfig },
};

int register_jni_BlinkDetection(JNIEnv *env)
{
    return jniRegisterNativeMethods(env, CLASS_BLINK_DETECTION, gMethods,
        sizeof(gMethods)/sizeof(JNINativeMethod));
}

