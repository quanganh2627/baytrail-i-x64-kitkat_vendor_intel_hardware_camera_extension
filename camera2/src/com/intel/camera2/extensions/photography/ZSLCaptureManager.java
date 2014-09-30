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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Size;
import android.view.Surface;

public class ZSLCaptureManager {
    private static final String TAG = ZSLCaptureManager.class.getSimpleName();

    private CameraCaptureSession.CaptureCallback mPreviewCallback;
    private ImageHistoryBuffer mHistoryQueue;
    private ImageReader mImageReader;
    private CallbackHandler mHandler;

    private boolean mWaitForImageEntry;
    private long mTimestampWaitingFor;
    private ZSLCaptureCallback mWaitingZSLCallback;

    private final int MAX_TEMP_QUEUE_SIZE = 2;

    public static final int ERROR_NO_MATCHED_IMAGE = -1;    //
    public static final int ERROR_UNKNOWN = -2;
    public static final int ERROR_INTERNAL_STATE = -3;
    public static final int ERROR_INVALID_ARGUMENT = -4;

    private final int STATUS_INVALID = 0;
    private final int STATUS_CONFIGURED = 1;
    private final int STATUS_IDLE = 2;
    private final int STATUS_CAPTURING = 3;

    private int mStatus = STATUS_INVALID;

    private final int MSG_ENQUEUE_METADATA = 1;
    private final int MSG_FOUND_MATCHED_ENTRY = 2;

    private static int sAcquiredcount = 0;

    private long mDeviceLatency;
    // for performance trace
//    private int mNumberOfJobs;
//    private long mThreadTick;

    private int mImageObjectSize;

    public ZSLCaptureManager() {
        HandlerThread ht = new HandlerThread("HandlerForZSLImageReader");
        ht.start();
        mHandler = new CallbackHandler(ht.getLooper());
    }

    private class CallbackHandler extends Handler {
        public CallbackHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ENQUEUE_METADATA: {
                    int status = getStatus();
                    if (status == STATUS_IDLE) {
                        if (mHistoryQueue != null) {
                            mHistoryQueue.enqueueMetadata((CaptureResult)msg.obj);
                        }
                    } else {
//                        Log.w(TAG, "status = " + status);
                    }
                    return;
                }
                case MSG_FOUND_MATCHED_ENTRY: {
                    changeStatus(STATUS_CAPTURING);
                    ImageResult entry = mHistoryQueue.getMatchedImage(mTimestampWaitingFor);
                    if (mWaitingZSLCallback != null) {
                        if (entry != null) {
                            mWaitingZSLCallback.onZSLCaptured(entry.image, entry.metadata);
                        } else {
                            mWaitingZSLCallback.onZSLError(ERROR_NO_MATCHED_IMAGE);
                        }
                    }
                    mWaitForImageEntry = false;
                    mTimestampWaitingFor = 0;
                    mWaitingZSLCallback = null;
                } break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    /**
     * helper method to get the available capture size list for using application ZSL feature
     * 
     * @param context the context of the application
     * @param cameraId the id of camera wanted for use
     * @param imageFormat the ImageForamt.
     * 
     * @return list of supported capture size for ZSL
     */
    public static ArrayList<Size> getSupportedSize(Context context, String cameraId, int imageFormat) {
        CameraManager cameraMgr = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (cameraMgr == null) {
            Log.e(TAG, "unable to get the camera serviec!");
            return null;
        }
        CameraCharacteristics characteristics;
        try {
            characteristics = cameraMgr.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return null;
        }
        Integer maxNbrOfOutputProc = characteristics.get(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_PROC);
        if (maxNbrOfOutputProc == null || maxNbrOfOutputProc < 2) {
            Log.w(TAG, "device should support 2 or more number of non stalling outputs");
            return null;
        }
        StreamConfigurationMap map = (StreamConfigurationMap) characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map != null && map.isOutputSupportedFor(imageFormat)) {
            Size[] outputSizes = map.getOutputSizes(imageFormat);
            if (outputSizes != null) {
                ArrayList<Size> supportedSizes = new ArrayList<Size>();
                for (Size size : outputSizes) {
                    if (map.getOutputStallDuration(imageFormat, size) == 0) {
                        supportedSizes.add(size);
                    }
                }
                if (supportedSizes.size() > 0) {
                    return supportedSizes;
                }
            }
        }
        return null;
    }

