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
package com.intel.camera2.extensions.photography;

import com.intel.camera2.extensions.IaFrame;
import com.intel.camera2.extensions.ImageConverter;

import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.hardware.camera2.CaptureResult;
import android.media.Image;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Size;

/**
 * <p>this class provide multi-frame blending APIs.</p>
 * <<p>currently, it supports {@link TYPE#HDR HDR} and {@link TYPE#ULL ULL} composition.
 * each composition will be done through adding source frames to call {{@link #addInputFrame(Image, CaptureResult)}
 * for the designated number of frames.</p>
 * <p>the composition process will be performed in the separated thread, and then the result will be delivered through
 * the {@link MultiFrameBlendCallback callback} which is set by {{@link #setCallback(MultiFrameBlendCallback)}</p>.
 */
public class MultiFrameBlender {
    private static final String TAG = MultiFrameBlender.class.getSimpleName();

    public static enum TYPE {
        NONE,
        HDR,
        ULL
    }

    public static enum TARGET {
        CPU,
        IPU,
    }

    private long mCPInstance;

    private TYPE mType = TYPE.NONE;
    private Size mSize;
    private TARGET mTarget;

    private LongSparseArray<IaFrame> mFrames;
    private LongSparseArray<CaptureResult> mMetadatas;

    private MultiFrameBlendCallback mCallback;
    private CallbackHandler mHandler;
    private int mFrameCount;
    private int mConvertedCount;

    // JNI API return values.
    // Those definitions should be synced up with 'ia_err' definitions in 'ia_types.h'
    private final int IA_ERR_NONE = 0;              // No errors
    private final int IA_ERR_GENERAL = (1 << 1);    // General error
    private final int IA_ERR_NO_MEMORY = (1 << 2);  // Out of memory
    private final int IA_ERR_DATA = (1 << 3);       // Corrupted data
    private final int IA_ERR_INTERNAL = (1 << 4);   // Error in code
    private final int IA_ERR_ARGUMENT = (1 << 5);   // Invalid argument

    private final int STATUS_INVALID = -1;
    private final int STATUS_INITILIZED = 0;
    private final int STATUS_READY = 1;
    private final int STATUS_BLENDING = 2;
    private final int STATUS_BLEND_DONE = 3;

    private int mState = STATUS_INVALID;

    /**
     * to check multi-frame blending feature supportiveness 
     * @return true if it is supported, or false
     */
    public static boolean isSupported() {
        return CPJNI.isSupported();
    }

    /**
     * create new {@link MultiFrameBlender blender} instance.
     * @return the instance of {@link MultiFrameBlender blender}
     */
    public static MultiFrameBlender newInstance() {
        if (!isSupported()) {
            return null;
        }
        return new MultiFrameBlender();
    }

    /**
     * create new {@link MultiFrameBlender blender} instance with given {@link MultiFrameBlendCallback callback} instance.
     * @param callback the {@link MultiFrameBlendCallback callback} interface to get the process result of {@link MultiFrameBlender blender}
     */
    private MultiFrameBlender() {
        HandlerThread ht = new HandlerThread("MultiFrameBlenderThread");
        ht.start();
        mHandler = new CallbackHandler(ht.getLooper());
    };

    /**
     * set the {@link MultiFrameBlendCallback callback} interface to get the processing result of {@link MultiFrameBlender blender}
     * @param callback the {@link MultiFrameBlendCallback callback} interface
     */
    public void setCallback(MultiFrameBlendCallback callback) {
        if (callback != null) {
            mCallback = callback;
        }
    }

    /**
     * get the configured {@link TYPE type} of this blender
     * @return the {@link TYPE type}
     */
    public TYPE getType() {
        checkAndThrowException();
        return mType;
    }

    /**
     * get the configured {@link android.util.Size size} of this blender
     * @return the {@link android.util.Size size}
     */
    public Size getSize() {
        checkAndThrowException();
        return mSize;
    }

