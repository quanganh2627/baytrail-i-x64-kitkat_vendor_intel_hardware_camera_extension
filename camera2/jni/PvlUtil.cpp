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
#include <stdio.h>

void mapImage(JNIEnv* env, pvl_image* dst, jobject src)
{
	dst->format = pvl_image_format_nv12;
	dst->data = getValueByteArray(env, src, "imageData");
	dst->width = getValueInt(env, src, "width");
	dst->height = getValueInt(env, src, "height");
	dst->stride = getValueInt(env, src, "stride");
	dst->size = getValueInt(env, src, "size");
	dst->rotation = getValueInt(env, src, "degree");
}

jobject createIaFrame(JNIEnv* env, pvl_image* src) {
    jclass cls = env->FindClass(CLASS_IAFRAME);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "([BIIIII)V");

    jbyteArray imageData = env->NewByteArray(src->size);
    env->SetByteArrayRegion(imageData, 0, src->size, (jbyte*)src->data);
    jobject iaFrame = env->NewObject(cls, constructor, imageData, src->stride, src->width, src->height, 17, src->rotation);
    env->DeleteLocalRef(cls);
    return iaFrame;
}

void print(pvl_image *img) {
    LOGE("data(%p, %d) size(%dx%d) format(%d)", img->data, img->size, img->width, img->height, img->format);
}

void dump(char* fileName, pvl_image *image) {
    print(image);

    unsigned char* imageData = image->data;
    int size = image->size;

    FILE *p = NULL;
    p = fopen(fileName, "wb");
    if (p != NULL) {
        fwrite(imageData, size, 1, p);
        fclose(p);
    }
}

jobject createJRect(JNIEnv* env, pvl_rect* rect) {
    jclass cls = env->FindClass(CLASS_RECT);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(IIII)V");

    jobject jRect = env->NewObject(cls, constructor, rect->left, rect->top, rect->right, rect->bottom);
    env->DeleteLocalRef(cls);
    return jRect;
}

jobject createJPoint(JNIEnv* env, pvl_point* point) {
    jclass cls = env->FindClass(CLASS_POINT);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(II)V");

    jobject jPoint = env->NewObject(cls, constructor, point->x, point->y);
    env->DeleteLocalRef(cls);
    return jPoint;
}

jobject createJVersion(JNIEnv* env, const pvl_version* version)
{
    jclass cls = env->FindClass(CLASS_PVL_VERSION);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(III" SIG_STRING ")V");

    jobject ret = env->NewObject(cls, constructor,
                          version->major,
                          version->minor,
                          version->patch,
                          env->NewStringUTF(version->description));
    env->DeleteLocalRef(cls);
    return ret;
}

void getValueRect(JNIEnv* env, pvl_rect* out, jobject obj, const char* field_name) {
    jobject rectObj = getValueObject(env, obj, field_name, CLASS_RECT);
    out->left = getValueInt(env, rectObj, "left");
    out->top = getValueInt(env, rectObj, "top");
    out->right = getValueInt(env, rectObj, "right");
    out->bottom = getValueInt(env, rectObj, "bottom");

    env->DeleteLocalRef(rectObj);
}

void getValuePoint(JNIEnv* env, pvl_point* out, jobject obj, const char* field_name) {
    jobject pointObj = getValueObject(env, obj, field_name, CLASS_POINT);
    out->x = getValueInt(env, pointObj, "x");
    out->y = getValueInt(env, pointObj, "y");

    env->DeleteLocalRef(pointObj);
}

