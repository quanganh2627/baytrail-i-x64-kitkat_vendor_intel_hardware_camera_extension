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
#include "pvl_face_recognition.h"

struct FaceRecognitionEngine {
    pvl_face_recognition *facerecognition;
    int debug;
};

static void getEDResult(JNIEnv* env, pvl_eye_detection_result *out, jobjectArray jEDResults, int index);
static jobjectArray createJResult(JNIEnv* env, pvl_face_recognition_result* results, int num, int facedataSize);
static jobjectArray createJResult2(JNIEnv* env, pvl_face_detection_result* fdResults, pvl_eye_detection_result* edResults, pvl_face_recognition_result* frResults, int num, int facedataSize);
static jobject createJConfig(JNIEnv *env, pvl_face_recognition *detection);
static jobject createJParam(JNIEnv *env, pvl_face_recognition_parameters *param);
static void getParam(JNIEnv *env, pvl_face_recognition_parameters *param, jobject jParam);

static pvl_face_recognition* getFaceRecognition(jlong instance)
{
    if (instance != 0) {
        FaceRecognitionEngine* fre = (FaceRecognitionEngine*)instance;
        return fre->facerecognition;
    } else {
        return NULL;
    }
}

jlong FaceRecognition_create(JNIEnv* env, jobject thiz)
{
    pvl_version version = {0, 0, 0, NULL};
    pvl_config config = {{0, 0, 0, NULL}, {pvl_false, pvl_false, pvl_false, pvl_false, NULL, NULL}, {NULL, NULL, NULL, NULL, NULL}};

    FaceRecognitionEngine *fre = NULL;
    pvl_face_recognition *fr = NULL;//(pvl_face_recognition*)calloc(1, sizeof(pvl_face_recognition));

    pvl_face_recognition_get_default_config(&config);
    pvl_err ret = pvl_face_recognition_create(&config, &fr);
    LOGV("pvl_face_recognition_create ret(%d)", ret);
    if (ret == pvl_success) {
        fre = (FaceRecognitionEngine*)calloc(1, sizeof(FaceRecognitionEngine));
        fre->facerecognition = fr;
    }

    return (jlong)fre;
}

void FaceRecognition_destroy(JNIEnv* env, jobject thiz, jlong instance) {
    pvl_face_recognition* fr = getFaceRecognition(instance);
    if (fr != NULL) {
        pvl_face_recognition_destroy(fr);
        free((void*)instance);
    }
}

jobjectArray FaceRecognition_runInImage(JNIEnv* env, jobject thiz, jlong instance, jobject jIaFrame, jobjectArray jEDResults)
{
    jobjectArray jResult = NULL;
    pvl_face_recognition* fr = getFaceRecognition(instance);
    if (fr != NULL) {
        pvl_image image;
        mapImage(env, &image, jIaFrame);

        jsize length = env->GetArrayLength(jEDResults);
        pvl_face_recognition_result results[length];
        memset(results, 0, sizeof(pvl_face_recognition_result) * length);

        int num_faces = length;
        pvl_point left_eyes[length];
        pvl_point right_eyes[length];

        for (int i = 0; i < length; i++) {
            pvl_eye_detection_result edResult;
            getEDResult(env, &edResult, jEDResults, i);
            memcpy(&left_eyes[i], &edResult.left_eye, sizeof(pvl_point));
            memcpy(&right_eyes[i], &edResult.right_eye, sizeof(pvl_point));
        }

        pvl_err ret = pvl_face_recognition_run_in_image(fr, &image, length, left_eyes, right_eyes, results);
        LOGE("pvl_face_recognition_run_in_image ret(%d)", ret);
        if (ret == pvl_success) {
            jResult = createJResult(env, results, length, fr->facedata_size);
        }
    }

    return jResult;
}

#if 0
#include "pvl_face_detection.h"
#include "pvl_eye_detection.h"

