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

#ifndef ACCCLIENT_H
#define ACCCLIENT_H

#include <utils/Mutex.h>
#include <IAccService.h>

namespace android {

class AccClient
{
public:
    AccClient();
    virtual ~AccClient();

public:
    static AccClient* connect();
    void disConnect();

    int CPInit();
    void CPUnInit();

    int CPHdrInit(const int width, const int height);
    int CPHdrCompose(const int inImageNum, const acc_ia_frame* inBuf, const acc_ia_frame* inPvBuf, acc_ia_frame* outBuf, acc_ia_frame* outPvBuf);
    int CPHdrUnInit();

    int CPUllInit(const int width, const int height);
    int CPUllCompose(const int inImageNum, const acc_ia_frame* inBuf, const acc_ia_frame* inPvBuf, acc_ia_frame* outBuf, acc_ia_frame* outPvBuf, ia_cp_ull_cfg* cfg);
    int CPUllUnInit();

    // TODO: to add the interfaces for general computing by using GPU.
private:
    // init and deInit the acc service
    int init();
    void deInit(int fd);

    sp<IAccService> getAccService();
    sp<IAccService> mAccService;

    static AccClient* mInstance;
    int mCallingPid;
    Mutex mLock;
    int mFd;
};

}

#endif // ACCCLIENT_H
