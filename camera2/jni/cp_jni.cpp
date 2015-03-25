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
#include "CpUtil.h"
#include "ia_cp_types.h"
#include "ia_cp.h"
#include "ia_aiq.h"

#define DUMP_INPUT_FRAME	0
const char SDCARD_DIR[] = "/sdcard";

struct CPEngine {
    ia_cp_context* pIaCpContext;
    ia_acceleration iaAcc;
    ia_env iaEnv;
    ia_cp_hdr* pIaCpHdr;
    ia_cp_ull* pIaCpUll;
};

ia_cp_target tgt_lut[] = {
		ia_cp_tgt_ia,        /**< Intel Architecture (IA) host */
		ia_cp_tgt_ipu,       /**< Image Processing Unit */
		ia_cp_tgt_gpu,       /**< Graphics Processing Unit */
		ia_cp_tgt_ate,       /**< ATE C bitexact reference model */
		ia_cp_tgt_ref
};

static CPEngine* getCpEngine(jlong instance);
static ia_cp_context* getCpContext(jlong instance);
static ia_cp_hdr* getCpHDR(jlong instance);
static ia_cp_ull* getCpULL(jlong instance);

jint CP_hdrUninit(JNIEnv* env, jobject thiz, jlong instance);
jint CP_ullUninit(JNIEnv* env, jobject thiz, jlong instance);

//uint16_t fwd_gamma_lut[1024] = {0,};

jlong CP_init(JNIEnv* env, jobject thiz)
{
	LOGI("%s", __FUNCTION__);
    CPEngine* cpe = (CPEngine*)calloc(1, sizeof(CPEngine));

	cpe->iaEnv.vdebug = vdebug;
	cpe->iaEnv.verror = verror;
	cpe->iaEnv.vinfo = vinfo;

	ia_cp_init(&cpe->pIaCpContext, &cpe->iaAcc, &cpe->iaEnv, NULL);
	LOGD("ia cp context = %p", cpe->pIaCpContext);

    return (jlong)cpe;
}

void CP_uninit(JNIEnv* env, jobject thiz, jlong instance)
{
	LOGI("%s", __FUNCTION__);
    CP_hdrUninit(env, thiz, instance);
    CP_ullUninit(env, thiz, instance);

    CPEngine* cpe = getCpEngine(instance);
    if (cpe != NULL) {
        if (cpe->pIaCpContext != NULL) {
            ia_cp_uninit(cpe->pIaCpContext);
            cpe->pIaCpContext = NULL;
        }

        free(cpe);
    }
}

jint CP_hdrInit(JNIEnv* env, jobject thiz, jlong instance, jint width, jint height, jobject jBlendOption)
{
	LOGI("%s", __FUNCTION__);
    CPEngine* cpe = getCpEngine(instance);
    ia_cp_context* ctx = getCpContext(instance);
    ia_cp_hdr* hdr = getCpHDR(instance);
    if (cpe == NULL || hdr != NULL) {
        LOGE("CPEngine(0x%08x) CpContext(0x%08x) CpHDR(0x%08x)", (uint32_t)cpe, (uint32_t)ctx, (uint32_t)hdr);
        return ia_err_general;
    }

	ia_binary_data aiqb_data;
	int tgt = 0;
	memset(&aiqb_data, 0, sizeof(ia_binary_data));
	if (jBlendOption != NULL) {
		copyValueByteArray(env, (unsigned char*)aiqb_data.data, jBlendOption, "binaryData");
		aiqb_data.size = getValueInt(env, jBlendOption, "binarySize");
		tgt = getValueInt(env, jBlendOption, "target");
		LOGD("blend option : data size = %d, target = %d",aiqb_data.size, tgt);
	} else {
		LOGE("blend option is null!");
	}
	ia_err err = ia_cp_hdr_init(&cpe->pIaCpHdr, ctx, width, height, &aiqb_data, tgt_lut[tgt]);
//	int i;
//	for (i = 0 ; i < 1024 ; i++)
//	{
//		fwd_gamma_lut[i] = i;
//	}
	LOGD("ret = %d", err);
	return err;
}

jint CP_hdrUninit(JNIEnv* env, jobject thiz, jlong instance)
{
	LOGI("%s", __FUNCTION__);
    CPEngine* cpe = getCpEngine(instance);
    if (cpe != NULL && cpe->pIaCpHdr != NULL) {
	    ia_err ret = ia_cp_hdr_uninit(cpe->pIaCpHdr);
        cpe->pIaCpHdr = NULL;
        return ret;
    } else {
        return ia_err_general;
    }
}

