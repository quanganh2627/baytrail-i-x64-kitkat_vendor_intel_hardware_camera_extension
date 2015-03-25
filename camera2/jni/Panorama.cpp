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
#include "PvlUtil.h"
#include "pvl_panorama.h"
#include "Panorama.h"

struct PanoramaEngine {
    pvl_panorama *panorama;
    int debug;
};

static jobject createJConfig(JNIEnv *env, pvl_panorama *panorama);
static jobject createJParam(JNIEnv *env, pvl_panorama_parameters *param);
static jobject createJPanoramaVersion(JNIEnv* env, const pvl_version* version);
static void getParam(JNIEnv *env, pvl_panorama_parameters *param, jobject jParam);

static pvl_panorama* getPanorama(jlong instance)
{
    if (instance != 0) {
        PanoramaEngine* pe = (PanoramaEngine*)instance;
        return pe->panorama;
    } else {
        return NULL;
    }
}

static bool isDebug(jlong instance)
{
    if (instance != 0) {
        PanoramaEngine* pe = (PanoramaEngine*)instance;
        return (pe->debug == 1);
    } else {
        return false;
    }
}

jlong Panorama_create(JNIEnv* env, jobject thiz)
{
    pvl_version version = {0, 0, 0, NULL};
    pvl_config config = {{0, 0, 0, NULL}, {pvl_false, pvl_false, pvl_false, pvl_false, NULL, NULL}, {NULL, NULL, NULL, NULL, NULL}};
    PanoramaEngine *pe = NULL;
    pvl_panorama *pano = NULL;//(pvl_panorama*)calloc(1, sizeof(pvl_panorama));

    pvl_panorama_get_default_config(&config);
    pvl_err ret = pvl_panorama_create(&config, &pano);
    LOGV("pvl_panorama_create ret(%d)", ret);
    if (ret == pvl_success) {
        pe = (PanoramaEngine*)calloc(1, sizeof(PanoramaEngine));
        pe->panorama = pano;

        LOGI("calloc pe(0x%08x) pano(0x%08x)", (uint32_t)pe, (uint32_t)pano);
        LOGI("max(%d) min(%d) max(%d), def(%d)", pano->max_supported_num_images, pano->min_overlapping_ratio, pano->max_overlapping_ratio, pano->default_overlapping_ratio);
    }

    return (jlong)pe;
}

void Panorama_destroy(JNIEnv* env, jobject thiz, jlong instance)
{
    pvl_panorama* pano = getPanorama(instance);
    if (pano != NULL) {
        LOGI("calloc instance(0x%08x) pano(0x%08x)", (uint32_t)instance, (uint32_t)pano);
        pvl_panorama_destroy(pano);
        free((void*)instance);
    }
}

void Panorama_reset(JNIEnv* env, jobject thiz, jlong instance)
{
    pvl_panorama* pano = getPanorama(instance);
    if (pano != NULL) {
        pvl_panorama_reset(pano);
    }
}

void Panorama_setParam(JNIEnv* env, jobject thiz, jlong instance, jint direction)
{
    pvl_panorama* pano = getPanorama(instance);
    if (pano != NULL) {
        LOGE("setParam(%d)", direction);
        pvl_panorama_parameters params;// = {0, (pvl_panorama_direction)direction};
        pvl_panorama_get_parameters(pano, &params);
        params.direction = (pvl_panorama_direction)direction;
        pvl_panorama_set_parameters(pano, &params);
    }
}

const char SDCARD_DIR[] = "/sdcard";
void Panorama_stitch(JNIEnv* env, jobject thiz, jlong instance, jobject jIaFrame, jint picNum)
{
    pvl_panorama* pano = getPanorama(instance);
    if (pano != NULL) {
        pvl_image image;
        mapImage(env, &image, jIaFrame);

        //pvl_point offset;
        //pvl_panorama_direction direction;
        //pvl_panorama_detect_frame_to_stitch(pano, &image, &offset, &direction);

        pvl_err err = pvl_panorama_stitch_one_image(pano, &image, NULL);
        LOGV("pvl_panorama_stitch_one_image - err(%d)", err);

        if (isDebug(instance)) {
            char fileName[255];
            sprintf(fileName, "%s/pano_%dx%d_%d.yuv", SDCARD_DIR, image.width, image.height, picNum);
            dump(fileName, &image);
        }
    }
}

jobject Panorama_run(JNIEnv* env, jobject thiz, jlong instance)
{
    jobject finalizedImage = NULL;
    pvl_panorama* pano = getPanorama(instance);
    if (pano != NULL) {
        pvl_image image;
        memset(&image, 0, sizeof(pvl_image));
        pvl_err err = pvl_panorama_run(pano, &image);
        LOGV("pvl_panorama_run - err(%d)", err);
        print(&image);

        if (err == pvl_success) {
            finalizedImage = createIaFrame(env, &image);
        }

        if (isDebug(instance)) {
            char fileName[255];
            sprintf(fileName, "%s/pano_%dx%d_result.yuv", SDCARD_DIR, image.width, image.height);
            dump(fileName, &image);
        }
    }

    return finalizedImage;
}

