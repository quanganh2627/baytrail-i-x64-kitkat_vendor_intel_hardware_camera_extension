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
#ifdef PLATFORM_ACC_SUPPORT
#include <utils/RefBase.h>
#include <binder/IMemory.h>
#include <binder/MemoryHeapBase.h>
#include <binder/MemoryBase.h>
#include <stdlib.h>
#include <sys/time.h>
#include <utils/Timers.h>
#include <string.h>

#include "CpIpuUtil.h"
#include "CpUtil.h"
#include "CpMemUtil.h"
#include "AccWrapper.h"
#include "cp_jni_common.h"
#include "cp_ipu_jni.h"


#define DUMP_INPUT_FRAME   0
const char DUMP_DATA_DIR[] = "/data/misc/media";

bool CP_ipu_getAvailableAccTarget()
{
    LOGI("%s", __FUNCTION__);
    // to connect ACC client, if it returns NO_ERROR,
    // the ACC service is supported,
    // or the ACC service isn't supported.
    // then disconect ACC client because it just
    // query the ACC capability
    status_t ret = initAcc();
    if (ret != NO_ERROR)
        return false;
    uninitAcc();
    return true;
}

jint CP_ipu_getHdrAccMode()
{
    LOGI("%s", __FUNCTION__);

    return ACC_STANDALONE;
}

jint CP_ipu_getUllAccMode()
{
    LOGI("%s", __FUNCTION__);

    return ACC_EXTENSION;
}

jlong CP_ipu_init(JNIEnv* env, jobject thiz)
{
    LOGI("%s", __FUNCTION__);

    CPEngine* cpe = (CPEngine*)calloc(1, sizeof(CPEngine));
    if (cpe == NULL) {
        LOGE("create CPEngine failed");
        return (jlong) NULL;
    }

    cpe->target = ia_cp_tgt_ipu;
    cpe->iaEnv.vdebug = vdebug;
    cpe->iaEnv.verror = verror;
    cpe->iaEnv.vinfo = vinfo;
    status_t ret = initAcc();
    if (ret != NO_ERROR)
        return (jlong)NULL;
    CpIpuInit();

    return (jlong)cpe;
}

void CP_ipu_uninit(JNIEnv* env, jobject thiz, jlong instance)
{
    LOGI("%s", __FUNCTION__);
    CpIpuHdrUnInit();
    CpIpuUllUnInit();
    CpIpuUnInit();
    if (instance != 0) {
        CPEngine *cpe = (CPEngine*)instance;
        free(cpe);
    }
    uninitAcc();
}

jint CP_ipu_hdrInit(JNIEnv* env, jobject thiz, jlong instance, jint width, jint height, jobject jBlendOption)
{
    LOGI("%s", __FUNCTION__);
    ia_err err = (ia_err)CpIpuHdrInit(width, height);

    LOGD("ret = %d", err);
    return err;
}

jint CP_ipu_hdrUninit(JNIEnv* env, jobject thiz, jlong instance)
{
    LOGI("%s", __FUNCTION__);
    ia_err ret = (ia_err)CpIpuHdrUnInit();
    return ret;
}