jobject CP_hdrCompose(JNIEnv* env, jobject thiz, jlong instance, jobjectArray jInputIaFrames, jobject jHdrOption)
{
	LOGI("%s", __FUNCTION__);
    ia_cp_hdr* pIaCpHdr = getCpHDR(instance);
	if (pIaCpHdr == NULL) {
		LOGE("cp_hdr is NULL!");
		return NULL;
	}

	if (jInputIaFrames == NULL) {
		LOGE("input IaFrame is NULL!");
		return NULL;
	}

	int numOfImages = env->GetArrayLength(jInputIaFrames);
	LOGD("number of input images = %d",numOfImages);
	ia_frame out;
	ia_frame out_pv;
	ia_frame in[numOfImages];
	ia_frame in_pv[numOfImages];
	ia_cp_hdr_cfg cfg;
	int i;
	int pv_w = 0;
	int pv_h = 0;
#if DUMP_INPUT_FRAME
	char filename[255];
	memset(filename, 0, sizeof(filename));
#endif //DUMP_INPUT_FRAME
	memset(&cfg, 0, sizeof(ia_cp_hdr_cfg));

	for(i = 0 ; i < numOfImages ; i++)
	{
		jobject jIaFrame = env->GetObjectArrayElement(jInputIaFrames, i);
		convert(env, &in[i], jIaFrame);
#if DUMP_INPUT_FRAME
		sprintf(filename, "%s/in_frame_%d_%dx%d.yuv", SDCARD_DIR, i, in[i].width, in[i].height);
		debugDumpData(filename, (unsigned char*)in[i].data, in[i].size);
#endif //DUMP_INPUT_FRAME
		if (pv_w == 0 && pv_h == 0) {
			pv_w = in[i].width / 10;
			pv_h = in[i].height / 10;
			LOGD("postview target size = %d x %d", pv_w, pv_h);
		}
		create_ia_frame(&in_pv[i], ia_frame_format_nv12, pv_w, pv_w, pv_h, 0);
		downscaleFrame(&in[i], &in_pv[i]);
#if DUMP_INPUT_FRAME
		sprintf(filename, "%s/in_pv_frame_%d_%dx%d.yuv", SDCARD_DIR, i, in_pv[i].width, in_pv[i].height);
		debugDumpData(filename, (unsigned char*)in_pv[i].data, in_pv[i].size);
#endif //DUMP_INPUT_FRAME
	}

	create_ia_frame(&out, ia_frame_format_nv12, in[0].stride, in[0].width, in[0].height, in[0].rotation);
	create_ia_frame(&out_pv, ia_frame_format_nv12, in_pv[0].stride, in_pv[0].width, in_pv[0].height, in_pv[0].rotation);

	cfg.gamma_lut.gamma_lut_size = getValueInt(env, jHdrOption, "gammaLutSize");
	copyValueCharArray(env, jHdrOption, "rGammaLut", cfg.gamma_lut.r_gamma_lut);
	copyValueCharArray(env, jHdrOption, "gGammaLut", cfg.gamma_lut.g_gamma_lut);
	copyValueCharArray(env, jHdrOption, "bGammaLut", cfg.gamma_lut.b_gamma_lut);
#if 0   // removed.
	cfg.gbce.ctc_gains_lut_size = getValueInt(env, jHdrOption, "ctcGainLutSize");
	copyValueCharArray(env, jHdrOption, "ctcGainLut", cfg.gbce.ctc_gains_lut);
#endif

//	printFrameInfo(&out);
//	printFrameInfo(&out_pv);

	ia_err ret = ia_cp_hdr_compose(pIaCpHdr, &out, &out_pv, in, in_pv, numOfImages, &cfg);
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
	LOGD("hdr post process done");
	return jOutputIaFrame;
}

jint CP_ullInit(JNIEnv* env, jobject thiz, jlong instance, jint width, jint height, jobject jBlendOption)
{
	LOGI("%s", __FUNCTION__);
    CPEngine* cpe = getCpEngine(instance);
    ia_cp_context* ctx = getCpContext(instance);
    ia_cp_ull* ull = getCpULL(instance);
    if (cpe == NULL || ctx == NULL || ull != NULL) {
        LOGE("CPEngine(0x%08x) CpContext(0x%08x) CpUll(0x%08x)", (uint32_t)cpe, (uint32_t)ctx, (uint32_t)ull);
        return ia_err_general;
    }

	ia_binary_data aiqb_data;
	int tgt = 0;
	memset(&aiqb_data, 0, sizeof(ia_binary_data));
	if (jBlendOption != NULL) {
		copyValueByteArray(env, (unsigned char*)aiqb_data.data, jBlendOption, "binaryData");
		aiqb_data.size = getValueInt(env, jBlendOption, "binarySize");
		tgt = getValueInt(env, jBlendOption, "target");
		LOGD("blend option : data size = %d, target = %d",aiqb_data.size, tgt_lut[tgt]);
	} else {
		LOGD("blend option is null!");
	}
	ia_err err = ia_cp_ull_init(&cpe->pIaCpUll, ctx, width, height, &aiqb_data, tgt_lut[tgt]);
	LOGD("ret = %d", err);
	return err;
}

jint CP_ullUninit(JNIEnv* env, jobject thiz, jlong instance)
{
	LOGI("%s", __FUNCTION__);
    CPEngine* cpe = getCpEngine(instance);
    if (cpe != NULL && cpe->pIaCpUll != NULL) {
	    ia_err ret = ia_cp_ull_uninit(cpe->pIaCpUll);
        cpe->pIaCpUll = NULL;
        return ret;
    } else {
        return ia_err_general;
    }
}