jobjectArray FaceRecognition_runInImage2(JNIEnv* env, jobject thiz, jlong instance, jobject jIaFrame)
{
    pvl_face_detection *fd = NULL;
    pvl_eye_detection *ed = NULL;
    int ret;
    int MAX_COUNT = 32;
    pvl_version version = {0, 0, 0, NULL};
    pvl_config config = {{0, 0, 0, NULL}, {pvl_false, pvl_false, pvl_false, pvl_false, NULL, NULL}, {NULL, NULL, NULL, NULL, NULL}};

    //** IaFrame *********************************
    pvl_image image;
    mapImage(env, &image, jIaFrame);


    //** FD **************************************
    pvl_face_detection_get_default_config(&config);
    ret = pvl_face_detection_create(&config, &fd);
    LOGV("pvl_face_detection_create ret(%d)", ret);

    pvl_face_detection_result fdResults[MAX_COUNT];
    memset(fdResults, 0, sizeof(pvl_face_detection_result) * MAX_COUNT);

    ret = pvl_face_detection_run_in_image(fd, &image, fdResults, MAX_COUNT);
    LOGV("pvl_face_detection_run_in_image - ret(%d)", ret);

    int num_faces = ret;


    //** ED **************************************
    ret = pvl_eye_detection_create(&config, &ed);
    LOGV("pvl_eye_detection_create ret(%d)", ret);

    pvl_eye_detection_result edResults[MAX_COUNT]; 
    memset(edResults, 0, sizeof(pvl_eye_detection_result) * MAX_COUNT);

    for (int i = 0; i < num_faces; i++) {
        pvl_eye_detection_run(ed, &image, &fdResults[i].rect, fdResults[i].rip_angle, &edResults[i]);
    }


    //** FR **************************************
    pvl_face_recognition* fr = getFaceRecognition(instance);

    pvl_face_recognition_result frResults[MAX_COUNT];
    memset(frResults, 0, sizeof(pvl_face_recognition_result) * MAX_COUNT);

    pvl_point left_eyes[MAX_COUNT];
    pvl_point right_eyes[MAX_COUNT];

    for (int i = 0; i < num_faces; i++) {
        memcpy(&left_eyes[i], &edResults[i].left_eye, sizeof(pvl_point));
        memcpy(&right_eyes[i], &edResults[i].right_eye, sizeof(pvl_point));
    }

    ret = pvl_face_recognition_run_in_image(fr, &image, num_faces, left_eyes, right_eyes, frResults);
    LOGE("pvl_face_recognition_run_in_image ret(%d)", ret);

    jobjectArray retObjects = createJResult2(env, fdResults, edResults, frResults, num_faces, fr->facedata_size);

    pvl_face_detection_destroy(fd);
    pvl_eye_detection_destroy(ed);

    return retObjects;
}
#endif

void FaceRecognition_registerFace(JNIEnv* env, jobject thiz, jlong instance, jobject jFRResult)
{
    pvl_face_recognition* fr = getFaceRecognition(instance);
    if (fr != NULL) {
    }
}

void FaceRecognition_unregisterFace(JNIEnv* env, jobject thiz, jlong instance, jlong faceId)
{
    pvl_face_recognition* fr = getFaceRecognition(instance);
    if (fr != NULL) {
    }
}

void FaceRecognition_updatePerson(JNIEnv* env, jobject thiz, jlong instance, jlong faceId, jint personId)
{
    pvl_face_recognition* fr = getFaceRecognition(instance);
    if (fr != NULL) {
    }
}

void FaceRecognition_deletePerson(JNIEnv* env, jobject thiz, jlong instance, jint personId)
{
    pvl_face_recognition* fr = getFaceRecognition(instance);
    if (fr != NULL) {
    }
}

jint FaceRecognition_getNumFacesInDatabase(JNIEnv* env, jobject thiz, jlong instance)
{
    pvl_face_recognition* fr = getFaceRecognition(instance);
    if (fr != NULL) {
        return 0;
    } else {
        return 0;
    }
}