jobject CP_ipu_hdrCompose(JNIEnv* env, jobject thiz, jlong instance, jobjectArray jInputIaFrames, jobject jHdrOption)
{
    LOGI("%s", __FUNCTION__);

    if (jInputIaFrames == NULL) {
        LOGE("input IaFrame is NULL!");
        return NULL;
    }

    int numOfImages = env->GetArrayLength(jInputIaFrames);
    LOGD("number of input images = %d",numOfImages);
    acc_ia_frame out;
    acc_ia_frame out_pv;
    acc_ia_frame in[numOfImages];
    acc_ia_frame in_pv[numOfImages];
    ia_cp_hdr_cfg cfg;
    int i;
    int pv_w = 0;
    int pv_h = 0;
    nsecs_t startTime = systemTime();
#if DUMP_INPUT_FRAME
    char filename[255];
    memset(filename, 0, sizeof(filename));
#endif //DUMP_INPUT_FRAME
    memset(&cfg, 0, sizeof(ia_cp_hdr_cfg));

    for(i = 0 ; i < numOfImages ; i++)
    {
        jobject jIaFrame = env->GetObjectArrayElement(jInputIaFrames, i);
        // get the width and height
        int stride = getValueInt(env, jIaFrame, "stride");
        int width = getValueInt(env, jIaFrame, "width");
        int height = getValueInt(env, jIaFrame, "height");
        int degree = getValueInt(env, jIaFrame, "degree");
        // allocate buffer
        create_ia_frame(&in[i], ia_frame_format_nv12, stride, width, height, degree);
        // read input frame to the buffer
        sp<IMemoryHeap> heapInBuf = in[i].data->getMemory();
        void *data = heapInBuf->base();
        copyValueByteArray(env,(unsigned char*)data ,jIaFrame,"imageData", ia_cp_tgt_ipu);
#if DUMP_INPUT_FRAME
        sprintf(filename, "%s/in_frame_%d_%dx%d.yuv", DUMP_DATA_DIR, i, in[i].width, in[i].height);
        debugDumpData(filename, (unsigned char*)data, in[i].size);
#endif //DUMP_INPUT_FRAME
        if (pv_w == 0 && pv_h == 0) {
            pv_w = in[i].width / 10;
            pv_h = in[i].height / 10;
            LOGD("postview target size = %d x %d", pv_w, pv_h);
        }
        create_ia_frame(&in_pv[i], ia_frame_format_nv12, pv_w, pv_w, pv_h, 0);
        downscaleAccFrame(&in[i], &in_pv[i]);
#if DUMP_INPUT_FRAME
        sprintf(filename, "%s/in_pv_frame_%d_%dx%d.yuv", DUMP_DATA_DIR, i, in_pv[i].width, in_pv[i].height);
        sp<IMemoryHeap> heapInPvBuf = in_pv[i].data->getMemory();
        void *inpvdata = heapInPvBuf->base();
        debugDumpData(filename, (unsigned char*)inpvdata, in_pv[i].size);
#endif //DUMP_INPUT_FRAME
        }

    create_ia_frame(&out, ia_frame_format_nv12, in[0].stride, in[0].width, in[0].height, in[0].rotation);
    create_ia_frame(&out_pv, ia_frame_format_nv12, in_pv[0].stride, in_pv[0].width, in_pv[0].height, in_pv[0].rotation);

    cfg.gamma_lut.gamma_lut_size = getValueInt(env, jHdrOption, "gammaLutSize");
    if (cfg.gamma_lut.r_gamma_lut)
        copyValueCharArray(env, jHdrOption, "rGammaLut", cfg.gamma_lut.r_gamma_lut);
    if (cfg.gamma_lut.g_gamma_lut)
        copyValueCharArray(env, jHdrOption, "gGammaLut", cfg.gamma_lut.g_gamma_lut);
    if (cfg.gamma_lut.b_gamma_lut)
        copyValueCharArray(env, jHdrOption, "bGammaLut", cfg.gamma_lut.b_gamma_lut);
    LOGD("start compose");

    ia_err ret = (ia_err)CpIpuHdrCompose(numOfImages, in, in_pv, &out, &out_pv);
    LOGD("hdr composed ret = %d", ret);
    if (ret != ia_err_none) {
        return NULL;
    }

    LOGD("output %d x %d and total data size = %d", out.width, out.height, out.size);
    jobject jOutputIaFrame = createIaFrame(env, &out);
    for (i = 0 ; i < numOfImages ; i++) {
        destroy_ia_frame(&in[i]);
        destroy_ia_frame(&in_pv[i]);
    }
    destroy_ia_frame(&out);
    destroy_ia_frame(&out_pv);
    LOGD("hdr post process done,cost %dus",(unsigned)((systemTime() - startTime) / 1000));
    return jOutputIaFrame;
}

jint CP_ipu_ullInit(JNIEnv* env, jobject thiz, jlong instance, jint width, jint height, jobject jBlendOption)
{
    LOGI("%s", __FUNCTION__);
    ia_binary_data aiqb_data;
    int tgt = 0;
    ia_err err = (ia_err)CpIpuUllInit(width, height);
    LOGD("ret = %d", err);
    return err;
}

jint CP_ipu_ullUninit(JNIEnv* env, jobject thiz, jlong instance)
{
    LOGI("%s", __FUNCTION__);
    ia_err ret =  (ia_err)CpIpuUllUnInit();
    return ret;
}

