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
#include "CpIpuUtil.h"
#include "CpMemUtil.h"
#include "JNIUtil.h"
#include "CpUtil.h"


void create_ia_frame(acc_ia_frame* pFrame, ia_frame_format format, int stride, int width, int height, int rotation)
{
     if (pFrame == NULL)
        return;
     if (format == ia_frame_format_nv12) {
        pFrame->width = width;
        pFrame->height = height;
        pFrame->stride = stride;
        pFrame->size = pFrame->stride * height * 3 / 2;
        pFrame->rotation = rotation;
        pFrame->format = format;
        pFrame->data = allocateMemory(pFrame->size);
    } else {
        LOGE("[%s] not supported frame format", __FUNCTION__);
    }
}

void destroy_ia_frame(acc_ia_frame* pFrame)
{
    if (pFrame != NULL && pFrame->data != NULL)
        deallocateMemory(pFrame->data);
}

void downscaleAccFrame(acc_ia_frame* src, acc_ia_frame* dest)
{
    unsigned char* pSrc;
    unsigned char* pDest;

    if (src == NULL || dest == NULL) {
        LOGE("src or dest is NULL");
        return;
    }
    if (src->data == NULL || dest->data == NULL) {
        LOGE("src buffer or dest buffer is NULL");
        return;
    }
    if (src->width == 0 || src->height == 0 || src->stride == 0
        || dest->width == 0 || dest->height == 0 || dest->stride == 0) {
        LOGE("the width, height or stride of src and height is 0");
        return;
    }
    sp<IMemoryHeap> srcBuf = src->data->getMemory();
    sp<IMemoryHeap> dstBuf = dest->data->getMemory();
    ia_frame iaSrc;
    ia_frame iaDest;
    iaSrc.data = (unsigned char*)srcBuf->base();
    iaSrc.format = src->format;
    iaSrc.width = src->width;
    iaSrc.height = src->height;
    iaSrc.stride = src->stride;
    iaSrc.rotation = src->rotation;
    iaSrc.size = src->size;
    iaDest.data = (unsigned char*)dstBuf->base();
    iaDest.format = dest->format;
    iaDest.width = dest->width;
    iaDest.height = dest->height;
    iaDest.stride = dest->stride;
    iaDest.rotation = dest->rotation;
    iaDest.size = dest->size;
    downscaleFrame(&iaSrc, &iaDest);
}

void printFrameInfo(acc_ia_frame* pFrame)
{
    if (pFrame == NULL)
        return;
    LOGD("frame info : w = %d, h = %d, stride = %d, size = %d, format = %d, rotation = %d",
          pFrame->width, pFrame->height, pFrame->stride, pFrame->size, pFrame->format, pFrame->rotation);

}

jobject createIaFrame(JNIEnv* env, acc_ia_frame* src)
{
    if (env == NULL || src == NULL || src->data == NULL) {
        LOGE("the env or the src is NULL");
        return NULL;
    }

    jclass cls = env->FindClass(CLASS_IAFRAME);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "([BIIIII)V");

    jbyteArray imageData = env->NewByteArray(src->size);
    sp<IMemoryHeap> srcBuf = src->data->getMemory();
    if (srcBuf == NULL) {
        LOGE("the src buffer is NULL");
        return NULL;
    }
    env->SetByteArrayRegion(imageData, 0, src->size, (jbyte*)srcBuf->base());
    jobject iaFrame = env->NewObject(cls, constructor, imageData,
                                     src->stride, src->width, src->height, 17, src->rotation);
    return iaFrame;
}

