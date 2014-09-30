/*
 * Copyright (C) 2015 Intel Corporation
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
#include <android/log.h>
#include "AccWrapper.h"
#include "CpMemUtil.h"
#include "JNIUtil.h"


using namespace android;

static android::AccClient *mAccClient = NULL;

status_t CpIpuInit ()
{
    if (mAccClient == NULL) {
        LOGE("ACC client singleton has not initialized.");
        return UNKNOWN_ERROR;
    }
    return mAccClient->CPInit();
}

void CpIpuUnInit()
{
    if (mAccClient == NULL) {
        LOGE("ACC client singleton has not initialized.");
        return;
    }
    mAccClient->CPUnInit();
}

status_t CpIpuHdrInit (const int width, const int height)
{
    if (mAccClient == NULL) {
        LOGE("ACC client singleton has not initialized.");
        return UNKNOWN_ERROR;
    }
    return mAccClient->CPHdrInit(width, height);
}

status_t CpIpuHdrUnInit ()
{
    if (mAccClient == NULL) {
        LOGE("ACC client singleton has not initialized.");
        return UNKNOWN_ERROR;
    }
    return mAccClient->CPHdrUnInit();
}

status_t CpIpuHdrCompose (const int inImageNum, const acc_ia_frame* inBuf,
                            const acc_ia_frame* inPvBuf, acc_ia_frame* outBuf,
                            acc_ia_frame* outPv)
{
    if (mAccClient == NULL) {
        LOGE("ACC client singleton has not initialized.");
        return UNKNOWN_ERROR;
    }
    return mAccClient->CPHdrCompose(inImageNum, inBuf, inPvBuf, outBuf, outPv);
}
status_t CpIpuUllInit (const int width, const int height)
{
    if (mAccClient == NULL) {
        LOGE("ACC client singleton has not initialized.");
        return UNKNOWN_ERROR;
    }
    return mAccClient->CPUllInit(width, height);
}
status_t CpIpuUllUnInit ()
{
    if (mAccClient == NULL) {
        LOGE("ACC client singleton has not initialized.");
        return UNKNOWN_ERROR;
    }
    return mAccClient->CPUllUnInit();
}
status_t CpIpuUllCompose (const int inImageNum, const acc_ia_frame* inBuf,
                           const acc_ia_frame* inPvBuf, acc_ia_frame* outBuf,
                           acc_ia_frame* outPv, ia_cp_ull_cfg* cfg)
{
    if (mAccClient == NULL) {
        LOGE("ACC client singleton has not initialized.");
        return UNKNOWN_ERROR;
    }
    return mAccClient->CPUllCompose(inImageNum, inBuf, inPvBuf, outBuf, outPv, cfg);
}
status_t initAcc()
{
    if (mAccClient == NULL) {
        mAccClient = AccClient::connect();
    }

    if (mAccClient == NULL) {
        ALOGE("AccClient can't connect!!!");
        return UNKNOWN_ERROR;
    }
    initMemPool();
    return NO_ERROR;
}
void uninitAcc()
{
     if (mAccClient != NULL) {
         mAccClient->disConnect();
         mAccClient = NULL;
     }
     unInitMemPool();
     return;
}

