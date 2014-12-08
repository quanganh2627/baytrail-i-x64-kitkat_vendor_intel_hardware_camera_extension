/*******************************************************************************
 * INTEL CORPORATION PROPRIETARY INFORMATION
 *
 * This software is supplied under the terms of a license agreement or nondisclosure
 * agreement with Intel Corporation and may not be copied or disclosed except in
 * accordance with the terms of that agreement
 * Copyright(c) 2014 Intel Corporation. All Rights Reserved.
 ********************************************************************************/


#define LOG_TAG "DepthFrameReader_JNI"
#include <utils/Log.h>
#include <utils/misc.h>
#include <utils/List.h>
#include <utils/String8.h>
#include <utils/Vector.h>

#include <cstdio>
#include <stdint.h>

#include <gui/CpuConsumer.h>
#include <gui/Surface.h>
#include <camera3.h>

#include <android_runtime/AndroidRuntime.h>
#include <android_runtime/android_view_Surface.h>

#include <jni.h>
#include <JNIHelp.h>

#include "ufo/graphics.h"

#include "DSCalibRectParametersUtil.h"
#include "DSConfig.h"
#include "CalibRectParametersIO.h"


#define ALIGN(x, mask) ( ((x) + (mask) - 1) & ~((mask) - 1) )
#define MIN(a, b) ( ((a) < (b)) ? (a) : (b) )
#define ANDROID_MEDIA_FRAMEREADER_COLOR_CTX_JNI_ID       "mColorNativeContext"
#define ANDROID_MEDIA_FRAMEREADER_DEPTH_CTX_JNI_ID       "mDepthNativeContext"
#define ANDROID_MEDIA_FRAMEREADER_IR_CTX_JNI_ID       "mIRNativeContext"
#define ANDROID_MEDIA_SURFACEIMAGE_BUFFER_JNI_ID   "mLockedBuffer"
#define ANDROID_MEDIA_SURFACEIMAGE_UVMAPBUFFER_JNI_ID   "mUVMapBuffer"
#define ANDROID_MEDIA_SURFACEIMAGE_TS_JNI_ID       "mTimestamp"
#define ANDROID_MEDIA_FRAMEREADER_SYNC_JNI_ID 	"mFrameSynchronizer"


// ----------------------------------------------------------------------------

using namespace android;

enum {
    IMAGE_READER_MAX_NUM_PLANES = 3,
};
/**
 * IMAGE_TYPE enum values match the defines in the java side
*/
enum {
	IMAGE_TYPE_IR_LEFT = 0,
	IMAGE_TYPE_IR_RIGHT = 1,
	IMAGE_TYPE_COLOR = 2,
	IMAGE_TYPE_DEPTH = 3,
	IMAGE_TYPE_MAX = 4,
};
enum {
    ACQUIRE_SUCCESS = 0,
    ACQUIRE_NO_BUFFERS = 1,
    ACQUIRE_MAX_IMAGES = 2,
};

static struct {
    jmethodID postEventFromNative;
    jfieldID mFrameSynchronizer;
} gDepthCameraFrameSyncClassInfo;

static struct {
    jfieldID mColorNativeContext;
    jfieldID mDepthNativeContext;
    jfieldID mIRNativeContext;
} gDepthFrameReaderClassInfo;

static struct {
	jfieldID mUVMapBuffer;
    jfieldID mLockedBuffer;
    jfieldID mTimestamp;
} gSurfaceImageClassInfo;

static struct {
    jclass clazz;
    jmethodID ctor;
} gSurfacePlaneClassInfo;

//forward declaration
class JNIDepthCameraImageReaderContext;

//////////////////
/**
 * JNIDepthCameraFrameSync
 * 	Class synchronizes between the different buffers and when all required images are
 * 	available, will call the native  postEventFromeNative method
 * 	to notify java class of the available frame
 */
class JNIDepthCameraFrameSync: public virtual RefBase {
public:
	JNIDepthCameraFrameSync(JNIEnv* env, jobject weakThiz, jclass clazz);
	~JNIDepthCameraFrameSync();
	void onImageAvailable(int imgType);
	void onFrameAvailable();
	void addImageType(int imgType, sp<JNIDepthCameraImageReaderContext> ctx); 

private:
    static JNIEnv* getJNIEnv(bool* needsDetach);
    static void detachJNI();
    static int mFramesNotInSyncCounter;
    void clearRecievedImages();
    
    jobject mWeakThiz;
    jclass mClazz;

    mutable Mutex mMutex;
    int mEnabledImages;
    int mRecievedImages;
    sp<JNIDepthCameraImageReaderContext> mImageContext[IMAGE_TYPE_MAX];
    bool mIsImageEnabled[IMAGE_TYPE_MAX];
    int mImageRecievedCounter[IMAGE_TYPE_MAX];
};


// ----------------------------------------------------------------------------

class JNIDepthCameraImageReaderContext : public CpuConsumer::FrameAvailableListener
{
public:
    JNIDepthCameraImageReaderContext(int maxImages);

    virtual ~JNIDepthCameraImageReaderContext();

    virtual void onFrameAvailable();

    CpuConsumer::LockedBuffer* getLockedBuffer();
    CpuConsumer::LockedBuffer* peakNextLockedBuffer();
    void popNextLockedBuffer();

    void returnLockedBuffer(CpuConsumer::LockedBuffer* buffer);

    void setCpuConsumer(const sp<CpuConsumer>& consumer) { mConsumer = consumer; }
    CpuConsumer* getCpuConsumer() { return mConsumer.get(); }

    void setProducer(const sp<IGraphicBufferProducer>& producer) { mProducer = producer; }
    IGraphicBufferProducer* getProducer() { return mProducer.get(); }

    void setFrameSync(const sp<JNIDepthCameraFrameSync>& frameSync) { mFrameSync = frameSync; }
    void setBufferFormat(int format) { mFormat = format; }
    int getBufferFormat() { return mFormat; }

    void setBufferWidth(int width) { mWidth = width; }
    int getBufferWidth() { return mWidth; }

    void setBufferHeight(int height) { mHeight = height; }
    int getBufferHeight() { return mHeight; }

    void setBufferImageType(int type) { mImageType = type; }
    int getBufferImageType() { return mImageType; }

    void setImageParam(bool val)
    {
    	if ( mImageType == IMAGE_TYPE_COLOR)
    		mImageParam.mRectifyRGB  = val;
    	else if (mImageType == IMAGE_TYPE_IR_LEFT || mImageType == IMAGE_TYPE_IR_RIGHT )
    		mImageParam.mIRInterlaced = val;
    	else//depth
    		mImageParam.mUVmapping =val;
    }

    bool getImageParam()
    {
    	if ( mImageType == IMAGE_TYPE_COLOR)
        		return mImageParam.mRectifyRGB;
        else if (mImageType == IMAGE_TYPE_IR_LEFT || mImageType == IMAGE_TYPE_IR_RIGHT )
        		return mImageParam.mIRInterlaced ;
        else
        	return mImageParam.mUVmapping;
    }

    void setCalibrationData(unsigned char* data );
    DSCalibRectParameters getCalibrationData()  { return mCalibrationData; }

    void initHelperBuffer(int queueSize, int bufferSize);
    float* popHelperBuffer();
    void pushHelperBuffer(float* buff);

    bool isEmptyHelperBuffer();
    bool isHelperBufferInitialized() { return mHelperBufferInitialized; }
    void performUVMapping(float* uvMapData, bool isColorRectified, int colorFormat, 
    int colorWidth, int colorHeight, DSCalibRectParameters calibData, uint16_t* depthData,
    int depthWidth, int depthHeight);
private:
    
    void sprintCalibrationDebug();
    bool fillRectParams( DSCalibRectParameters* calib_params, unsigned int width, unsigned int height,
                                DSCalibIntrinsicsNonRectified *params_non_rectified, DSCalibIntrinsicsRectified *params_rectified);

    void scaleRectParams(
                const DSCalibIntrinsicsNonRectified *non_rectified,
                const DSCalibIntrinsicsRectified *rectified,
                const double *rotation, unsigned int width, unsigned int height,
                DSCalibIntrinsicsNonRectified *params_non_rectified, DSCalibIntrinsicsRectified *params_rectified);

    List<CpuConsumer::LockedBuffer*> mBuffers;
    sp<CpuConsumer> mConsumer;
    sp<IGraphicBufferProducer> mProducer;
    sp<JNIDepthCameraFrameSync> mFrameSync;

    int mFormat;
    int mWidth;
    int mHeight;
    int mImageType; //extra processing will be done based on type
    DSCalibRectParameters  mCalibrationData;//relevant for Color and Depth only
                           //for rectification/uv mapping
    DSConfig mConfig;
    union
    {
    	bool mRectifyRGB;
    	bool mIRInterlaced;
    	bool mUVmapping;
    } mImageParam;
    mutable Mutex mMutex;
    mutable Mutex mHelperBufferMutex;
    
