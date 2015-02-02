/*
 * Copyright 2013 The Android Open Source Project
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

//#define LOG_NDEBUG 0
#define LOG_TAG "DepthCameraCalibrationDataMap_JNI"
#include <utils/Log.h>
#include <utils/misc.h>
#include <utils/List.h>
#include <utils/String8.h>

#include <cstdio>

#include <gui/Surface.h>
#include <camera3.h>

#include <android_runtime/AndroidRuntime.h>
#include <android_runtime/android_view_Surface.h>

#include <jni.h>
#include <JNIHelp.h>

#include <stdint.h>
#include <inttypes.h>

#include "ufo/graphics.h"

#include "DSCalibRectParametersUtil.h"
#include "CalibRectParametersIO.h"
#include "DSMath.h"

#include "ds4/ds4_camera_metadata_tags.h"
#define MIN(a, b) ( ((a) < (b)) ? (a) : (b) )

// ----------------------------------------------------------------------------
#define INTEL_DEPTHCAMERA_CALIBRATION_DATA_CONTEXT_JNI_ID       "mNativeContext"

using namespace android;

static struct {
    jclass clazz;
    jmethodID ctor;
} gIntriniscParamsClassInfo;
static struct {
    jclass clazz;
    jmethodID ctor;
} gExtrinsicParamsClassInfo;
static struct {
    jclass clazz;
    jmethodID ctor;
    jmethodID setDepthIntrinsics;
    jmethodID setColorIntrinsics;
    jmethodID setAuxIntrinsics;

    jmethodID setDepthToColorExtrinsics;
    jmethodID setDepthToWorldExtrinsics;

} gCameraCalibrationDataClassInfo;

static struct {
    jfieldID mNativeContext;
} gDepthCameraCalibrationDataMapClassInfo;

class JNIDepthCameraCalibrationDataMap : public virtual RefBase
{
public:
    JNIDepthCameraCalibrationDataMap(unsigned char*  data)
    {
        ALOGV("%s", __FUNCTION__);


        if ( data != NULL)
        {
          DSCalibRectParameters*  tmp = (DSCalibRectParameters*) data;
          memcpy(&mCalibrationData, tmp, sizeof(DSCalibRectParameters));
          sprintCalibrationDebug();
        }
    }

    virtual ~JNIDepthCameraCalibrationDataMap() {}

    DSCalibRectParameters getCalibrationData()  { return mCalibrationData; }

private:

    void sprintCalibrationDebug();
    DSCalibRectParameters  mCalibrationData;
};


//#define PRINT_DEBUG
#ifdef PRINT_DEBUG
bool calibrationDataDumped = false;
void MyDumpAsTCSV (const DSCalibIntrinsicsNonRectified &cri, char*name)
{

  ALOGW("%f %s%s",cri.fx, name,"_fx");
  ALOGW("%f %s%s",cri.fy, name,"_fy");
  ALOGW("%f %s%s", cri.px, name,"_px");
  ALOGW("%f %s%s", cri.py, name,"_py");
  for ( int i=0;i<5;i++)
    ALOGW("%f %s%s%d", cri.k[i], name,"_k",i);
  ALOGW("%d %s%s", (int)cri.w, name,"_w");
  ALOGW("%d %s%s", (int)cri.h, name,"_h");
}

void MyDumpAsTCSV (const DSCalibIntrinsicsRectified &crm, char* name)
{

    ALOGW("%f %s%s",crm.rfx, name,"_rfx");
    ALOGW("%f %s%s",crm.rfy, name,"_rfy");;
    ALOGW("%f %s%s",crm.rpx, name,"_rpx");
    ALOGW("%f %s%s",crm.rpy, name,"_rpy");
    ALOGW("%d %s%s",(int)crm.rw, name,"_rw");
    ALOGW("%d %s%s",(int)crm.rh, name,"_rh");
}
#endif
void JNIDepthCameraCalibrationDataMap::sprintCalibrationDebug()
{
#ifdef PRINT_DEBUG
    if (!calibrationDataDumped )
    {
        ALOGW("JNIDepthCameraCalibrationDataMap::sprintCalibrationDebug()");
        ALOGW("%d %s",(int)mCalibrationData.versionNumber, "versionNumber");
        ALOGW("%d %s",mCalibrationData.numIntrinsicsRight, "numIntrinsicsRight");
        ALOGW("%d %s",mCalibrationData.numIntrinsicsThird, "numIntrinsicsThird");
        ALOGW("%d %s",mCalibrationData.numRectifiedModesLR, "numResolutionModesLR");
        ALOGW("%d %s",mCalibrationData.numRectifiedModesThird, "numResolutionModesThird");
        MyDumpAsTCSV( mCalibrationData.intrinsicsLeft, "intrinsicsLeft");
        for ( int i=0; i< mCalibrationData.numIntrinsicsRight; i++)
          MyDumpAsTCSV( mCalibrationData.intrinsicsRight[i], "intrinsicsRight");
        for ( int i=0; i< mCalibrationData.numRectifiedModesThird; i++)
          MyDumpAsTCSV( mCalibrationData.intrinsicsThird[i], "intrinsicsThird");
        for ( int i=0; i< mCalibrationData.numIntrinsicsRight; i++)
          for ( int j=0; j< mCalibrationData.numRectifiedModesLR; j++)
            MyDumpAsTCSV( mCalibrationData.modesLR[i][j], "modesLR");
        for ( int i=0; i< mCalibrationData.numIntrinsicsRight; i++)
          for ( int j=0; j< mCalibrationData.numIntrinsicsThird; j++)
            for ( int k=0; k< mCalibrationData.numRectifiedModesThird; k++)
                MyDumpAsTCSV( mCalibrationData.modesThird[i][j][k], "modesThird");
        for ( int i=0; i< mCalibrationData.numIntrinsicsRight; i++)
          for ( int j=0; j< 9; j++)
            ALOGW("%f %s[%d][%d]",mCalibrationData.Rleft[i][j], "Rleft", i,j);
        for ( int i=0; i< mCalibrationData.numIntrinsicsRight; i++)
                  for ( int j=0; j< 9; j++)
                    ALOGW("%f %s[%d][%d]",mCalibrationData.Rright[i][j], "Rright", i,j);
        for ( int i=0; i< mCalibrationData.numIntrinsicsRight; i++)
          for ( int j=0; j< 9; j++)
            ALOGW("%f %s[%d][%d]",mCalibrationData.Rthird[i][j], "Rthird", i,j);

        for ( int j=0; j< 3; j++)
          ALOGW("%f %s[%d]",mCalibrationData.B[j], "B", j);
        for ( int i=0; i< mCalibrationData.numIntrinsicsRight; i++)
          for ( int j=0; j< 3; j++)
            ALOGW("%f %s[%d][%d]",mCalibrationData.T[i][j], "T", i,j);
        for ( int j=0; j< 9; j++)
          ALOGW("%f %s[%d]",mCalibrationData.Rworld[j], "Rworld", j);
        for ( int j=0; j< 3; j++)
          ALOGW("%f %s[%d]",mCalibrationData.Tworld[j], "Tworld", j);

      calibrationDataDumped = true;
    }
#endif
}
// ----------------------------------------------------------------------------

extern "C" {

static void scaleRectParams(
                const DSCalibIntrinsicsNonRectified *non_rectified,
                const DSCalibIntrinsicsRectified *rectified,
                const double *rotation, unsigned int width, unsigned int height,
                DSCalibIntrinsicsNonRectified *params_non_rectified, DSCalibIntrinsicsRectified *params_rectified)
{
    int i;

    float scaler = MIN((float)non_rectified->w / width, (float)non_rectified->h / height);
    int center_x = (int)((non_rectified->w - scaler * width) / 2);
    int center_y = (int)((non_rectified->h - scaler * height) / 2);

    params_non_rectified->fx = non_rectified->fx / scaler;
    params_non_rectified->fy = non_rectified->fy / scaler;
    params_non_rectified->px = (0.5f + non_rectified->px - center_x) / scaler - 0.5f;
    params_non_rectified->py = (0.5f + non_rectified->py - center_y) / scaler - 0.5f;
    // k's are unit-less so we don't scale
    for (i = 0; i < 5; i++)
        params_non_rectified->k[i] = non_rectified->k[i];
    params_non_rectified->w = width;
    params_non_rectified->h = height;
    if ( params_rectified  == NULL )
        return;
    params_rectified->rfx = rectified->rfx / scaler;
    params_rectified->rfy = rectified->rfy / scaler;
    params_rectified->rpx = (0.5f + rectified->rpx - center_x) / scaler - 0.5f;
    params_rectified->rpy = (0.5f + rectified->rpy - center_y) / scaler - 0.5f;
    params_rectified->rw = width;
    params_rectified->rh = height;

}

static bool fillRectParams( DSCalibRectParameters* calib_params, unsigned int width, unsigned int height,
                                DSCalibIntrinsicsNonRectified *params_non_rectified, DSCalibIntrinsicsRectified *params_rectified)
{
    /*
     * DS4 is calibrated only in 2 modes: 1280x1080 and 640x480. If the DS4 is in any
     * of those 2 modes, we can just extract the calibration params from the calibration
     * data. For other resolutions we'll have to extrapolate the calibration data from the
     * entry in the calibration data with the same aspect ratio. i.e. for resolutions with
     * 16:9 aspect ratio we'll extrapolate the calibration params from the 1920x1080 entries
     * and for resolutions with 4:3 aspect ratio we'll extrapolate from the 640x480 entries.
     */
    int i,j;
    float calib_ratio;
    bool scale_rectified = true;
    // First look for an exact match resolution
    for (i = 0; i < calib_params->numIntrinsicsThird; i++)
    {
        for (j = 0; j < calib_params->numRectifiedModesThird; j++)
        {
            if ((calib_params->modesThird[0][i][j].rw == width) &&
                (calib_params->modesThird[0][i][j].rh == height))
            {

                DSCalibIntrinsicsRectified* rectified = &calib_params->modesThird[0][i][j];
                params_rectified->rfx = rectified->rfx;
                params_rectified->rfy = rectified->rfy;
                params_rectified->rpx = rectified->rpx;
                params_rectified->rpy = rectified->rpy;
                params_rectified->rw = rectified->rw;
                params_rectified->rh = rectified->rh;
                scale_rectified = false;
                break;
            }
        }
    }
    for (i = 0; i < calib_params->numIntrinsicsThird; i++) {
        if ((calib_params->intrinsicsThird[i].w == width) &&
            (calib_params->intrinsicsThird[i].h == height)) {
            DSCalibIntrinsicsNonRectified* non_rectified = &calib_params->intrinsicsThird[i];
            ALOGD("%s(): Found matching rectification data for %dx%d at entry %d",
                    __func__, width, height, i);
            params_non_rectified->fx = non_rectified->fx;
            params_non_rectified->fy = non_rectified->fy;
            params_non_rectified->px = non_rectified->px;
            params_non_rectified->py = non_rectified->py;
            for (i = 0; i < 5; i++)
                params_non_rectified->k[i] = non_rectified->k[i];
            params_non_rectified->w = non_rectified->w;
            params_non_rectified->h = non_rectified->h;
            //cannot be a case where rectified need to be scaled but not non-rectified.
            if ( scale_rectified == true )
                ALOGE("%s(): Couldn't find matching rectification data for rectified %dx%d (while for none rectified parameters were found)!!!", __func__, width, height);
            return true;
        }
    }

    /*
     * No exact match. Find a calibration resolution with the same aspect ratio
     * and extrapolate from that.
     */
    float ratio = (float)width / height;
    for (i = 0; i < calib_params->numIntrinsicsThird; i++) {
        calib_ratio = (float)calib_params->intrinsicsThird[i].w / calib_params->intrinsicsThird[i].h;
        if (calib_ratio == ratio) {
            ALOGD("%s(): Extrapolating rectification data for %dx%d from entry %d (%dx%d)",
                    __func__, width, height, i, calib_params->intrinsicsThird[i].w,
                    calib_params->intrinsicsThird[i].h);

            scaleRectParams(&calib_params->intrinsicsThird[i],
                              &calib_params->modesThird[0][i][0],
                              (const double *)&calib_params->Rthird[0],
                              width, height, params_non_rectified, scale_rectified ? params_rectified : NULL );
            return true;
        }
    }

    // No match for aspect ratio? Shouldn't happen...
    ALOGE("%s(): Couldn't find matching rectification data for %dx%d", __func__, width, height);
    return false;
}
static JNIDepthCameraCalibrationDataMap* DepthCameraCalibrationDataMap_getContext(JNIEnv* env, jobject thiz)
{
    JNIDepthCameraCalibrationDataMap *ctx = reinterpret_cast<JNIDepthCameraCalibrationDataMap *>
                    (env->GetLongField(thiz, gDepthCameraCalibrationDataMapClassInfo.mNativeContext));
    return ctx;
}

