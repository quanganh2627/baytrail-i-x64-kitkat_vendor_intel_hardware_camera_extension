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
    return iaFrame;
}

void print(pvl_image *img) {
    LOGE("data(%08x, %d) size(%dx%d) format(%d)", (uint)img->data, img->size, img->width, img->height, img->format);
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
    return jRect;
}

jobject createJPoint(JNIEnv* env, pvl_point* point) {
    jclass cls = env->FindClass(CLASS_POINT);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(II)V");

    jobject jPoint = env->NewObject(cls, constructor, point->x, point->y);
    return jPoint;
}

jobject createJVersion(JNIEnv* env, const pvl_version* version)
{
    jclass cls = env->FindClass(CLASS_PVL_VERSION);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(III" SIG_STRING ")V");

    return env->NewObject(cls, constructor,
                          version->major,
                          version->minor,
                          version->patch,
                          env->NewStringUTF(version->description));
}

void getValueRect(JNIEnv* env, pvl_rect* out, jobject obj, const char* field_name) {
    jobject rectObj = getValueObject(env, obj, field_name, CLASS_RECT);
    out->left = getValueInt(env, rectObj, "left");
    out->top = getValueInt(env, rectObj, "top");
    out->right = getValueInt(env, rectObj, "right");
    out->bottom = getValueInt(env, rectObj, "bottom");
}

void getValuePoint(JNIEnv* env, pvl_point* out, jobject obj, const char* field_name) {
    jobject rectObj = getValueObject(env, obj, field_name, CLASS_POINT);
    out->x = getValueInt(env, rectObj, "x");
    out->y = getValueInt(env, rectObj, "y");
}