    bool mHelperBufferInitialized;
    Vector<float*> mHelperBuffer; //for uvmapping
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

void JNIDepthCameraImageReaderContext::scaleRectParams(
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

    params_rectified->rfx = rectified->rfx / scaler;
    params_rectified->rfy = rectified->rfy / scaler;
    params_rectified->rpx = (0.5f + rectified->rpx - center_x) / scaler - 0.5f;
    params_rectified->rpy = (0.5f + rectified->rpy - center_y) / scaler - 0.5f;
    params_rectified->rw = width;
    params_rectified->rh = height;

}

bool JNIDepthCameraImageReaderContext::fillRectParams( DSCalibRectParameters* calib_params, unsigned int width, unsigned int height,
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
    int i;
    float calib_ratio;
    // First look for an exact match resolution
    for (i = 0; i < calib_params->numIntrinsicsThird; i++) {
        if ((calib_params->intrinsicsThird[i].w == width) &&
            (calib_params->intrinsicsThird[i].h == height)) {
            DSCalibIntrinsicsNonRectified* non_rectified = &calib_params->intrinsicsThird[i];
            DSCalibIntrinsicsRectified* rectified = &calib_params->modesThird[0][i][0];
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

            params_rectified->rfx = rectified->rfx;
            params_rectified->rfy = rectified->rfy;
            params_rectified->rpx = rectified->rpx;
            params_rectified->rpy = rectified->rpy;
            params_rectified->rw = rectified->rw;
            params_rectified->rh = rectified->rh;

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
                              width, height, params_non_rectified, params_rectified);
            return true;
        }
    }

    // No match for aspect ratio? Shouldn't happen...
    ALOGE("%s(): Couldn't find matching rectification data for %dx%d", __func__, width, height);
    return false;
}

void JNIDepthCameraImageReaderContext::performUVMapping(float* uvMapData, bool isColorRectified, int colorFormat, 
    int colorWidth, int colorHeight, DSCalibRectParameters calibData, uint16_t* depthData,
    int depthWidth, int depthHeight)
{
    ALOGV("%s", __FUNCTION__);
    ALOGI("%s color %dx%d depth %dx%d isColorRectified=%d", __FUNCTION__, colorWidth,colorHeight, depthWidth, depthHeight, isColorRectified);
    sprintCalibrationDebug();

#ifdef PRINT_DEBUG
	calibrationDataDumped = true;
#endif
    if (depthData == NULL)
    {
        ALOGE("%s Can't convert src direct buffer address", __FUNCTION__);
        return;
    }
    if (uvMapData == NULL)
    {
        ALOGE("%s uvmap buffer is null", __FUNCTION__);
        return;
    }
    int depthBytePerPixel = 2;
    uint32_t depthSize = depthWidth * depthHeight * depthBytePerPixel;

    double translation[3];
    double rotation[9];
    int cropPixels = 12;

    //currently using a fixed settings for uvmapping
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
		return;
    }

    DSCalibIntrinsicsRectified zIntrinsics = calibData.modesLR[0][zEntry];

    zIntrinsics.rw -= cropPixels;
    zIntrinsics.rh -= cropPixels;
    zIntrinsics.rpx -= cropPixels * 0.5f;
    zIntrinsics.rpy -= cropPixels * 0.5f;

    DSCalibIntrinsicsNonRectified thirdIntrinsicsNonRect;
    DSCalibIntrinsicsRectified thirdIntrinsicsRec;
    if ( !fillRectParams(&calibData,colorWidth, colorHeight, &thirdIntrinsicsNonRect, &thirdIntrinsicsRec) )
        return;

    if ( isColorRectified )
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
    	DSMul_3x3_3x1(translation, calibData.Rthird[0], T);
    }


    memset(uvMapData, 0, depthHeight * depthWidth * 2 * sizeof(float));
#ifdef PRINT_DEBUG
    		   MyDumpAsTCSV( zIntrinsics, "zIntrinsics");
    		   MyDumpAsTCSV( thirdIntrinsicsNonRect, "thirdIntrinsicsNonRect");

    		   	for ( int j=0; j< 9; j++)
    		   		ALOGW("%f %s[%d]",rotation[j], "rotation", j);

    		    for ( int j=0; j< 3; j++)
    		    	ALOGW("%f %s[%d]",translation[j], "translation", j);
#endif
    float zImage[3];
    int i=0;

    for (int y = 0; y < depthHeight ; y++)
    {
        for (int x = 0; x < depthWidth; x++)
        {
        	if ( depthData[i] != 0 )
        	{
        		zImage[0] = x;
        		zImage[1] = y;
				zImage[2] = depthData[i];

				if ( isColorRectified )
				{
					 DSTransformFromZImageToRectThirdImage(zIntrinsics, translation,
							 thirdIntrinsicsRec, zImage, uvMapData);
				}
				else
				{
					DSTransformFromZImageToNonRectThirdImage(zIntrinsics, rotation,translation,
							thirdIntrinsicsNonRect, zImage, uvMapData);
				}
        	}
        	i++;
            uvMapData+=2;
        }
    }
}
void JNIDepthCameraImageReaderContext::initHelperBuffer(int queueSize, int bufferSize)
{
    Mutex::Autolock lock(mHelperBufferMutex);
    if ( !mHelperBufferInitialized )
    {
        float* buff;
        for ( int i=0; i<queueSize; i++ )
        {
            buff = new float[bufferSize];
            mHelperBuffer.push_back(buff);
        }
        mHelperBufferInitialized = true;
    }
}


void JNIDepthCameraImageReaderContext::setCalibrationData(unsigned char* data )
{
	ALOGV("%s", __FUNCTION__);


	if ( data != NULL)
	{
		DSCalibRectParameters*  tmp = (DSCalibRectParameters*) data;
		memcpy(&mCalibrationData, tmp, sizeof(DSCalibRectParameters));
		sprintCalibrationDebug();
	}
}