    /**
     * configure {@link MultiFrameBlender} to the given setting.
     * if blender already configured with different settings, it will re-initialize blender with the given new settings.
     * @param type the type of blender. it could be {@link TYPE#HDR} or {@link TYPE#ULL} now.
     * @param size the size of source frames
     * @param target the target unit of composition run on.
     * @param numberOfFrames the number of source frames which are used to compose
     * @return the result of the multi-frame blender initialization
     */
    public boolean configureBlender(TYPE type, Size size, TARGET target, int numberOfFrames) {
        // TODO : create proper blender option

        mCPInstance = CPJNI.init();
        if (mCPInstance == 0) {
            throw new IllegalArgumentException("fail to create CP instance");
        }
        mFrameCount = numberOfFrames;
        Log.d(TAG, "type = " + type + ", w = " + size.getWidth() + ", h = " + size.getHeight());
        int ret = IA_ERR_NONE;
        if (type == TYPE.NONE|| size.getWidth() == 0 || size.getHeight() == 0) {
            throw new IllegalArgumentException("Invalid argument(s)!");
        }
        if (type != mType || !size.equals(mSize) || target != mTarget) {
            if (mType == TYPE.HDR) {
                CPJNI.hdrUninit(mCPInstance);
            } else if (mType == TYPE.ULL) {
                CPJNI.ullUninit(mCPInstance);
            }
        }
        BlenderOption option;
        if (target == TARGET.IPU) {
            option = new BlenderOption(0, CPJNI.TARGET_IPU);
        } else {
            option = new BlenderOption(0, CPJNI.TARGET_CPU);
        }
        mTarget = target;
        mType = type;
        mSize = size;
        if (mType == TYPE.HDR) {
            ret = CPJNI.hdrInit(mCPInstance, size.getWidth(), size.getHeight(), option);
        } else if (mType == TYPE.ULL) {
            ret = CPJNI.ullInit(mCPInstance, size.getWidth(), size.getHeight(), option);
        }
        if (mFrames == null) {
            mFrames = new LongSparseArray<IaFrame>();
        }
        mFrames.clear();
        if (mMetadatas == null) {
            mMetadatas = new LongSparseArray<CaptureResult>();
        }
        mMetadatas.clear();
        if (ret == IA_ERR_NONE) {
            mState = STATUS_INITILIZED;
        }
        return (ret == IA_ERR_NONE) ? true : false;
    }

    /**
     * clear all frames and metadatas in the {@link MultiFrameBlender}.
     * if the internal status of blender is BLENDING, blending operation will be aborted.
     */
    public void flush() {
        Log.d(TAG, "");
        if (mState == STATUS_BLENDING) {
            abortBlending();
        }
        mFrames.clear();
        mMetadatas.clear();
        mState = STATUS_INITILIZED;
    }

    /**
     * add an Image to {@link MultiFrameBlender}. when the number of added image reach to the frame count
     * blending will be performed automatically.
     * which are configured through {@link #configureBlender(int, Size, BlenderOption, int)} 
     * once given Image object converted to IaFrame object, Image object will be closed automatically
     * to release Image object's memory.
     * 
     * @param image the acquired Image instance read from ImageReader.
     * @param metadata the CaptureResult instance which is matched with image instance.
     * @return number of added images or 0 if it has error
     */
    public int addInputFrame(Image image, CaptureResult metadata) {
        Log.d(TAG,"");
        int ret = 0;
        checkAndThrowException();
        if (mState == STATUS_INVALID) {
            throw new IllegalStateException("blender is not initilazed");
        }
        if (image == null) {
            Log.e(TAG, "image is null");
            return ret;
        }
        if (image.getWidth() != mSize.getWidth() || image.getHeight() != mSize.getHeight()) {
            Log.e(TAG, "image size is not suitable for this blender! request  = " + mSize.toString() + ", captured = " + image.getWidth() + "x" + image.getHeight());
            return ret;
        }
        if (image.getFormat() != ImageFormat.YUV_420_888) {
            Log.e(TAG, "not supported image format! " + image.getFormat());
            return ret;
        }
        if (metadata != null) {
            long key = image.getTimestamp();
            long metadataTimestamp = metadata.get(CaptureResult.SENSOR_TIMESTAMP);
            if (metadataTimestamp != key) {
                Log.w(TAG, "image and metadata timestamp are not identical!");
            }
            mMetadatas.put(metadataTimestamp, metadata);
            ret = mMetadatas.size();
            ImageConvertThread convertThread = new ImageConvertThread(image);
            convertThread.start();
        }
        return ret;
    }

