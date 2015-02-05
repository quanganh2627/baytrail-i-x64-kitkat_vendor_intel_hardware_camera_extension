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
#define LOG_TAG "DepthCameraImageReader_JNI"
#include <utils/Log.h>
#include <utils/misc.h>
#include <utils/List.h>
#include <utils/String8.h>

#include <cstdio>

#include <gui/CpuConsumer.h>
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

#define ANDROID_MEDIA_IMAGEREADER_CTX_JNI_ID       "mNativeContext"
#define ANDROID_MEDIA_SURFACEIMAGE_BUFFER_JNI_ID   "mLockedBuffer"
#define ANDROID_MEDIA_SURFACEIMAGE_TS_JNI_ID       "mTimestamp"

// ----------------------------------------------------------------------------

using namespace android;

enum {
    IMAGE_READER_MAX_NUM_PLANES = 3,
};

enum {
    ACQUIRE_SUCCESS = 0,
    ACQUIRE_NO_BUFFERS = 1,
    ACQUIRE_MAX_IMAGES = 2,
};

static struct {
    jfieldID mNativeContext;
    jmethodID postEventFromNative;
} gDepthCameraImageReaderClassInfo;

static struct {
    jfieldID mLockedBuffer;
    jfieldID mTimestamp;
} gSurfaceImageClassInfo;

static struct {
    jclass clazz;
    jmethodID ctor;
} gSurfacePlaneClassInfo;

// ----------------------------------------------------------------------------

class JNIDepthCameraImageReaderContext : public CpuConsumer::FrameAvailableListener
{
public:
    JNIDepthCameraImageReaderContext(JNIEnv* env, jobject weakThiz, jclass clazz, int maxImages);

    virtual ~JNIDepthCameraImageReaderContext();

    virtual void onFrameAvailable();

    CpuConsumer::LockedBuffer* getLockedBuffer();

    void returnLockedBuffer(CpuConsumer::LockedBuffer* buffer);

    void setCpuConsumer(const sp<CpuConsumer>& consumer) { mConsumer = consumer; }
    CpuConsumer* getCpuConsumer() { return mConsumer.get(); }

    void setProducer(const sp<IGraphicBufferProducer>& producer) { mProducer = producer; }
    IGraphicBufferProducer* getProducer() { return mProducer.get(); }

    void setBufferFormat(int format) { mFormat = format; }
    int getBufferFormat() { return mFormat; }

    void setBufferWidth(int width) { mWidth = width; }
    int getBufferWidth() { return mWidth; }

    void setBufferHeight(int height) { mHeight = height; }
    int getBufferHeight() { return mHeight; }

private:
    static JNIEnv* getJNIEnv(bool* needsDetach);
    static void detachJNI();

    List<CpuConsumer::LockedBuffer*> mBuffers;
    sp<CpuConsumer> mConsumer;
    sp<IGraphicBufferProducer> mProducer;
    jobject mWeakThiz;
    jclass mClazz;
    int mFormat;
    int mWidth;
    int mHeight;
};

JNIDepthCameraImageReaderContext::JNIDepthCameraImageReaderContext(JNIEnv* env,
        jobject weakThiz, jclass clazz, int maxImages) :
    mWeakThiz(env->NewGlobalRef(weakThiz)),
    mClazz((jclass)env->NewGlobalRef(clazz)) {
    for (int i = 0; i < maxImages; i++) {
        CpuConsumer::LockedBuffer *buffer = new CpuConsumer::LockedBuffer;
        mBuffers.push_back(buffer);
    }
}

