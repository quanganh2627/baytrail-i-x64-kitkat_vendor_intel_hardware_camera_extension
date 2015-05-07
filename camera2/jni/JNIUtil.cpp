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
#include "JNIUtil.h"
#include <stdio.h>
#include <android/bitmap.h>
#include <stdarg.h>

#define LOG_BUF_SIZE 256

void traceLog(int prio, const char* tag, const char* function, int line, const char* format, ...)
{
    va_list ap;
    char buf[LOG_BUF_SIZE];

    va_start(ap, format);
    vsprintf(buf, format, ap);
    va_end(ap);

    __android_log_print(prio, tag, "[%s:%d] %s", function, line, buf);
}

unsigned char* getValueByteArray(JNIEnv* env, jobject obj, const char* field_name) {
    jclass cls;
    jfieldID fid;
    jbyteArray jba;
    unsigned char* array = NULL;

    cls = env->GetObjectClass(obj);
    if (cls == NULL) {
        LOGE("Failed at 'GetObjectClass' ");
        env->ExceptionClear();
        return NULL;
    }

    fid = env->GetFieldID(cls, field_name, "[B");
    if(fid == NULL) {
        env->DeleteLocalRef(cls);
        LOGE("Failed in 'GetFieldID' %s ", field_name);
        env->ExceptionClear();
        return NULL;
    }

    jba = (jbyteArray)env->GetObjectField(obj, fid);
    if(jba == NULL) {
        env->DeleteLocalRef(cls);
        LOGE("imageData array jba == NULL.");
        env->ExceptionClear();
        return NULL;
    }
    array = (unsigned char*)env->GetByteArrayElements(jba, 0);
    env->ReleaseByteArrayElements(jba, (jbyte *)array, 0);
    env->DeleteLocalRef(cls);

    return array;
}

int getValueInt(JNIEnv* env, jobject obj, const char* field_name) {
    jclass cls;
    jfieldID fid;

    cls = env->GetObjectClass(obj);
    if (cls == NULL) {
        LOGE("Failed at 'GetObjectClass' ");
        env->ExceptionClear();
        return -1;
    }

    fid = env->GetFieldID(cls, field_name, "I");
    if (fid == NULL) {
        env->DeleteLocalRef(cls);
        LOGE("Failed in 'GetFieldID' %s", field_name);
        env->ExceptionClear();
        return -1;
    }

    int ret = env->GetIntField(obj, fid);
    env->DeleteLocalRef(cls);
    return ret;
}

long getValueLong(JNIEnv* env, jobject obj, const char* field_name) {
    jclass cls;
    jfieldID fid;

    cls = env->GetObjectClass(obj);
    if (cls == NULL) {
        LOGE("Failed at 'GetObjectClass' ");
        env->ExceptionClear();
        return -1;
    }

    fid = env->GetFieldID(cls, field_name, "J");
    if (fid == NULL) {
        env->DeleteLocalRef(cls);
        LOGE("Failed in 'GetFieldID' %s", field_name);
        env->ExceptionClear();
        return -1;
    }

    long ret = env->GetLongField(obj, fid);
    env->DeleteLocalRef(cls);
    return ret;
}

float getValueFloat(JNIEnv* env, jobject obj, const char* field_name) {
    jclass cls;
    jfieldID fid;

    cls = env->GetObjectClass(obj);
    if (cls == NULL) {
        LOGE("Failed at 'GetObjectClass' ");
        env->ExceptionClear();
        return -1.0f;
    }

    fid = env->GetFieldID(cls, field_name, "F");
    if (fid == NULL) {
        env->DeleteLocalRef(cls);
        LOGE("Failed in 'GetFieldID' %s", field_name);
        env->ExceptionClear();
        return -1.0f;
    }

    float ret = (float) env->GetFloatField(obj, fid);
    env->DeleteLocalRef(cls);
    return ret;
}