void getEDResult(JNIEnv* env, pvl_eye_detection_result *out, jobjectArray jEDResults, int index) {
    jobject edResult = env->GetObjectArrayElement(jEDResults, index);

    getValuePoint(env, &out->left_eye, edResult, "leftEye");
    getValuePoint(env, &out->right_eye, edResult, "rightEye");
    out->confidence = getValueInt(env, edResult, "confidence");
}

jobjectArray createJResult(JNIEnv* env, pvl_face_recognition_result* results, int num, int facedataSize) {
    jclass cls = env->FindClass(CLASS_FACE_RECOGNITION);
    jobjectArray retArray = (jobjectArray)env->NewObjectArray(num, cls, NULL);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(IJIJII[B)V");

    for (int i = 0; i < num; i++) {
        pvl_face_recognition_result* result = &results[i];
        pvl_face_recognition_facedata* facedata = &result->facedata;
        jbyteArray feature = env->NewByteArray(facedataSize);
        env->SetByteArrayRegion(feature, 0, facedataSize, (jbyte*)facedata->data);

        jobject jResult = env->NewObject(cls, constructor,
                results->similarity,
                facedata->face_id,
                facedata->person_id,
                facedata->time_stamp,
                facedata->condition,
                facedata->checksum,
                feature);
        env->SetObjectArrayElement(retArray, i, jResult);
    }

    return retArray;
}

jobjectArray createJResult2(JNIEnv* env, pvl_face_detection_result* fdResults, pvl_eye_detection_result* edResults, pvl_face_recognition_result* frResults, int num, int facedataSize)
{
    jclass cls = env->FindClass(CLASS_FACE_DATA);
    jobjectArray retArray = (jobjectArray)env->NewObjectArray(num, cls, NULL);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "()V");
    jmethodID setFDdataMID = env->GetMethodID(cls, "setFDdata", "(" SIG_RECT "IIII)V");
    jmethodID setEDdataMID = env->GetMethodID(cls, "setEDdata", "(" SIG_POINT SIG_POINT ")V");
    jmethodID setFRdataMID = env->GetMethodID(cls, "setFRdata", "(IJIJII[B)V");
    LOGE("MID: constructor(0x%08x) fd(0x%08x) ed(0x%08x) fr(0x%08x)", (uint32_t)constructor, (uint32_t)setFDdataMID, (uint32_t)setEDdataMID, (uint32_t)setFRdataMID);

    for (int i = 0; i < num; i++) {
        jobject jResult = env->NewObject(cls, constructor);
        LOGE("id(%d) jResult(0x%08x)", i, (uint32_t)jResult);

        //* FD *****
        pvl_face_detection_result* fd = &fdResults[i];
        env->CallVoidMethod(jResult, setFDdataMID,
                createJRect(env, &fd->rect),
                fd->confidence,
                fd->rip_angle,
                fd->rop_angle,
                fd->tracking_id);

        LOGE("id(%d) fd ok", i);

        //* ED *****
        pvl_eye_detection_result* ed = &edResults[i];
        env->CallVoidMethod(jResult, setEDdataMID,
                createJPoint(env, &ed->left_eye),
                createJPoint(env, &ed->right_eye),
                ed->confidence);

        LOGE("id(%d) ed ok", i);

        //* FR *****
        if (false) {
        pvl_face_recognition_result* fr = &frResults[i];
        pvl_face_recognition_facedata* facedata = &fr->facedata;
        jbyteArray feature = env->NewByteArray(facedataSize);
        env->SetByteArrayRegion(feature, 0, facedataSize, (jbyte*)facedata->data);

        LOGE("id(%d) feature ok", i);

        env->CallVoidMethod(jResult, setFRdataMID,
                fr->similarity,
                fr->facedata.face_id,
                fr->facedata.person_id,
                fr->facedata.time_stamp,
                fr->facedata.condition,
                fr->facedata.checksum,
                feature);

        LOGE("id(%d) fr ok", i);
        }

        env->SetObjectArrayElement(retArray, i, jResult);
    }

    return retArray;
}