static void DepthCameraCalibrationDataMap_setNativeContext(JNIEnv* env,
        jobject thiz, sp<JNIDepthCameraCalibrationDataMap> ctx)
{
    JNIDepthCameraCalibrationDataMap* const p = DepthCameraCalibrationDataMap_getContext(env, thiz);
    if (ctx != 0) {
        ctx->incStrong((void*)DepthCameraCalibrationDataMap_setNativeContext);
    }
    if (p) {
        p->decStrong((void*)DepthCameraCalibrationDataMap_setNativeContext);
    }
    env->SetLongField(thiz, gDepthCameraCalibrationDataMapClassInfo.mNativeContext,
                  reinterpret_cast<jlong>(ctx.get()));


}
static void DepthCameraCalibrationDataMap_init(JNIEnv* env, jobject thiz,
                             jbyteArray calibdata)
{

    jclass clazz = env->GetObjectClass(thiz);
    if (clazz == NULL) {
        jniThrowRuntimeException(env, "Can't find intel/camera2/extensions/DepthCamera/DepthCameraCalibrationDataMap");
        return;
    }
    jsize len = env->GetArrayLength(calibdata);
    // Get the elements
    jbyte* calibdataByte = env->GetByteArrayElements(calibdata, 0);

    sp<JNIDepthCameraCalibrationDataMap> ctx(new JNIDepthCameraCalibrationDataMap((unsigned char*) calibdataByte));
    env->ReleaseByteArrayElements(calibdata, calibdataByte, 0);

    DepthCameraCalibrationDataMap_setNativeContext(env, thiz, ctx);

}


