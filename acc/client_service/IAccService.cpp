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

#define LOG_TAG "IAccService"
#include <utils/Log.h>

#include <stdint.h>
#include <sys/types.h>


#include <IAccService.h>

#include <utils/Errors.h>  // for status_t
#include <utils/String8.h>

#include <stdio.h>
#include <binder/IInterface.h>
#include <binder/Parcel.h>
#include <binder/IMemory.h>
#include <binder/IBinder.h>
#include <binder/Binder.h>
#include <binder/ProcessState.h>
#include <binder/IPCThreadState.h>
#include <binder/IServiceManager.h>


namespace android {

enum {
    CREATE = IBinder::FIRST_CALL_TRANSACTION,
    INIT,
    DEINIT,
    CP_INIT,
    CP_UNINIT,
    CP_HDR_INIT,
    CP_HDR_UNINIT,
    CP_HDR_COMPOSE,
    CP_ULL_INIT,
    CP_ULL_UNINIT,
    CP_ULL_COMPOSE,
};

class BpAccService: public BpInterface<IAccService>
{
public:
    BpAccService(const sp<IBinder>& impl);
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
};

BpAccService::BpAccService(const sp<IBinder>& impl)
    : BpInterface<IAccService>(impl)
{
}

int BpAccService::init()
{
    Parcel data, reply;
    data.writeInterfaceToken(IAccService::getInterfaceDescriptor());
    remote()->transact(INIT, data, &reply);
    return reply.readInt32();
}

void BpAccService::deInit(int fd)
{
    Parcel data, reply;
    data.writeInt32(fd);
    data.writeInterfaceToken(IAccService::getInterfaceDescriptor());
    remote()->transact(DEINIT, data, &reply);
}

int BpAccService::CPInit()
{
    Parcel data, reply;
    data.writeInterfaceToken(IAccService::getInterfaceDescriptor());
    remote()->transact(CP_INIT, data, &reply);
    return reply.readInt32();
}

void BpAccService::CPUnInit()
{
    Parcel data, reply;
    data.writeInterfaceToken(IAccService::getInterfaceDescriptor());
    remote()->transact(CP_UNINIT, data, &reply);
}

int BpAccService::CPHdrInit(int width, int height)
{
    Parcel data, reply;
    data.writeInterfaceToken(IAccService::getInterfaceDescriptor());
    data.writeInt32(width);
    data.writeInt32(height);
    remote()->transact(CP_HDR_INIT, data, &reply);
    return reply.readInt32();
}

int BpAccService::CPHdrUnInit()
{
    Parcel data, reply;
    data.writeInterfaceToken(IAccService::getInterfaceDescriptor());
    remote()->transact(CP_HDR_UNINIT, data, &reply);
    return reply.readInt32();
}

int BpAccService::CPHdrCompose(const int inImageNum, const acc_ia_frame* inBuf, const acc_ia_frame* inPvBuf, acc_ia_frame* outBuf, acc_ia_frame* outPvBuf)
{
    Parcel data, reply;
    data.writeInterfaceToken(IAccService::getInterfaceDescriptor());

    data.writeInt32(inImageNum);

    for (int i = 0; i < inImageNum; i++) {
        data.writeStrongBinder(inBuf->data->asBinder());
        data.writeInt32(inBuf->size);
        data.writeInt32(inBuf->width);
        data.writeInt32(inBuf->height);
        data.writeInt32(inBuf->format);
        data.writeInt32(inBuf->stride);
        data.writeInt32(inBuf->rotation);
    }

    for (int i = 0; i < inImageNum; i++) {
        data.writeStrongBinder(inPvBuf->data->asBinder());
        data.writeInt32(inPvBuf->size);
        data.writeInt32(inPvBuf->width);
        data.writeInt32(inPvBuf->height);
        data.writeInt32(inPvBuf->format);
        data.writeInt32(inPvBuf->stride);
        data.writeInt32(inPvBuf->rotation);
    }

    data.writeStrongBinder(outBuf->data->asBinder());
    data.writeInt32(outBuf->size);
    data.writeInt32(outBuf->width);
    data.writeInt32(outBuf->height);
    data.writeInt32(outBuf->format);
    data.writeInt32(outBuf->stride);
    data.writeInt32(outBuf->rotation);

    data.writeStrongBinder(outPvBuf->data->asBinder());
    data.writeInt32(outPvBuf->size);
    data.writeInt32(outPvBuf->width);
    data.writeInt32(outPvBuf->height);
    data.writeInt32(outPvBuf->format);
    data.writeInt32(outPvBuf->stride);
    data.writeInt32(outPvBuf->rotation);

    remote()->transact(CP_HDR_COMPOSE, data, &reply);

    return reply.readInt32();
}

int BpAccService::CPUllInit(int width, int height)
{
    Parcel data, reply;
    data.writeInterfaceToken(IAccService::getInterfaceDescriptor());
    data.writeInt32(width);
    data.writeInt32(height);
    remote()->transact(CP_ULL_INIT, data, &reply);
    return reply.readInt32();
}

int BpAccService::CPUllUnInit()
{
    Parcel data, reply;
    data.writeInterfaceToken(IAccService::getInterfaceDescriptor());
    remote()->transact(CP_ULL_UNINIT, data, &reply);
    return reply.readInt32();
}

int BpAccService::CPUllCompose(const int inImageNum, const acc_ia_frame* inBuf, const acc_ia_frame* inPvBuf, acc_ia_frame* outBuf, acc_ia_frame* outPvBuf, ia_cp_ull_cfg* cfg)
{
    Parcel data, reply;
    data.writeInterfaceToken(IAccService::getInterfaceDescriptor());

    data.writeInt32(inImageNum);

    for (int i = 0; i < inImageNum; i++) {
        data.writeStrongBinder(inBuf->data->asBinder());
        data.writeInt32(inBuf->size);
        data.writeInt32(inBuf->width);
        data.writeInt32(inBuf->height);
        data.writeInt32(inBuf->format);
        data.writeInt32(inBuf->stride);
        data.writeInt32(inBuf->rotation);
    }

    for (int i = 0; i < inImageNum; i++) {
        data.writeStrongBinder(inPvBuf->data->asBinder());
        data.writeInt32(inPvBuf->size);
        data.writeInt32(inPvBuf->width);
        data.writeInt32(inPvBuf->height);
        data.writeInt32(inPvBuf->format);
        data.writeInt32(inPvBuf->stride);
        data.writeInt32(inPvBuf->rotation);
    }

    data.writeStrongBinder(outBuf->data->asBinder());
    data.writeInt32(outBuf->size);
    data.writeInt32(outBuf->width);
    data.writeInt32(outBuf->height);
    data.writeInt32(outBuf->format);
    data.writeInt32(outBuf->stride);
    data.writeInt32(outBuf->rotation);

    data.writeStrongBinder(outPvBuf->data->asBinder());
    data.writeInt32(outPvBuf->size);
    data.writeInt32(outPvBuf->width);
    data.writeInt32(outPvBuf->height);
    data.writeInt32(outPvBuf->format);
    data.writeInt32(outPvBuf->stride);
    data.writeInt32(outPvBuf->rotation);

    data.writeInt32(cfg->exposure.exposure_time_us);
    data.writeFloat(cfg->exposure.analog_gain);
    data.writeFloat(cfg->exposure.digital_gain);
    data.writeFloat(cfg->exposure.aperture_fn);
    data.writeInt32(cfg->exposure.total_target_exposure);
    data.writeInt32(cfg->exposure.nd_filter_enabled);
    data.writeInt32(cfg->exposure.iso);
    data.writeInt32(cfg->zoom_factor);

    remote()->transact(CP_ULL_COMPOSE, data, &reply);

    return reply.readInt32();
}


IMPLEMENT_META_INTERFACE(AccService, "android.media.IAccService");

// ----------------------------------------------------------------------

status_t BnAccService::onTransact(
    uint32_t code, const Parcel& data, Parcel* reply, uint32_t flags)
{
    switch (code) {
        case INIT: {
            CHECK_INTERFACE(IAccService, data, reply);
            int ret = init();
            reply->writeInt32(ret);
            return NO_ERROR;
        } break;
        case DEINIT: {
            CHECK_INTERFACE(IAccService, data, reply);
            deInit(data.readInt32());
            return NO_ERROR;
        } break;

        case CP_INIT: {
            CHECK_INTERFACE(IAccService, data, reply);
            int ret = CPInit();
            reply->writeInt32(ret);
            return NO_ERROR;
        } break;
        case CP_UNINIT: {
            CHECK_INTERFACE(IAccService, data, reply);
            CPUnInit();
            return NO_ERROR;
        } break;
        case CP_HDR_INIT: {
            CHECK_INTERFACE(IAccService, data, reply);
            int width, height;
            width = data.readInt32();
            height = data.readInt32();
            int ret = CPHdrInit(width, height);
            reply->writeInt32(ret);
            return NO_ERROR;
        } break;
        case CP_HDR_UNINIT: {
            CHECK_INTERFACE(IAccService, data, reply);
            int ret = CPHdrUnInit();
            reply->writeInt32(ret);
            return NO_ERROR;
        } break;
        case CP_HDR_COMPOSE: {
            CHECK_INTERFACE(IAccService, data, reply);
            int inImageNum = data.readInt32();
            acc_ia_frame inBuf[inImageNum];
            acc_ia_frame inPvBuf[inImageNum];
            acc_ia_frame outBuf;
            acc_ia_frame outPvBuf;

            for (int i = 0; i < inImageNum; i++) {
                inBuf[i].data = interface_cast<IMemory>(data.readStrongBinder());
                inBuf[i].size = data.readInt32();
                inBuf[i].width = data.readInt32();
                inBuf[i].height = data.readInt32();
                inBuf[i].format = (ia_frame_format)data.readInt32();
                inBuf[i].stride = data.readInt32();
                inBuf[i].rotation = data.readInt32();
            }

            for (int i = 0; i < inImageNum; i++) {
                inPvBuf[i].data = interface_cast<IMemory>(data.readStrongBinder());
                inPvBuf[i].size = data.readInt32();
                inPvBuf[i].width = data.readInt32();
                inPvBuf[i].height = data.readInt32();
                inPvBuf[i].format = (ia_frame_format)data.readInt32();
                inPvBuf[i].stride = data.readInt32();
                inPvBuf[i].rotation = data.readInt32();
            }

            outBuf.data = interface_cast<IMemory>(data.readStrongBinder());
            outBuf.size = data.readInt32();
            outBuf.width = data.readInt32();
            outBuf.height = data.readInt32();
            outBuf.format = (ia_frame_format)data.readInt32();
            outBuf.stride = data.readInt32();
            outBuf.rotation = data.readInt32();

            outPvBuf.data = interface_cast<IMemory>(data.readStrongBinder());
            outPvBuf.size = data.readInt32();
            outPvBuf.width = data.readInt32();
            outPvBuf.height = data.readInt32();
            outPvBuf.format = (ia_frame_format)data.readInt32();
            outPvBuf.stride = data.readInt32();
            outPvBuf.rotation = data.readInt32();

            int ret = CPHdrCompose(inImageNum, inBuf, inPvBuf, &outBuf, &outPvBuf);
            reply->writeInt32(ret);
            return NO_ERROR;
        } break;
        case CP_ULL_INIT: {
            CHECK_INTERFACE(IAccService, data, reply);
            int width, height;
            width = data.readInt32();
            height = data.readInt32();
            int ret = CPUllInit(width, height);
            reply->writeInt32(ret);
            return NO_ERROR;
        } break;
        case CP_ULL_UNINIT: {
            CHECK_INTERFACE(IAccService, data, reply);
            int ret = CPUllUnInit();
            reply->writeInt32(ret);
            return NO_ERROR;
        } break;
        case CP_ULL_COMPOSE: {
            CHECK_INTERFACE(IAccService, data, reply);
            int inImageNum = data.readInt32();
            acc_ia_frame inBuf[inImageNum];
            acc_ia_frame inPvBuf[inImageNum];
            acc_ia_frame outBuf;
            acc_ia_frame outPvBuf;
            ia_cp_ull_cfg cfg;

            for (int i = 0; i < inImageNum; i++) {
                inBuf[i].data = interface_cast<IMemory>(data.readStrongBinder());
                inBuf[i].size = data.readInt32();
                inBuf[i].width = data.readInt32();
                inBuf[i].height = data.readInt32();
                inBuf[i].format = (ia_frame_format)data.readInt32();
                inBuf[i].stride = data.readInt32();
                inBuf[i].rotation = data.readInt32();
            }

            for (int i = 0; i < inImageNum; i++) {
                inPvBuf[i].data = interface_cast<IMemory>(data.readStrongBinder());
                inPvBuf[i].size = data.readInt32();
                inPvBuf[i].width = data.readInt32();
                inPvBuf[i].height = data.readInt32();
                inPvBuf[i].format = (ia_frame_format)data.readInt32();
                inPvBuf[i].stride = data.readInt32();
                inPvBuf[i].rotation = data.readInt32();
            }

            outBuf.data = interface_cast<IMemory>(data.readStrongBinder());
            outBuf.size = data.readInt32();
            outBuf.width = data.readInt32();
            outBuf.height = data.readInt32();
            outBuf.format = (ia_frame_format)data.readInt32();
            outBuf.stride = data.readInt32();
            outBuf.rotation = data.readInt32();

            outPvBuf.data = interface_cast<IMemory>(data.readStrongBinder());
            outPvBuf.size = data.readInt32();
            outPvBuf.width = data.readInt32();
            outPvBuf.height = data.readInt32();
            outPvBuf.format = (ia_frame_format)data.readInt32();
            outPvBuf.stride = data.readInt32();
            outPvBuf.rotation = data.readInt32();

            memset(&cfg, 0, sizeof(cfg));
            cfg.exposure.exposure_time_us = data.readInt32();
            cfg.exposure.analog_gain = data.readFloat();
            cfg.exposure.digital_gain = data.readFloat();
            cfg.exposure.aperture_fn = data.readFloat();
            cfg.exposure.total_target_exposure = data.readInt32();
            cfg.exposure.nd_filter_enabled = data.readInt32();
            cfg.exposure.iso = data.readInt32();
            cfg.zoom_factor = data.readInt32();

            int ret = CPUllCompose(inImageNum, inBuf, inPvBuf, &outBuf, &outPvBuf, &cfg);
            reply->writeInt32(ret);
            return NO_ERROR;
        } break;
        default:
            return BBinder::onTransact(code, data, reply, flags);
    }
}

// ----------------------------------------------------------------------------

}