void FaceRecognition_setParam(JNIEnv* env, jobject thiz, jlong instance, jobject jParam)
{
    pvl_face_recognition* fr = getFaceRecognition(instance);
    if (fr != NULL) {
        pvl_face_recognition_parameters param;
        getParam(env, &param, jParam);
        pvl_face_recognition_set_parameters(fr, &param);
    }
}

jobject FaceRecognition_getParam(JNIEnv* env, jobject thiz, jlong instance)
{
    pvl_face_recognition* fr = getFaceRecognition(instance);
    if (fr != NULL) {
        pvl_face_recognition_parameters param;
        pvl_face_recognition_get_parameters(fr, &param);
        return createJParam(env, &param);
    }
    return NULL;
}

jobject FaceRecognition_getConfig(JNIEnv* env, jobject thiz, jlong instance)
{
    pvl_face_recognition* fr = getFaceRecognition(instance);
    if (fr != NULL) {
        return createJConfig(env, fr);
    }
    return NULL;
}

jobject createJConfig(JNIEnv *env, pvl_face_recognition *fr)
{
    jclass cls = env->FindClass(CLASS_FACE_RECOGNITION_CONFIG);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(" SIG_PVL_VERSION "IIIII)V");
    return env->NewObject(cls, constructor,
                            createJVersion(env, &fr->version),
                            fr->max_supported_faces_in_preview,
                            fr->max_faces_in_database,
                            fr->max_persons_in_database,
                            fr->max_faces_per_person,
                            fr->facedata_size);
}

jobject createJParam(JNIEnv *env, pvl_face_recognition_parameters *param)
{
    jclass cls = env->FindClass(CLASS_FACE_RECOGNITION_PARAM);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(I)V");
    return env->NewObject(cls, constructor,
                            param->max_faces_in_preview);
}

void getParam(JNIEnv *env, pvl_face_recognition_parameters *param, jobject jParam)
{
    param->max_faces_in_preview = getValueInt(env, jParam, "max_faces_in_preview");
}

static JNINativeMethod gMethods[] = {
    { "create",
      "()J",
      (void*)FaceRecognition_create },

    { "destroy",
      "(J)V",
      (void*)FaceRecognition_destroy },

    { "runInImage",
      "(J"SIG_IAFRAME"["SIG_EYE_DETECTION_INFO")["SIG_FACE_RECOGNITION_INFO,
      (void*)FaceRecognition_runInImage },

#if 0
    { "runInImage2",
      "(J"SIG_IAFRAME")["SIG_FACE_DATA,
      (void*)FaceRecognition_runInImage2 },
#endif

    { "registerFace",
      "(J"SIG_FACE_RECOGNITION_INFO")V",
      (void*)FaceRecognition_registerFace },

    { "unregisterFace",
      "(JJ)V",
      (void*)FaceRecognition_unregisterFace },

    { "updatePerson",
      "(JJI)V",
      (void*)FaceRecognition_updatePerson },

    { "deletePerson",
      "(JI)V",
      (void*)FaceRecognition_deletePerson },

    { "getNumFacesInDatabase",
      "(J)I",
      (void*)FaceRecognition_getNumFacesInDatabase },

    { "setParam",
      "(J"SIG_FACE_RECOGNITION_PARAM")V",
      (void*)FaceRecognition_setParam },

    { "getParam",
      "(J)"SIG_FACE_RECOGNITION_PARAM,
      (void*)FaceRecognition_getParam },

    { "getConfig",
      "(J)"SIG_FACE_RECOGNITION_CONFIG,
      (void*)FaceRecognition_getConfig },
};

int register_jni_FaceRecognition(JNIEnv *env)
{
    return jniRegisterNativeMethods(env, CLASS_FACE_RECOGNITION, gMethods,
        sizeof(gMethods)/sizeof(JNINativeMethod));
}