static void DepthCameraCalibrationDataMap_classInit(JNIEnv* env, jclass clazz)
{
    ALOGV("%s:", __FUNCTION__);

    gDepthCameraCalibrationDataMapClassInfo.mNativeContext = env->GetFieldID(
            clazz, INTEL_DEPTHCAMERA_CALIBRATION_DATA_CONTEXT_JNI_ID, "J");
    LOG_ALWAYS_FATAL_IF(gDepthCameraCalibrationDataMapClassInfo.mNativeContext == NULL,
                        "can't find com/intel/camera2/extensions/depthcamera/DepthCameraCalibrationDataMap.%s",
                        INTEL_DEPTHCAMERA_CALIBRATION_DATA_CONTEXT_JNI_ID);

    jclass intrinsicsClazz = env->FindClass("com/intel/camera2/extensions/depthcamera/DepthCameraCalibrationDataMap$IntrinsicParams");
    LOG_ALWAYS_FATAL_IF(intrinsicsClazz == NULL, "Can not find IntrinsicParams class");
    // FindClass only gives a local reference of jclass object.
    gIntriniscParamsClassInfo.clazz = (jclass) env->NewGlobalRef(intrinsicsClazz);
    gIntriniscParamsClassInfo.ctor = env->GetMethodID(gIntriniscParamsClassInfo.clazz, "<init>",
        "(Lcom/intel/camera2/extensions/depthcamera/DepthCameraCalibrationDataMap;FFFF[DIII)V");
    LOG_ALWAYS_FATAL_IF(gIntriniscParamsClassInfo.ctor == NULL,
            "Can not find IntrinsicParams constructor");
    jclass extrinsicsClazz = env->FindClass("com/intel/camera2/extensions/depthcamera/DepthCameraCalibrationDataMap$ExtrinsicParams");
    LOG_ALWAYS_FATAL_IF(extrinsicsClazz == NULL, "Can not find ExtrinsicParams class");
    // FindClass only gives a local reference of jclass object.
    gExtrinsicParamsClassInfo.clazz = (jclass) env->NewGlobalRef(extrinsicsClazz);
    gExtrinsicParamsClassInfo.ctor = env->GetMethodID(gExtrinsicParamsClassInfo.clazz, "<init>",
            "(Lcom/intel/camera2/extensions/depthcamera/DepthCameraCalibrationDataMap;[F[D)V");
    LOG_ALWAYS_FATAL_IF(gExtrinsicParamsClassInfo.ctor == NULL,
            "Can not find ExtrinsicParams constructor");

    jclass calibDataClazz = env->FindClass("com/intel/camera2/extensions/depthcamera/DepthCameraCalibrationDataMap$DepthCameraCalibrationData");
    LOG_ALWAYS_FATAL_IF(calibDataClazz == NULL, "Can not find DepthCameraCalibrationData class");
    // FindClass only gives a local reference of jclass object.
    gCameraCalibrationDataClassInfo.clazz = (jclass) env->NewGlobalRef(calibDataClazz);
    gCameraCalibrationDataClassInfo.ctor = env->GetMethodID(gCameraCalibrationDataClassInfo.clazz, "<init>",
            "(Lcom/intel/camera2/extensions/depthcamera/DepthCameraCalibrationDataMap;)V");
    LOG_ALWAYS_FATAL_IF(gCameraCalibrationDataClassInfo.ctor == NULL,
            "Can not find DepthCameraCalibrationData constructor");


    gCameraCalibrationDataClassInfo.setDepthIntrinsics = env->GetMethodID(gCameraCalibrationDataClassInfo.clazz, "setDepthCameraIntrinsics",
            "(Lcom/intel/camera2/extensions/depthcamera/DepthCameraCalibrationDataMap$IntrinsicParams;)V");
    LOG_ALWAYS_FATAL_IF(gCameraCalibrationDataClassInfo.setDepthIntrinsics == NULL,
            "Can not find setDepthCameraIntrinsics");

    gCameraCalibrationDataClassInfo.setColorIntrinsics = env->GetMethodID(gCameraCalibrationDataClassInfo.clazz, "setColorCameraIntrinsics",
            "(Lcom/intel/camera2/extensions/depthcamera/DepthCameraCalibrationDataMap$IntrinsicParams;)V");
    LOG_ALWAYS_FATAL_IF(gCameraCalibrationDataClassInfo.setColorIntrinsics == NULL,
            "Can not find setColorCameraIntrinsics");

    gCameraCalibrationDataClassInfo.setAuxIntrinsics = env->GetMethodID(gCameraCalibrationDataClassInfo.clazz, "setAuxCamerasIntrinisics",
            "(Lcom/intel/camera2/extensions/depthcamera/DepthCameraCalibrationDataMap$IntrinsicParams;I)V");
    LOG_ALWAYS_FATAL_IF(gCameraCalibrationDataClassInfo.setAuxIntrinsics == NULL,
            "Can not find setAuxCamerasIntrinisics");


    gCameraCalibrationDataClassInfo.setDepthToColorExtrinsics = env->GetMethodID(gCameraCalibrationDataClassInfo.clazz, "setDepthToColorExtrinsics",
            "(Lcom/intel/camera2/extensions/depthcamera/DepthCameraCalibrationDataMap$ExtrinsicParams;)V");
    LOG_ALWAYS_FATAL_IF(gCameraCalibrationDataClassInfo.setDepthToColorExtrinsics == NULL,
            "Can not find setDepthToColorExtrinsics");

    gCameraCalibrationDataClassInfo.setDepthToWorldExtrinsics = env->GetMethodID(gCameraCalibrationDataClassInfo.clazz, "setDepthToWorldExtrinsics",
            "(Lcom/intel/camera2/extensions/depthcamera/DepthCameraCalibrationDataMap$ExtrinsicParams;)V");
    LOG_ALWAYS_FATAL_IF(gCameraCalibrationDataClassInfo.setDepthToWorldExtrinsics == NULL,
            "Can not find setDepthToWorldExtrinsics");
}