    /**
     * <p>Initialize ZSLCaptureManager.</p>
     * <p>It will determine internal queue size for keeping historical images according to the
     * input parameters.</p>
     *  
     * @param format the ImageForamt for ZSL capture. It only supports ImageForamt.YUV_420_888 now.
     * @param size the size of image for ZSL capture.
     * @param refFps the reference preview frame rate to calculate ZSL queue size.
     * @param maxDelayForZSL the delay microseconds to calculate ZSL queue size.
     * @param maxHistories the maximum history queue size
     * @param historyInterval the sampling interval microseconds to store historical images
     * 
     * @return <p>true if the output surface changed. if not, it will return false.</p>
     * <p>if it return true, application should do reconfiguration of ZSL surface for the {@link CameraCaptureSession} and {@link CaptureRequest}
     * using {@link #configureOutputSurface(List, boolean)} and {@link #configureOutputTarget(android.hardware.camera2.CaptureRequest.Builder, boolean)}.</p>
     */
    public synchronized boolean setup(int format, Size size, int refFps, long maxDelayForZSL, int maxHistories, long historyInterval) {
        if (format != ImageFormat.YUV_420_888) {
            throw new IllegalArgumentException("Not supported ImageFormat");
        }
        mImageObjectSize = calculateImageObjectSize(format, size);
        int zslQueueSize = (int)(refFps * maxDelayForZSL / 1000) + 1;
        return setupZSL(format, size, zslQueueSize, 0, maxHistories, historyInterval);
    }

    /**
     * <p>set {@link CameraCaptureSession.CaptureCallback} instance of application.</p>
     * <p>it should be set for passing CaptureResult of preview frames from ZSLCaptureManager to application.</p>
     * 
     * @param callback the CaptureCallback instance to get the callback in the application
     */
    public void setCaptureCallback(CameraCaptureSession.CaptureCallback callback) {
        mPreviewCallback = callback;
    }

    public CameraCaptureSession.CaptureCallback getCaptureCallback() {
        return mMainCaptureCallback;
    }

    private int calculateImageObjectSize(int format, Size size) {
        switch (format) {
            case ImageFormat.YUV_420_888:
                return size.getWidth() * size.getHeight() * 2;
            default:
                Log.w(TAG, "not supported image format");
                break;
        }
        return 0;
    }

    private boolean setupZSL(int format, Size size, int zslQueueSize, long zslInterval, int historyQueueSize, long historyInterval) {
        boolean isChangedOutput = false;
        if (mHistoryQueue != null) {
            mHistoryQueue.releaseAllEntries();
            mHistoryQueue = null;
        }
        mHistoryQueue = new ImageHistoryBuffer(zslQueueSize, zslInterval, historyQueueSize, historyInterval);
        int maxImages = mHistoryQueue.getQueueSize() + MAX_TEMP_QUEUE_SIZE + 1 + 1;
        Log.v(TAG, "max acquire images for ImageReader " + maxImages + ", it will use approximatly " + (maxImages * mImageObjectSize / 1024 / 1024) + " MB memory");
        if (mImageReader == null) {
            Log.d(TAG, "created new ImageReader instance");
            mImageReader = ImageReader.newInstance(size.getWidth(), size.getHeight(), format, maxImages);
            mImageReader.setOnImageAvailableListener(mImageAvailableListener, mHandler);
            isChangedOutput = true;
        } else {
            if (mImageReader.getWidth() != size.getWidth() || mImageReader.getHeight() != size.getHeight()) {
                Log.d(TAG, "size was changed! camera session needs to re-create");
                mImageReader.close();
                mImageReader = null;
                mImageReader = ImageReader.newInstance(size.getWidth(), size.getHeight(), format, maxImages);
                mImageReader.setOnImageAvailableListener(mImageAvailableListener, mHandler);
                isChangedOutput = true;
            }
        }
        changeStatus(STATUS_CONFIGURED);
        return isChangedOutput;
    }

