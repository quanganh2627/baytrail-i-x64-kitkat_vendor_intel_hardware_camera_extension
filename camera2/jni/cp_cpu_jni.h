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
#ifndef __CPCPUJNI_H__
#define __CPCPUJNI_H__

#include "cp_jni_common.h"
#include "CpUtil.h"

jlong CP_cpu_init(JNIEnv* env, jobject thiz);
void CP_cpu_uninit(JNIEnv* env, jobject thiz, jlong instance);
jint CP_cpu_hdrInit(JNIEnv* env, jobject thiz, jlong instance,
                      jint width, jint height, jobject jBlendOption);
jint CP_cpu_hdrUninit(JNIEnv* env, jobject thiz, jlong instance);
jobject CP_cpu_hdrCompose(JNIEnv* env, jobject thiz, jlong instance,
                         jobjectArray jInputIaFrames, jobject jHdrOption);
jint CP_cpu_ullInit(JNIEnv* env, jobject thiz, jlong instance,
                   jint width, jint height, jobject jBlendOption);
jint CP_cpu_ullUninit(JNIEnv* env, jobject thiz, jlong instance);
jobject CP_cpu_ullCompose(JNIEnv* env, jobject thiz, jlong instance,
                               jobjectArray jInputIaFrames, jobject jUllOption);
jobject CP_cpu_debugFrameConvert(JNIEnv* env, jobject thiz, jobject jInIaFrame);
#endif  /* __CPCPUJNI_H__ */