JNIEnv* JNIDepthCameraImageReaderContext::getJNIEnv(bool* needsDetach) {
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

void JNIDepthCameraImageReaderContext::detachJNI() {
    JavaVM* vm = AndroidRuntime::getJavaVM();
    int result = vm->DetachCurrentThread();
    if (result != JNI_OK) {
        ALOGE("thread detach failed: %#x", result);
    }
}

CpuConsumer::LockedBuffer* JNIDepthCameraImageReaderContext::getLockedBuffer() {
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
    mBuffers.push_back(buffer);
}

JNIDepthCameraImageReaderContext::~JNIDepthCameraImageReaderContext() {
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

    // Delete LockedBuffers
    for (List<CpuConsumer::LockedBuffer *>::iterator it = mBuffers.begin();
            it != mBuffers.end(); it++) {
        delete *it;
    }
    mBuffers.clear();
    mConsumer.clear();
}

void JNIDepthCameraImageReaderContext::onFrameAvailable()
{
    ALOGV("%s: frame available", __FUNCTION__);
    bool needsDetach = false;
    JNIEnv* env = getJNIEnv(&needsDetach);
    if (env != NULL) {
        env->CallStaticVoidMethod(mClazz, gDepthCameraImageReaderClassInfo.postEventFromNative, mWeakThiz);
    } else {
        ALOGW("onFrameAvailable event will not posted");
    }
    if (needsDetach) {
        detachJNI();
    }
}

// ----------------------------------------------------------------------------

extern "C" {

static JNIDepthCameraImageReaderContext* DepthCameraImageReader_getContext(JNIEnv* env, jobject thiz)
{
    JNIDepthCameraImageReaderContext *ctx;
    ctx = reinterpret_cast<JNIDepthCameraImageReaderContext *>
          (env->GetLongField(thiz, gDepthCameraImageReaderClassInfo.mNativeContext));
    return ctx;
}

static CpuConsumer* DepthCameraImageReader_getCpuConsumer(JNIEnv* env, jobject thiz)
{
    ALOGV("%s:", __FUNCTION__);
    JNIDepthCameraImageReaderContext* const ctx = DepthCameraImageReader_getContext(env, thiz);
    if (ctx == NULL) {
        jniThrowRuntimeException(env, "ImageReaderContext is not initialized");
        return NULL;
    }
    return ctx->getCpuConsumer();
}

static IGraphicBufferProducer* DepthCameraImageReader_getProducer(JNIEnv* env, jobject thiz)
{
    ALOGV("%s:", __FUNCTION__);
    JNIDepthCameraImageReaderContext* const ctx = DepthCameraImageReader_getContext(env, thiz);
    if (ctx == NULL) {
        jniThrowRuntimeException(env, "ImageReaderContext is not initialized");
        return NULL;
    }
    return ctx->getProducer();
}

static void DepthCameraImageReader_setNativeContext(JNIEnv* env,
        jobject thiz, sp<JNIDepthCameraImageReaderContext> ctx)
{
    ALOGV("%s:", __FUNCTION__);
    JNIDepthCameraImageReaderContext* const p = DepthCameraImageReader_getContext(env, thiz);
    if (ctx != 0) {
        ctx->incStrong((void*)DepthCameraImageReader_setNativeContext);
    }
    if (p) {
        p->decStrong((void*)DepthCameraImageReader_setNativeContext);
    }
    env->SetLongField(thiz, gDepthCameraImageReaderClassInfo.mNativeContext,
                      reinterpret_cast<jlong>(ctx.get()));
}

static CpuConsumer::LockedBuffer* Image_getLockedBuffer(JNIEnv* env, jobject image)
{
    return reinterpret_cast<CpuConsumer::LockedBuffer*>(
               env->GetLongField(image, gSurfaceImageClassInfo.mLockedBuffer));
}

static void Image_setBuffer(JNIEnv* env, jobject thiz,
                            const CpuConsumer::LockedBuffer* buffer)
{
    env->SetLongField(thiz, gSurfaceImageClassInfo.mLockedBuffer, reinterpret_cast<jlong>(buffer));
}

// Some formats like JPEG defined with different values between android.graphics.ImageFormat and
// graphics.h, need convert to the one defined in graphics.h here.
static int Image_getPixelFormat(JNIEnv* env, int format)
{
    int jpegFormat;
    jfieldID fid;

    ALOGV("%s: format = 0x%x", __FUNCTION__, format);

    jclass imageFormatClazz = env->FindClass("android/graphics/ImageFormat");
    ALOG_ASSERT(imageFormatClazz != NULL);

    fid = env->GetStaticFieldID(imageFormatClazz, "JPEG", "I");
    jpegFormat = env->GetStaticIntField(imageFormatClazz, fid);

    // Translate the JPEG to BLOB for camera purpose.
    if (format == jpegFormat) {
        format = HAL_PIXEL_FORMAT_BLOB;
    }

    return format;
}

static uint32_t Image_getJpegSize(CpuConsumer::LockedBuffer* buffer, bool usingRGBAOverride)
{
    ALOG_ASSERT(buffer != NULL, "Input buffer is NULL!!!");
    uint32_t size = 0;
    uint32_t width = buffer->width;
    uint8_t* jpegBuffer = buffer->data;

    if (usingRGBAOverride) {
        width = (buffer->width + buffer->stride * (buffer->height - 1)) * 4;
    }

    // First check for JPEG transport header at the end of the buffer
    uint8_t* header = jpegBuffer + (width - sizeof(struct camera3_jpeg_blob));
    struct camera3_jpeg_blob *blob = (struct camera3_jpeg_blob*)(header);
    if (blob->jpeg_blob_id == CAMERA3_JPEG_BLOB_ID) {
        size = blob->jpeg_size;
        ALOGV("%s: Jpeg size = %d", __FUNCTION__, size);
    }

    // failed to find size, default to whole buffer
    if (size == 0) {
        /*
         * This is a problem because not including the JPEG header
         * means that in certain rare situations a regular JPEG blob
         * will be misidentified as having a header, in which case
         * we will get a garbage size value.
         */
        ALOGW("%s: No JPEG header detected, defaulting to size=width=%d",
              __FUNCTION__, width);
        size = width;
    }

    return size;
}

static bool usingRGBAToJpegOverride(int32_t bufferFormat, int32_t readerCtxFormat) {
    return readerCtxFormat == HAL_PIXEL_FORMAT_BLOB && bufferFormat == HAL_PIXEL_FORMAT_RGBA_8888;
}

static int32_t applyFormatOverrides(int32_t bufferFormat, int32_t readerCtxFormat)
{
    // Using HAL_PIXEL_FORMAT_RGBA_8888 gralloc buffers containing JPEGs to get around SW
    // write limitations for some platforms (b/17379185).
    if (usingRGBAToJpegOverride(bufferFormat, readerCtxFormat)) {
        return HAL_PIXEL_FORMAT_BLOB;
    }
    return bufferFormat;
}

static void Image_getLockedBufferInfo(JNIEnv* env, CpuConsumer::LockedBuffer* buffer, int idx,
                                      uint8_t **base, uint32_t *size, int32_t readerFormat)
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

    bool usingRGBAOverride = usingRGBAToJpegOverride(fmt, readerFormat);
    fmt = applyFormatOverrides(fmt, readerFormat);
    switch (fmt) {
        case HAL_PIXEL_FORMAT_Z16_INTEL:
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            bytesPerPixel = 2;
            dataSize = buffer->stride * buffer->height * bytesPerPixel;
            pData = buffer->data;
            break;
        case HAL_PIXEL_FORMAT_UVMAP64_INTEL:
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            bytesPerPixel = 8; //2*sizeof(float);
            dataSize = buffer->stride * buffer->height * bytesPerPixel;
            pData = buffer->data;
            break;


        default:
            jniThrowExceptionFmt(env, "java/lang/UnsupportedOperationException",
                                 "format: 0x%x is unsupported by DepthCameraImageReader", fmt);
            break;
    }

    *base = pData;
    *size = dataSize;
}

static jint Image_imageGetPixelStride(JNIEnv* env, CpuConsumer::LockedBuffer* buffer, int idx,
                                      int32_t readerFormat)
{
    ALOGV("%s: buffer index: %d", __FUNCTION__, idx);
    ALOG_ASSERT((idx < IMAGE_READER_MAX_NUM_PLANES) && (idx >= 0), "Index is out of range:%d", idx);

    int pixelStride = 0;
    ALOG_ASSERT(buffer != NULL, "buffer is NULL");

    int32_t fmt = buffer->format;

    fmt = applyFormatOverrides(fmt, readerFormat);

    switch (fmt) {
        case HAL_PIXEL_FORMAT_Z16_INTEL:
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            pixelStride = 2;
            break;
        case HAL_PIXEL_FORMAT_UVMAP64_INTEL:
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            pixelStride = 8;//2*sizeof(float);
            break;
        default:
            jniThrowExceptionFmt(env, "java/lang/UnsupportedOperationException",
                                 "format: 0x%x is unsupported by DepthCameraImageReader", fmt);
            break;
    }

    return pixelStride;
}

static jint Image_imageGetRowStride(JNIEnv* env, CpuConsumer::LockedBuffer* buffer, int idx,
                                    int32_t readerFormat)
{
    ALOGV("%s: buffer index: %d", __FUNCTION__, idx);
    ALOG_ASSERT((idx < IMAGE_READER_MAX_NUM_PLANES) && (idx >= 0));

    int rowStride = 0;
    ALOG_ASSERT(buffer != NULL, "buffer is NULL");

    int32_t fmt = buffer->format;

    fmt = applyFormatOverrides(fmt, readerFormat);

    switch (fmt) {
        case HAL_PIXEL_FORMAT_Z16_INTEL:
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            rowStride = buffer->stride;
            break;
        case HAL_PIXEL_FORMAT_UVMAP64_INTEL:
            ALOG_ASSERT(idx == 0, "Wrong index: %d", idx);
            rowStride = buffer->stride * sizeof(float);
            break;
        default:
            jniThrowExceptionFmt(env, "java/lang/UnsupportedOperationException",
                                 "format: 0x%x is unsupported by DepthCameraImageReader", fmt);
            break;
    }

    return rowStride;
}

// ----------------------------------------------------------------------------

static void DepthCameraImageReader_classInit(JNIEnv* env, jclass clazz)
{
    ALOGV("%s:", __FUNCTION__);

    jclass imageClazz = env->FindClass("com/intel/camera2/extensions/depthcamera/DepthCameraImageReader$SurfaceImage");
    LOG_ALWAYS_FATAL_IF(imageClazz == NULL,
                        "can't find com/intel/camera2/extensions/depthcamera/DepthCameraImageReader$SurfaceImage");
    gSurfaceImageClassInfo.mLockedBuffer = env->GetFieldID(
            imageClazz, ANDROID_MEDIA_SURFACEIMAGE_BUFFER_JNI_ID, "J");
    LOG_ALWAYS_FATAL_IF(gSurfaceImageClassInfo.mLockedBuffer == NULL,
                        "can't find com/intel/camera2/extensions/depthcamera/DepthCameraImageReader.%s",
                        ANDROID_MEDIA_SURFACEIMAGE_BUFFER_JNI_ID);

    gSurfaceImageClassInfo.mTimestamp = env->GetFieldID(
                                            imageClazz, ANDROID_MEDIA_SURFACEIMAGE_TS_JNI_ID, "J");
    LOG_ALWAYS_FATAL_IF(gSurfaceImageClassInfo.mTimestamp == NULL,
                        "can't find com/intel/camera2/extensions/depthcamera/DepthCameraImageReader.%s",
                        ANDROID_MEDIA_SURFACEIMAGE_TS_JNI_ID);

    gDepthCameraImageReaderClassInfo.mNativeContext = env->GetFieldID(
                clazz, ANDROID_MEDIA_IMAGEREADER_CTX_JNI_ID, "J");
    LOG_ALWAYS_FATAL_IF(gDepthCameraImageReaderClassInfo.mNativeContext == NULL,
                        "can't find com/intel/camera2/extensions/depthcamera/DepthCameraImageReader.%s",
                        ANDROID_MEDIA_IMAGEREADER_CTX_JNI_ID);

    gDepthCameraImageReaderClassInfo.postEventFromNative = env->GetStaticMethodID(
                clazz, "postEventFromNative", "(Ljava/lang/Object;)V");
    LOG_ALWAYS_FATAL_IF(gDepthCameraImageReaderClassInfo.postEventFromNative == NULL,
                        "can't find com/intel/camera2/extensions/depthcamera/DepthCameraImageReader.postEventFromNative");

    jclass planeClazz = env->FindClass("com/intel/camera2/extensions/depthcamera/DepthCameraImageReader$SurfaceImage$SurfacePlane");
    LOG_ALWAYS_FATAL_IF(planeClazz == NULL, "Can not find SurfacePlane class");
    // FindClass only gives a local reference of jclass object.
    gSurfacePlaneClassInfo.clazz = (jclass) env->NewGlobalRef(planeClazz);
    gSurfacePlaneClassInfo.ctor = env->GetMethodID(gSurfacePlaneClassInfo.clazz, "<init>",
                                  "(Lcom/intel/camera2/extensions/depthcamera/DepthCameraImageReader$SurfaceImage;III)V");
    LOG_ALWAYS_FATAL_IF(gSurfacePlaneClassInfo.ctor == NULL,
                        "Can not find SurfacePlane constructor");
}

static void DepthCameraImageReader_init(JNIEnv* env, jobject thiz, jobject weakThiz,
                                        jint width, jint height, jint format, jint maxImages)
{
    status_t res;
    int nativeFormat;

    ALOGV("%s: width:%d, height: %d, format: 0x%x, maxImages:%d",
          __FUNCTION__, width, height, format, maxImages);

    nativeFormat = Image_getPixelFormat(env, format);

    sp<IGraphicBufferProducer> gbProducer;
    sp<IGraphicBufferConsumer> gbConsumer;
    BufferQueue::createBufferQueue(&gbProducer, &gbConsumer);
    sp<CpuConsumer> consumer = new CpuConsumer(gbConsumer, maxImages,
            /*controlledByApp*/true);
    // TODO: throw dvm exOutOfMemoryError?
    if (consumer == NULL) {
        jniThrowRuntimeException(env, "Failed to allocate native CpuConsumer");
        return;
    }

    jclass clazz = env->GetObjectClass(thiz);
    if (clazz == NULL) {
        jniThrowRuntimeException(env, "Can't find com/intel/camera2/extensions/depthcamera/DepthCameraImageReader");
        return;
    }
    sp<JNIDepthCameraImageReaderContext> ctx(new JNIDepthCameraImageReaderContext(env, weakThiz, clazz, maxImages));
    ctx->setCpuConsumer(consumer);
    ctx->setProducer(gbProducer);
    consumer->setFrameAvailableListener(ctx);
    DepthCameraImageReader_setNativeContext(env, thiz, ctx);
    ctx->setBufferFormat(nativeFormat);
    ctx->setBufferWidth(width);
    ctx->setBufferHeight(height);

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

static void DepthCameraImageReader_close(JNIEnv* env, jobject thiz)
{
    ALOGV("%s:", __FUNCTION__);

    JNIDepthCameraImageReaderContext* const ctx = DepthCameraImageReader_getContext(env, thiz);
    if (ctx == NULL) {
        // ImageReader is already closed.
        return;
    }

    CpuConsumer* consumer = DepthCameraImageReader_getCpuConsumer(env, thiz);
    if (consumer != NULL) {
        consumer->abandon();
        consumer->setFrameAvailableListener(NULL);
    }
    DepthCameraImageReader_setNativeContext(env, thiz, NULL);
}

static void DepthCameraImageReader_imageRelease(JNIEnv* env, jobject thiz, jobject image)
{
    ALOGV("%s:", __FUNCTION__);
    JNIDepthCameraImageReaderContext* ctx = DepthCameraImageReader_getContext(env, thiz);
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
    consumer->unlockBuffer(*buffer);
    Image_setBuffer(env, image, NULL);
    ctx->returnLockedBuffer(buffer);
}

static jint DepthCameraImageReader_imageSetup(JNIEnv* env, jobject thiz,
        jobject image)
{
    ALOGV("%s:", __FUNCTION__);
    JNIDepthCameraImageReaderContext* ctx = DepthCameraImageReader_getContext(env, thiz);
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

    int imgReaderFmt = ctx->getBufferFormat();
    int imageReaderWidth = ctx->getBufferWidth();
    int imageReaderHeight = ctx->getBufferHeight();
    if ((buffer->format != HAL_PIXEL_FORMAT_BLOB) && (imgReaderFmt != HAL_PIXEL_FORMAT_BLOB) &&
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

    int bufFmt = buffer->format;
    if (imgReaderFmt != bufFmt) {

        if (imgReaderFmt == HAL_PIXEL_FORMAT_YCbCr_420_888 && (bufFmt ==
                HAL_PIXEL_FORMAT_YCrCb_420_SP || bufFmt == HAL_PIXEL_FORMAT_YV12)) {
            // Special casing for when producer switches to a format compatible with flexible YUV
            // (HAL_PIXEL_FORMAT_YCbCr_420_888).
            ctx->setBufferFormat(bufFmt);
            ALOGD("%s: Overriding buffer format YUV_420_888 to %x.", __FUNCTION__, bufFmt);
        } else if (imgReaderFmt == HAL_PIXEL_FORMAT_BLOB && bufFmt == HAL_PIXEL_FORMAT_RGBA_8888) {
            // Using HAL_PIXEL_FORMAT_RGBA_8888 gralloc buffers containing JPEGs to get around SW
            // write limitations for (b/17379185).
            ALOGD("%s: Receiving JPEG in HAL_PIXEL_FORMAT_RGBA_8888 buffer.", __FUNCTION__);
        } else {
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
    }
    // Set SurfaceImage instance member variables
    Image_setBuffer(env, image, buffer);
    env->SetLongField(image, gSurfaceImageClassInfo.mTimestamp,
                      static_cast<jlong>(buffer->timestamp));

    return ACQUIRE_SUCCESS;
}

static jobject DepthCameraImageReader_getSurface(JNIEnv* env, jobject thiz)
{
    ALOGV("%s: ", __FUNCTION__);

    IGraphicBufferProducer* gbp = DepthCameraImageReader_getProducer(env, thiz);
    if (gbp == NULL) {
        jniThrowRuntimeException(env, "CpuConsumer is uninitialized");
        return NULL;
    }

    // Wrap the IGBP in a Java-language Surface.
    return android_view_Surface_createFromIGraphicBufferProducer(env, gbp);
}

static jobject Image_createSurfacePlane(JNIEnv* env, jobject thiz, int idx, int readerFormat)
{
    int rowStride, pixelStride;
    ALOGV("%s: buffer index: %d", __FUNCTION__, idx);

    CpuConsumer::LockedBuffer* buffer = Image_getLockedBuffer(env, thiz);

    ALOG_ASSERT(buffer != NULL);
    if (buffer == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException", "Image was released");
    }

    readerFormat = Image_getPixelFormat(env, readerFormat);

    rowStride = Image_imageGetRowStride(env, buffer, idx, readerFormat);
    pixelStride = Image_imageGetPixelStride(env, buffer, idx, readerFormat);

    jobject surfPlaneObj = env->NewObject(gSurfacePlaneClassInfo.clazz,
                                          gSurfacePlaneClassInfo.ctor, thiz, idx, rowStride, pixelStride);

    return surfPlaneObj;
}

static jobject Image_getByteBuffer(JNIEnv* env, jobject thiz, int idx, int readerFormat)
{
    uint8_t *base = NULL;
    uint32_t size = 0;
    jobject byteBuffer;

    ALOGV("%s: buffer index: %d", __FUNCTION__, idx);

    CpuConsumer::LockedBuffer* buffer = Image_getLockedBuffer(env, thiz);

    if (buffer == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException", "Image was released");
    }

    readerFormat = Image_getPixelFormat(env, readerFormat);

    // Create byteBuffer from native buffer
    Image_getLockedBufferInfo(env, buffer, idx, &base, &size, readerFormat);

    if (size > static_cast<uint32_t>(INT32_MAX)) {
        // Byte buffer have 'int capacity', so check the range
        jniThrowExceptionFmt(env, "java/lang/IllegalStateException",
                             "Size too large for bytebuffer capacity %" PRIu32, size);
        return NULL;
    }

    byteBuffer = env->NewDirectByteBuffer(base, size);
    // TODO: throw dvm exOutOfMemoryError?
    if ((byteBuffer == NULL) && (env->ExceptionCheck() == false)) {
        jniThrowException(env, "java/lang/IllegalStateException", "Failed to allocate ByteBuffer");
    }

    return byteBuffer;
}

//#define PRINT_DEBUG
#ifdef PRINT_DEBUG
bool calibrationDataDumped = false;
void MyDumpAsTCSV (const DSCalibIntrinsicsNonRectified &cri, char*name)
{

    ALOGW("%f %s%s", cri.fx, name, "_fx");
    ALOGW("%f %s%s", cri.fy, name, "_fy");
    ALOGW("%f %s%s", cri.px, name, "_px");
    ALOGW("%f %s%s", cri.py, name, "_py");
    for (int i = 0; i < 5; i++)
        ALOGW("%f %s%s%d", cri.k[i], name, "_k", i);
    ALOGW("%d %s%s", (int)cri.w, name, "_w");
    ALOGW("%d %s%s", (int)cri.h, name, "_h");
}

void MyDumpAsTCSV (const DSCalibIntrinsicsRectified &crm, char* name)
{

    ALOGW("%f %s%s", crm.rfx, name, "_rfx");
    ALOGW("%f %s%s", crm.rfy, name, "_rfy");;
    ALOGW("%f %s%s", crm.rpx, name, "_rpx");
    ALOGW("%f %s%s", crm.rpy, name, "_rpy");
    ALOGW("%d %s%s", (int)crm.rw, name, "_rw");
    ALOGW("%d %s%s", (int)crm.rh, name, "_rh");
}
#endif

void convertToIntrinsicsRectified(float fx, float fy, float px, float py, int width, int height, DSCalibIntrinsicsRectified* res)
{
    if (res == NULL)
    {
        ALOGE("res == NULL");
        return;
    }

    res->rfx = fx;
    res->rfy = fy;
    res->rpx = px;
    res->rpy = py;
    res->rw = width;
    res->rh = height;
}

void convertToIntrinsicsNonRectified(float fx, float fy, float px, float py, double* distortion, int width, int height, DSCalibIntrinsicsNonRectified* res)
{
    if (res == NULL || distortion == NULL)
    {
        ALOGE("res or distortion == NULL");
        return;
    }

    res->fx = fx;
    res->fy = fy;
    res->px = px;
    res->py = py;
    res->w = width;
    res->h = height;

    for (int i = 0; i < 5; i++)
        res->k[i] = distortion[i];
}

void getIntrinsicsAndExtrinsics(JNIEnv* env, CpuConsumer::LockedBuffer* buffer, jdoubleArray jrotation, jfloatArray jtranslation, float depthfx,
                                float depthfy, float depthpx, float depthpy, float colorfx, float colorfy,
                                float colorpx, float colorpy, jdoubleArray jdistortion, int colorWidth, int colorHeight,
                                bool rectified, DSCalibIntrinsicsRectified* zIntrinsics, DSCalibIntrinsicsNonRectified* thirdIntrinsicsNonRect,
                                DSCalibIntrinsicsRectified* thirdIntrinsicsRec, double* rotation, double* translation)
{

    if (buffer == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException", "Image was released");
    }

    //converting rotation

    jsize len = env->GetArrayLength(jrotation);
    // Get the elements
    jdouble* jdoubleRotation = env->GetDoubleArrayElements(jrotation, 0);
    memcpy(rotation, (double*) jdoubleRotation, sizeof(double) * 9);
    env->ReleaseDoubleArrayElements(jrotation, jdoubleRotation, 0);


    convertToIntrinsicsRectified(depthfx, depthfy, depthpx, depthpy, buffer->width, buffer->height, zIntrinsics);

    //converting translation

    len = env->GetArrayLength(jtranslation);
    // Get the elements
    jfloat* jfloatTranslation = env->GetFloatArrayElements(jtranslation, 0);
    translation[0] = jfloatTranslation[0];
    translation[1] = jfloatTranslation[1];
    translation[2] = jfloatTranslation[2];
    env->ReleaseFloatArrayElements(jtranslation, jfloatTranslation, 0);



    if (rectified)
    {
        convertToIntrinsicsRectified(colorfx, colorfy, colorpx, colorpy, colorWidth, colorHeight, thirdIntrinsicsRec);

    }
    else
    {
        //converting distortion
        double distortion[5];

        jsize len = env->GetArrayLength(jdistortion);
        // Get the elements
        jdouble* jdoubleDistortion = env->GetDoubleArrayElements(jdistortion, 0);
        memcpy(distortion, (double*) jdoubleDistortion, sizeof(double) * 5);
        env->ReleaseDoubleArrayElements(jdistortion, jdoubleDistortion, 0);

        convertToIntrinsicsNonRectified(colorfx, colorfy, colorpx, colorpy, distortion, colorWidth, colorHeight, thirdIntrinsicsNonRect);
    }

#ifdef PRINT_DEBUG
    MyDumpAsTCSV(zIntrinsics, "zIntrinsics");
    MyDumpAsTCSV(thirdIntrinsicsNonRect, "thirdIntrinsicsNonRect");

    for (int j = 0; j < 9; j++)
        ALOGW("%f %s[%d]", rotation[j], "rotation", j);

    for (int j = 0; j < 3; j++)
        ALOGW("%f %s[%d]", translation[j], "translation", j);
#endif

}
static void Image_getUVMapPerRegion(JNIEnv* env, jobject thiz, jdoubleArray jrotation, jfloatArray jtranslation, float depthfx,
                                    float depthfy, float depthpx, float depthpy, float colorfx, float colorfy,
                                    float colorpx, float colorpy, jdoubleArray jdistortion, int colorWidth, int colorHeight,
                                    bool rectified, int x, int y, int width, int height, jobject dest)
{
    DSCalibIntrinsicsRectified zIntrinsics;
    DSCalibIntrinsicsNonRectified thirdIntrinsicsNonRect;
    DSCalibIntrinsicsRectified thirdIntrinsicsRec;
    double rotation[9];
    double translation[3];
    //Depth Intrinsics
    CpuConsumer::LockedBuffer* buffer = Image_getLockedBuffer(env, thiz);

    getIntrinsicsAndExtrinsics(env, buffer,  jrotation,  jtranslation,  depthfx,
                               depthfy,  depthpx,  depthpy,  colorfx,  colorfy,
                               colorpx,  colorpy,  jdistortion,  colorWidth,  colorHeight,
                               rectified, &zIntrinsics, &thirdIntrinsicsNonRect, &thirdIntrinsicsRec, rotation, translation);

    float zImage[3];


    uint16_t *depthData = (uint16_t *)buffer->data;
    float *res = (float*) env->GetDirectBufferAddress(dest);

    for (int row = 0; row < height; row++)
        for (int col = 0; col < width; col++)
        {
            zImage[0] = x + col;
            zImage[1] = y + row;
            if (zImage[0] >= buffer->width || zImage[1] >= buffer->height)
                jniThrowRuntimeException(env, "Array indexes out of bound!");

            zImage[2] = depthData[(int) (zImage[1] * buffer->width + zImage[0])];
            if (zImage[2] != 0)
            {
                if (rectified)
                {
                    DSTransformFromZImageToRectThirdImage(zIntrinsics, translation,
                                                          thirdIntrinsicsRec, zImage, res);
                }
                else
                {
                    DSTransformFromZImageToNonRectThirdImage(zIntrinsics, rotation, translation,
                            thirdIntrinsicsNonRect, zImage, res);
                }
            }
            res += 2;
        }
}

static void Image_getUVMapPerPoint(JNIEnv* env, jobject thiz, jdoubleArray jrotation, jfloatArray jtranslation, float depthfx,
                                   float depthfy, float depthpx, float depthpy, float colorfx, float colorfy,
                                   float colorpx, float colorpy, jdoubleArray jdistortion, int colorWidth, int colorHeight,
                                   bool rectified, int x, int y,  jobject dest)
{
    // Get a class reference for java.lang.Integer
    DSCalibIntrinsicsRectified zIntrinsics;
    DSCalibIntrinsicsNonRectified thirdIntrinsicsNonRect;
    DSCalibIntrinsicsRectified thirdIntrinsicsRec;

    double rotation[9];
    double translation[3];

    CpuConsumer::LockedBuffer* buffer = Image_getLockedBuffer(env, thiz);
    getIntrinsicsAndExtrinsics(env, buffer,  jrotation,  jtranslation,  depthfx,
                               depthfy,  depthpx,  depthpy,  colorfx,  colorfy,
                               colorpx,  colorpy,  jdistortion,  colorWidth,  colorHeight,
                               rectified, &zIntrinsics, &thirdIntrinsicsNonRect, &thirdIntrinsicsRec, rotation, translation);

    uint16_t *depthData = (uint16_t *)buffer->data;

    float zImage[3];
    zImage[0] = x;
    zImage[1] = y;
    float *res = (float*) env->GetDirectBufferAddress(dest);
    if (zImage[0] >= buffer->width || zImage[1] >= buffer->height)
        jniThrowRuntimeException(env, "Array indexes out of bound!");

    zImage[2] = depthData[(int) (zImage[1] * buffer->width + zImage[0])];
    if (zImage[2] != 0)
    {
        if (rectified)
        {
            DSTransformFromZImageToRectThirdImage(zIntrinsics, translation,
                                                  thirdIntrinsicsRec, zImage, res);
        }
        else
        {
            DSTransformFromZImageToNonRectThirdImage(zIntrinsics, rotation, translation,
                    thirdIntrinsicsNonRect, zImage, res);
        }
    }
}


} // extern "C"

// ----------------------------------------------------------------------------

static JNINativeMethod gDepthCameraImageReaderMethods[] = {
    {"nativeClassInit",        "()V",                        (void*)DepthCameraImageReader_classInit },
    {"nativeInit",             "(Ljava/lang/Object;IIII)V",  (void*)DepthCameraImageReader_init },
    {"nativeClose",            "()V",                        (void*)DepthCameraImageReader_close },
    {"nativeReleaseImage",     "(Landroid/media/Image;)V",   (void*)DepthCameraImageReader_imageRelease },
    {"nativeImageSetup",       "(Landroid/media/Image;)I",    (void*)DepthCameraImageReader_imageSetup },
    {"nativeGetSurface",       "()Landroid/view/Surface;",   (void*)DepthCameraImageReader_getSurface },
};

static JNINativeMethod gImageMethods[] = {
    {"nativeImageGetBuffer",   "(II)Ljava/nio/ByteBuffer;",   (void*)Image_getByteBuffer },
    {   "nativeCreatePlane",      "(II)Lcom/intel/camera2/extensions/depthcamera/DepthCameraImageReader$SurfaceImage$SurfacePlane;",
        (void*)Image_createSurfacePlane
    },
    {"nativeCalcUVMapVal", "([D[FFFFFFFFF[DIIZIILjava/nio/ByteBuffer;)V", (void*) Image_getUVMapPerPoint },
    {"nativeCalcUVMapValForRegion", "([D[FFFFFFFFF[DIIZIIIILjava/nio/ByteBuffer;)V", (void*) Image_getUVMapPerRegion },
};

int register_intel_camera2_extensions_depthcamera_DepthCameraImageReader(JNIEnv *env) {

    int ret1 = AndroidRuntime::registerNativeMethods(env,
               "com/intel/camera2/extensions/depthcamera/DepthCameraImageReader", gDepthCameraImageReaderMethods, NELEM(gDepthCameraImageReaderMethods));

    int ret2 = AndroidRuntime::registerNativeMethods(env,
               "com/intel/camera2/extensions/depthcamera/DepthCameraImageReader$SurfaceImage", gImageMethods, NELEM(gImageMethods));

    return (ret1 || ret2);
}

//externs to register all functions
extern int register_intel_camera2_extensions_depthcamera_DepthCameraCalibrationDataMap(JNIEnv *env);
extern int register_intel_camera2_extensions_depthcamera_DepthSurfaceConfiguration(JNIEnv *env);
extern int register_intel_camera2_extensions_depthcamera_SurfaceQuery(JNIEnv *env);

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        ALOGE("ERROR: GetEnv failed\n");
        goto fail;
    }
    assert(env != NULL);

    if (register_intel_camera2_extensions_depthcamera_SurfaceQuery(env) < 0) {
        ALOGE("ERROR: native registration of SurfaceQuery failed\n");
        goto fail;
    }
    if (register_intel_camera2_extensions_depthcamera_DepthCameraCalibrationDataMap(env) < 0) {
        ALOGE("ERROR: native registration of DepthCameraCalibrationDataMap failed\n");
        goto fail;
    }
    if (register_intel_camera2_extensions_depthcamera_DepthSurfaceConfiguration(env) < 0) {
        ALOGE("ERROR: native registration of DepthSurfaceConfiguration failed\n");
        goto fail;
    }
    if (register_intel_camera2_extensions_depthcamera_DepthCameraImageReader(env) < 0) {
        ALOGE("ERROR: native registration of DepthCameraImageReader failed\n");
        goto fail;
    }
    result = JNI_VERSION_1_4;

fail:
    return result;
}