    /**
     * <p>Close all acquired Image object in the history queue and also close ImageReader object.
     * Once this method is called, user should call setup() again to configure.</p>
     */
    public void release() {
        Log.v(TAG,"");
        changeStatus(STATUS_INVALID);
        if (mHistoryQueue != null) {
            mHistoryQueue.initialize();
            mHistoryQueue = null;
        }
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
//        Debug.stopMethodTracing();
    }

    /**
     * <p>Suspend ZSL queue updating and the arrived new Image object will be closed automatically.
     * user should call this method prior to directly call getImage() API.</p>
     */
    public synchronized void suspend() {
        int status = getStatus();
        if (status == STATUS_IDLE) {
            changeStatus(STATUS_CAPTURING);
        } else if (status == STATUS_CAPTURING) {
            Log.i(TAG, "ZSL already in the suspended status");
        } else {
            throw new IllegalStateException("invalid status("+status+")");
        }
    }

    /**
     * initialize all internal buffers and acquired image object to the configured status.
     */
    public synchronized void flush() {
        Log.d(TAG,"");
        int status = getStatus();
        if (status == STATUS_CAPTURING || status == STATUS_IDLE) {
            mHistoryQueue.initialize();
        } else if (status == STATUS_INVALID) {
            throw new IllegalStateException("invalid status (" + status + "). ZSL should be configured by calling setup() API");
        }
        changeStatus(STATUS_IDLE);
//        mNumberOfJobs = 0;
//        Debug.startMethodTracing("trace_zsl");
    }