    /**
     * blend all added frames into the configured {@link TYPE} of output.
     * result will be delivered through {@link MultiFrameBlendCallback}
     */
    private void blend() {
        checkAndThrowException();
        if (mState != STATUS_READY || mMetadatas.size() != mFrames.size()) {
            Log.e(TAG, "blender is not in ready state");
            Message msg = Message.obtain();
            msg.what = MSG_BLEND_FAIL;
            msg.arg1 = IA_ERR_INTERNAL;
            mHandler.sendMessage(msg);
            return;
        }
        Thread blendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                IaFrame[] frames = new IaFrame[mFrames.size()];
                for (int i = 0 ; i < mFrames.size() ; i++) {
                    frames[i] = mFrames.valueAt(i);
                }
                IaFrame outFrame = null;
                int ret = 0;
                long time = System.currentTimeMillis();
                if (mType == TYPE.HDR) {
                    // TODO : calculate and create HdrOption using metadata 
                    HdrOption cfg = new HdrOption(0, 0);
                    outFrame = CPJNI.hdrCompose(mCPInstance, frames, cfg);
                } else if (mType == TYPE.ULL) {
                    // TODO : calculate and create UllOption using metadata
                    CaptureResult metadata = null;
                    if (mMetadatas.size() > 0) {
                        metadata = mMetadatas.valueAt(0);
                    }
                    float analogGain = -1;
                    float digitalGain = -1;
                    float aperture = -1;
                    int exposureTimeUs = 0;
                    int iso = 100;
                    int totalExposure = -1;
                    int zoomFactor = 1;
                    boolean enabledNdFilter = false;
                    if (metadata != null) {
                        Float apertureData = metadata.get(CaptureResult.LENS_APERTURE);
                        if (apertureData != null) {
                            aperture = apertureData.floatValue();
                            Log.d(TAG, "aperture = " + aperture);
                        }
                        Long exposureTimeData = metadata.get(CaptureResult.SENSOR_EXPOSURE_TIME);
                        if (exposureTimeData != null) {
                            exposureTimeUs = exposureTimeData.intValue() / 1000;
                            Log.d(TAG, "exposure time = " + exposureTimeUs);
                        }
                        Integer isoData = metadata.get(CaptureResult.SENSOR_SENSITIVITY);
                        if (isoData != null) {
                            iso = isoData.intValue();
                            Log.d(TAG, "iso = " + iso);
                        }
                    }
                    UllOption cfg = new UllOption(analogGain, digitalGain, aperture, exposureTimeUs, iso, totalExposure, zoomFactor, enabledNdFilter);
                    outFrame = CPJNI.ullCompose(mCPInstance, frames, cfg);
                }
                Log.d(TAG, "elapsed time for composition = " + (System.currentTimeMillis() - time));
                mState = STATUS_BLEND_DONE;
                if (outFrame != null) {
                    Message msg = Message.obtain();
                    msg.what = MSG_BLEND_DONE;
                    msg.obj = ImageConverter.convertToYuvImage(outFrame.imageData, ImageFormat.YUV_420_888, outFrame.stride, outFrame.width, outFrame.height);
                    mHandler.sendMessage(msg);
                } else {
                    Message msg = Message.obtain();
                    msg.what = MSG_BLEND_FAIL;
                    msg.arg1 = ret;
                    mHandler.sendMessage(msg);
                }
            }
        });
        mState = STATUS_BLENDING;
        blendThread.start();
    }

    /**
     * abort blending if the blender is in the blending process.
     */
    public void abortBlending() {
        checkAndThrowException();
        if (mState == STATUS_BLENDING) {
            if (mType == TYPE.HDR) {
                CPJNI.hdrAbort(mCPInstance);
            } else if (mType == TYPE.ULL) {
                CPJNI.ullAbort(mCPInstance);
            }
        }
        if (mCallback != null) {
            mCallback.onBlendAborted();
        }
    }

    /**
     * release all resources and metadatas of this {@link MultiFrameBlender}
     */
    public void release() {
        flush();
        if (mType == TYPE.HDR) {
            CPJNI.hdrUninit(mCPInstance);
        } else if (mType == TYPE.ULL) {
            CPJNI.ullUninit(mCPInstance);
        }
        CPJNI.uninit(mCPInstance);
        mCPInstance = 0;

        mState = STATUS_INVALID;
        mType = TYPE.NONE;
        mSize = null;
        mFrames = null;
        mMetadatas = null;
        mCallback = null;
    }

    
    @Override
    protected void finalize() throws Throwable {
        if (mState != STATUS_INVALID) {
            release();
        }
        super.finalize();
    }


    /**
     * callback interface to notify {@link MultiFrameBlender blender's} event.
     */
    public interface MultiFrameBlendCallback {
        public void onAddedFrame(Image image, CaptureResult metadata);
        public void onBlendCompleted(YuvImage image);
        public void onBlendFailed(int error);
        public void onBlendAborted();
    }

    private void checkAndThrowException() {
        if (mState == STATUS_INVALID) {
            throw new IllegalStateException("Blender is not initialized! call initBlender() before to use this");
        }
    }

    private static final int MSG_ADDED_FRAME = 0;
    private static final int MSG_BLEND_DONE = 1;
    private static final int MSG_BLEND_FAIL = 2;

    private class CallbackHandler extends Handler {
        public CallbackHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADDED_FRAME: {
                    if (mCallback != null) {
                        long key = mMetadatas.keyAt(msg.arg1);
                        mCallback.onAddedFrame((Image)msg.obj, mMetadatas.get(key));
                    }
                    mConvertedCount++;
                    if (mConvertedCount == mFrameCount) {
                        mState = STATUS_READY;
                        mConvertedCount = 0;
                        blend();
                    }
                    break;
                }
                case MSG_BLEND_DONE: {
                    if (mCallback != null) {
                        mCallback.onBlendCompleted((YuvImage)msg.obj);
                    }
                    break;
                }
                case MSG_BLEND_FAIL: {
                    if (mCallback != null) {
                        mCallback.onBlendFailed(msg.arg1);
                    }
                    break;
                }
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    private class ImageConvertThread extends Thread {
        private Image image;

        private ImageConvertThread(Image image) {
            this.image = image;
        }

        @Override
        public void run() {
            long key = image.getTimestamp();
            IaFrame frame = new IaFrame(image, IaFrame.IaFormat.NV12, 0);
            mFrames.put(key, frame);
            Message msg = Message.obtain();
            msg.what = MSG_ADDED_FRAME;
            msg.arg1 = mMetadatas.indexOfKey(key);
            msg.obj = image;
            mHandler.sendMessage(msg);
        }
    }

//    public static YuvImage debug_jniTest(Image image) {
//        IaFrame convertedFrame = new IaFrame();
//        IaFrame inputFrame = new IaFrame(image);
//        CP.debugFrameConvert(inputFrame, convertedFrame);
//        return new YuvImage(convertedFrame.imageData, ImageFormat.NV21, convertedFrame.width, convertedFrame.height, null);
//    }
}