bool getValueBoolean(JNIEnv* env, jobject obj, const char* field_name) {
    jclass cls;
    jfieldID fid;

    cls = env->GetObjectClass(obj);
    if (cls == NULL) {
        LOGE("Failed at 'GetObjectClass' ");
        env->ExceptionClear();
        return false;
    }

    fid = env->GetFieldID(cls, field_name, "Z");
    if (fid == NULL) {
        env->DeleteLocalRef(cls);
        LOGE("Failed in 'GetFieldID' %s", field_name);
        env->ExceptionClear();
        return false;
    }

    bool ret = (bool) env->GetBooleanField(obj, fid);
    env->DeleteLocalRef(cls);
    return ret;
}

jobject getValueObject(JNIEnv* env, jobject obj, const char* field_name, const char* field_type) {
    jclass cls;
    jfieldID fid;

    cls = env->GetObjectClass(obj);
    if (cls == NULL) {
        LOGE("Failed at 'GetObjectClass' ");
        env->ExceptionClear();
        return NULL;
    }

    fid = env->GetFieldID(cls, field_name, field_type);
    if (fid == NULL) {
        env->DeleteLocalRef(cls);
        LOGE("Failed in 'GetFieldID' %s", field_name);
        env->ExceptionClear();
        return NULL;
    }

    jobject ret = env->GetObjectField(obj, fid);
    env->DeleteLocalRef(cls);
    return ret;
}

void copyValueByteArray(JNIEnv* env, unsigned char* dst_buf, jobject src_obj, const char* field_name) {
    if (dst_buf == NULL) {
        LOGE("dst_buf is null");
        return;
    }

    jclass cls;
    jfieldID fid;
    jbyteArray jba;
    unsigned char* array = NULL;
    int len = 0;
    cls = env->GetObjectClass(src_obj);
    if (cls == NULL) {
        LOGE("Failed at 'GetObjectClass' ");
        env->ExceptionClear();
        return;
    }

    fid = env->GetFieldID(cls, field_name, "[B");
    if(fid == NULL) {
        env->DeleteLocalRef(cls);
        LOGE("Failed in 'GetFieldID' %s ", field_name);
        env->ExceptionClear();
        return;
    }

    jba = (jbyteArray)env->GetObjectField(src_obj, fid);
    if(jba == NULL) {
        env->DeleteLocalRef(cls);
        LOGE("%s's byte array is NULL.", field_name);
        env->ExceptionClear();
        return;
    }
    len = env->GetArrayLength(jba);
    LOGD("%s array length = %d", field_name, len);
    array = (unsigned char*)env->GetByteArrayElements(jba, 0);
    if (array != NULL) {
        memcpy(dst_buf, array, len);
        env->ReleaseByteArrayElements(jba, (jbyte *)array, JNI_ABORT);
    }
    env->DeleteLocalRef(cls);
}

void copyValueCharArray(JNIEnv* env, jobject obj, const char* field_name, unsigned short* buf) {
    if (buf == NULL) {
        LOGE("dst_buf is null");
        return;
    }

    jclass cls;
    jfieldID fid;
    jcharArray jca;
    unsigned short* array = NULL;
    int len = 0;

    cls = env->GetObjectClass(obj);
    if (cls == NULL) {
        LOGE("Failed at 'GetObjectClass' ");
        env->ExceptionClear();
        return;
    }

    fid = env->GetFieldID(cls, field_name, "[C");
    if(fid == NULL) {
        env->DeleteLocalRef(cls);
        LOGE("Failed in 'GetFieldID' %s ", field_name);
        env->ExceptionClear();
        return;
    }

    jca = (jcharArray)env->GetObjectField(obj, fid);
    if(jca == NULL) {
        env->DeleteLocalRef(cls);
        LOGE("%s's byte array is NULL.", field_name);
        env->ExceptionClear();
        return;
    }
    len = env->GetArrayLength(jca);
    array = (unsigned short*)env->GetCharArrayElements(jca, 0);
    if (array != NULL) {
        memcpy(buf, array, len);
        env->ReleaseCharArrayElements(jca, (jchar *)array, JNI_ABORT);
    }
    env->DeleteLocalRef(cls);
}

#define RGB2GRAY(r, g, b) ((unsigned char)(((r)*77+(g)*151+(b)*28)>>8))