    private OnImageAvailableListener mImageAvailableListener = new OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            if (reader != null) {
                try {
                    Image temp = reader.acquireNextImage();
                    Long now = System.nanoTime();
//                    Log.d(TAG, "timestamp = " + temp.getTimestamp() + ", now = " + now + " / nano delta for image= " + (now - temp.getTimestamp()));
                    mDeviceLatency = now - temp.getTimestamp();
                    sAcquiredcount++;
                    long status = getStatus();
                    if (status == STATUS_IDLE) {
                        mHistoryQueue.enqueueImage(temp);
                    } else {
                        temp.close();
                        sAcquiredcount--;
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    };

    /**
     * <p>add or remove ZSL surface as the output target to the preview capture request builder</p>
     * 
     * @param builder the capture request builder for preview
     * @param attach if it is true, ZSL surface will be added in the capture request builder.
     * and if it is false, ZSL surface will be removed.
     */
    public void configureOutputTarget(CaptureRequest.Builder builder, boolean attach) {
        int status = getStatus();
        if (status == STATUS_INVALID) {
            throw new IllegalStateException("invalid status (" + status + "). configure() should be called before using this API");
        }
        if (attach) {
            builder.addTarget(mImageReader.getSurface());
        } else {
            builder.removeTarget(mImageReader.getSurface());
        }
    }

    /**
     * <p>add or remove output surface from/to list of surfaces to create {@link android.hardware.camera2.CameraCaptureSession Session}</p>
     * 
     * @param surfaces the list of surfaces
     * @param attach if it is true, ZSL surface will be added to the list. And if it is false, surface will be removed from the list.
     */
    public void configureOutputSurface(List<Surface> surfaces, boolean attach) {
        int status = getStatus();
        if (status == STATUS_INVALID) {
            throw new IllegalStateException("invalid status (" + status + "). configure() should be called before using this API");
        }
        if (attach) {
            surfaces.add(mImageReader.getSurface());
        } else {
            surfaces.remove(mImageReader.getSurface());
        }
    }

    /**
     * <p>Take a picture using ZSL feature. A image which has the nearest time stamp with the input shutter time
     * will be delivered through callback interface. the returned {@link Image image} object should be released by calling
     * {@link #processDone(long)} to release resource. Or call {@link #flush()} API to release all resources and restart ZSL manager.</p>
     * 
     * @param shutterTime the nanoseconds unit time stamp.
     * @param callback the callback interface to receive captured image.
     */
    public synchronized int captureZSL(long shutterTime, ZSLCaptureCallback callback) {
        if (callback == null) {
            Log.w(TAG, "callback is null");
            return -1;
        }
//        waitZSLIdleStatus();
//        changeStatus(STATUS_CAPTURING);
        if (mHistoryQueue == null) {
            Log.w(TAG, "History queue for ZSL is null!");
            return -1;
        }
        if (!mHistoryQueue.canInvokeImage()) {
            Log.d(TAG, "unable to capture now");
            return -2;
        }
        ImageResult entry = mHistoryQueue.getNearestImage(shutterTime);
        if (entry != null && entry.image != null && entry.metadata != null) {
            callback.onZSLCaptured(entry.image, entry.metadata);
        } else {
            callback.onZSLError(ERROR_NO_MATCHED_IMAGE);
            mHistoryQueue.initialize();
            changeStatus(STATUS_IDLE);
        }
        return 0;
    }

    /**
     * <p>Take a picture using ZSL feature. A image which has same time stamp with the CaptureResult.SENSOR_TIMESTAMP
     * of specific CaptureResult object will be delivered through callback interface.</p>
     * <p>once all received Image process done, call {@link #flush()} to start ZSL manager again.</p>
     * 
     * @param result the CaptureResult object which includes CaptureResult.SENSOR_TIMESTAMP.
     * @param callback the callback interface to receive captured image.
     */
    public void captureZSL(final CaptureResult result, final ZSLCaptureCallback callback) {
        if (callback == null) {
            Log.w(TAG, "callback is null");
            return;
        }
        if (getStatus() != STATUS_IDLE) {
            Log.w(TAG, "invalid status");
            callback.onZSLError(ERROR_INTERNAL_STATE);
            return;
        }
        long latestTimestamp = mHistoryQueue.getLastEntryTimestamp();
        Long timestamp = result.get(CaptureResult.SENSOR_TIMESTAMP);
        if (timestamp == null) {
            callback.onZSLError(ERROR_INVALID_ARGUMENT);
            return;
        }
        if (latestTimestamp == 0) {
            Log.w(TAG, "internal error");
            callback.onZSLError(ERROR_INTERNAL_STATE);
            return;
        }
        if (latestTimestamp < timestamp.longValue()) {
            // ImageEntry was not delivered yet. set waiting flag
            Log.d(TAG, "requsted timestamp = " + timestamp + ", last entry's timestamp = " + latestTimestamp);
            mWaitingZSLCallback = callback;
            mWaitForImageEntry = true;
            mTimestampWaitingFor = timestamp;
        } else {
            changeStatus(STATUS_CAPTURING);
            ImageResult entry = mHistoryQueue.getMatchedImage(timestamp);
            if (entry != null) {
                if (entry.image != null && entry.metadata != null) {
                    callback.onZSLCaptured(entry.image, entry.metadata);
                    return;
                } 
            }
            Log.w(TAG, "no entry in the ZSL queue. please check the ZSL queue size is valid for the current use case");
            callback.onZSLError(ERROR_NO_MATCHED_IMAGE);
        }
    }

    /**
     * <p>Take a series of pictures using Time-Nudge feature.</p>
     * <p>this method will provide a list of ImageEntry which includes Image object and CaptureResult pair through TimeNudgeCallback interface. 
     * once all received Image process is done, call initialize() API to start ZSL manager again.</p>
     * 
     * @param callback the callback interface to receive result of Time-Nudge capture.
     */
    public synchronized void captureTimeNudge(TimeNudgeCallback callback) {
        if (callback == null) {
            Log.w(TAG, "callback is null");
            return;
        }
        waitZSLIdleStatus();
        changeStatus(STATUS_CAPTURING);
        if (mHistoryQueue == null) {
            Log.w(TAG, "History queue for Time-Nudge is null");
            return;
        }
        ArrayList<ImageResult> entries = mHistoryQueue.getTimeNudgeImages();
        if (entries.size() > 0) {
            callback.onTimeNudgeCaptured(entries);
        } else {
            callback.onTimeNudgeError(ERROR_NO_MATCHED_IMAGE);
            mHistoryQueue.initialize();
            changeStatus(STATUS_IDLE);
        }
    }

    /**
     * <p>notify to ZSLCaptureManager about the ZSL captured Image related process is done
     * in the application. ZSLCaptureManager will release corresponding resources once this was called.</p>
     * <p>it must be called whether {@link Image#close()} method was called in the application.</p>
     * 
     * @param key the timestamp value for {@link Image image} object. 
     * it could be get from the {@link Image image} object by calling {@link Image#getTimestamp()} API.
     */
    public synchronized void processDone(long key) {
        if (mHistoryQueue != null) {
            mHistoryQueue.releaseProcessedEntry(key);
        }
    }

    private void changeStatus(int status) {
        Log.v(TAG, "status changed " + mStatus + " to " + status);
        mStatus = status;
    }

    private int getStatus() {
        return mStatus;
    }

    private final int WAIT_TIMEOUT = 5000;  // 5secs
    private void waitZSLIdleStatus() {
        Log.i(TAG, "waiting ZSL capture done");
        if (getStatus() == STATUS_CAPTURING) {
            int tmo = 0;
            while (true) {
                int status = getStatus();
                if (status != STATUS_IDLE) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    tmo += 10;
                    if (tmo > WAIT_TIMEOUT) {
                        Log.e(TAG, "timeout!");
                        return;
                    }
                } else {
                    break;
                }
            }
        }
        int tmo = 0;
        while (true) {
            if (mHistoryQueue != null) {
                if (mHistoryQueue.getZSLEntryCount() > 0) {
                    break;
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    tmo += 10;
                    if (tmo > WAIT_TIMEOUT) {
                        Log.e(TAG, "waiting time out!");
                        return;
                    }
                }
            }
        }
        Log.i(TAG, "capture done");
    }

    /**
     * data class to represent ZSL capture result. it consists of {@link Image image} object, 
     * {@link CaptureResult result} object, and timestamp in nanoseconds unit which means the time when
     * this instance created.
     */
    public class ImageResult {
        public final Image image;
        public final long timestamp;
        public final CaptureResult metadata;
        public ImageResult(long timestamp, Image image, CaptureResult metadata) {
            this.image = image;
            this.timestamp = timestamp;
            this.metadata = metadata;
        }

        private void release() {
            try {
                this.image.close();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        private long getSensorTimestamp() {
            if (image != null) {
                return image.getTimestamp();
            } else if (metadata != null){
                return metadata.get(CaptureResult.SENSOR_TIMESTAMP);
            }
            Log.e(TAG, "invalid image entry!!");
            return 0;
        }
    }

    private class ImageHistoryBuffer {
        private LongSparseArray<Image> mTempImages;
        private LongSparseArray<CaptureResult> mTempMetadatas;

        private LongSparseArray<ImageResult> mLv1Queue;
        private int mLv1QueueSize;
        private long mLv1Interval;
        private long mLv1LastTime;

        private LongSparseArray<ImageResult> mLv2Queue;
        private int mLv2QueueSize;
        private long mLv2Interval;
        private long mLv2LastTime;

        private LongSparseArray<ImageResult> mInvokedImageQueue;
        private int mInvokedQueueMaxSize;

        private ImageHistoryBuffer(int zslSize, long zslIntervalMs, int historySize, long historyIntervalMs) {
            Log.d(TAG, "created internal buffers zsl size = " + zslSize + 
                    ", zsl interval = " + zslIntervalMs + 
                    ", history size = " + historySize + ", " +
                    "history interval = " + historyIntervalMs);
            if (zslSize == 0) {
                Log.w(TAG, "minimum ZSL queue size is 1.");
                zslSize = 1;
            }
            mLv1QueueSize = zslSize;
            mLv1Queue = new LongSparseArray<ImageResult>();
            mLv1Interval = zslIntervalMs;
            mLv1LastTime = 0;
            mLv2QueueSize = historySize;
            mLv2Queue = new LongSparseArray<ImageResult>();
            mLv2Interval = historyIntervalMs;
            mLv2LastTime = 0;
            mTempImages = new LongSparseArray<Image>();
            mTempMetadatas = new LongSparseArray<CaptureResult>();

            mInvokedImageQueue = new LongSparseArray<ImageResult>();
            mInvokedQueueMaxSize = mLv1QueueSize - 1;
        }

        private void initialize() {
            Log.d(TAG, "");
            releaseAllEntries();
            mLv1LastTime = 0;
            mLv2LastTime = 0;
        }

        private void releaseAllEntries() {
            Log.d(TAG, "");
            for (int i = 0 ; i < mLv1Queue.size() ; i++) {
                ImageResult entry = mLv1Queue.valueAt(i);
                if (entry != null) {
                    entry.release();
                    sAcquiredcount--;
                }
            }
            mLv1Queue.clear();
            for (int i = 0 ; i < mLv2Queue.size() ; i++) {
                ImageResult entry = mLv2Queue.valueAt(i);
                if (entry != null) {
                    entry.release();
                    sAcquiredcount--;
                }
            }
            mLv2Queue.clear();
            for (int i = 0 ; i < mTempImages.size() ; i++) {
                Image image = mTempImages.valueAt(i);
                image.close();
                sAcquiredcount--;
            }
            mTempImages.clear();
            mTempMetadatas.clear();
            for (int i = 0 ; i < mInvokedImageQueue.size() ; i++) {
                ImageResult entry = mInvokedImageQueue.valueAt(i);
                if (entry != null) {
                    entry.release();
                }
            }
            mInvokedImageQueue.clear();
        }

        private ImageResult enqueueLv1(ImageResult entry) {
            ImageResult oldest = null;
            if (mLv1Queue.size() == (mLv1QueueSize - mInvokedImageQueue.size())) {
                oldest = mLv1Queue.valueAt(0);
                mLv1Queue.removeAt(0);
            }
            long newKey = entry.getSensorTimestamp();
            mLv1Queue.put(newKey, entry);
            return oldest;
        }

        private ImageResult enqueueLv2(ImageResult entry) {
            if (mLv2QueueSize == 0) {
                return entry;
            }
            ImageResult oldest = null;
            if (mLv2Queue.size() == mLv2QueueSize) {
                oldest = mLv2Queue.valueAt(0);
                mLv2Queue.removeAt(0);
            }
            long newKey = entry.getSensorTimestamp();
            mLv2Queue.put(newKey, entry);
            return oldest;
        }

        private int getQueueSize() {
            return mLv1QueueSize + mLv2QueueSize;
        }

        private void enqueueMetadata(CaptureResult metadata) {
//            if (mThreadTick == 0) mThreadTick = Debug.threadCpuTimeNanos();
            long key = metadata.get(CaptureResult.SENSOR_TIMESTAMP);
//            Log.d(TAG, "receive metadata " + key);
            Image tempImage = mTempImages.get(key);
            if (tempImage != null) {
                long now = System.nanoTime() / 1000 * 1000;
                ImageResult entry = new ImageResult(now, tempImage, metadata);
                enqueue(entry);
                mTempImages.remove(key);
//                if (mThreadTick != 0) {
//                    Log.d(TAG, "#" + mNumberOfJobs + " : elapsed time nano = " + (Debug.threadCpuTimeNanos() - mThreadTick));
//                    mThreadTick = 0;
//                }
            } else {
                if (mTempMetadatas.size() == MAX_TEMP_QUEUE_SIZE) {
                    mTempMetadatas.removeAt(0);
                }
                mTempMetadatas.put(key, metadata);
            }
        }

        private void enqueueImage(Image newImage) {
//            if (mThreadTick == 0) mThreadTick = Debug.threadCpuTimeNanos();
            long key = newImage.getTimestamp();
//            Log.d(TAG, "receive image " + key);
            CaptureResult tempMetadata = mTempMetadatas.get(key);
            if (tempMetadata != null) {
                long now = System.nanoTime() / 1000 * 1000;
                ImageResult entry = new ImageResult(now, newImage, tempMetadata);
                enqueue(entry);
                mTempMetadatas.remove(key);
//                if (mThreadTick != 0) {
//                    Log.d(TAG, "#" + mNumberOfJobs + " : elapsed time nano = " + (Debug.threadCpuTimeNanos() - mThreadTick));
//                    mThreadTick = 0;
//                }
            } else {
                if (mTempImages.size() == MAX_TEMP_QUEUE_SIZE) {
                    Image image = mTempImages.valueAt(0);
                    if (image != null) {
                        image.close();
                        sAcquiredcount--;
                    }
                    mTempImages.removeAt(0);
                }
                mTempImages.put(key, newImage);
            }
        }

//        final long refresh = 3000;
        private void enqueue(ImageResult entry) {
            long current = entry.timestamp;
//            Log.d(TAG, "enqueue(" + current + ")");
            if (current - mLv1LastTime > mLv1Interval) {
//                Log.d(TAG, "enqueueLv1()");
                ImageResult oldestLv1Entry = enqueueLv1(entry);
                mLv1LastTime = current;
                if (oldestLv1Entry != null) {
                    if (current - mLv2LastTime > mLv2Interval) {
                        mLv2LastTime = current;
//                        Log.d(TAG, "enqueueLv2()");
                        ImageResult oldestLv2Entry = enqueueLv2(oldestLv1Entry);
                        if (oldestLv2Entry != null) {
                            oldestLv2Entry.release();
                            sAcquiredcount--;
                        }
                    } else {
                        oldestLv1Entry.release();
                        sAcquiredcount--;
                    }
                }
                if (mWaitForImageEntry) {
                    if (entry.getSensorTimestamp() == mTimestampWaitingFor) {
                        Log.d(TAG, "deliverred matched Entry");
                        if (mHandler != null) {
                            mHandler.sendEmptyMessage(MSG_FOUND_MATCHED_ENTRY);
                        }
                    }
                }
            } else {
                entry.release();
                sAcquiredcount--;
            }
//            long now = System.currentTimeMillis();
//            if (now - current > refresh) {
//                Log.d(TAG, "queue status : lv1 = " + mLv1Queue.size() + " / lv2 = " + mLv2Queue.size());
////                current = now;
//            }
        }

        private ImageResult getNearestImage(long timeStamp) {
            Log.d(TAG, "time stamp = " + timeStamp);
            if (timeStamp == 0) {
                return moveImageResultToInvokedQueue(1, -1);
            }
            timeStamp -= mDeviceLatency;
            Log.d(TAG,"adjusted timestamp = " + timeStamp);
            long minDiff = Long.MAX_VALUE;
            int minIndex = -1;
            long diff;
            ImageResult lastLv2Entry = null;
            if (mLv2Queue.size() > 1) {
                lastLv2Entry = mLv2Queue.valueAt(mLv2Queue.size() - 1);
            }
            if (lastLv2Entry != null && lastLv2Entry.timestamp >= timeStamp) {
                for (int i = 0 ; i < mLv2Queue.size() ; i++) {
                    ImageResult entry = mLv2Queue.valueAt(i);
                    if (entry != null) {
                        diff = timeStamp - entry.timestamp;
                        if (diff < minDiff) {
                            minDiff = diff;
                            minIndex = i;
                        }
                    }
                }
                if (minIndex != -1) {
                    return moveImageResultToInvokedQueue(2, minIndex);
                }
            } else {
                for (int i = 0 ; i < mLv1Queue.size() ; i++) {
                    ImageResult entry = mLv1Queue.valueAt(i);
                    if (entry != null) {
                        diff = timeStamp - entry.timestamp;
                        if (diff < minDiff) {
                            minDiff = diff;
                            minIndex = i;
                        }
                    }
                }
                if (minIndex != -1) {
                    return moveImageResultToInvokedQueue(1, minIndex);
                }
            }
            Log.e(TAG, "No image in the both queue!");
            return null;
        }

        private ImageResult getMatchedImage(long resultTimestamp) {
            Log.d(TAG, "timestamp = " + resultTimestamp);
            ImageResult entry = mLv1Queue.get(resultTimestamp);
            if (entry != null) {
                return entry;
            }
            entry = mLv2Queue.get(resultTimestamp);
            if (entry != null) {
                return entry;
            }
            Log.e(TAG, "No matched image in the queue!");
            return null;
        }

        private ArrayList<ImageResult> getTimeNudgeImages() {
            ArrayList<ImageResult> entries = new ArrayList<ImageResult>();
            if (mLv1Queue.size() > 0) {
                entries.add(mLv1Queue.valueAt(mLv1Queue.size() - 1));
            }
            for (int i = 0 ; i < mLv2Queue.size() ; i++) {
                entries.add(mLv2Queue.valueAt(i));
            }
            return entries;
        }

        private int getZSLEntryCount() {
            return mLv1Queue.size();
        }

//        private int getHistoryQueueSize() {
//            return mLv2Queue.size();
//        }

        private void releaseProcessedEntry(long key) {
            ImageResult entry = mInvokedImageQueue.get(key);
            if (entry != null) {
                entry.release();
                mInvokedImageQueue.remove(key);
            } else {
                entry = mLv2Queue.get(key);
                if (entry != null) {
                    entry.release();
                }
            }
            Log.d(TAG, "invoked queue size = " + mInvokedImageQueue.size());
        }

        private boolean canInvokeImage() {
            Log.d(TAG, "" + mInvokedImageQueue.size() + " / " + mInvokedQueueMaxSize);
            return (mInvokedImageQueue.size() >= mInvokedQueueMaxSize) ? false : true;
        }

        private ImageResult moveImageResultToInvokedQueue(int queueLvl, int index) {
            if (mLv1Queue.size() == 0) {
                Log.w(TAG, "there is no image in the ZSL queue!!");
                return null;
            }
            ImageResult imageResult;
            if (index == -1) {
                imageResult = mLv1Queue.valueAt(mLv1Queue.size() - 1);
                mLv1Queue.remove(imageResult.getSensorTimestamp());
            } else {
                if (queueLvl == 1) {
                    imageResult = mLv1Queue.valueAt(index);
                    mLv1Queue.removeAt(index);
                } else {
                    imageResult = mLv2Queue.valueAt(index);
                    mLv2Queue.removeAt(index);
                }
            }
            mInvokedImageQueue.put(imageResult.getSensorTimestamp(), imageResult);
            return imageResult;
        }

        private long getLastEntryTimestamp() {
            if (mLv1Queue.size() == 0) {
                Log.w(TAG, "there is no image in the ZSL queue!!");
                return 0;
            }
            ImageResult lastEntry = mLv1Queue.valueAt(mLv1Queue.size() - 1);
            return lastEntry.getSensorTimestamp();
        }
    }

    public interface ZSLCaptureCallback {
        public void onZSLCaptured(Image image, CaptureResult metadata);
        public void onZSLError(int error);
    }

    public interface TimeNudgeCallback {
        public void onTimeNudgeCaptured(ArrayList<ImageResult> images);
        public void onTimeNudgeError(int error);
    }

    private CameraCaptureSession.CaptureCallback mMainCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
//            long now = System.nanoTime();
//        Log.d(TAG, "nano timestamp = " + timestamp + " , now = " + now + " / delta = " + (now - timestamp));
            if (mPreviewCallback != null) {
                mPreviewCallback.onCaptureStarted(session, request, timestamp, frameNumber);
            }
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            if (mPreviewCallback != null) {
                mPreviewCallback.onCaptureProgressed(session, request, partialResult);
            }
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
//        long stamp = result.get(CaptureResult.SENSOR_TIMESTAMP);
//        Log.d(TAG, "timestamp = " + stamp + " / nano delta for metadata = " + (System.nanoTime() - stamp));
            Message msg = Message.obtain();
            msg.what = MSG_ENQUEUE_METADATA;
            msg.obj = result;
            mHandler.sendMessage(msg);
            if (mPreviewCallback != null) {
                mPreviewCallback.onCaptureCompleted(session, request, result);
            }
        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
            if (mPreviewCallback != null) {
                mPreviewCallback.onCaptureFailed(session, request, failure);
            }
        }

        @Override
        public void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId, long frameNumber) {
            if (mPreviewCallback != null) {
                mPreviewCallback.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
            }
        }

        @Override
        public void onCaptureSequenceAborted(CameraCaptureSession session, int sequenceId) {
            if (mPreviewCallback != null) {
                mPreviewCallback.onCaptureSequenceAborted(session, sequenceId);
            }
        }
    };

//    @Override
//    public void onReceive(CaptureRequest request, CaptureResult result) {
////        Log.d(TAG, "metadata for " + result.get(CaptureResult.SENSOR_TIMESTAMP));
//        Message msg = Message.obtain();
//        msg.what = MSG_ENQUEUE_METADATA;
//        msg.obj = result;
//        mHandler.sendMessage(msg);
//    }
    
}