void JNIDepthCameraImageReaderContext::sprintCalibrationDebug()
{

#ifdef PRINT_DEBUG
		if (!calibrationDataDumped )
		{
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
float* JNIDepthCameraImageReaderContext::popHelperBuffer()
{
	ALOGV("%s",__FUNCTION__);
    Mutex::Autolock lock(mHelperBufferMutex);
    if ( !mHelperBuffer.empty() )
    {
        float* front = mHelperBuffer.top();
        mHelperBuffer.pop();
        return front;
    }
    return NULL;
}
void JNIDepthCameraImageReaderContext::pushHelperBuffer(float* buff)
{
	ALOGV("%s",__FUNCTION__);
    if ( buff != NULL  )
    {
        Mutex::Autolock lock(mHelperBufferMutex);
        mHelperBuffer.push_back(buff);
    }
}
bool JNIDepthCameraImageReaderContext::isEmptyHelperBuffer()
{
    Mutex::Autolock lock(mHelperBufferMutex);
    return mHelperBuffer.empty();
}
JNIDepthCameraImageReaderContext::JNIDepthCameraImageReaderContext(int maxImages)
{
	Mutex::Autolock lock(mMutex);
    for (int i = 0; i < maxImages; i++) {
        CpuConsumer::LockedBuffer *buffer = new CpuConsumer::LockedBuffer;
        mBuffers.push_back(buffer);
    }
}

CpuConsumer::LockedBuffer* JNIDepthCameraImageReaderContext::peakNextLockedBuffer() {
	ALOGV("%s",__FUNCTION__);
	Mutex::Autolock lock(mMutex);
    if (mBuffers.empty()) {
        return NULL;
    }
    //just peak into next entry, do not lock it
    List<CpuConsumer::LockedBuffer*>::iterator it = mBuffers.begin();
    CpuConsumer::LockedBuffer* buffer = *it;
    return buffer;
}
void JNIDepthCameraImageReaderContext::popNextLockedBuffer() {
	ALOGV("%s",__FUNCTION__);
	Mutex::Autolock lock(mMutex);
    if (mBuffers.empty()) {
        return;
    }
    List<CpuConsumer::LockedBuffer*>::iterator it = mBuffers.begin();
    CpuConsumer::LockedBuffer* buffer = *it;
    mBuffers.erase(it);
    mBuffers.push_back(buffer);
}
CpuConsumer::LockedBuffer* JNIDepthCameraImageReaderContext::getLockedBuffer() {
	ALOGV("%s",__FUNCTION__);
	Mutex::Autolock lock(mMutex);
    if (mBuffers.empty()) {
        return NULL;
    }
    // Return a LockedBuffer pointer and remove it from the list
    List<CpuConsumer::LockedBuffer*>::iterator it = mBuffers.begin();
    CpuConsumer::LockedBuffer* buffer = *it;
    mBuffers.erase(it);
    return buffer;
}

void JNIDepthCameraImageReaderContext::returnLockedBuffer(CpuConsumer::LockedBuffer* buffer) {
	Mutex::Autolock lock(mMutex);
    mBuffers.push_back(buffer);
}

JNIDepthCameraImageReaderContext::~JNIDepthCameraImageReaderContext() {

    // Delete LockedBuffers
    for (List<CpuConsumer::LockedBuffer *>::iterator it = mBuffers.begin();
            it != mBuffers.end(); it++) {
        delete *it;
    }
    //Delete HelperBuffers
    for (Vector<float*>::iterator it = mHelperBuffer.begin();
            it != mHelperBuffer.end(); it++) {
        delete *it;
    }
    mHelperBuffer.clear();
    mBuffers.clear();
    mConsumer.clear();
    mFrameSync.clear();
}

void JNIDepthCameraImageReaderContext::onFrameAvailable()
{
	//events will need to be posted when all images are available,
	//This overrides the CpuConsumer::FrameAvailableListener implementation, 
    //and not the ConsumerBase::onFrameAvailable
	//implementation which basically shouldn't be overridden
    ALOGV("%s: image available", __FUNCTION__);

    ALOG_ASSERT(mFrameSync != NULL, "Frame Sync is NULL");
    mFrameSync->onImageAvailable(mImageType);
}
JNIDepthCameraFrameSync::JNIDepthCameraFrameSync(JNIEnv* env,
        jobject weakThiz, jclass clazz) :
    mWeakThiz(env->NewGlobalRef(weakThiz)),
    mClazz((jclass)env->NewGlobalRef(clazz)),
    mEnabledImages(0),
    mRecievedImages(0)
{
	for ( int i=0; i<IMAGE_TYPE_MAX; i++)
	{
		mIsImageEnabled[i]= false;
		mImageRecievedCounter[i] = 0;
		mImageContext[i]= NULL;
	}
}


JNIEnv* JNIDepthCameraFrameSync::getJNIEnv(bool* needsDetach) {
    LOG_ALWAYS_FATAL_IF(needsDetach == NULL, "needsDetach is null!!!");
    *needsDetach = false;
    JNIEnv* env = AndroidRuntime::getJNIEnv();
    if (env == NULL) {
        JavaVMAttachArgs args = {JNI_VERSION_1_4, NULL, NULL};
        JavaVM* vm = AndroidRuntime::getJavaVM();
        int result = vm->AttachCurrentThread(&env, (void*) &args);
        if (result != JNI_OK) {
            ALOGE("thread attach failed: %#x", result);
            return NULL;
        }
        *needsDetach = true;
    }
    return env;
}

void JNIDepthCameraFrameSync::detachJNI() {
    JavaVM* vm = AndroidRuntime::getJavaVM();
    int result = vm->DetachCurrentThread();
    if (result != JNI_OK) {
        ALOGE("thread detach failed: %#x", result);
    }
}
void JNIDepthCameraFrameSync::clearRecievedImages()
{
	//When calling this lock is already held
	for ( int i=0; i<IMAGE_TYPE_MAX; i++)
	{
		mImageRecievedCounter[i] = 0;
	}
	mRecievedImages =0;
}
void JNIDepthCameraFrameSync::onImageAvailable(int imgType)
{

	ALOGV("%s imgType %d",__FUNCTION__, imgType);
	if ( mIsImageEnabled[imgType] == false )
	{
		 ALOGE("Image Type available but not enabled: %d!!!", imgType);
		 return;
	}
	bool frameAvailable = false;
	{
		Mutex::Autolock lock(mMutex);

		mImageRecievedCounter[imgType]++;
		if ( mImageRecievedCounter[imgType] == 1 )
		{
			mRecievedImages++;
		}
		if ( mRecievedImages >= mEnabledImages)
		{
			// All registered Images are available => Frame is available
			frameAvailable = true;
			//update counters
			for (int i=0; i<IMAGE_TYPE_MAX; i++ )
			{
				if ( mIsImageEnabled[i])
				{
					mImageRecievedCounter[i]--;
					if (  mImageRecievedCounter[i] == 0)
					{
						mRecievedImages--;
					}

				}
			}
		}
	}
	if ( frameAvailable )
	{
		onFrameAvailable();
	}

}

void JNIDepthCameraFrameSync::addImageType(int imgType, sp<JNIDepthCameraImageReaderContext> ctx)
{
	ALOG_ASSERT(imgType < IMAGE_TYPE_MAX );
	ALOGV("%s",__FUNCTION__);
	if ( ctx == NULL )
	{
		 ALOGW("addImageType - ctx == null!! image type cannot be added!");
		 return;
	}
	Mutex::Autolock lock(mMutex);
	if ( mIsImageEnabled[imgType] == false )
	{
		mEnabledImages++;
		mIsImageEnabled[imgType] = true;
		mImageContext[imgType] = ctx;
	}

}


void JNIDepthCameraFrameSync::onFrameAvailable()
{
	ALOGV("%s",__FUNCTION__);
    bool needsDetach = false;
    JNIEnv* env = getJNIEnv(&needsDetach);
    if (env != NULL) {
        env->CallStaticVoidMethod(mClazz, gDepthCameraFrameSyncClassInfo.postEventFromNative, mWeakThiz);
    } else {
        ALOGW("onFrameAvailable event cannot be posted");
    }
    if (needsDetach) {
        detachJNI();
    }
}

JNIDepthCameraFrameSync::~JNIDepthCameraFrameSync() {
    bool needsDetach = false;
    JNIEnv* env = getJNIEnv(&needsDetach);
    if (env != NULL) {
        env->DeleteGlobalRef(mWeakThiz);
        env->DeleteGlobalRef(mClazz);
    } else {
        ALOGW("leaking JNI object references");
    }
    if (needsDetach) {
        detachJNI();
    }
}

// ----------------------------------------------------------------------------

extern "C" {

static JNIDepthCameraFrameSync* DepthCameraFrameSync_getSync(JNIEnv* env, jobject thiz)
{
	JNIDepthCameraFrameSync* synchronizer = reinterpret_cast<JNIDepthCameraFrameSync *>
    (env->GetLongField(thiz, gDepthCameraFrameSyncClassInfo.mFrameSynchronizer));
	return synchronizer;
}
static JNIDepthCameraImageReaderContext* DepthCameraImageReader_getContext(JNIEnv* env, jobject thiz, int imgType)
{
    JNIDepthCameraImageReaderContext *ctx;
    switch ( imgType )
    {
    case IMAGE_TYPE_COLOR:
    	ctx = reinterpret_cast<JNIDepthCameraImageReaderContext *>
    	              (env->GetLongField(thiz, gDepthFrameReaderClassInfo.mColorNativeContext));
    	break;
    case IMAGE_TYPE_DEPTH:
        ctx = reinterpret_cast<JNIDepthCameraImageReaderContext *>
        	              (env->GetLongField(thiz, gDepthFrameReaderClassInfo.mDepthNativeContext));
        break;
    case IMAGE_TYPE_IR_LEFT:
    case IMAGE_TYPE_IR_RIGHT:
    	ctx = reinterpret_cast<JNIDepthCameraImageReaderContext *>
            	              (env->GetLongField(thiz, gDepthFrameReaderClassInfo.mIRNativeContext));
        break;
    default:
    	ctx = NULL;
    	jniThrowRuntimeException(env, "Bad image type for image reader");
    }

    return ctx;
}

static IGraphicBufferProducer* DepthCameraImageReader_getProducer(JNIEnv* env, jobject thiz, int imgType)
{
	ALOGV("%s",__FUNCTION__);
    JNIDepthCameraImageReaderContext* const ctx = DepthCameraImageReader_getContext(env, thiz, imgType);
    if (ctx == NULL) {
        jniThrowRuntimeException(env, "ImageReaderContext is not initialized");
        return NULL;
    }
    return ctx->getProducer();
}

static void DepthCameraImageReader_setNativeContext(JNIEnv* env,
        jobject thiz, sp<JNIDepthCameraImageReaderContext> ctx, int imgType)
{
    JNIDepthCameraImageReaderContext* const p = DepthCameraImageReader_getContext(env, thiz, imgType);
    if (ctx != 0) {
        ctx->incStrong((void*)DepthCameraImageReader_setNativeContext);
    }
    if (p) {
        p->decStrong((void*)DepthCameraImageReader_setNativeContext);
    }
    switch ( imgType )
       {
       case IMAGE_TYPE_COLOR:
    	   env->SetLongField(thiz, gDepthFrameReaderClassInfo.mColorNativeContext,
    	            reinterpret_cast<jlong>(ctx.get()));

       	break;
       case IMAGE_TYPE_DEPTH:
    	   env->SetLongField(thiz, gDepthFrameReaderClassInfo.mDepthNativeContext,
    	      	            reinterpret_cast<jlong>(ctx.get()));

           break;
       case IMAGE_TYPE_IR_LEFT:
       case IMAGE_TYPE_IR_RIGHT:
    	   env->SetLongField(thiz, gDepthFrameReaderClassInfo.mIRNativeContext,
    	     	      	            reinterpret_cast<jlong>(ctx.get()));
    	     break;
       default:
       	jniThrowRuntimeException(env, "Bad image type for image reader");
      }

}
static float* Image_getUVMapBuffer(JNIEnv* env, jobject image)
{
	ALOGV("%s",__FUNCTION__);
    //Get from the image instance the mUVMapBuffer field
    return reinterpret_cast<float*>(env->GetLongField(image, gSurfaceImageClassInfo.mUVMapBuffer));
}

static void Image_setUVMapBuffer(JNIEnv* env, jobject thiz,
        const float* buffer)
{
	ALOGV("%s",__FUNCTION__);
	//This works on thiz - specific instance of image - so it will update the relevant mUVMapBuffer
    env->SetLongField(thiz, gSurfaceImageClassInfo.mUVMapBuffer, reinterpret_cast<jlong>(buffer));
}
static CpuConsumer::LockedBuffer* Image_getLockedBuffer(JNIEnv* env, jobject image)
{
	ALOGV("%s",__FUNCTION__);
	//Get from the image instance the mLockedBuffer field
    return reinterpret_cast<CpuConsumer::LockedBuffer*>(
            env->GetLongField(image, gSurfaceImageClassInfo.mLockedBuffer));
}

static void Image_setBuffer(JNIEnv* env, jobject thiz,
        const CpuConsumer::LockedBuffer* buffer)
{
	ALOGV("%s",__FUNCTION__);
	//This works on thiz - specific instance of image - so it will update the relevant mLockedBuffer
    env->SetLongField(thiz, gSurfaceImageClassInfo.mLockedBuffer, reinterpret_cast<jlong>(buffer));
}

// Some formats like JPEG defined with different values between android.graphics.ImageFormat and
// graphics.h, need convert to the one defined in graphics.h here.
static int Image_getPixelFormat(JNIEnv* env, int format)
{
    int jpegFormat, rawSensorFormat;
    jfieldID fid;

    ALOGV("%s: format = 0x%x", __FUNCTION__, format);

    jclass imageFormatClazz = env->FindClass("android/graphics/ImageFormat");
    ALOG_ASSERT(imageFormatClazz != NULL);

    fid = env->GetStaticFieldID(imageFormatClazz, "JPEG", "I");
    jpegFormat = env->GetStaticIntField(imageFormatClazz, fid);
    fid = env->GetStaticFieldID(imageFormatClazz, "RAW_SENSOR", "I");
    rawSensorFormat = env->GetStaticIntField(imageFormatClazz, fid);

    // Translate the JPEG to BLOB for camera purpose, an add more if more mismatch is found.
    if (format == jpegFormat) {
        format = HAL_PIXEL_FORMAT_BLOB;
    }
    // Same thing for RAW_SENSOR format
    if (format == rawSensorFormat) {
        format = HAL_PIXEL_FORMAT_RAW_SENSOR;
    }

    return format;
}

static uint32_t Image_getJpegSize(CpuConsumer::LockedBuffer* buffer)
{
    ALOG_ASSERT(buffer != NULL, "Input buffer is NULL!!!");
    uint32_t size = 0;
    uint32_t width = buffer->width;
    uint8_t* jpegBuffer = buffer->data;

    // First check for JPEG transport header at the end of the buffer
    uint8_t* header = jpegBuffer + (width - sizeof(struct camera3_jpeg_blob));
    struct camera3_jpeg_blob *blob = (struct camera3_jpeg_blob*)(header);
    if (blob->jpeg_blob_id == CAMERA3_JPEG_BLOB_ID) {
        size = blob->jpeg_size;
        ALOGV("%s: Jpeg size = %d", __FUNCTION__, size);
    }

    // failed to find size, default to whole buffer
    if (size == 0) {
        size = width;
    }

    return size;
}
//returns the relevant plane address, idx is the plane idx
static void Image_getLockedBufferInfo(JNIEnv* env, CpuConsumer::LockedBuffer* buffer, int idx,
                                uint8_t **base, uint32_t *size)
{
    ALOG_ASSERT(buffer != NULL, "Input buffer is NULL!!!");
    ALOG_ASSERT(base != NULL, "base is NULL!!!");
    ALOG_ASSERT(size != NULL, "size is NULL!!!");
    ALOG_ASSERT((idx < IMAGE_READER_MAX_NUM_PLANES) && (idx >= 0));

    ALOGV("%s: buffer: %p", __FUNCTION__, buffer);

    uint32_t dataSize, ySize, cSize, cStride;

    uint8_t *cb, *cr;
    uint8_t *pData = NULL;
    int bytesPerPixel = 0;

    dataSize = ySize = cSize = cStride = 0;
    int32_t fmt = buffer->format;
    switch (fmt) {
        case HAL_PIXEL_FORMAT_YCbCr_420_888:
            pData =
                (idx == 0) ?
                    buffer->data :
                (idx == 1) ?
                    buffer->dataCb :
                buffer->dataCr;
            if (idx == 0) {
                dataSize = buffer->stride * buffer->height;
            } else {
                dataSize = buffer->chromaStride * buffer->height / 2;
            }
            break;
        // NV21
        case HAL_PIXEL_FORMAT_YCrCb_420_SP:
            cr = buffer->data + (buffer->stride * buffer->height);
            cb = cr + 1;
            ySize = buffer->width * buffer->height;
            cSize = buffer->width * buffer->height / 2;

            pData =
                (idx == 0) ?
                    buffer->data :
                (idx == 1) ?
                    cb:
                cr;

            dataSize = (idx == 0) ? ySize : cSize;
            break;
        case HAL_PIXEL_FORMAT_YV12:
            // Y and C stride need to be 16 pixel aligned.
            LOG_ALWAYS_FATAL_IF(buffer->stride % 16,
                                "Stride is not 16 pixel aligned %d", buffer->stride);

            ySize = buffer->stride * buffer->height;
            cStride = ALIGN(buffer->stride / 2, 16);
            cr = buffer->data + ySize;
            cSize = cStride * buffer->height / 2;
            cb = cr + cSize;

            pData =
                (idx == 0) ?
                    buffer->data :
                (idx == 1) ?
                    cb :
                cr;
            dataSize = (idx == 0) ? ySize : cSize;
            break;
        case HAL_PIXEL_FORMAT_Y8:
            // Single plane, 8bpp.
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);

            pData = buffer->data;
            dataSize = buffer->stride * buffer->height;
            break;
        case HAL_PIXEL_FORMAT_Y16:
            // Single plane, 16bpp, strides are specified in pixels, not in bytes
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);

            pData = buffer->data;
            dataSize = buffer->stride * buffer->height * 2;
            break;
        case HAL_PIXEL_FORMAT_BLOB:
            // Used for JPEG data, height must be 1, width == size, single plane.
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            ALOG_ASSERT(buffer->height == 1, "JPEG should has height value %d", buffer->height);

            pData = buffer->data;
            dataSize = Image_getJpegSize(buffer);
            break;
        case HAL_PIXEL_FORMAT_RAW_SENSOR:
            // Single plane 16bpp bayer data.
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            pData = buffer->data;
            dataSize = buffer->width * 2 * buffer->height;
            break;
        case HAL_PIXEL_FORMAT_RGBA_8888:
        case HAL_PIXEL_FORMAT_RGBX_8888:
            // Single plane, 32bpp.
            bytesPerPixel = 4;
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            pData = buffer->data;
            dataSize = buffer->stride * buffer->height * bytesPerPixel;
            break;
        case HAL_PIXEL_FORMAT_RGB_565:
            // Single plane, 16bpp.
            bytesPerPixel = 2;
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            pData = buffer->data;
            dataSize = buffer->stride * buffer->height * bytesPerPixel;
            break;
        case HAL_PIXEL_FORMAT_RGB_888:
            // Single plane, 24bpp.
            bytesPerPixel = 3;
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            pData = buffer->data;
            dataSize = buffer->stride * buffer->height * bytesPerPixel;
            break;
        case HAL_PIXEL_FORMAT_Z16_INTEL:
        	// we will use one buffer for both planes, and pass the relevant address.. for the plane
        	int depthBytePerPixel;
        	uint32_t depthSize;

        	depthBytePerPixel = 2;
        	depthSize= buffer->stride * buffer->height * depthBytePerPixel;

        	ALOG_ASSERT(idx <= 1, "Wrong index: %d", idx);
        	if ( idx == 0 ) //depth
        	{
        		pData = buffer->data;
        		dataSize = depthSize;
        		bytesPerPixel = depthBytePerPixel;
        	}
        	else //uv mapp
        	{
        		bytesPerPixel = 2*sizeof(float); //each pixel is 2 floats
        		pData = buffer->data + depthSize;
        		dataSize = buffer->width * buffer->height * bytesPerPixel;
        	}
        	break;
        default:
            jniThrowExceptionFmt(env, "java/lang/UnsupportedOperationException",
                                 "Pixel format: 0x%x is unsupported", fmt);
            break;
    }

    *base = pData;
    *size = dataSize;
}

static jint Image_imageGetPixelStride(JNIEnv* env, CpuConsumer::LockedBuffer* buffer, int idx)
{
    ALOGV("%s: buffer index: %d", __FUNCTION__, idx);
    ALOG_ASSERT((idx < IMAGE_READER_MAX_NUM_PLANES) && (idx >= 0), "Index is out of range:%d", idx);

    int pixelStride = 0;
    ALOG_ASSERT(buffer != NULL, "buffer is NULL");

    int32_t fmt = buffer->format;
    switch (fmt) {
        case HAL_PIXEL_FORMAT_YCbCr_420_888:
            pixelStride = (idx == 0) ? 1 : buffer->chromaStep;
            break;
        case HAL_PIXEL_FORMAT_YCrCb_420_SP:
            pixelStride = (idx == 0) ? 1 : 2;
            break;
        case HAL_PIXEL_FORMAT_Y8:
            // Single plane 8bpp data.
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            pixelStride;
            break;
        case HAL_PIXEL_FORMAT_YV12:
            pixelStride = 1;
            break;
        case HAL_PIXEL_FORMAT_BLOB:
            // Used for JPEG data, single plane, row and pixel strides are 0
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            pixelStride = 0;
            break;
        case HAL_PIXEL_FORMAT_Y16:
        case HAL_PIXEL_FORMAT_RAW_SENSOR:
        case HAL_PIXEL_FORMAT_RGB_565:
            // Single plane 16bpp data.
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            pixelStride = 2;
            break;
        case HAL_PIXEL_FORMAT_RGBA_8888:
        case HAL_PIXEL_FORMAT_RGBX_8888:
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            pixelStride = 4;
            break;
        case HAL_PIXEL_FORMAT_RGB_888:
            // Single plane, 24bpp.
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            pixelStride = 3;
            break;
        case HAL_PIXEL_FORMAT_Z16_INTEL:
        	pixelStride = (idx == 0 )?  2 : 2*sizeof(float);
        	break;
        default:
            jniThrowExceptionFmt(env, "java/lang/UnsupportedOperationException",
                                 "Pixel format: 0x%x is unsupported", fmt);
            break;
    }

    return pixelStride;
}

static jint Image_imageGetRowStride(JNIEnv* env, CpuConsumer::LockedBuffer* buffer, int idx)
{
    ALOGV("%s: buffer index: %d", __FUNCTION__, idx);
    ALOG_ASSERT((idx < IMAGE_READER_MAX_NUM_PLANES) && (idx >= 0));

    int rowStride = 0;
    ALOG_ASSERT(buffer != NULL, "buffer is NULL");

    int32_t fmt = buffer->format;

    switch (fmt) {
        case HAL_PIXEL_FORMAT_YCbCr_420_888:
            rowStride = (idx == 0) ? buffer->stride : buffer->chromaStride;
            break;
        case HAL_PIXEL_FORMAT_YCrCb_420_SP:
            rowStride = buffer->width;
            break;
        case HAL_PIXEL_FORMAT_YV12:
            LOG_ALWAYS_FATAL_IF(buffer->stride % 16,
                                "Stride is not 16 pixel aligned %d", buffer->stride);
            rowStride = (idx == 0) ? buffer->stride : ALIGN(buffer->stride / 2, 16);
            break;
        case HAL_PIXEL_FORMAT_BLOB:
            // Used for JPEG data, single plane, row and pixel strides are 0
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            rowStride = 0;
            break;
        case HAL_PIXEL_FORMAT_Y8:
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            LOG_ALWAYS_FATAL_IF(buffer->stride % 16,
                                "Stride is not 16 pixel aligned %d", buffer->stride);
            rowStride = buffer->stride;
            break;
        case HAL_PIXEL_FORMAT_Y16:
        case HAL_PIXEL_FORMAT_RAW_SENSOR:
            // In native side, strides are specified in pixels, not in bytes.
            // Single plane 16bpp bayer data. even width/height,
            // row stride multiple of 16 pixels (32 bytes)
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            LOG_ALWAYS_FATAL_IF(buffer->stride % 16,
                                "Stride is not 16 pixel aligned %d", buffer->stride);
            rowStride = buffer->stride * 2;
            break;
        case HAL_PIXEL_FORMAT_RGB_565:
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            rowStride = buffer->stride * 2;
            break;
        case HAL_PIXEL_FORMAT_RGBA_8888:
        case HAL_PIXEL_FORMAT_RGBX_8888:
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            rowStride = buffer->stride * 4;
            break;
        case HAL_PIXEL_FORMAT_RGB_888:
            // Single plane, 24bpp.
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            rowStride = buffer->stride * 3;
            break;
        case HAL_PIXEL_FORMAT_Z16_INTEL:
        	rowStride = (idx == 0 )?  buffer->stride :  buffer->stride * sizeof(float);
            break;
        default:
            ALOGE("%s Pixel format: 0x%x is unsupported", __FUNCTION__, fmt);
            jniThrowException(env, "java/lang/UnsupportedOperationException",
                              "unsupported buffer format");
          break;
    }

    return rowStride;
}

// ----------------------------------------------------------------------------

static void DepthCameraImageReader_classInit(JNIEnv* env, jclass clazz)
{
    ALOGV("%s:", __FUNCTION__);

    jclass imageClazz = env->FindClass("com/intel/camera2/extensions/depthcamera/DepthCameraSetup$DepthFrameReader$SurfaceImage");
    LOG_ALWAYS_FATAL_IF(imageClazz == NULL,
                        "can't find com/intel/camera2/extensions/depthcamera/DepthCameraSetup$DepthFrameReader$SurfaceImage");
    gSurfaceImageClassInfo.mUVMapBuffer = env->GetFieldID(
                imageClazz, ANDROID_MEDIA_SURFACEIMAGE_UVMAPBUFFER_JNI_ID, "J");
    LOG_ALWAYS_FATAL_IF(gSurfaceImageClassInfo.mUVMapBuffer == NULL,
                            "can't find com/intel/camera2/extensions/depthcamera/DepthCameraSetup$DepthFrameReader.%s",
                            ANDROID_MEDIA_SURFACEIMAGE_UVMAPBUFFER_JNI_ID);
    gSurfaceImageClassInfo.mLockedBuffer = env->GetFieldID(
            imageClazz, ANDROID_MEDIA_SURFACEIMAGE_BUFFER_JNI_ID, "J");
    LOG_ALWAYS_FATAL_IF(gSurfaceImageClassInfo.mLockedBuffer == NULL,
                        "can't find com/intel/camera2/extensions/depthcamera/DepthCameraSetup$DepthFrameReader.%s",
                        ANDROID_MEDIA_SURFACEIMAGE_BUFFER_JNI_ID);

    gSurfaceImageClassInfo.mTimestamp = env->GetFieldID(
            imageClazz, ANDROID_MEDIA_SURFACEIMAGE_TS_JNI_ID, "J");
    LOG_ALWAYS_FATAL_IF(gSurfaceImageClassInfo.mTimestamp == NULL,
                        "can't find com/intel/camera2/extensions/depthcamera/DepthCameraSetup$DepthFrameReader.%s",
                        ANDROID_MEDIA_SURFACEIMAGE_TS_JNI_ID);

    gDepthFrameReaderClassInfo.mColorNativeContext = env->GetFieldID(
            clazz, ANDROID_MEDIA_FRAMEREADER_COLOR_CTX_JNI_ID, "J");
    LOG_ALWAYS_FATAL_IF(gDepthFrameReaderClassInfo.mColorNativeContext == NULL,
                        "can't find com/intel/camera2/extensions/depthcamera/DepthCameraSetup$DepthFrameReader.%s",
                        ANDROID_MEDIA_FRAMEREADER_COLOR_CTX_JNI_ID);

    gDepthFrameReaderClassInfo.mDepthNativeContext = env->GetFieldID(
            clazz, ANDROID_MEDIA_FRAMEREADER_DEPTH_CTX_JNI_ID, "J");
    LOG_ALWAYS_FATAL_IF(gDepthFrameReaderClassInfo.mDepthNativeContext == NULL,
                        "can't find com/intel/camera2/extensions/depthcamera/DepthCameraSetup$DepthFrameReader.%s",
                        ANDROID_MEDIA_FRAMEREADER_DEPTH_CTX_JNI_ID);

    gDepthFrameReaderClassInfo.mIRNativeContext = env->GetFieldID(
            clazz, ANDROID_MEDIA_FRAMEREADER_IR_CTX_JNI_ID, "J");
    LOG_ALWAYS_FATAL_IF(gDepthFrameReaderClassInfo.mIRNativeContext == NULL,
                        "can't find com/intel/camera2/extensions/depthcamera/DepthCameraSetup$DepthFrameReader.%s",
                        ANDROID_MEDIA_FRAMEREADER_IR_CTX_JNI_ID);

    gDepthCameraFrameSyncClassInfo.postEventFromNative = env->GetStaticMethodID(
            clazz, "postEventFromNative", "(Ljava/lang/Object;)V");
    LOG_ALWAYS_FATAL_IF(gDepthCameraFrameSyncClassInfo.postEventFromNative == NULL,
                        "can't find com/intel/camera2/extensions/depthcamera/DepthCameraSetup$DepthFrameReader.postEventFromNative");

    gDepthCameraFrameSyncClassInfo.mFrameSynchronizer = env->GetFieldID(
                clazz, ANDROID_MEDIA_FRAMEREADER_SYNC_JNI_ID, "J");
   LOG_ALWAYS_FATAL_IF(gDepthCameraFrameSyncClassInfo.mFrameSynchronizer == NULL,
                            "can't find com/intel/camera2/extensions/depthcamera/DepthCameraSetup$DepthFrameReader.%s",
                            ANDROID_MEDIA_FRAMEREADER_SYNC_JNI_ID);

    jclass planeClazz = env->FindClass("com/intel/camera2/extensions/depthcamera/DepthCameraSetup$DepthFrameReader$SurfaceImage$SurfacePlane");
    LOG_ALWAYS_FATAL_IF(planeClazz == NULL, "Can not find SurfacePlane class");
    // FindClass only gives a local reference of jclass object.
    gSurfacePlaneClassInfo.clazz = (jclass) env->NewGlobalRef(planeClazz);
    gSurfacePlaneClassInfo.ctor = env->GetMethodID(gSurfacePlaneClassInfo.clazz, "<init>",
            "(Lcom/intel/camera2/extensions/depthcamera/DepthCameraSetup$DepthFrameReader$SurfaceImage;III)V");
    LOG_ALWAYS_FATAL_IF(gSurfacePlaneClassInfo.ctor == NULL,
            "Can not find SurfacePlane constructor");
}

static void DepthCameraFrameReader_setNativeSync(JNIEnv* env,
        jobject thiz, sp<JNIDepthCameraFrameSync> frameSync)
{
    ALOGV("%s:", __FUNCTION__);
    JNIDepthCameraFrameSync* const p = DepthCameraFrameSync_getSync(env, thiz);

    if (frameSync != 0) {
    	frameSync->incStrong((void*)DepthCameraFrameReader_setNativeSync);
    }
    if (p) {
        p->decStrong((void*)DepthCameraFrameReader_setNativeSync);
    }
    env->SetLongField(thiz, gDepthCameraFrameSyncClassInfo.mFrameSynchronizer,
    	            reinterpret_cast<jlong>(frameSync.get()));


}
static void DepthCameraFrameSync_init(JNIEnv* env, jobject thiz,jobject weakThiz)
{
	ALOGV("%s",__FUNCTION__);
	jclass clazz = env->GetObjectClass(thiz);
	if (clazz == NULL) {
		 jniThrowRuntimeException(env, "Can't find intel/camera2/extensions/DepthCamera/DepthCameraSetup.DepthFrameReader");
		 return;
	}

	sp<JNIDepthCameraFrameSync> frameSync(new JNIDepthCameraFrameSync(env, weakThiz, clazz));

	DepthCameraFrameReader_setNativeSync(env, thiz, frameSync);

}
static void DepthCameraFrameSync_close(JNIEnv* env, jobject thiz)
{
    ALOGV("%s:", __FUNCTION__);

    JNIDepthCameraFrameSync* const frameSync = DepthCameraFrameSync_getSync(env, thiz);
    if (frameSync == NULL )
    {
	  //already closed
	  return;
    }

    DepthCameraFrameReader_setNativeSync(env, thiz, NULL);
}

static void DepthCameraImageReader_init(JNIEnv* env, jobject thiz,
                             jint width, jint height, jint format, jint imgType, jboolean imgParam, jbyteArray calibdata,
                             jint maxImages)
{
    status_t res;
    int nativeFormat;

    ALOGV("%s: width:%d, height: %d, format: 0x%x, imgType:%d, maxImages:%d",
          __FUNCTION__, width, height, format, imgType, maxImages);

    nativeFormat = Image_getPixelFormat(env, format);

    sp<IGraphicBufferProducer> gbProducer;
    sp<IGraphicBufferConsumer> gbConsumer;
    BufferQueue::createBufferQueue(&gbProducer, &gbConsumer);
    sp<CpuConsumer> consumer = new CpuConsumer(gbConsumer, maxImages,
                                               /*controlledByApp*/true);
    if (consumer == NULL) {
        jniThrowRuntimeException(env, "Failed to allocate native CpuConsumer");
        return;
    }

    jclass clazz = env->GetObjectClass(thiz);
    if (clazz == NULL) {
        jniThrowRuntimeException(env, "Can't find intel/camera2/extensions/DepthCamera/DepthCameraSetup.DepthFrameReader");
        return;
    }
    sp<JNIDepthCameraImageReaderContext> ctx(new JNIDepthCameraImageReaderContext(maxImages));

    JNIDepthCameraFrameSync* const frameSync = DepthCameraFrameSync_getSync(env, thiz);
    if (frameSync == NULL )
    {
    	 ALOGW("ImageReader#init before calling FrameSync Init");
    	 return;
    }

    ctx->setFrameSync(frameSync);
    frameSync->addImageType(imgType, ctx);
    ctx->setCpuConsumer(consumer);
    ctx->setProducer(gbProducer);
    consumer->setFrameAvailableListener(ctx);
    DepthCameraImageReader_setNativeContext(env, thiz, ctx, imgType);
    ctx->setBufferFormat(nativeFormat);
    ctx->setBufferWidth(width);
    ctx->setBufferHeight(height);
    ctx->setBufferImageType(imgType);

    if ((imgType == IMAGE_TYPE_COLOR || imgType == IMAGE_TYPE_DEPTH) && /*imgParam == true*/ calibdata != NULL)
    {
        jsize len = env->GetArrayLength(calibdata);
    	// Get the elements
    	jbyte* calibdataByte = env->GetByteArrayElements(calibdata, 0);

    	ctx->setCalibrationData((unsigned char*) calibdataByte);

    	env->ReleaseByteArrayElements(calibdata, calibdataByte, 0);
    }
    ctx->setImageParam(imgParam);

    if ( imgType == IMAGE_TYPE_DEPTH && imgParam == true )
    {
    	ctx->initHelperBuffer(maxImages, width*height*2);
    }
    // Set the width/height/format to the CpuConsumer

    res = consumer->setDefaultBufferSize(width, height);
    if (res != OK) {
        jniThrowException(env, "java/lang/IllegalStateException",
                          "Failed to set CpuConsumer buffer size");
        return;
    }
    res = consumer->setDefaultBufferFormat(nativeFormat);
    if (res != OK) {
        jniThrowException(env, "java/lang/IllegalStateException",
                          "Failed to set CpuConsumer buffer format");
    }
}

static void DepthCameraImageReader_close(JNIEnv* env, jobject thiz, jint imgType)
{
    ALOGV("%s:", __FUNCTION__);

    JNIDepthCameraImageReaderContext* const ctx = DepthCameraImageReader_getContext(env, thiz, imgType);
    if (ctx == NULL) {
        // ImageReader is already closed.
        return;
    }

    CpuConsumer* consumer = ctx->getCpuConsumer();
    if (consumer != NULL) {
        consumer->abandon();
        consumer->setFrameAvailableListener(NULL);
    }
    DepthCameraImageReader_setNativeContext(env, thiz, NULL, imgType);
}

static void DepthCameraImageReader_imageRelease(JNIEnv* env, jobject thiz, jobject image, jint imgType)
{
    ALOGV("%s:", __FUNCTION__);
    JNIDepthCameraImageReaderContext* ctx = DepthCameraImageReader_getContext(env, thiz, imgType);
    if (ctx == NULL) {
        ALOGW("ImageReader#close called before Image#close, consider calling Image#close first");
        return;
    }
    CpuConsumer* consumer = ctx->getCpuConsumer();
    CpuConsumer::LockedBuffer* buffer = Image_getLockedBuffer(env, image);
    if (!buffer) {
        ALOGW("Image already released!!!");
        return;
    }

    if ( ctx->getImageParam() == true &&  imgType == IMAGE_TYPE_DEPTH )
    {
       float* uvBuff = Image_getUVMapBuffer(env,image);
       if ( !uvBuff )
       {
           ALOGW("UV Map Buff already released!!!");
           return;
       }
       //else
       ctx->pushHelperBuffer(uvBuff);
       Image_setUVMapBuffer(env,image,NULL);
    }
    consumer->unlockBuffer(*buffer);
    Image_setBuffer(env, image, NULL);
    ctx->returnLockedBuffer(buffer);
}

static jint DepthCameraImageReader_imageSetup(JNIEnv* env, jobject thiz,
                                             jobject image, jint imgType)
{
    ALOGV("%s:", __FUNCTION__);
    JNIDepthCameraImageReaderContext* ctx = DepthCameraImageReader_getContext(env, thiz,imgType);
    if (ctx == NULL) {
        jniThrowRuntimeException(env, "ImageReaderContext is not initialized");
        return -1;
    }

    CpuConsumer* consumer = ctx->getCpuConsumer();
    CpuConsumer::LockedBuffer* buffer = ctx->getLockedBuffer();
    if (buffer == NULL) {
        ALOGW("Unable to acquire a lockedBuffer, very likely client tries to lock more than"
            " maxImages buffers");
        return ACQUIRE_MAX_IMAGES;
    }
    status_t res = consumer->lockNextBuffer(buffer);
    if (res != NO_ERROR) {
        ctx->returnLockedBuffer(buffer);
        if (res != BAD_VALUE /*no buffers*/) {
            if (res == NOT_ENOUGH_DATA) {
                return ACQUIRE_MAX_IMAGES;
            } else {
                ALOGE("%s Fail to lockNextBuffer with error: %d ",
                      __FUNCTION__, res);
                jniThrowExceptionFmt(env, "java/lang/AssertionError",
                          "Unknown error (%d) when we tried to lock buffer.",
                          res);
            }
        }
        return ACQUIRE_NO_BUFFERS;
    }

    if (buffer->format == HAL_PIXEL_FORMAT_YCrCb_420_SP) {
        jniThrowException(env, "java/lang/UnsupportedOperationException",
                "NV21 format is not supported by ImageReader");
        return -1;
    }

    // Check if the left-top corner of the crop rect is origin, we currently assume this point is
    // zero, will revist this once this assumption turns out problematic.
    Point lt = buffer->crop.leftTop();
    if (lt.x != 0 || lt.y != 0) {
        jniThrowExceptionFmt(env, "java/lang/UnsupportedOperationException",
                "crop left top corner [%d, %d] need to be at origin", lt.x, lt.y);
        return -1;
    }

    // Check if the producer buffer configurations match what ImageReader configured.
    // We want to fail for the very first image because this case is too bad.
    int outputWidth = buffer->width;
    int outputHeight = buffer->height;

    // Correct width/height when crop is set.
    if (!buffer->crop.isEmpty()) {
        outputWidth = buffer->crop.getWidth();
        outputHeight = buffer->crop.getHeight();
    }

    int imageReaderWidth = ctx->getBufferWidth();
    int imageReaderHeight = ctx->getBufferHeight();
    if ((buffer->format != HAL_PIXEL_FORMAT_BLOB) &&
            (imageReaderWidth != outputWidth || imageReaderHeight > outputHeight)) {
        /**
         * For video decoder, the buffer height is actually the vertical stride,
         * which is always >= actual image height. For future, decoder need provide
         * right crop rectangle to CpuConsumer to indicate the actual image height,
         * see bug 9563986. After this bug is fixed, we can enforce the height equal
         * check. Right now, only make sure buffer height is no less than ImageReader
         * height.
         */
        jniThrowExceptionFmt(env, "java/lang/IllegalStateException",
                "Producer buffer size: %dx%d, doesn't match ImageReader configured size: %dx%d",
                outputWidth, outputHeight, imageReaderWidth, imageReaderHeight);
        return -1;
    }

    if (ctx->getBufferFormat() != buffer->format) {
        // Return the buffer to the queue.
        consumer->unlockBuffer(*buffer);
        ctx->returnLockedBuffer(buffer);

        // Throw exception
        ALOGE("Producer output buffer format: 0x%x, ImageReader configured format: 0x%x",
              buffer->format, ctx->getBufferFormat());
        String8 msg;
        msg.appendFormat("The producer output buffer format 0x%x doesn't "
                "match the ImageReader's configured buffer format 0x%x.",
                buffer->format, ctx->getBufferFormat());
        jniThrowException(env, "java/lang/UnsupportedOperationException",
                msg.string());
        return -1;
    }
    if ( imgType == IMAGE_TYPE_DEPTH && ctx->getImageParam() == true )
    {
        //get and save buffer for uv mapping
        float* uvMapBuffer = ctx->popHelperBuffer();
        if (uvMapBuffer == NULL )
        {
            ALOGW("%s Fail to lock uv Map buffer - no more buffs available ", __FUNCTION__);
            consumer->unlockBuffer(*buffer); //to allow other images
            ctx->returnLockedBuffer(buffer);
            return ACQUIRE_MAX_IMAGES;
        }
        //perform UV Mapping when depth data is ready
        Image_setUVMapBuffer(env, image, uvMapBuffer);
    }
    // Set SurfaceImage instance member variables
    Image_setBuffer(env, image, buffer);
    env->SetLongField(image, gSurfaceImageClassInfo.mTimestamp,
            static_cast<jlong>(buffer->timestamp));

    return ACQUIRE_SUCCESS;
}

static jint DepthCameraImageReader_getSurfaceWidth(JNIEnv* env, jobject thiz, jobject surface)
{
    ALOGV("%s: ", __FUNCTION__);
    sp<ANativeWindow> anw = android_view_Surface_getNativeWindow(env, surface);
    int width;
    if ((anw->query(anw.get(), NATIVE_WINDOW_WIDTH, &width)) != OK) {
           ALOGE("%s: Failed to query Surface width", __FUNCTION__);
           return 0;
    }

    return width;
}

static jint DepthCameraImageReader_getSurfaceHeight(JNIEnv* env, jobject thiz, jobject surface)
{
    ALOGV("%s: ", __FUNCTION__);
    sp<ANativeWindow> anw = android_view_Surface_getNativeWindow(env, surface);
    int height;
    if ((anw->query(anw.get(), NATIVE_WINDOW_HEIGHT, &height)) != OK) {
              ALOGE("%s: Failed to query Surface height", __FUNCTION__);
               return 0;
    }

    return height;
}

static jint DepthCameraImageReader_getSurfaceFormat(JNIEnv* env, jobject thiz, jobject surface)
{
    ALOGV("%s: ", __FUNCTION__);
    sp<ANativeWindow> anw = android_view_Surface_getNativeWindow(env, surface);
    int format;

    if ((anw->query(anw.get(), NATIVE_WINDOW_FORMAT, &format)) != OK) {
           ALOGE("%s: Failed to query Surface height", __FUNCTION__);
           return 0;
    }

    return format;
}

static jobject DepthCameraImageReader_getSurface(JNIEnv* env, jobject thiz, jint imgType)
{
    ALOGV("%s: ", __FUNCTION__);

    IGraphicBufferProducer* producer = DepthCameraImageReader_getProducer(env, thiz,imgType);
    if (producer == NULL) {
        jniThrowRuntimeException(env, "CpuConsumer is uninitialized");
        return NULL;
    }

    // Wrap the IGBP in a Java-language Surface.
    return android_view_Surface_createFromIGraphicBufferProducer(env, producer);
}

static jobject Image_createSurfacePlane(JNIEnv* env, jobject thiz, int idx)
{
    int rowStride, pixelStride;
    ALOGV("%s: buffer index: %d", __FUNCTION__, idx);

    CpuConsumer::LockedBuffer* buffer = Image_getLockedBuffer(env, thiz);

    ALOG_ASSERT(buffer != NULL);
    if (buffer == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException", "Image_createSurfacePlane: Image was released");
    }
    rowStride = Image_imageGetRowStride(env, buffer, idx);
    pixelStride = Image_imageGetPixelStride(env, buffer, idx);

    jobject surfPlaneObj = env->NewObject(gSurfacePlaneClassInfo.clazz,
            gSurfacePlaneClassInfo.ctor, thiz, idx, rowStride, pixelStride);

    return surfPlaneObj;
}
/////////////////////////////////////////////////////////
static const unsigned int hist_size = 256 * 256;
static const unsigned int table_nr_colors = 2;
static const unsigned int table_size = (table_nr_colors - 1) * 256 * 3;
static uint8_t depth_colormap[table_size];
static int depth_histogram[hist_size];

// initializes the hue space color ramp
void initDepthColorMap(void)
{
	uint8_t colors[table_nr_colors][3] = {
		{255, 0, 0},
		{20, 40, 255}
	};
	for (unsigned int k = 0; k < table_nr_colors - 1; k++) {
		for (int i = 0; i < 256; i++) {
			int j = 3 * (k * 256 + i);
			double a = double(i) / 255;
			depth_colormap[j] = uint8_t((1 - a) * colors[k][0] + a * colors[k + 1][0]);
			depth_colormap[j + 1] = uint8_t((1 - a) * colors[k][1] + a * colors[k + 1][1]);
			depth_colormap[j + 2] = uint8_t((1 - a) * colors[k][2] + a * colors[k + 1][2]);
		}
	}

	depth_colormap[0] = depth_colormap[1] = depth_colormap[2] = 0;
}


static void uvMapToARGB8888(const void *inUVMap, const void* inColorPixels,
			    void *out,
			    int depthWidth, int depthHeight, int colorWidth, int colorHeight)
{
	int stride = depthWidth;
	if ( inUVMap == NULL || inColorPixels == NULL || out == NULL )
	{
		ALOGW("%s invalid inputs", __FUNCTION__);
		return;
	}
	int* uvmapRGB = (int*) out;
	float* uvMap = (float*) inUVMap;
	int* colorPixels = (int*) inColorPixels;
	int x,y;
	int idx = 0;
	int uvMapIdx = 0;

	for ( int row = 0; row < depthHeight ; row++)
	{
		for ( int col = 0; col < depthWidth; col++)
		{
			x = (int) uvMap[uvMapIdx++];
			y = (int) uvMap[uvMapIdx++];
			if (!(x==0 && y==0) && ( x < colorWidth && y < colorHeight ) && (x>=0 && y>=0))
			{
				uvmapRGB[idx+col]  = colorPixels[(colorWidth*y + x)];
			}
			else
			{
				uvmapRGB[idx+col]  = 0xFF000000;
			}
		}
		idx+=stride;
	}
}
static void z16ToRGBA8888(const void *in,
			    void *out,
			    int depthsize)
{
	ALOGV("%s: Start ", __FUNCTION__);
	const uint16_t *depth = (const uint16_t *)in;
	uint8_t *rgb = (uint8_t *)out;
	int nr_valid = 0;
	unsigned int wh = depthsize/2; //working with int16 not bytes
	unsigned int i;
    int x;
	static bool init = false;

	if (!init) {
		initDepthColorMap();
		init = true;
	}

	// Build cumulative histogram
	memset(depth_histogram, 0, hist_size * sizeof(int));

	for (i = 0; i < wh; i++) {
		uint16_t d = depth[i];
		if (d) {
			depth_histogram[d]++;
			nr_valid++;
		}
	}
	for (i = 1; i < hist_size; i++)
		depth_histogram[i] += depth_histogram[i - 1];

	unsigned int k = table_size / 3 - 1;
	double n = 1.0 / double(nr_valid);
	for (i = 1; i < hist_size; i++)
		depth_histogram[i] = (int)(k * depth_histogram[i] * n);

	// Set rgb values
	i = 0;

	int rgbsize = depthsize*2; //in bytes, 32 bits , depth is 16 bits
	for (x = 0; x < rgbsize; x += 4)
	{
		uint16_t d = depth[i++];
		int v = 3 * depth_histogram[d];
		rgb[x] = depth_colormap[v];
		rgb[x + 1] = depth_colormap[v + 1];
		rgb[x + 2] = depth_colormap[v + 2];
		rgb[x + 3] = 0xff;
	}
}

static void Image_convertUVMapToRGB(JNIEnv* env, jobject thiz,jobject colorPixelsSrc,jobject uvMapSrc, jobject dst,  int depthWidth, int depthHeight, int colorWidth, int colorHeight)
{
	if ( depthWidth <= 0 ||  depthHeight <= 0)
	{
		ALOGW("%s: depth buffer sizes is not valid: w %d h %d", __FUNCTION__, depthWidth, depthHeight);
		return;
	}
	if ( colorWidth <= 0 ||  colorHeight <= 0)
	{
		ALOGW("%s: color buffer sizes is not valid: w %d h %d", __FUNCTION__, colorWidth, colorHeight);
		return;
	}
	void *colorPixels = env->GetDirectBufferAddress(colorPixelsSrc);
	void *uvMap = env->GetDirectBufferAddress(uvMapSrc);
	void *uvMapRGB = env->GetDirectBufferAddress(dst);

	if (colorPixels == NULL)
	{
		jniThrowRuntimeException(env, "Can't convert colorPixelsSrc direct buffer address");
		return;
	}
	if (uvMap == NULL)
	{
		jniThrowRuntimeException(env, "Can't convert uvMapSrc direct buffer address");
		return;
	}
	if (uvMapRGB == NULL)
	{
		jniThrowRuntimeException(env, "Can't convert dst direct buffer address");
		return;
	}
	uvMapToARGB8888(uvMap,colorPixels, uvMapRGB, depthWidth,  depthHeight, colorWidth, colorHeight);
	return;
}

static void Image_convertZ16ToRGB(JNIEnv* env, jobject thiz,jobject src, jobject dst, int depthsize)
{
	if ( depthsize <= 0 )
	{
		ALOGW("%s: depth buffer size is not valid: %d", __FUNCTION__, depthsize);
		return;
	}
	void *depthBuf = env->GetDirectBufferAddress(src);
	void *rgbBuf = env->GetDirectBufferAddress(dst);

	if (depthBuf == NULL)
	{
		jniThrowRuntimeException(env, "Can't convert src direct buffer address");
		return;
	}
	if (rgbBuf == NULL)
	{
		jniThrowRuntimeException(env, "Can't convert dst direct buffer address");
		return;
	}
	z16ToRGBA8888(depthBuf, rgbBuf, depthsize);
	return;
}

static jobject Image_getByteBuffer(JNIEnv* env, jobject thiz, int idx)
{
    uint8_t *base = NULL;
    uint32_t size = 0;
    jobject byteBuffer;

    ALOGV("%s: buffer index: %d", __FUNCTION__, idx);

    CpuConsumer::LockedBuffer* buffer = Image_getLockedBuffer(env, thiz);

    if (buffer == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException", "Image was released");
    }

    // Create byteBuffer from native buffer
    Image_getLockedBufferInfo(env, buffer, idx, &base, &size);
    byteBuffer = env->NewDirectByteBuffer(base, size);
    // TODO: throw dvm exOutOfMemoryError?
    if ((byteBuffer == NULL) && (env->ExceptionCheck() == false)) {
        jniThrowException(env, "java/lang/IllegalStateException", "Failed to allocate ByteBuffer");
    }

    return byteBuffer;
}

static jobject Image_getUVMapByteBuffer(JNIEnv* env, jobject thiz, jobject ctxThiz, jboolean isColorRectified,
    jint colorFormat, jint colorWidth, jint colorHeight)
{
    
	ALOGV("%s:", __FUNCTION__);
    float* uvMap = Image_getUVMapBuffer(env,thiz);
    if (uvMap == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException", "Image UV MAP was released");
    }

    uint32_t size = 0;
    jobject byteBuffer;
    JNIDepthCameraImageReaderContext* depthCtx = DepthCameraImageReader_getContext(env, ctxThiz,IMAGE_TYPE_DEPTH);
    if (depthCtx == NULL) {
        jniThrowRuntimeException(env, "ImageReaderContext is not initialized");
    }

    DSCalibRectParameters calibData = depthCtx->getCalibrationData();

    CpuConsumer::LockedBuffer* buffer = Image_getLockedBuffer(env, thiz);

    if (buffer == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException", "Image_getUVMapByteBuffer Image was released");
    }

    uint16_t *depthData = (uint16_t *)buffer->data;

    //Fill uvMap
    depthCtx->performUVMapping((float*)uvMap ,isColorRectified, colorFormat,
    colorWidth, colorHeight, calibData, depthData,
    depthCtx->getBufferWidth(), depthCtx->getBufferHeight());

    // Create byteBuffer from native buffer
    size = depthCtx->getBufferWidth()* depthCtx->getBufferHeight() * 2 *sizeof(float);
    byteBuffer = env->NewDirectByteBuffer((char*)uvMap, size);
    if ((byteBuffer == NULL) && (env->ExceptionCheck() == false)) {
       jniThrowException(env, "java/lang/IllegalStateException", "Failed to allocate ByteBuffer");
    }

    return byteBuffer;
}


} // extern "C"

