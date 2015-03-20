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
#include "pvl_face_detection.h"
#include "pvl_eye_detection.h"

struct EyeDetectionEngine {
    pvl_eye_detection *eyedetection;
    int debug;
};

static jobjectArray createJResult(JNIEnv* env, pvl_eye_detection_result* results, int num);
static jobject createJConfig(JNIEnv *env, pvl_eye_detection *detection);
static void getFDResult(JNIEnv* env, pvl_face_detection_result *out, jobjectArray jFDResults, int index);

static pvl_eye_detection* getEyeDetection(jlong instance)
{
    if (instance != 0) {
        EyeDetectionEngine* ede = (EyeDetectionEngine*)instance;
        return ede->eyedetection;
    } else {
        return NULL;
    }
}

jlong EyeDetection_create(JNIEnv* env, jobject thiz)
{
    pvl_version version = {0, 0, 0, NULL};
    pvl_config config = {{0, 0, 0, NULL}, {pvl_false, pvl_false, pvl_false, pvl_false, NULL, NULL}, {NULL, NULL, NULL, NULL, NULL}};

    EyeDetectionEngine *ede = NULL;
    pvl_eye_detection *ed = NULL;//(pvl_eye_detection*)calloc(1, sizeof(pvl_eye_detection));

    pvl_eye_detection_get_default_config(&config);
    pvl_err ret = pvl_eye_detection_create(&config, &ed);
    LOGV("pvl_eye_detection_create ret(%d)", ret);
    if (ret == pvl_success) {
        ede = (EyeDetectionEngine*)calloc(1, sizeof(EyeDetectionEngine));
        ede->eyedetection = ed;
    }

    return (jlong)ede;
}

void EyeDetection_destroy(JNIEnv* env, jobject thiz, jlong instance) {
    pvl_eye_detection* ed = getEyeDetection(instance);
    if (ed != NULL) {
        pvl_eye_detection_destroy(ed);
        free((void*)instance);
    }
}

jobjectArray EyeDetection_runInImage(JNIEnv* env, jobject thiz, jlong instance, jobject jIaFrame, jobjectArray jFDResults)
{
    jobjectArray jResult = NULL;
    pvl_eye_detection* ed = getEyeDetection(instance);
    if (ed != NULL) {
        pvl_image image;
        mapImage(env, &image, jIaFrame);

        jsize length = env->GetArrayLength(jFDResults);
        pvl_eye_detection_result results[length]; 
        memset(results, 0, sizeof(pvl_eye_detection_result) * length);

        for (int i = 0; i < length; i++) {
            pvl_face_detection_result fdResult;
            getFDResult(env, &fdResult, jFDResults, i);

            pvl_eye_detection_run(ed, &image, &fdResult.rect, fdResult.rip_angle, &results[i]);
        }

        jResult = createJResult(env, results, length);
    }

    return jResult;
}

jobject EyeDetection_getConfig(JNIEnv* env, jobject thiz, jlong instance)
{
    pvl_eye_detection* ed = getEyeDetection(instance);
    if (ed != NULL) {
        return createJConfig(env, ed);
    }
    return NULL;
}

void getFDResult(JNIEnv* env, pvl_face_detection_result *out, jobjectArray jFDResults, int index)
{
    jobject fdResult = env->GetObjectArrayElement(jFDResults, index);
    getValueRect(env, &out->rect, fdResult, "faceRect");
    out->confidence = getValueInt(env, fdResult, "confidence");
    out->rip_angle = getValueInt(env, fdResult, "ripAngle");
    out->rop_angle = getValueInt(env, fdResult, "ropAngle");
    out->tracking_id = getValueInt(env, fdResult, "trackingId");
}

jobjectArray createJResult(JNIEnv* env, pvl_eye_detection_result* results, int num)
{
    jclass cls = env->FindClass(CLASS_EYE_DETECTION_INFO);
    jobjectArray retArray = (jobjectArray)env->NewObjectArray(num, cls, NULL);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(" SIG_POINT SIG_POINT "I)V");

    for (int i = 0; i < num; i++) {
        pvl_eye_detection_result* result = &results[i];
        jobject jResult = env->NewObject(cls, constructor,
                createJPoint(env, &result->left_eye),
                createJPoint(env, &result->right_eye),
                result->confidence);
        env->SetObjectArrayElement(retArray, i, jResult);
    }

    return retArray;
}

jobject createJConfig(JNIEnv *env, pvl_eye_detection *detection)
{
    jclass cls = env->FindClass(CLASS_EYE_DETECTION_CONFIG);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(" SIG_PVL_VERSION "FF)V");
    return env->NewObject(cls, constructor,
                            createJVersion(env, &detection->version),
                            detection->max_face_width_ratio,
                            detection->max_rip_error_tolerance);
}

static JNINativeMethod gMethods[] = {
    { "create",
      "()J",
      (void*)EyeDetection_create },

    { "destroy",
      "(J)V",
      (void*)EyeDetection_destroy },

    { "runInImage",
      "(J"SIG_IAFRAME"["SIG_FACE_DETECTION_INFO")["SIG_EYE_DETECTION_INFO,
      (void*)EyeDetection_runInImage },

    { "getConfig",
      "(J)"SIG_EYE_DETECTION_CONFIG,
      (void*)EyeDetection_getConfig },
};

int register_jni_EyeDetection(JNIEnv *env)
{
    return jniRegisterNativeMethods(env, CLASS_EYE_DETECTION, gMethods,
        sizeof(gMethods)/sizeof(JNINativeMethod));
}

