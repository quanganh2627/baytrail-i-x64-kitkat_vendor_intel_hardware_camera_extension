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
#ifndef __CPIPUJNI_H__
#define __CPIPUJNI_H__

#include "cp_jni_common.h"
#include <jni.h>
#define UNUSED(x) (void)(x)

#ifdef PLATFORM_ACC_SUPPORT
bool CP_ipu_getAvailableAccTarget();
jint CP_ipu_getHdrAccMode();
jint CP_ipu_getUllAccMode();
/*
 * methods for ACC IPU mode, connect to ACC client
 * init IPU ACC work mode
 */
jlong CP_ipu_init(JNIEnv* env, jobject thiz);

/*
 * uninit IPU ACC work mode, disconnet ACC client
 */
void CP_ipu_uninit(JNIEnv* env, jobject thiz, jlong instance);
jint CP_ipu_hdrInit(JNIEnv* env, jobject thiz, jlong instance,
                       jint width, jint height, jobject jBlendOption);
jint CP_ipu_hdrUninit(JNIEnv* env, jobject thiz, jlong instance);
jobject CP_ipu_hdrCompose(JNIEnv* env, jobject thiz, jlong instance,
                          jobjectArray jInputIaFrames, jobject jHdrOption);
jint CP_ipu_ullInit(JNIEnv* env, jobject thiz, jlong instance,
                      jint width, jint height, jobject jBlendOption);
jint CP_ipu_ullUninit(JNIEnv* env, jobject thiz, jlong instance);
jobject CP_ipu_ullCompose(JNIEnv* env, jobject thiz, jlong instance,
                          jobjectArray jInputIaFrames, jobject jUllOption);
#else
bool CP_ipu_getAvailableAccTarget() {return false;}
jint CP_ipu_getHdrAccMode() {return ERROR_MODE;}
jint CP_ipu_getUllAccMode() {return ERROR_MODE;}
jlong CP_ipu_init(JNIEnv* env, jobject thiz) {
      UNUSED(env);
      UNUSED(thiz);
      return -1;
}

void CP_ipu_uninit(JNIEnv* env, jobject thiz, jlong instance) {
      UNUSED(env);
      UNUSED(thiz);
      UNUSED(instance);
      return;
}
jint CP_ipu_hdrInit(JNIEnv* env, jobject thiz, jlong instance,
                       jint width, jint height, jobject jBlendOption)  {
      UNUSED(env);
      UNUSED(thiz);
      UNUSED(instance);
      UNUSED(width);
      UNUSED(height);
      UNUSED(jBlendOption);
      return -1;
}
jint CP_ipu_hdrUninit(JNIEnv* env, jobject thiz, jlong instance)   {
      UNUSED(env);
      UNUSED(thiz);
      UNUSED(instance);
      return -1;
}
jobject CP_ipu_hdrCompose(JNIEnv* env, jobject thiz, jlong instance,
                          jobjectArray jInputIaFrames, jobject jHdrOption)   {
      UNUSED(env);
      UNUSED(thiz);
      UNUSED(instance);
      UNUSED(jInputIaFrames);
      UNUSED(jHdrOption);
      return NULL;
};
jint CP_ipu_ullInit(JNIEnv* env, jobject thiz, jlong instance,
                      jint width, jint height, jobject jBlendOption)   {
      UNUSED(env);
      UNUSED(thiz);
      UNUSED(instance);
      UNUSED(width);
      UNUSED(height);
      UNUSED(jBlendOption);
      return -1;
}
jint CP_ipu_ullUninit(JNIEnv* env, jobject thiz, jlong instance)   {
      UNUSED(env);
      UNUSED(thiz);
      UNUSED(instance);
      return -1;
}
jobject CP_ipu_ullCompose(JNIEnv* env, jobject thiz, jlong instance,
                          jobjectArray jInputIaFrames, jobject jUllOption)    {
      UNUSED(env);
      UNUSED(thiz);
      UNUSED(instance);
      UNUSED(jInputIaFrames);
      UNUSED(jUllOption);
      return NULL;
};

#endif

#endif  /* __CPIPUJNI_H__ */