// ----------------------------------------------------------------------------


static JNINativeMethod gDepthImageReaderMethods[] = {
	{"nativeClassInit",        "()V",                        (void*)DepthCameraImageReader_classInit },
    {"nativeFrameSyncClose", "()V",        (void*)DepthCameraFrameSync_close},
    {"nativeFrameSyncInit", "(Ljava/lang/Object;)V", (void*) DepthCameraFrameSync_init },
    {"nativeInit",             "(IIIIZ[BI)V",  (void*)DepthCameraImageReader_init },
    {"nativeClose",            "(I)V",                        (void*)DepthCameraImageReader_close },
    {"nativeReleaseImage",     "(Landroid/media/Image;I)V",   (void*)DepthCameraImageReader_imageRelease },
    {"nativeImageSetup",       "(Landroid/media/Image;I)I",    (void*)DepthCameraImageReader_imageSetup },
    {"nativeGetSurface",       "(I)Landroid/view/Surface;",   (void*)DepthCameraImageReader_getSurface },
    {"nativeGetSurfaceHeight", "(Landroid/view/Surface;)I", (void*)DepthCameraImageReader_getSurfaceHeight },
    {"nativeGetSurfaceWidth", "(Landroid/view/Surface;)I", (void*)DepthCameraImageReader_getSurfaceWidth },
    {"nativeGetSurfaceFormat", "(Landroid/view/Surface;)I", (void*)DepthCameraImageReader_getSurfaceFormat },
};

