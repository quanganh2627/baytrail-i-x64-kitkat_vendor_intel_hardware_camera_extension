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

struct FaceDetectionEngine {
    pvl_face_detection *facedetection;
    int debug;
};

static jobjectArray createJResult(JNIEnv *env, pvl_face_detection_result *results, int num);
static jobject createJConfig(JNIEnv *env, pvl_face_detection *detection);
static jobject createJParam(JNIEnv *env, pvl_face_detection_parameters *param);
static void getParam(JNIEnv *env, pvl_face_detection_parameters *param, jobject jParam);

static pvl_face_detection* getFaceDetection(jlong instance)
{
    if (instance != 0) {
        FaceDetectionEngine* fde = (FaceDetectionEngine*)instance;
        return fde->facedetection;
    } else {
        return NULL;
    }
}

jlong FaceDetection_create(JNIEnv* env, jobject thiz)
{
    pvl_version version = {0, 0, 0, NULL};
    pvl_config config = {{0, 0, 0, NULL}, {pvl_false, pvl_false, pvl_false, pvl_false, NULL, NULL}, {NULL, NULL, NULL, NULL, NULL}};

    FaceDetectionEngine *fde = NULL;
    pvl_face_detection *fd = NULL;//(pvl_face_detection*)calloc(1, sizeof(pvl_face_detection));

    pvl_face_detection_get_default_config(&config);
    pvl_err ret = pvl_face_detection_create(&config, &fd);
    LOGV("pvl_face_detection_create ret(%d)", ret);
    if (ret == pvl_success) {
        fde = (FaceDetectionEngine*)calloc(1, sizeof(FaceDetectionEngine));
        fde->facedetection = fd;
        LOGE("calloc fd(0x%08x) fde(0x%08x)", (uint32_t)fd, (uint32_t)fde);
    }

    return (jlong)fde;
}

void FaceDetection_destroy(JNIEnv* env, jobject thiz, jlong instance)
{
    pvl_face_detection* fd = getFaceDetection(instance);
    if (fd != NULL) {
        pvl_face_detection_destroy(fd);
        LOGE("free fd(0x%08x) fde(0x%08x)", (uint32_t)fd, (uint32_t)instance);
        free((void*)instance);
    }
}

jobjectArray FaceDetection_runInImage(JNIEnv* env, jobject thiz, jlong instance, jobject jIaFrame)
{
    jobjectArray jResult = NULL;
    pvl_face_detection* fd = getFaceDetection(instance);
    if (fd != NULL) {
        pvl_image image;
        mapImage(env, &image, jIaFrame);

        int MAX_COUNT = fd->max_supported_num_faces;
        pvl_face_detection_result results[MAX_COUNT];
        memset(results, 0, sizeof(pvl_face_detection_result) * MAX_COUNT);

        int ret = pvl_face_detection_run_in_image(fd, &image, results, MAX_COUNT);
        LOGV("pvl_face_detection_run_in_image - ret(%d)", ret);

        if (ret > 0) {
            jResult = createJResult(env, results, ret);
        }
    }

    return jResult;
}

jbyteArray FaceDetection_convertToGray(JNIEnv* env, jobject thiz, jobject jBitmap)
{
    return convertToGray(env, jBitmap);
}

void FaceDetection_setParam(JNIEnv* env, jobject thiz, jlong instance, jobject jParam)
{
    pvl_face_detection* fd = getFaceDetection(instance);
    if (fd != NULL) {
        pvl_face_detection_parameters param;
        getParam(env, &param, jParam);
        pvl_face_detection_set_parameters(fd, &param);
    }
}

jobject FaceDetection_getParam(JNIEnv* env, jobject thiz, jlong instance)
{
    pvl_face_detection* fd = getFaceDetection(instance);
    if (fd != NULL) {
        pvl_face_detection_parameters param;
        pvl_face_detection_get_parameters(fd, &param);
        return createJParam(env, &param);
    }
    return NULL;
}

jobject FaceDetection_getConfig(JNIEnv* env, jobject thiz, jlong instance)
{
    pvl_face_detection* fd = getFaceDetection(instance);
    if (fd != NULL) {
        return createJConfig(env, fd);
    }
    return NULL;
}

jobjectArray createJResult(JNIEnv* env, pvl_face_detection_result* results, int num)
{
    jclass cls = env->FindClass(CLASS_FACE_DETECTION_INFO);
    jobjectArray retArray = (jobjectArray)env->NewObjectArray(num, cls, NULL);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(" SIG_RECT "IIII)V");

    for (int i = 0; i < num; i++) {
        pvl_face_detection_result* result = &results[i];
        jobject jResult = env->NewObject(cls, constructor, createJRect(env, &result->rect), result->confidence, result->rip_angle, result->rop_angle, result->tracking_id);
        env->SetObjectArrayElement(retArray, i, jResult);
    }

    return retArray;
}

jobject createJConfig(JNIEnv *env, pvl_face_detection *detection)
{
    jclass cls = env->FindClass(CLASS_FACE_DETECTION_CONFIG);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(" SIG_PVL_VERSION "IIIIII)V");
    return env->NewObject(cls, constructor,
                            createJVersion(env, &detection->version),
                            detection->max_supported_num_faces,
                            detection->min_face_size,
                            detection->rip_range_max,
                            detection->rip_range_resolution,
                            detection->rop_range_max,
                            detection->rop_range_resolution);
}

jobject createJParam(JNIEnv *env, pvl_face_detection_parameters *param)
{
    jclass cls = env->FindClass(CLASS_FACE_DETECTION_PARAM);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(IFIII)V");
    return env->NewObject(cls, constructor,
                            param->max_num_faces,
                            param->min_face_ratio,
                            param->rip_range,
                            param->rop_range,
                            param->num_rollover_frames);
}

void getParam(JNIEnv *env, pvl_face_detection_parameters *param, jobject jParam)
{
    param->max_num_faces = getValueInt(env, jParam, "max_num_faces");
    param->min_face_ratio = getValueFloat(env, jParam, "min_face_ratio");
    param->rip_range = getValueInt(env, jParam, "rip_range");
    param->rop_range = getValueInt(env, jParam, "rop_range");
    param->num_rollover_frames = getValueInt(env, jParam, "num_rollover_frames");
}

static JNINativeMethod gMethods[] = {
    { "create",
      "()J",
      (void*)FaceDetection_create },

    { "destroy",
      "(J)V",
      (void*)FaceDetection_destroy },

    { "runInImage",
      "(J"SIG_IAFRAME")["SIG_FACE_DETECTION_INFO,
      (void*)FaceDetection_runInImage },

    { "convertToGray",
      "("SIG_BITMAP")[B",
      (void*)FaceDetection_convertToGray },

    { "setParam",
      "(J"SIG_FACE_DETECTION_PARAM")V",
      (void*)FaceDetection_setParam },

    { "getParam",
      "(J)"SIG_FACE_DETECTION_PARAM,
      (void*)FaceDetection_getParam },

    { "getConfig",
      "(J)"SIG_FACE_DETECTION_CONFIG,
      (void*)FaceDetection_getConfig },
};

int register_jni_FaceDetection(JNIEnv *env)
{
    return jniRegisterNativeMethods(env, CLASS_FACE_DETECTION, gMethods,
        sizeof(gMethods)/sizeof(JNINativeMethod));
}

