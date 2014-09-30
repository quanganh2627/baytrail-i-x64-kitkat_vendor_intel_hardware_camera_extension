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

#define LOG_TAG "AccClient"

#include <utils/RefBase.h>
#include <binder/IInterface.h>
#include <binder/Parcel.h>

#include "AccClient.h"
#include <IAccService.h>

#include <utils/Log.h>
#include <utils/threads.h>
#include <utils/Mutex.h>

#include <binder/IPCThreadState.h>
#include <binder/IServiceManager.h>
#include <binder/IMemory.h>

#include <stdio.h>
#include <binder/IInterface.h>
#include <binder/Parcel.h>
#include <binder/IBinder.h>
#include <binder/Binder.h>
#include <binder/ProcessState.h>
#include <binder/IPCThreadState.h>
#include <binder/IServiceManager.h>

namespace android {
static volatile int32_t gLoglevel = 0;

#define ALOG1(...) ALOGD_IF(gLoglevel >= 1, __VA_ARGS__);
#define ALOG2(...) ALOGD_IF(gLoglevel >= 2, __VA_ARGS__);

AccClient* AccClient::mInstance = NULL;

static int getCallingPid() {
    return IPCThreadState::self()->getCallingPid();
}

AccClient::AccClient()
{
    ALOG1("@%s", __FUNCTION__);
    mCallingPid = getCallingPid();
    mFd = -1;
}

AccClient::~AccClient()
{
    ALOG1("@%s", __FUNCTION__);
}

AccClient* AccClient::connect()
{
    ALOG1("@%s", __FUNCTION__);
    nsecs_t startTime = systemTime();

    if (mInstance == NULL) {
        mInstance = new AccClient();
        if (NULL == mInstance->getAccService().get()
            || mInstance->init() < 0) {
            delete mInstance;
            return NULL;
        }
    }
    ALOG1("@%s consume:%ums", __FUNCTION__, (unsigned)((systemTime() - startTime) / 1000000));
    return mInstance;
}

void AccClient::disConnect()
{
    ALOG1("@%s", __FUNCTION__);
    nsecs_t startTime = systemTime();

    if (mCallingPid != getCallingPid()) {
        ALOGE("@%s, line:%d, calling pid is not the same, mCallingPid:%d, getCallingPid():%d", __FUNCTION__, __LINE__, mCallingPid, getCallingPid());
        return;
    }
    if (mInstance) {
        deInit(mFd);
        delete mInstance;
        mInstance = NULL;
    }

    ALOG1("@%s consume:%ums", __FUNCTION__, (unsigned)((systemTime() - startTime) / 1000000));
}

int AccClient::CPInit()
{
    if (mCallingPid != getCallingPid()) {
        ALOGE("@%s, line:%d, calling pid is not the same, mCallingPid:%d, getCallingPid():%d", __FUNCTION__, __LINE__, mCallingPid, getCallingPid());
        return -1;
    }

    Mutex::Autolock lock(mLock);
    ALOG1("@%s, line:%d", __FUNCTION__, __LINE__);
    nsecs_t startTime = systemTime();

    int ret = mAccService->CPInit();
    ALOG1("@%s consume:%ums", __FUNCTION__, (unsigned)((systemTime() - startTime) / 1000000));
    return ret;
}

void AccClient::CPUnInit()
{
    if (mCallingPid != getCallingPid()) {
        ALOGE("@%s, line:%d, calling pid is not the same, mCallingPid:%d, getCallingPid():%d", __FUNCTION__, __LINE__, mCallingPid, getCallingPid());
        return;
    }

    Mutex::Autolock lock(mLock);
    ALOG1("@%s, line:%d", __FUNCTION__, __LINE__);
    nsecs_t startTime = systemTime();

    mAccService->CPUnInit();
    ALOG1("@%s consume:%ums", __FUNCTION__, (unsigned)((systemTime() - startTime) / 1000000));
}

int AccClient::CPHdrInit(int width, int height)
{
    if (mCallingPid != getCallingPid()) {
        ALOGE("@%s, line:%d, calling pid is not the same, mCallingPid:%d, getCallingPid():%d", __FUNCTION__, __LINE__, mCallingPid, getCallingPid());
        return -1;
    }

    Mutex::Autolock lock(mLock);
    ALOG1("@%s, line:%d", __FUNCTION__, __LINE__);
    nsecs_t startTime = systemTime();

    int ret = mAccService->CPHdrInit(width, height);
    ALOG1("@%s consume:%ums", __FUNCTION__, (unsigned)((systemTime() - startTime) / 1000000));
    return ret;
}

int AccClient::CPHdrUnInit()
{
    if (mCallingPid != getCallingPid()) {
        ALOGE("@%s, line:%d, calling pid is not the same, mCallingPid:%d, getCallingPid():%d", __FUNCTION__, __LINE__, mCallingPid, getCallingPid());
        return -1;
    }

    Mutex::Autolock lock(mLock);
    ALOG1("@%s, line:%d", __FUNCTION__, __LINE__);
    nsecs_t startTime = systemTime();

    int ret = mAccService->CPHdrUnInit();
    ALOG1("@%s consume:%ums", __FUNCTION__, (unsigned)((systemTime() - startTime) / 1000000));
    return ret;
}

int AccClient::CPHdrCompose(const int inImageNum, const acc_ia_frame* inBuf, const acc_ia_frame* inPvBuf, acc_ia_frame* outBuf, acc_ia_frame* outPvBuf)
{
    if (mCallingPid != getCallingPid()) {
        ALOGE("@%s, line:%d, calling pid is not the same, mCallingPid:%d, getCallingPid():%d", __FUNCTION__, __LINE__, mCallingPid, getCallingPid());
        return -1;
    }

    Mutex::Autolock lock(mLock);
    ALOG1("@%s, line:%d", __FUNCTION__, __LINE__);
    nsecs_t startTime = systemTime();

    int ret = mAccService->CPHdrCompose(inImageNum, inBuf, inPvBuf, outBuf, outPvBuf);
    ALOG1("@%s consume:%ums", __FUNCTION__, (unsigned)((systemTime() - startTime) / 1000000));
    return ret;
}

int AccClient::CPUllInit(const int width, const int height)
{
    if (mCallingPid != getCallingPid()) {
        ALOGE("@%s, line:%d, calling pid is not the same, mCallingPid:%d, getCallingPid():%d", __FUNCTION__, __LINE__, mCallingPid, getCallingPid());
        return -1;
    }

    Mutex::Autolock lock(mLock);
    ALOG1("@%s, line:%d", __FUNCTION__, __LINE__);
    nsecs_t startTime = systemTime();

    int ret = mAccService->CPUllInit(width, height);
    ALOG1("@%s consume:%ums", __FUNCTION__, (unsigned)((systemTime() - startTime) / 1000000));
    return ret;
}

int AccClient::CPUllUnInit()
{
    if (mCallingPid != getCallingPid()) {
        ALOGE("@%s, line:%d, calling pid is not the same, mCallingPid:%d, getCallingPid():%d", __FUNCTION__, __LINE__, mCallingPid, getCallingPid());
        return -1;
    }

    Mutex::Autolock lock(mLock);
    ALOG1("@%s, line:%d", __FUNCTION__, __LINE__);
    nsecs_t startTime = systemTime();

    int ret = mAccService->CPUllUnInit();
    ALOG1("@%s consume:%ums", __FUNCTION__, (unsigned)((systemTime() - startTime) / 1000000));
    return ret;
}

int AccClient::CPUllCompose(const int inImageNum, const acc_ia_frame* inBuf, const acc_ia_frame* inPvBuf, acc_ia_frame* outBuf, acc_ia_frame* outPvBuf, ia_cp_ull_cfg* cfg)
{
    if (mCallingPid != getCallingPid()) {
        ALOGE("@%s, line:%d, calling pid is not the same, mCallingPid:%d, getCallingPid():%d", __FUNCTION__, __LINE__, mCallingPid, getCallingPid());
        return -1;
    }

    Mutex::Autolock lock(mLock);
    ALOG1("@%s, line:%d", __FUNCTION__, __LINE__);
    nsecs_t startTime = systemTime();

    int ret = mAccService->CPUllCompose(inImageNum, inBuf, inPvBuf, outBuf, outPvBuf, cfg);
    ALOG1("@%s consume:%ums", __FUNCTION__, (unsigned)((systemTime() - startTime) / 1000000));
    return ret;
}

sp<IAccService> AccClient::getAccService()
{
    Mutex::Autolock lock(mLock);
    ALOG1("@%s, line:%d", __FUNCTION__, __LINE__);
    nsecs_t startTime = systemTime();

    const int pollDelay = 500000; // 0.5s
    unsigned tryTime = 20;

    if (mAccService.get() == 0) {
        sp<IServiceManager> sm = defaultServiceManager();
        sp<IBinder> binder;
        do {
            binder = sm->getService(String16(ACC_SERVICE_NAME));
            if (binder != 0) {
                break;
            }
            ALOGW("AccService not published, waiting...");
            usleep(pollDelay);
        } while(tryTime--);

        mAccService = interface_cast<IAccService>(binder);
    }
    ALOGE_IF(mAccService == 0, "no AccService!?");
    ALOG1("@%s consume:%ums", __FUNCTION__, (unsigned)((systemTime() - startTime) / 1000000));

    return mAccService;
}

int AccClient::init()
{
    Mutex::Autolock lock(mLock);
    ALOG1("@%s, line:%d", __FUNCTION__, __LINE__);
    nsecs_t startTime = systemTime();

    mFd = mAccService->init();
    ALOG1("@%s consume:%ums", __FUNCTION__, (unsigned)((systemTime() - startTime) / 1000000));
    return mFd;
}

void AccClient::deInit(int fd)
{
    Mutex::Autolock lock(mLock);
    ALOG1("@%s, line:%d", __FUNCTION__, __LINE__);
    nsecs_t startTime = systemTime();

    mAccService->deInit(fd);
    ALOG1("@%s consume:%ums", __FUNCTION__, (unsigned)((systemTime() - startTime) / 1000000));
}

}