static JNINativeMethod gImageMethods[] = {
    {"nativeImageGetBuffer",   "(I)Ljava/nio/ByteBuffer;",   (void*)Image_getByteBuffer },
    {"nativeImageGetUVMapBuffer", "(Lcom/intel/camera2/extensions/depthcamera/DepthCameraSetup$DepthFrameReader;ZIII)Ljava/nio/ByteBuffer;",   (void*)Image_getUVMapByteBuffer },
    {"nativeCreatePlane",      "(I)Lcom/intel/camera2/extensions/depthcamera/DepthCameraSetup$DepthFrameReader$SurfaceImage$SurfacePlane;",
                                                             (void*)Image_createSurfacePlane },
};

static JNINativeMethod gDepthImageMethods[] = {
    {"nativeConvertBuffToRGBFormat",   "(Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;I)V",   (void*)Image_convertZ16ToRGB },
    {"nativeConvertUVMapBuffToRGBFormat",   "(Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;IIII)V",   (void*)Image_convertUVMapToRGB},
};


int register_intel_camera2_extensions_depthcamera_DepthCameraSetup(JNIEnv *env) {

	ALOGV("%s - registering gDepthImageReaderMethods", __FUNCTION__);
    int ret1 = AndroidRuntime::registerNativeMethods(env,
                   "com/intel/camera2/extensions/depthcamera/DepthCameraSetup$DepthFrameReader", gDepthImageReaderMethods, NELEM(gDepthImageReaderMethods));
    int ret2 = AndroidRuntime::registerNativeMethods(env,
                   "com/intel/camera2/extensions/depthcamera/DepthCameraSetup$DepthFrameReader$SurfaceImage", gImageMethods, NELEM(gImageMethods));
    int ret3 = AndroidRuntime::registerNativeMethods(env,
                       "com/intel/camera2/extensions/depthcamera/DepthCameraSetup$DepthFrameReader$FrameReaderDepthImage", gDepthImageMethods, NELEM(gDepthImageMethods));
    return (ret1 || ret2 );
}

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
    	ALOGE("ERROR: GetEnv failed\n");
        goto fail;
    }
    assert(env != NULL);

    if (register_intel_camera2_extensions_depthcamera_DepthCameraSetup(env) < 0) {
        ALOGE("ERROR: native registration failed\n");
        goto fail;
    }
    result = JNI_VERSION_1_4;

fail:
    return result;
}
