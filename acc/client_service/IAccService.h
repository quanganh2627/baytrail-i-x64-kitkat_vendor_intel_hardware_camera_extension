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

#ifndef IACCSERVICE_H
#define IACCSERVICE_H

#include <utils/Errors.h>  // for status_t
#include <utils/KeyedVector.h>
#include <utils/RefBase.h>
#include <utils/String8.h>
#include <binder/IInterface.h>
#include <binder/IMemory.h>
#include <binder/Parcel.h>
#include <system/audio.h>

#include <ia_types.h>
#include <ia_cp_types.h>

namespace android {

#define UNUSED(x) (void)(x)
#define ACC_SERVICE_NAME "media.ipu.acc"

typedef struct {
    sp<IMemory>     data;
    int             size;     /**< Total number of bytes in data*/
    int             width;    /**< Width of the frame in pixels */
    int             height;   /**< Height of the frame in lines */
    ia_frame_format format;   /**< Frame format */
    int             stride;   /**< Stride, bytes per line*/
    int             rotation; /**< degrees 0-360 */
} acc_ia_frame;

class IAccService: public IInterface
{
public:
    DECLARE_META_INTERFACE(AccService);

    virtual int init() = 0;
    virtual void deInit(int fd) = 0;

    virtual int CPInit() = 0;
    virtual void CPUnInit() = 0;

    virtual int CPHdrInit(int width, int height) = 0;
    virtual int CPHdrUnInit() = 0;
    virtual int CPHdrCompose(const int inImageNum, const acc_ia_frame* inBuf, const acc_ia_frame* inPvBuf, acc_ia_frame* outBuf, acc_ia_frame* outPvBuf) = 0;

    virtual int CPUllInit(int width, int height) = 0;
    virtual int CPUllUnInit() = 0;
    virtual int CPUllCompose(const int inImageNum, const acc_ia_frame* inBuf, const acc_ia_frame* inPvBuf, acc_ia_frame* outBuf, acc_ia_frame* outPvBuf, ia_cp_ull_cfg* cfg) = 0;
};

// ----------------------------------------------------------------------------

class BnAccService: public BnInterface<IAccService>
{
public:
    virtual status_t    onTransact( uint32_t code,
                                    const Parcel& data,
                                    Parcel* reply,
                                    uint32_t flags = 0);
};

}; // namespace android

#endif // IACCSERVICE_H