void Panorama_setDebug(JNIEnv* env, jobject thiz, jlong instance, jint debug)
{
    PanoramaEngine *pe = (PanoramaEngine*)instance;
    if (pe != NULL) {
        pe->debug = debug;
    }
}

void Panorama_setParam2(JNIEnv* env, jobject thiz, jlong instance, jobject jParam)
{
    pvl_panorama* panorama = getPanorama(instance);
    if (panorama != NULL) {
        pvl_panorama_parameters param;
        getParam(env, &param, jParam);
        pvl_panorama_set_parameters(panorama, &param);
    }
}

jobject Panorama_getParam(JNIEnv* env, jobject thiz, jlong instance)
{
    pvl_panorama* panorama = getPanorama(instance);
    if (panorama != NULL) {
        pvl_panorama_parameters param;
        pvl_panorama_get_parameters(panorama, &param);
        return createJParam(env, &param);
    }
    return NULL;
}

jobject Panorama_getConfig(JNIEnv* env, jobject thiz, jlong instance)
{
    pvl_panorama* panorama = getPanorama(instance);
    if (panorama != NULL) {
        return createJConfig(env, panorama);
    }
    return NULL;
}

jobject createJConfig(JNIEnv *env, pvl_panorama *panorama)
{
    jclass cls = envFindClass(env, CLASS_PANORAMA_CONFIG);
    jmethodID constructor = envGetMethodID(env, cls, "<init>", "(" SIG_PANORAMA_VERSION "IIII)V");
    return env->NewObject(cls, constructor,
                            createJPanoramaVersion(env, &panorama->version),
                            panorama->max_supported_num_images,
                            panorama->min_overlapping_ratio,
                            panorama->max_overlapping_ratio,
                            panorama->default_overlapping_ratio);
}

jobject createJParam(JNIEnv *env, pvl_panorama_parameters *param)
{
    jclass cls = envFindClass(env, CLASS_PANORAMA_PARAM);
    jmethodID constructor = envGetMethodID(env, cls, "<init>", "(II)V");
    return env->NewObject(cls, constructor,
                            param->overlapping_ratio,
                            (int)param->direction);
}

jobject createJPanoramaVersion(JNIEnv* env, const pvl_version* version)
{
    jclass cls = envFindClass(env, CLASS_PANORAMA_VERSION);
    jmethodID constructor = envGetMethodID(env, cls, "<init>", "(III" SIG_STRING ")V");

    return env->NewObject(cls, constructor,
                          version->major,
                          version->minor,
                          version->patch,
                          env->NewStringUTF(version->description));
}

void getParam(JNIEnv *env, pvl_panorama_parameters *param, jobject jParam)
{
    param->overlapping_ratio = getValueInt(env, jParam, "overlapping_ratio");
    param->direction = (pvl_panorama_direction)getValueInt(env, jParam, "direction");
}

static JNINativeMethod gMethods[] = {
    { "create",   "()J",                (void*)Panorama_create },
    { "destroy",  "(J)V",               (void*)Panorama_destroy },
    { "reset",    "(J)V",               (void*)Panorama_reset },
    { "setParam", "(JI)V",              (void*)Panorama_setParam },
    { "stitch",   "(J"SIG_IAFRAME"I)V", (void*)Panorama_stitch },
    { "run",      "(J)"SIG_IAFRAME,     (void*)Panorama_run },
    { "setDebug", "(JI)V",              (void*)Panorama_setDebug },

    { "setParam",
      "(J"SIG_PANORAMA_PARAM")V",
      (void*)Panorama_setParam2 },

    { "getParam",
      "(J)"SIG_PANORAMA_PARAM,
      (void*)Panorama_getParam },

    { "getConfig",
      "(J)"SIG_PANORAMA_CONFIG,
      (void*)Panorama_getConfig },
};

int register_jni_Panorama(JNIEnv *env)
{
    return jniRegisterNativeMethods(env, CLASS_PANORAMA, gMethods,
        sizeof(gMethods)/sizeof(JNINativeMethod));
}

#define REG_METHOD(_method_)    extern int _method_(JNIEnv *env);\
                                _method_(env);

jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv *env = NULL;
    int ret;
    int register_count = 0;

    if(vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        LOGE("JNI GetEnv Error");
        return JNI_ERR;
    }

    LOGD("start load...");

    REG_METHOD(register_jni_Panorama);

    return JNI_VERSION_1_6;
}


void JNI_OnUnload(JavaVM *vm, void *reserved)
{
    LOGD("Labrary Unloaded...");
}