jobject CP_ullCompose(JNIEnv* env, jobject thiz, jlong instance, jobjectArray jInputIaFrames, jobject jUllOption)
{
	LOGI("%s", __FUNCTION__);

    ia_cp_ull* pIaCpUll = getCpULL(instance);
	if (pIaCpUll == NULL) {
		LOGE("cp_ull is NULL!");
		return NULL;
	}

	if (jInputIaFrames == NULL) {
		LOGE("input IaFrame is NULL!");
		return NULL;
	}

	int numOfImages = env->GetArrayLength(jInputIaFrames);
	LOGD("number of input images = %d",numOfImages);
	ia_frame out;
	ia_frame out_pv;
	ia_frame in[numOfImages];
	ia_frame in_pv[numOfImages];
	ia_cp_ull_cfg cfg;
	int i;
	int pv_w = 0;
	int pv_h = 0;
	memset(&cfg, 0, sizeof(ia_cp_hdr_cfg));
#if DUMP_INPUT_FRAME
	char filename[255];
	memset(filename, 0, sizeof(filename));
#endif //DUMP_INPUT_FRAME
	for(i = 0 ; i < numOfImages ; i++)
	{
		jobject jIaFrame = env->GetObjectArrayElement(jInputIaFrames, i);
		convert(env, &in[i], jIaFrame);
		if (pv_w == 0 && pv_h == 0) {
			pv_w = in[i].width / 10;
			pv_h = in[i].height / 10;
			LOGD("postview target size = %d x %d", pv_w, pv_h);
		}
#if DUMP_INPUT_FRAME
		sprintf(filename, "%s/in_frame_%d_%dx%d.yuv", SDCARD_DIR, i, in[i].width, in[i].height);
		debugDumpData(filename, (unsigned char*)in[i].data, in[i].size);
#endif //DUMP_INPUT_FRAME
		create_ia_frame(&in_pv[i], ia_frame_format_nv12, pv_w, pv_w, pv_h, 0);
		downscaleFrame(&in[i], &in_pv[i]);
#if DUMP_INPUT_FRAME
		sprintf(filename, "%s/in_pv_frame_%d_%dx%d.yuv", SDCARD_DIR, i, in_pv[i].width, in_pv[i].height);
		debugDumpData(filename, (unsigned char*)in_pv[i].data, in_pv[i].size);
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

	ia_err ret = ia_cp_ull_compose(pIaCpUll, &out, &out_pv, in, in_pv, numOfImages, &cfg);
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
	LOGD("hdr post process done");
	return jOutputIaFrame;
}

// just for the debugging purpose
jobject CP_debugFrameConvert(JNIEnv* env, jobject thiz, jobject jInIaFrame)
{
	ia_frame iaFrame;
	convert(env, &iaFrame, jInIaFrame);
	return createIaFrame(env, &iaFrame);
}

static CPEngine* getCpEngine(jlong instance)
{
    if (instance != 0) {
        return (CPEngine*)instance;
    } else {
        return NULL;
    }
}

static ia_cp_context* getCpContext(jlong instance)
{
    CPEngine* cpe = getCpEngine(instance);
    if (cpe != NULL) {
        return cpe->pIaCpContext;
    } else {
        return NULL;
    }
}

static ia_cp_hdr* getCpHDR(jlong instance)
{
    CPEngine* cpe = getCpEngine(instance);
    if (cpe != NULL) {
        return cpe->pIaCpHdr;
    } else {
        return NULL;
    }
}

static ia_cp_ull* getCpULL(jlong instance)
{
    CPEngine* cpe = getCpEngine(instance);
    if (cpe != NULL) {
        return cpe->pIaCpUll;
    } else {
        return NULL;
    }
}

static JNINativeMethod gMethods[] = {
    { "init",
      "()J",
      (void*)CP_init },

    { "uninit",
      "(J)V",
      (void*)CP_uninit },

    { "hdrInit",
      "(JII" SIG_BLENDER_OPTION ")I",
      (void*)CP_hdrInit },

    { "hdrUninit",
      "(J)I",
      (void*)CP_hdrUninit },

    { "hdrCompose",
      "(J[" SIG_IAFRAME SIG_HDR_OPTION ")" SIG_IAFRAME,
      (void*)CP_hdrCompose },

    { "ullInit",
      "(JII" SIG_BLENDER_OPTION ")I",
      (void*)CP_ullInit },

    { "ullUninit",
      "(J)I",
      (void*)CP_ullUninit },

    { "ullCompose",
      "(J[" SIG_IAFRAME SIG_ULL_OPTION ")" SIG_IAFRAME,
      (void*)CP_ullCompose },
};

int register_jni_CP(JNIEnv *env)
{
    return jniRegisterNativeMethods(env, CLASS_CP, gMethods,
        sizeof(gMethods)/sizeof(JNINativeMethod));
}
