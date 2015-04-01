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
/**
 *  JNI type refer link: http://docs.oracle.com/javase/6/docs/technotes/guides/jni/spec/types.html
 *  JNI reference example link: https://thenewcircle.com/s/post/1292/jni_reference_example
 *  link: https://www3.ntu.edu.sg/home/ehchua/programming/java/JavaNativeInterface.html

 * jni types (signiture)
  jboolean   : Z
  jbyte      : B
  jint       : I
  void       : V
  jlong      : J
  jfloat     : F
  jdouble    : D
  ----jXXArray   : [XX
  jbyteArray : [B
  jintArray  : [I

  jstring : Ljava/lang/String;
  Bitmap : Landroid/graphics/Bitmap;
  Rect : Landroid/graphics/Rect;
  Point : Landroid/graphics/Point;
 **********************************/

#ifndef __JNIUTIL_H__
#define __JNIUTIL_H__

#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>

void traceLog(int prio, const char* tag, const char* function, int line, const char* format, ...);

#define LOGE(...)  traceLog(ANDROID_LOG_ERROR,TAG,__FUNCTION__,__LINE__,__VA_ARGS__)
#define LOGV(...)  traceLog(ANDROID_LOG_VERBOSE,TAG,__FUNCTION__,__LINE__,__VA_ARGS__)
#define LOGI(...)  traceLog(ANDROID_LOG_INFO,TAG,__FUNCTION__,__LINE__,__VA_ARGS__)
#define LOGD(...)  traceLog(ANDROID_LOG_DEBUG,TAG,__FUNCTION__,__LINE__,__VA_ARGS__)

//#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)
//#define LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,TAG,__VA_ARGS__)
//#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__)
//#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)

/*
 * PACKAGE was defined in Android.mk
 * --> LOCAL_CFLAGS := -DPACKAGE="\"com/intel/camera2/extensions\""
 */
#define CLASS_STRING    "java/lang/String"
#define SIG_STRING      "L" CLASS_STRING ";"
#define CLASS_BITMAP    "android/graphics/Bitmap"
#define SIG_BITMAP      "L" CLASS_BITMAP ";"
#define CLASS_RECT      "android/graphics/Rect"
#define SIG_RECT        "L" CLASS_RECT ";"
#define CLASS_POINT     "android/graphics/Point"
#define SIG_POINT       "L" CLASS_POINT ";"

#define CLASS_IAFRAME   PACKAGE "/IaFrame"
#define SIG_IAFRAME     "L" CLASS_IAFRAME ";"


unsigned char* getValueByteArray(JNIEnv* env, jobject obj, const char* field_name);
int getValueInt(JNIEnv* env, jobject obj, const char* field_name);
long getValueLong(JNIEnv* env, jobject obj, const char* field_name);
float getValueFloat(JNIEnv* env, jobject obj, const char* field_name);
bool getValueBoolean(JNIEnv* env, jobject obj, const char* field_name);
jobject getValueObject(JNIEnv* env, jobject obj, const char* field_name, const char* field_type);
void copyValueByteArray(JNIEnv* env, unsigned char* dst_buf, jobject src_obj, const char* field_name);
void copyValueCharArray(JNIEnv* env, jobject obj, const char* field_name, unsigned short* buf);
jbyteArray convertToGray(JNIEnv* env, jobject jBitmap);

const char* jstringToChar(JNIEnv* env, jstring str);
//jstring charToJstring(JNIEnv* env, const char* str);
int jniRegisterNativeMethods(JNIEnv* env, const char* className, const JNINativeMethod* gMethods, int numMethods);

jclass envFindClass(JNIEnv *env, const char* class_name);
jmethodID envGetMethodID(JNIEnv *env, jclass cls, const char* method_name, const char* sig_name);

static void vdebug(const char *fmt, va_list ap)
{
    LOGD(fmt, ap);
}

static void verror(const char *fmt, va_list ap)
{
    LOGE(fmt, ap);
}

static void vinfo(const char *fmt, va_list ap)
{
    LOGI(fmt, ap);
}


#endif  /* __JNIUTIL_H__ */