jobject CP_ipu_ullCompose(JNIEnv* env, jobject thiz, jlong instance, jobjectArray jInputIaFrames, jobject jUllOption)
{
     LOGI("%s", __FUNCTION__);

     if (jInputIaFrames == NULL) {
        LOGE("input IaFrame is NULL!");
        return NULL;
     }

     int numOfImages = env->GetArrayLength(jInputIaFrames);
     LOGD("number of input images = %d",numOfImages);
     acc_ia_frame out;
     acc_ia_frame out_pv;
     acc_ia_frame in[numOfImages];
     acc_ia_frame in_pv[numOfImages];
     ia_cp_ull_cfg cfg;
     int i;
     int pv_w = 0;
     int pv_h = 0;
     memset(&cfg, 0, sizeof(ia_cp_ull_cfg));
     nsecs_t startTime = systemTime();
#if DUMP_INPUT_FRAME
     char filename[255];
     memset(filename, 0, sizeof(filename));
#endif //DUMP_INPUT_FRAME
     for (i = 0 ; i < numOfImages ; i++) {
        jobject jIaFrame = env->GetObjectArrayElement(jInputIaFrames, i);
         // get the width and height
        int stride = getValueInt(env, jIaFrame, "stride");
        int width = getValueInt(env, jIaFrame, "width");
        int height = getValueInt(env, jIaFrame, "height");
        int degree = getValueInt(env, jIaFrame, "degree");
        // allocate buffer
        create_ia_frame(&in[i], ia_frame_format_nv12, stride, width, height, degree);
        // read input frame to the buffer
        sp<IMemoryHeap> heapInBuf = in[i].data->getMemory();
        void *data = heapInBuf->base();
        copyValueByteArray(env,(unsigned char*)data ,jIaFrame,"imageData", ia_cp_tgt_ipu);
         //convert(env, &in[i], jIaFrame);
         if (pv_w == 0 && pv_h == 0) {
             pv_w = in[i].width / 10;
             pv_h = in[i].height / 10;
             LOGD("postview target size = %d x %d", pv_w, pv_h);
         }
#if DUMP_INPUT_FRAME
         sprintf(filename, "%s/in_frame_%d_%dx%d.yuv", DUMP_DATA_DIR, i, in[i].width, in[i].height);
         debugDumpData(filename, (unsigned char*)data, in[i].size);
#endif //DUMP_INPUT_FRAME
         create_ia_frame(&in_pv[i], ia_frame_format_nv12, pv_w, pv_w, pv_h, 0);
         downscaleAccFrame(&in[i], &in_pv[i]);
#if DUMP_INPUT_FRAME
         sprintf(filename, "%s/in_pv_frame_%d_%dx%d.yuv", DUMP_DATA_DIR, i, in_pv[i].width, in_pv[i].height);
         sp<IMemoryHeap> heapInPvBuf = in_pv[i].data->getMemory();
         void *inpvdata = heapInPvBuf->base();
         debugDumpData(filename, (unsigned char*)inpvdata, in_pv[i].size);
#endif //DUMP_INPUT_FRAME
     }

     create_ia_frame(&out, ia_frame_format_nv12, in[0].stride, in[0].width, in[0].height, in[0].rotation);
     create_ia_frame(&out_pv, ia_frame_format_nv12, in_pv[0].stride, in_pv[0].width, in_pv[0].height, in_pv[0].rotation);

     cfg.exposure.analog_gain = getValueFloat(env, jUllOption, "analogGain");
     LOGD("cfg.exposure.analog_gain = %f", cfg.exposure.analog_gain);
     cfg.exposure.aperture_fn = getValueFloat(env, jUllOption, "aperture");
     LOGD("cfg.exposure.aperture_fn = %f", cfg.exposure.aperture_fn);
     cfg.exposure.digital_gain = getValueFloat(env, jUllOption, "digitalGain");
     LOGD("cfg.exposure.digital_gain = %f", cfg.exposure.digital_gain);
     cfg.exposure.exposure_time_us = getValueInt(env, jUllOption, "exposureTime");
     LOGD("cfg.exposure.exposure_time_us = %d", cfg.exposure.exposure_time_us);
     cfg.exposure.iso = getValueInt(env, jUllOption, "iso");
     LOGD("cfg.exposure.iso = %d", cfg.exposure.iso);
     cfg.exposure.nd_filter_enabled = getValueBoolean(env, jUllOption, "enabledNdFilter");
     LOGD("cfg.exposure.nd_filter_enabled = %d", cfg.exposure.nd_filter_enabled);
     cfg.exposure.total_target_exposure = getValueInt(env, jUllOption, "totalExposure");
     LOGD("cfg.exposure.total_target_exposure = %d", cfg.exposure.total_target_exposure);
     cfg.imreg_fallback = NULL;
     cfg.zoom_factor = getValueInt(env, jUllOption, "zoomFactor");
     LOGD("cfg.zoom_factor = %d", cfg.zoom_factor);

     ia_err ret = (ia_err)CpIpuUllCompose(numOfImages, in, in_pv, &out, &out_pv, &cfg);
     LOGD("ull composed ret = %d", ret);
     if (ret != ia_err_none) {
         return NULL;
     }

     LOGD("output %d x %d and total data size = %d", out.width, out.height, out.size);
     jobject jOutputIaFrame = createIaFrame(env, &out);
     for (i = 0 ; i < numOfImages ; i++) {
          destroy_ia_frame(&in[i]);
          destroy_ia_frame(&in_pv[i]);
     }
     destroy_ia_frame(&out);
     destroy_ia_frame(&out_pv);
     LOGD("hdr post process done,cost %dus",(unsigned)((systemTime() - startTime) / 1000));
     return jOutputIaFrame;
}

#endif
