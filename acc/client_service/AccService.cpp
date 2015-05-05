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


#define LOG_TAG "AccService"
#include <utils/Log.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <sys/time.h>

#include <string.h>

#include <cutils/atomic.h>
#include <cutils/properties.h> // for property_get

#include <utils/misc.h>

#include <binder/IBatteryStats.h>
#include <binder/IPCThreadState.h>
#include <binder/IServiceManager.h>
#include <binder/MemoryHeapBase.h>
#include <binder/MemoryBase.h>
#include <gui/Surface.h>
#include <utils/Errors.h>  // for status_t
#include <utils/String8.h>
#include <utils/SystemClock.h>
#include <utils/Timers.h>
#include <utils/Vector.h>

#include <AccService.h>
#include <acc.h>

#include "ia_cp_types.h"
#include "ia_cp.h"
#include "ia_aiq.h"

namespace android {
static volatile int32_t gLoglevel = 0;

#define ALOG1(...) ALOGD_IF(gLoglevel >= 1, __VA_ARGS__);
#define ALOG2(...) ALOGD_IF(gLoglevel >= 2, __VA_ARGS__);

static AccService* mAccService = NULL;

#define DUMP_PATH "/data/misc/media"
#define DUMP_INPUT_FRAME 0
#define DUMP_OUTPUT_FRAME 0

void AccService::instantiate(int32_t logLevel) {
    if (mAccService == NULL) {
        sp < IServiceManager > sm = defaultServiceManager();
        mAccService = new AccService();
        status_t status = sm->addService(String16(ACC_SERVICE_NAME), mAccService);
        if (logLevel)
            gLoglevel = logLevel;
        ALOG1("@%s, line:%d, status:%d, gLoglevel:%d", __FUNCTION__, __LINE__, status, gLoglevel);
    } else
        ALOGW("@%s, WARNING, gAccService is not null, instantiate() is called again", __FUNCTION__);
}

AccService::AccService()
{
    mFd = -1;
    mAcc = new AccControl();
    return;
}

AccService::~AccService()
{
    delete mAcc;
    ALOGV("AccService destroyed");
}

int AccService::init() {
    AutoMutex lock(mAccServiceLock);
    mFd = mAcc->acc_init();
    if (mFd >= 0)
        return mFd;
    return -1;
}

void AccService::deInit(int fd) {
    AutoMutex lock(mAccServiceLock);
    if (fd >= 0)
        mAcc->acc_deinit(fd);
}

static void* getHwHandler()
{
    return (void*)mAccService;
}

static void getHwVersion(int& CssMajor, int& CssMinor, int& IspMajor, int& IspMinor)
{
    CssMajor = mAccService->getAcc()->get_css_major_version();
    CssMinor = mAccService->getAcc()->get_css_minor_version();
    IspMajor = mAccService->getAcc()->get_isp_hw_major_version();
    IspMinor = mAccService->getAcc()->get_isp_hw_minor_version();
    ALOG1("@%s, line:%d, CssMajor:%d, CssMinor:%d, IspMajor:%d, IspMinor:%d", __FUNCTION__, __LINE__, CssMajor, CssMinor, IspMajor, IspMinor);
}

static int get_file_size (FILE *file)
{
    int len;

    if (file == NULL) return 0;

    if (fseek(file, 0, SEEK_END)) return 0;

    len = ftell(file);

    if (fseek(file, 0, SEEK_SET)) return 0;

    return len;
}

static void* open_firmware(const char *fw_path, unsigned *size)
{
    FILE *file;
    unsigned len;
    void *fw;

    ALOG1("@%s", __FUNCTION__);
    if (!fw_path)
        return NULL;

    file = fopen(fw_path, "rb");
    if (!file)
        return NULL;

    len = get_file_size(file);

    if (!len) {
        fclose(file);
        return NULL;
    }

    fw = malloc(len);
    if (!fw) {
        fclose(file);
        return NULL;
    }

    if (fread(fw, 1, len, file) != len) {
        fclose(file);
        free(fw);
        return NULL;
    }

    *size = len;

    fclose(file);

    return fw;
}

static int load_firmware(void *isp, void *fw, unsigned size, unsigned *handle)
{
    UNUSED(isp);
    ALOG1("@%s", __FUNCTION__);
    return mAccService->getAcc()->acc_load_fw(fw, size, handle);
}

static int load_firmware_ext(void *isp, void *fw, unsigned size, unsigned *handle, int dst)
{
    UNUSED(isp);
    ALOG1("@%s", __FUNCTION__);
    return mAccService->getAcc()->acc_load_fw_pipe(fw, size, handle, dst);
}

static int load_firmware_pipe(void *isp, void *fw, unsigned size, unsigned *handle)
{
    UNUSED(isp);
    ALOG1("@%s", __FUNCTION__);
    return mAccService->getAcc()->acc_load_fw_pipe(fw, size, handle, PREVIEW_VFPP);
}

static int unload_firmware(void *isp, unsigned handle)
{
    UNUSED(isp);
    ALOG1("@%s", __FUNCTION__);
    return mAccService->getAcc()->acc_unload_fw(handle);
}

static int map_firmware_arg(void *isp, void *val, size_t size, unsigned long *ptr)
{
    UNUSED(isp);
    ALOG1("@%s", __FUNCTION__);
    return mAccService->getAcc()->acc_map(val, size, ptr);
}

static int unmap_firmware_arg (void *isp, unsigned long val, size_t size)
{
    UNUSED(isp);
    ALOG1("@%s", __FUNCTION__);
    return mAccService->getAcc()->acc_unmap(val, size);
}

static int set_firmware_arg(void *isp, unsigned handle, unsigned num, void *val, size_t size)
{
    UNUSED(isp);
    ALOG1("@%s", __FUNCTION__);
    return mAccService->getAcc()->acc_set_fw_arg(handle, num, val, size);
}

static int set_mapped_arg(void *isp, unsigned handle, unsigned mem, unsigned long val, size_t size)
{
    UNUSED(isp);
    ALOG1("@%s", __FUNCTION__);
    return mAccService->getAcc()->acc_set_mapped_arg(handle, mem, val, size);
}

static int start_firmware(void *isp, unsigned handle)
{
    UNUSED(isp);
    ALOG1("@%s", __FUNCTION__);
    return mAccService->getAcc()->acc_start_fw(handle);
}

static int wait_for_firmware(void *isp, unsigned handle)
{
    UNUSED(isp);
    ALOG1("@%s", __FUNCTION__);
    return mAccService->getAcc()->acc_wait_fw(handle);
}

static int abort_firmware(void *isp, unsigned handle, unsigned timeout)
{
    UNUSED(isp);
    ALOG1("@%s", __FUNCTION__);
    return mAccService->getAcc()->acc_abort_fw(handle, timeout);
}

static int set_stage_state(void *isp, unsigned int handle, bool enable)
{
    UNUSED(isp);
    ALOG1("@%s", __FUNCTION__);
    return mAccService->getAcc()->acc_set_stage_state(handle, enable);
}

static int wait_stage_update(void *isp, unsigned int handle)
{
    UNUSED(isp);
    ALOG1("@%s", __FUNCTION__);
    return mAccService->getAcc()->acc_wait_stage_update(handle);
}

static void vdebug(const char *fmt, va_list ap)
{
    ALOGD(fmt, ap);
}

static void verror(const char *fmt, va_list ap)
{
    ALOGE(fmt, ap);
}

static void vinfo(const char *fmt, va_list ap)
{
    ALOGI(fmt, ap);
}

int AccService::registerAccCallback(ia_acceleration& iaAcc)
{
    ALOG1("@%s, line:%d", __FUNCTION__, __LINE__);

    int cssMinor, cssMajor;
    int ispMinor, ispMajor;
    getHwVersion(cssMajor, cssMinor, ispMajor, ispMinor);
    iaAcc.version_css.major = cssMajor;
    iaAcc.version_css.minor = cssMinor;
    iaAcc.version_isp.major = ispMajor;
    iaAcc.version_isp.minor = ispMinor;
    ALOG1("@%s: version infor css.major:%d, minor:%d, isp.major:%d, isp.minor:%d",
        __FUNCTION__,
        iaAcc.version_css.major, iaAcc.version_css.minor,
        iaAcc.version_isp.major, iaAcc.version_isp.minor);

    iaAcc.isp               = getHwHandler();
    iaAcc.open_firmware     = open_firmware;
    iaAcc.load_firmware     = load_firmware;
    iaAcc.unload_firmware   = unload_firmware;
    iaAcc.set_firmware_arg  = set_firmware_arg;
    iaAcc.start_firmware    = start_firmware;
    iaAcc.wait_for_firmware = wait_for_firmware;
    iaAcc.abort_firmware    = abort_firmware;

    /* Differentiate between CSS 1.5 and CSS 1.0.
     * If Acceleration API v1.5 specific functions stay NULL,
     * then Acceleration API v1.0 shall be called. */
    if ((ispMajor*10 + ispMinor) > 10) {
        iaAcc.map_firmware_arg   = map_firmware_arg;
        iaAcc.unmap_firmware_arg = unmap_firmware_arg;
        iaAcc.set_mapped_arg     = set_mapped_arg;
    }
    else {
        iaAcc.map_firmware_arg   = NULL;
        iaAcc.unmap_firmware_arg = NULL;
        iaAcc.set_mapped_arg     = NULL;
    }

    iaAcc.set_stage_state   = set_stage_state;
    iaAcc.wait_stage_update = wait_stage_update;
    iaAcc.load_firmware_ext = load_firmware_ext;

    return 0;
}

int AccService::CPInit()
{
    ALOG1("%s", __FUNCTION__);

    CPEngine* cpe = NULL;
    cpe = (CPEngine*)calloc(1, sizeof(CPEngine));
    if (NULL == cpe) {
        ALOGE("@%s, allocate memory for cpe fail", __FUNCTION__);
        return -1;
    }
    cpe->iaEnv.vdebug = vdebug;
    cpe->iaEnv.verror = verror;
    cpe->iaEnv.vinfo = vinfo;
    int ret = registerAccCallback(cpe->iaAcc);
    ia_cp_init(&cpe->pIaCpContext, &cpe->iaAcc, &cpe->iaEnv, NULL);
    ALOG1("ia cp context = %p, ret:%d", cpe->pIaCpContext, ret);

    mCpe = cpe;
    return ret;
}

void AccService::CPUnInit()
{
    ALOG1("%s", __FUNCTION__);
    CPHdrUnInit();
    CPUllUnInit();

    if (mCpe != NULL) {
        if (mCpe->pIaCpContext != NULL) {
            ia_cp_uninit(mCpe->pIaCpContext);
            mCpe->pIaCpContext = NULL;
        }

        free(mCpe);
    }
}

int AccService::CPHdrInit(int width, int height)
{
    ALOG1("%s", __FUNCTION__);
    ia_cp_context* ctx = getCpContext(mCpe);
    ia_cp_hdr* hdr = getCpHDR(mCpe);
    if (mCpe == NULL || hdr != NULL) {
        ALOGE("CPEngine(%p) CpContext(%p) CpHDR(%p)", mCpe, ctx, hdr);
        return -1;
    }

    ia_binary_data aiqb_data;
    ia_cp_target tgt = ia_cp_tgt_ipu;
    memset(&aiqb_data, 0, sizeof(ia_binary_data));
    ALOG1("blend option : data size = %d, target = %d",aiqb_data.size, tgt);

    ia_err err = ia_cp_hdr_init(&mCpe->pIaCpHdr, ctx, width, height, &aiqb_data, tgt);
    ALOG1("ret = %d", err);
    return (err != ia_err_none) ? -1 : 0;
}

int AccService::CPHdrUnInit()
{
    ALOG1("%s", __FUNCTION__);
    ia_err ret = ia_err_general;
    if (mCpe != NULL && mCpe->pIaCpHdr != NULL) {
        ret = ia_cp_hdr_uninit(mCpe->pIaCpHdr);
        mCpe->pIaCpHdr = NULL;
        ALOG1("ret = %d", ret);
    } else {
        return -1;
    }

    return (ret != ia_err_none) ? -1 : 0;
}

int AccService::CPHdrCompose(const int inImageNum, const acc_ia_frame* inBuf, const acc_ia_frame* inPvBuf, acc_ia_frame* outBuf, acc_ia_frame* outPvBuf)
{
    ALOG1("@%s, line:%d", __FUNCTION__, __LINE__);
    nsecs_t startTime = systemTime();

    ia_cp_hdr* pIaCpHdr = getCpHDR(mCpe);
    if (pIaCpHdr == NULL) {
        ALOGE("cp_hdr is NULL!");
        return -1;
    }

    if (!inImageNum) {
        ALOGE("inImageNum is 0");
        return -1;
    }
    ALOG1("number of input images = %d", inImageNum);

    ia_frame out;
    ia_frame out_pv;
    ia_frame in[inImageNum];
    ia_frame in_pv[inImageNum];
    ia_cp_hdr_cfg cfg;
    sp<IMemoryHeap> heap;
#if DUMP_INPUT_FRAME
    char filename[255];
    memset(filename, 0, sizeof(filename));
#endif
    memset(&cfg, 0, sizeof(ia_cp_hdr_cfg));

    for(int i = 0 ; i < inImageNum ; i++) {
        heap = inBuf[i].data->getMemory();
        in[i].data = heap->base();
        in[i].size = inBuf[i].size;
        in[i].width = inBuf[i].width;
        in[i].height = inBuf[i].height;
        in[i].format = inBuf[i].format;
        in[i].stride = inBuf[i].stride;
        in[i].rotation = inBuf[i].rotation;

#if DUMP_INPUT_FRAME
        sprintf(filename, "%s/in_frame_%d_%dx%d.yuv", DUMP_PATH, i, in[i].width, in[i].height);
        debugDumpData(filename, (unsigned char*)in[i].data, in[i].size);
#endif
        heap = inPvBuf[i].data->getMemory();
        in_pv[i].data = heap->base();
        in_pv[i].size = inPvBuf[i].size;
        in_pv[i].width = inPvBuf[i].width;
        in_pv[i].height = inPvBuf[i].height;
        in_pv[i].format = inPvBuf[i].format;
        in_pv[i].stride = inPvBuf[i].stride;
        in_pv[i].rotation = inPvBuf[i].rotation;
#if DUMP_INPUT_FRAME
        sprintf(filename, "%s/in_pv_frame_%d_%dx%d.yuv", DUMP_PATH, i, in_pv[i].width, in_pv[i].height);
        debugDumpData(filename, (unsigned char*)in_pv[i].data, in_pv[i].size);
#endif
    }

    heap = outBuf->data->getMemory();
    out.data = heap->base();
    out.size = outBuf->size;
    out.width = outBuf->width;
    out.height = outBuf->height;
    out.format = outBuf->format;
    out.stride = outBuf->stride;
    out.rotation = outBuf->rotation;

    heap = outPvBuf->data->getMemory();
    out_pv.data = heap->base();
    out_pv.size = outPvBuf->size;
    out_pv.width = outPvBuf->width;
    out_pv.height = outPvBuf->height;
    out_pv.format = outPvBuf->format;
    out_pv.stride = outPvBuf->stride;
    out_pv.rotation = outPvBuf->rotation;

    ia_err ret = ia_cp_hdr_compose(pIaCpHdr, &out, &out_pv, in, in_pv, inImageNum, &cfg);
    ALOG1("hdr composed ret = %d", ret);
    if (ret != ia_err_none) {
        return -1;
    }

#if DUMP_OUTPUT_FRAME
    sprintf(filename, "%s/out_frame_%dx%d.yuv", DUMP_PATH, out.width, out.height);
    debugDumpData(filename, (unsigned char*)out.data, out.size);
    sprintf(filename, "%s/out_pv_frame_%dx%d.yuv", DUMP_PATH, out_pv.width, out_pv.height);
    debugDumpData(filename, (unsigned char*)out_pv.data, out_pv.size);
#endif

    ALOG1("@%s, hdr post process done, output %d x %d and total data size = %d, consume:%ums",
        __FUNCTION__, out.width, out.height, out.size, (unsigned)((systemTime() - startTime) / 1000000));
    return 0;
}

int AccService::CPUllInit(int width, int height)
{
    ALOG1("%s", __FUNCTION__);
    ia_cp_context* ctx = getCpContext(mCpe);
    ia_cp_ull* ull = getCpULL(mCpe);
    ia_err err = ia_err_none;
    if (mCpe == NULL || ctx == NULL || ull != NULL) {
        ALOGE("CPEngine(%p) CpContext(%p) CpUll(%p)", mCpe, ctx, ull);
        return -1;
    }

    ia_binary_data aiqb_data;
    ia_cp_target tgt = ia_cp_tgt_ipu;
    memset(&aiqb_data, 0, sizeof(ia_binary_data));
    err = ia_cp_load_extensions(ctx);
    if (err != ia_err_none) {
        ALOGE("load extension err = %d", err);
        return err;
    }
    err = ia_cp_ull_init(&mCpe->pIaCpUll, ctx, width, height, &aiqb_data, tgt);
    ALOG1("ull init ret = %d", err);

    return err;
}

int AccService::CPUllUnInit()
{
    ALOG1("%s", __FUNCTION__);
    ia_err ret = ia_err_general;
    ia_cp_context* ctx = getCpContext(mCpe);
    if (mCpe != NULL && mCpe->pIaCpUll != NULL) {
        if (ctx != NULL) {
            ret = ia_cp_unload_extensions(ctx);
            ALOG1("unload extension ret = %d", ret);
        }
        ret = ia_cp_ull_uninit(mCpe->pIaCpUll);
        mCpe->pIaCpUll = NULL;
        ALOG1("ull uninit ret = %d", ret);
    } else {
        return -1;
    }

    return (ret != ia_err_none) ? -1 : 0;
}

int AccService::CPUllCompose(const int inImageNum, const acc_ia_frame* inBuf, const acc_ia_frame* inPvBuf, acc_ia_frame* outBuf, acc_ia_frame* outPvBuf, ia_cp_ull_cfg* cfg)
{
    ALOG1("@%s, line:%d", __FUNCTION__, __LINE__);
    nsecs_t startTime = systemTime();

    ia_cp_ull* pIaCpUll = getCpULL(mCpe);
    if (pIaCpUll == NULL) {
        ALOGE("cp_ull is NULL!");
        return -1;
    }

    if (!inImageNum) {
        ALOGE("inImageNum is 0");
        return -1;
    }
    ALOG1("number of input images = %d", inImageNum);

    ia_frame out;
    ia_frame out_pv;
    ia_frame in[inImageNum];
    ia_frame in_pv[inImageNum];
    sp<IMemoryHeap> heap;

#if DUMP_INPUT_FRAME
    char filename[255];
    memset(filename, 0, sizeof(filename));
#endif

    for(int i = 0 ; i < inImageNum ; i++) {
        heap = inBuf[i].data->getMemory();
        in[i].data = heap->base();
        in[i].size = inBuf[i].size;
        in[i].width = inBuf[i].width;
        in[i].height = inBuf[i].height;
        in[i].format = inBuf[i].format;
        in[i].stride = inBuf[i].stride;
        in[i].rotation = inBuf[i].rotation;

#if DUMP_INPUT_FRAME
        sprintf(filename, "%s/in_frame_%d_%dx%d.yuv", DUMP_PATH, i, in[i].width, in[i].height);
        debugDumpData(filename, (unsigned char*)in[i].data, in[i].size);
#endif
        heap = inPvBuf[i].data->getMemory();
        in_pv[i].data = heap->base();
        in_pv[i].size = inPvBuf[i].size;
        in_pv[i].width = inPvBuf[i].width;
        in_pv[i].height = inPvBuf[i].height;
        in_pv[i].format = inPvBuf[i].format;
        in_pv[i].stride = inPvBuf[i].stride;
        in_pv[i].rotation = inPvBuf[i].rotation;
#if DUMP_INPUT_FRAME
        sprintf(filename, "%s/in_pv_frame_%d_%dx%d.yuv", DUMP_PATH, i, in_pv[i].width, in_pv[i].height);
        debugDumpData(filename, (unsigned char*)in_pv[i].data, in_pv[i].size);
#endif
    }

    heap = outBuf->data->getMemory();
    out.data = heap->base();
    out.size = outBuf->size;
    out.width = outBuf->width;
    out.height = outBuf->height;
    out.format = outBuf->format;
    out.stride = outBuf->stride;
    out.rotation = outBuf->rotation;

    heap = outPvBuf->data->getMemory();
    out_pv.data = heap->base();
    out_pv.size = outPvBuf->size;
    out_pv.width = outPvBuf->width;
    out_pv.height = outPvBuf->height;
    out_pv.format = outPvBuf->format;
    out_pv.stride = outPvBuf->stride;
    out_pv.rotation = outPvBuf->rotation;

    ALOG1("cfg.exposure.analog_gain = %f", cfg->exposure.analog_gain);
    ALOG1("cfg.exposure.aperture_fn = %f", cfg->exposure.aperture_fn);
    ALOG1("cfg.exposure.digital_gain = %f", cfg->exposure.digital_gain);
    ALOG1("cfg.exposure.exposure_time_us = %d", cfg->exposure.exposure_time_us);
    ALOG1("cfg.exposure.iso = %d", cfg->exposure.iso);
    ALOG1("cfg.exposure.nd_filter_enabled = %d", cfg->exposure.nd_filter_enabled);
    ALOG1("cfg.exposure.total_target_exposure = %d", cfg->exposure.total_target_exposure);
    ALOG1("cfg.zoom_factor = %d", cfg->zoom_factor);
    cfg->imreg_fallback = NULL;

    ia_err ret = ia_cp_ull_compose(pIaCpUll, &out, &out_pv, in, in_pv, inImageNum, cfg);
    ALOG1("ull composed ret = %d", ret);
    if (ret != ia_err_none) {
        return -1;
    }

    ALOGD("@%s, ull post process done, output %d x %d and total data size = %d, consume:%ums",
        __FUNCTION__, out.width, out.height, out.size, (unsigned)((systemTime() - startTime) / 1000000));
    return 0;
}

ia_cp_context* AccService::getCpContext(CPEngine* instance)
{
    if (instance != NULL) {
        return instance->pIaCpContext;
    } else {
        return NULL;
    }
}

ia_cp_hdr* AccService::getCpHDR(CPEngine* instance)
{
    if (instance != NULL) {
        return instance->pIaCpHdr;
    } else {
        return NULL;
    }
}

ia_cp_ull* AccService::getCpULL(CPEngine* instance)
{
    if (instance != NULL) {
        return instance->pIaCpUll;
    } else {
        return NULL;
    }
}

void AccService::debugDumpData(const char* filename, unsigned char* data, int bytes)
{
    ALOG1("dumping data %p -> %s", data, filename);
    FILE *fp = NULL;
    size_t ret;

    if (NULL == filename || NULL == data || 0 >= bytes) {
        ALOGE("@%s, data is null or the bytes is 0", __FUNCTION__);
        return;
    }

    fp = fopen(filename, "w+");
    if (fp) {
        ret = fwrite(data, sizeof(unsigned char), bytes, fp);
        fclose(fp);
    }
}

} // namespace android
