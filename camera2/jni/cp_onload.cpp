#include <jni.h>
#include <stdio.h>
#include "CpUtil.h"

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

    REG_METHOD(register_jni_CP);

    return JNI_VERSION_1_6;
}


void JNI_OnUnload(JavaVM *vm, void *reserved)
{
    LOGD("Labrary Unloaded...");
}

