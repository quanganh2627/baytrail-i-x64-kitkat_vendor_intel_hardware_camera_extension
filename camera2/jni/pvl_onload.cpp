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
#include <jni.h>
#include <stdio.h>
#include "PvlUtil.h"

#define REG_METHOD(_method_)    extern int _method_(JNIEnv *env);\
                                _method_(env);

jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv *env = NULL;

    if(vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        LOGE("JNI GetEnv Error");
        return JNI_ERR;
    }

    LOGD("start load...");

    REG_METHOD(register_jni_FaceDetection);
    REG_METHOD(register_jni_EyeDetection);
    REG_METHOD(register_jni_SmileDetection);
    REG_METHOD(register_jni_BlinkDetection);
    REG_METHOD(register_jni_FaceRecognition);
    REG_METHOD(register_jni_FaceRecognitionWithDb);

    return JNI_VERSION_1_6;
}


void JNI_OnUnload(JavaVM *vm, void *reserved)
{
    LOGD("Labrary Unloaded...");
}

