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

import android.graphics.ImageFormat;
import android.hardware.camera2.CaptureResult;
import android.media.Image;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Size;

public class MultiFrameBlender {
    private static final String TAG = MultiFrameBlender.class.getSimpleName();

    public static enum TYPE {
        NONE,
        HDR,
        ULL
    }

    private long mCPInstance;

    private TYPE mType = TYPE.NONE;
    private Size mSize;
    private BlenderOption mOption;

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
     */
    public static boolean isSupported() {
        return CPJNI.isSupported();
    }

    /**
     * create new {@link MultiFrameBlender blender} instance with given {@link MultiFrameBlendCallback callback} instance.
     * 
     * @param callback the callback interface to get the process result of {@link MultiFrameBlender blender}
     * @param frameCount the number of frame to be blended by this instance
     * @return {@link MultiFrameBlender blender} instance
     */
    public static MultiFrameBlender newInstance(MultiFrameBlendCallback callback, int frameCount) {
        return new MultiFrameBlender(callback, frameCount);
    }

    private MultiFrameBlender(MultiFrameBlendCallback callback, int frameCount) {
        if (callback == null) {
            throw new IllegalArgumentException("callback should not be null");
        }
        if (CPJNI.isSupported()) {
            mCPInstance = CPJNI.init();
        }
        if (mCPInstance == 0) {
            throw new IllegalArgumentException("CP library can't be loaded.");
        }

        mCallback = callback;
        mFrameCount = frameCount;

        HandlerThread ht = new HandlerThread("MultiFrameBlenderThread");
        ht.start();
        mHandler = new CallbackHandler(ht.getLooper());
    };

    public int getType() {
        checkAndThrowException();
        return mType.ordinal();
    }

    public Size getSize() {
        checkAndThrowException();
        return mSize;
    }

    /**
     * configure {@link MultiFrameBlender} to the given setting.
     * if blender already configured with different settings, it will re-initialize blender with the given new settings.
     * @param type the type of blender. it could be {@link TYPE#HDR} or {@link TYPE#ULL} now.
     * @param size the size of blended target image.
     * @param option the {@link BlenderOption option} to be used for initializing {@link MultiFrameBlender}.
     * @return the result value for initializing blender.
     */
    public boolean config(int type, Size size, BlenderOption option) {
        // TODO : create proper blender option
        if (option == null) {
            option = new BlenderOption(0, CPJNI.TARGET_CPU);
        }

        Log.d(TAG, "type = " + type + ", w = " + size.getWidth() + ", h = " + size.getHeight());
        int ret = IA_ERR_NONE;
        if (type == TYPE.NONE.ordinal()|| size.getWidth() == 0 || size.getHeight() == 0) {
            throw new IllegalArgumentException("Invalid argument(s)!");
        }
        if (type != mType.ordinal() || !size.equals(mSize) || option.equals(mOption)) {
            if (mType == TYPE.HDR) {
                CPJNI.hdrUninit(mCPInstance);
            } else if (mType == TYPE.ULL) {
                CPJNI.ullUninit(mCPInstance);
            }
        }
        mType = type == TYPE.NONE.ordinal() ? TYPE.NONE : (type == TYPE.HDR.ordinal() ? TYPE.HDR : TYPE.ULL);
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
     * close all frames and metadatas and than close native CP context.
     * After calling this method, this {@link MultiFrameBlendCallback} can not be used anymore.
     * @return the result code .
     */
    public int close() {
        int ret = IA_ERR_NONE;
        if (mType == TYPE.HDR) {
            ret = CPJNI.hdrUninit(mCPInstance);
        } else if (mType == TYPE.ULL) {
            ret = CPJNI.ullUninit(mCPInstance);
        }
        CPJNI.uninit(mCPInstance);
        mCPInstance = 0;

        mSize = null;
        mFrames.clear();
        mFrames = null;
        mMetadatas.clear();
        mMetadatas = null;
        mState = STATUS_INVALID;
        return ret;
    }

    /**
     * add an Image to {@link MultiFrameBlender} which is converted to an IaFrame internally.
     * converting operation will be performed in the separate thread since it could block the
     * caller's thread.
     * once given Image object converted to IaFrame object, Image object will be closed automatically
     * to release Image object's memory.
     * 
     * @param image the acquired Image instance read from ImageReader.
     * @param metadata the CaptureResult instance which is matched with image instance.
     */
    public void addFrame(Image image, CaptureResult metadata) {
        Log.d(TAG,"");
        checkAndThrowException();
        if (mState == STATUS_INVALID) {
            throw new IllegalStateException("blender is not initilazed");
        }
        if (image == null) {
            Log.e(TAG, "image is null");
            return;
        }
        if (image.getWidth() != mSize.getWidth() || image.getHeight() != mSize.getHeight()) {
            Log.e(TAG, "image size is not suitable for this blender! request  = " + mSize.toString() + ", captured = " + image.getWidth() + "x" + image.getHeight());
            return;
        }
        if (image.getFormat() != ImageFormat.YUV_420_888) {
            Log.e(TAG, "not supported image format! " + image.getFormat());
            return;
        }
        if (metadata != null) {
            long key = image.getTimestamp();
            long metadataTimestamp = metadata.get(CaptureResult.SENSOR_TIMESTAMP);
            if (metadataTimestamp != key) {
                Log.w(TAG, "image and metadata timestamp are not identical!");
            }
            mMetadatas.put(metadataTimestamp, metadata);
            ImageConvertThread convertThread = new ImageConvertThread(image);
            convertThread.start();
        }
    }

    /**
     * blend all added frames into the configured {@link TYPE} of output.
     * result will be delivered through {@link MultiFrameBlendCallback}
     */
    public void blend() {
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
                    msg.obj = outFrame;
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
     * not implemented yet
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
    public void destroy() {
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
    }

    /**
     * callback interface to notify {@link MultiFrameBlender blender's} event.
     */
    public interface MultiFrameBlendCallback {
        public void onAddedFrame(Image image, CaptureResult metadata);
        public void onReadyToBlend();
        public void onBlendDone(IaFrame frame);
        public void onBlendFail(int error);
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
                        if (mCallback != null) {
                            mCallback.onReadyToBlend();
                        }
                    }
                    break;
                }
                case MSG_BLEND_DONE: {
                    if (mCallback != null) {
                        mCallback.onBlendDone((IaFrame)msg.obj);
                    }
                    break;
                }
                case MSG_BLEND_FAIL: {
                    if (mCallback != null) {
                        mCallback.onBlendFail(msg.arg1);
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
