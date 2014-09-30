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

#ifndef ACCSERVICE_H
#define ACCSERVICE_H

#include <arpa/inet.h>

#include <utils/threads.h>
#include <utils/Errors.h>
#include <utils/KeyedVector.h>
#include <utils/String8.h>
#include <utils/Vector.h>

#include <system/audio.h>

#include "IAccService.h"
#include "acc.h"

namespace android {

class AccService : public BnAccService
{
public:
    static void instantiate(int logLevel);

    virtual int init();
    virtual void deInit(int fd);

    virtual int CPInit();
    virtual void CPUnInit();

    virtual int CPHdrInit(int width, int height);
    virtual int CPHdrUnInit();
    virtual int CPHdrCompose(const int inImageNum, const acc_ia_frame* inBuf, const acc_ia_frame* inPvBuf, acc_ia_frame* outBuf, acc_ia_frame* outPvBuf);

    virtual int CPUllInit(int width, int height);
    virtual int CPUllUnInit();
    virtual int CPUllCompose(const int inImageNum, const acc_ia_frame* inBuf, const acc_ia_frame* inPvBuf, acc_ia_frame* outBuf, acc_ia_frame* outPvBuf, ia_cp_ull_cfg* cfg);
public:
    AccControl* getAcc() { return mAcc; }
private:
    AccService();
    virtual ~AccService();

    int mFd;
    AccControl* mAcc;
    Mutex mAccServiceLock;
private:
    typedef struct {
        ia_cp_context* pIaCpContext;
        ia_acceleration iaAcc;
        ia_env iaEnv;
        ia_cp_hdr* pIaCpHdr;
        ia_cp_ull* pIaCpUll;
    } CPEngine;
    CPEngine* mCpe;

    int registerAccCallback(ia_acceleration& iaAcc);
    void uninitAcc();
    ia_cp_context* getCpContext(CPEngine* instance);
    ia_cp_hdr* getCpHDR(CPEngine* instance);
    ia_cp_ull* getCpULL(CPEngine* instance);
    void debugDumpData(const char* filename, unsigned char* data, int bytes);
};

}

#endif // ACCSERVICE_H