jbyteArray convertToGray(JNIEnv* env, jobject jBitmap)
{
    AndroidBitmapInfo info;
    if (AndroidBitmap_getInfo(env, jBitmap, &info) < 0) {
        LOGE("AndroidBitmap_getInfo failed.");
        return NULL;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("format is not RGBA_8888. format(%d)", info.format);
        return NULL;
    }

    void* bitmapPixels;
    if (AndroidBitmap_lockPixels(env, jBitmap, &bitmapPixels) < 0) {
        LOGE("AndroidBitmap_lockPixels failed.");
        return NULL;
    }

    int stride = info.stride / 4;
    int height = info.height;
    int size = stride * height;
    uint32_t* src = (uint32_t*) bitmapPixels;
    unsigned char* dst = (unsigned char*) malloc(size);
    if (dst == NULL) {
        LOGE("No enough memory.");
        return NULL;
    }

    //LOGV("stride(%d) height(%d) size(%d)", stride, height, size);
    //LOGV("src(%p) dst(%p)", src, dst);

    unsigned char *r, *g, *b, *out;
    for (int j = 0; j < height; j++) {
        r = (unsigned char*)&src[j * stride];
        g = &r[1];
        b = &r[2];
        out = &dst[j * stride];

        for (int i = 0; i < stride; i++) {
            *out = RGB2GRAY(*r, *g, *b);
            r+=4;
            g+=4;
            b+=4;
            out++;
        }
    }

    jbyteArray byteArray = env->NewByteArray(size);
    env->SetByteArrayRegion(byteArray, 0, size, (jbyte*)dst);

    return byteArray;
}

/**
  *
  *@brief Convert java String to char*
  *       : must free memory
  *
  *@author    Olaworks. Inc
  *@date      2009-12-01
  *
  *@param env    [JNIEnv]
  *@param str    [jstring] Java String
  *
  *@return    Ola_Return value in "OlaTypes.h"
  */
/* Not Using */
const char* jstringToChar(JNIEnv* env, jstring str)
{
    if (str != NULL) {
        return env->GetStringUTFChars(str, 0);
    } else {
        return NULL;
    }
}
//
//jstring charToJstring(JNIEnv* env, const char* str)
//{
//    return env->NewStringUTF(str);
//}

int jniRegisterNativeMethods
    (JNIEnv* env, const char* className, const JNINativeMethod* gMethods, int numMethods)
{
    int ret = JNI_OK;
    int numMethodsWrong = 0;
    LOGD("register native methods...");
    LOGD("\tclass: %s, num: %d", className, numMethods);

    if (numMethods == 0) return 0;
    else if (numMethods < 0) return JNI_ERR;

    jclass clazz = env->FindClass(className);
    if (clazz == NULL) {
        LOGE("FindClass Error. %s", className);
        env->ExceptionClear();
        return JNI_ERR;
    }

    char tempStr[256];
    for (int index = 0; index < numMethods; index++) {
        ret = env->RegisterNatives(clazz, &gMethods[index], 1);

        sprintf(tempStr, "\tIndex[%3d] Ret[%3d] method[ %s ] signiture[ %s ]", index, ret, gMethods[index].name, gMethods[index].signature);
        if (ret == JNI_OK) {
            LOGD(tempStr);
        } else {
            LOGE(tempStr);
            numMethodsWrong++;
            env->ExceptionClear();
        }
    }
    env->DeleteLocalRef(clazz);

    LOGD("\tRegisterNatives Error. (%d/%d)\n", numMethodsWrong, numMethods);
    LOGD(" ");

    if (numMethodsWrong == 0)
        return 0;
    else
        return JNI_ERR;
}

jclass envFindClass(JNIEnv *env, const char* class_name)
{
    jclass cls = env->FindClass(class_name);
    if (cls == NULL) {
        LOGE("[%s] was not found.", class_name);
        env->ExceptionClear();
    }
    return cls;
}

jmethodID envGetMethodID(JNIEnv *env, jclass cls, const char* method_name, const char* sig_name)
{
    jmethodID methodID = env->GetMethodID(cls, method_name, sig_name);
    if (methodID == NULL) {
        LOGE("class[%p] method[%s] sig[%s]", cls, method_name, sig_name);
        env->ExceptionClear();
    }
    return methodID;
}