static jobject DepthCameraCalibrationDataMap_getCalibrationData(JNIEnv* env, jobject thiz,
                               jint colorWidth,jint colorHeight, jint depthWidth, jint depthHeight, jboolean isRectified, jint cameraId)
{

  //initialize calibration data class
  JNIDepthCameraCalibrationDataMap* ctx = DepthCameraCalibrationDataMap_getContext(env, thiz);
  if (ctx == NULL) {
        jniThrowRuntimeException(env, "ImageReaderContext is not initialized");
  }

  DSCalibRectParameters calibData = ctx->getCalibrationData();
  float translation[3];
  double rotation[9] = {0};
  int cropPixels = 12;

  int zEntry = -1;
  for ( int i=0; i < calibData.numRectifiedModesLR; i++)
  {
    if ( ( calibData.modesLR[0][i].rw == depthWidth && calibData.modesLR[0][i].rh == depthHeight ) ||
         ( (calibData.modesLR[0][i].rw - 12)== depthWidth && (calibData.modesLR[0][i].rh - 12) == depthHeight ) // big mode
         )
    {
      zEntry = i;
      break;
    }
  }

  if ( zEntry == -1 )
  {
    ALOGE("%s non valid depth width %d", __FUNCTION__, depthWidth);
    return NULL;
  }

  DSCalibIntrinsicsRectified zIntrinsics = calibData.modesLR[0][zEntry];

  zIntrinsics.rw -= cropPixels;
  zIntrinsics.rh -= cropPixels;
  zIntrinsics.rpx -= cropPixels * 0.5f;
  zIntrinsics.rpy -= cropPixels * 0.5f;

  //call constructor for intrinisics for depth
  jobject depthIntrObj = env->NewObject(gIntriniscParamsClassInfo.clazz,
            gIntriniscParamsClassInfo.ctor, thiz, zIntrinsics.rfx, zIntrinsics.rfy, zIntrinsics.rpx, zIntrinsics.rpy,
            NULL /*no distortion */,zIntrinsics.rw, zIntrinsics.rh,cameraId );

  if ( depthIntrObj == NULL )
  {
    ALOGE("%s failed to construct the intrinsic object for depth", __FUNCTION__ );
    return NULL;
  }

  DSCalibIntrinsicsNonRectified thirdIntrinsicsNonRect;
  DSCalibIntrinsicsRectified thirdIntrinsicsRec;
  if ( !fillRectParams(&calibData,colorWidth, colorHeight, &thirdIntrinsicsNonRect, &thirdIntrinsicsRec) )
        return NULL;

  //call constructor for intrinisics for depth
  jobject colorIntrObj;
  jdoubleArray distortionDoubleArray = env->NewDoubleArray(5);
  if ( isRectified )
    colorIntrObj = env->NewObject(gIntriniscParamsClassInfo.clazz,
            gIntriniscParamsClassInfo.ctor, thiz, thirdIntrinsicsRec.rfx, thirdIntrinsicsRec.rfy, thirdIntrinsicsRec.rpx, thirdIntrinsicsRec.rpy,
            NULL /*no distortion */,thirdIntrinsicsRec.rw, thirdIntrinsicsRec.rh,cameraId );
  else
  {

    env->SetDoubleArrayRegion(distortionDoubleArray, 0 , 5, thirdIntrinsicsNonRect.k );
    colorIntrObj = env->NewObject(gIntriniscParamsClassInfo.clazz,
            gIntriniscParamsClassInfo.ctor, thiz, thirdIntrinsicsNonRect.fx, thirdIntrinsicsNonRect.fy, thirdIntrinsicsNonRect.px, thirdIntrinsicsNonRect.py,
            distortionDoubleArray ,thirdIntrinsicsNonRect.w, thirdIntrinsicsNonRect.h,cameraId );
  }

  if ( colorIntrObj == NULL )
  {
    ALOGE("%s failed to construct the intrinsic object for color - rectification mode %d", __FUNCTION__ , isRectified);
    return NULL;
  }
  //depth to color extrinsics
  if ( isRectified )
  {
      const float* T = calibData.T[0];
      translation[0] = T[0];
      translation[1] = T[1];
      translation[2] = T[2];
  }
  else
  {
    memcpy(rotation, calibData.Rthird[0], sizeof(double) * 9);

    const double T[3] = {calibData.T[0][0], calibData.T[0][1], calibData.T[0][2]};
    double dtranslation[3];
    DSMul_3x3_3x1(dtranslation, calibData.Rthird[0], T);
    translation[0] = (float) dtranslation[0];
    translation[1] = (float) dtranslation[1];
    translation[2] = (float) dtranslation[2];
  }

  jdoubleArray rotationDoubleArray = env->NewDoubleArray(9);
  env->SetDoubleArrayRegion(rotationDoubleArray, 0 , 9, rotation);
  jfloatArray translationFloatArray = env->NewFloatArray(3);
  env->SetFloatArrayRegion(translationFloatArray, 0, 3, translation);
  //call constr for extrinsics
  jobject depthToColorExtrObj = env->NewObject(gExtrinsicParamsClassInfo.clazz,
            gExtrinsicParamsClassInfo.ctor, thiz, translationFloatArray, rotationDoubleArray );
  if ( depthToColorExtrObj == NULL )
  {
    ALOGE("%s failed to construct the extrinsic object for depth to color ", __FUNCTION__ );
    return NULL;
  }
  // depth to world Extrinsics
  DSMul_3x3_3x3(rotation, calibData.Rworld, calibData.Rleft[0]);
  translation[0] = calibData.Tworld[0];
  translation[1] = calibData.Tworld[1];
  translation[2] = calibData.Tworld[2];

  //call constr for extrinsics
  env->SetDoubleArrayRegion(rotationDoubleArray, 0 , 9, rotation);
  env->SetFloatArrayRegion(translationFloatArray, 0, 3, translation);
  jobject depthToWorldExtrObj = env->NewObject(gExtrinsicParamsClassInfo.clazz,
            gExtrinsicParamsClassInfo.ctor, thiz, translationFloatArray, rotationDoubleArray);

  if ( depthToWorldExtrObj == NULL )
  {
    ALOGE("%s failed to construct the extrinsic object for depth to world ", __FUNCTION__ );
    return NULL;
  }
  // Intrinsics Left/Right
  //Right
  DSCalibIntrinsicsNonRectified rightIntr = calibData.intrinsicsRight[0];
  env->SetDoubleArrayRegion(distortionDoubleArray, 0 , 5, rightIntr.k );
  jobject rightIntrObj = env->NewObject(gIntriniscParamsClassInfo.clazz,
            gIntriniscParamsClassInfo.ctor, thiz, rightIntr.fx, rightIntr.fy, rightIntr.px, rightIntr.py,
            distortionDoubleArray ,rightIntr.w, rightIntr.h, -1 /*N/A*/ );
  if ( rightIntrObj == NULL )
  {
    ALOGE("%s failed to construct the intrinsic object for right", __FUNCTION__ );
    return NULL;
  }

  //Left
  DSCalibIntrinsicsNonRectified leftIntr = calibData.intrinsicsLeft;
  env->SetDoubleArrayRegion(distortionDoubleArray, 0 , 5, leftIntr.k );
  jobject leftIntrObj = env->NewObject(gIntriniscParamsClassInfo.clazz,
            gIntriniscParamsClassInfo.ctor, thiz, leftIntr.fx, leftIntr.fy, leftIntr.px, leftIntr.py,
            distortionDoubleArray ,leftIntr.w, leftIntr.h, -1 /*N/A*/ );

  if ( leftIntrObj == NULL )
  {
    ALOGE("%s failed to construct the intrinsic object for right", __FUNCTION__ );
    return NULL;
  }
  //call constructor for CalibrationData
  jobject calibrationDataMapObj = env->NewObject(gCameraCalibrationDataClassInfo.clazz,
            gCameraCalibrationDataClassInfo.ctor, thiz);

  env->CallVoidMethod(calibrationDataMapObj, gCameraCalibrationDataClassInfo.setDepthIntrinsics, depthIntrObj );
  env->CallVoidMethod(calibrationDataMapObj, gCameraCalibrationDataClassInfo.setColorIntrinsics, colorIntrObj );
  env->CallVoidMethod(calibrationDataMapObj, gCameraCalibrationDataClassInfo.setAuxIntrinsics, rightIntrObj  , INTEL_DEPTHCOMMON_AVAILABLE_NODES_RIGHT );
  env->CallVoidMethod(calibrationDataMapObj, gCameraCalibrationDataClassInfo.setAuxIntrinsics, leftIntrObj  , INTEL_DEPTHCOMMON_AVAILABLE_NODES_LEFT );
  env->CallVoidMethod(calibrationDataMapObj, gCameraCalibrationDataClassInfo.setDepthToColorExtrinsics, depthToColorExtrObj );
  env->CallVoidMethod(calibrationDataMapObj, gCameraCalibrationDataClassInfo.setDepthToWorldExtrinsics, depthToWorldExtrObj);

  return calibrationDataMapObj;
}

} //extern C
static JNINativeMethod gDepthCameraCalibrationDataMap[] = {
    {"nativeClassInit", "()V", (void*)DepthCameraCalibrationDataMap_classInit },
    {"nativeCalibrationDataMapClassInit","([B)V" , (void*)DepthCameraCalibrationDataMap_init},
    {"nativeGetCalibrationData", "(IIIIZI)Lcom/intel/camera2/extensions/depthcamera/DepthCameraCalibrationDataMap$DepthCameraCalibrationData;", (void*)DepthCameraCalibrationDataMap_getCalibrationData }
};

int register_intel_camera2_extensions_depthcamera_DepthCameraCalibrationDataMap(JNIEnv *env) {

    return AndroidRuntime::registerNativeMethods(env,
                   "com/intel/camera2/extensions/depthcamera/DepthCameraCalibrationDataMap", gDepthCameraCalibrationDataMap, NELEM(gDepthCameraCalibrationDataMap));

}

// jint JNI_OnLoad(JavaVM* vm, void* reserved) in com_intel_camera2_extensions_DepthCameraImageReader.cpp

