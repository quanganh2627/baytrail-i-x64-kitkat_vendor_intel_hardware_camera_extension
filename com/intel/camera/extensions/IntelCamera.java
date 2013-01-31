/*
 * Copyright 2012, Intel Corporation
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

package com.intel.camera.extensions;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.List;
import java.util.Iterator;

/**
 * The IntelCamera class is used for accessing Intel's camera extensions.

 * This class allows user access to Intel camera features, such as Intel Parameters setting, smart scene detection,
 * panorama, and so on.
 * <p>
 * IntelCamera is an extension of android.hardware.Camera. When user create an IntelCamera instance, a Camera instance
 * will be opened, user can get it by calling IntelCamera.{@link #getCameraDevice()}. So in this case, don't calling Camera.open()
 * to obtain a Camera instance.
 * <p>
 * Please release Intel camera by IntelCamera.{@link #release()} after using.
 * <p>
 * IntelCamera provide interface access to Intel parameters, user can get all existing parameters with
 * IntelCamera.{@link #getParameters()}, modify Intel parameters with IntelCamera APIs, then update them with
 * IntelCamera.{@link #setParameters(Parameters param)}.
 * <p>
 * To access the Intel camera device, user must declare the Intel camera extension library in Android project:
 * <p>
 * In Android.mk:
 * <pre>
      LOCAL_JAVA_LIBRARIES:=com.intel.camera.extensions
 * </pre>
 * In AndroidManifest.xml:
 * <pre>
       <CODE><</CODE>uses-library android:name="com.intel.camera.extensions" /<CODE>></CODE>
 * </pre>
 * <p>
 */
public class IntelCamera {
    private static final String SUPPORTED_VALUES_SUFFIX = "-values";
    private static final String KEY_FOCUS_WINDOW = "focus-window";
    private static final String KEY_XNR = "xnr";
    private static final String KEY_ANR = "anr";
    private static final String KEY_GDC = "gdc";
    private static final String KEY_TEMPORAL_NOISE_REDUCTION = "temporal-noise-reduction";
    private static final String KEY_NOISE_REDUCTION_AND_EDGE_ENHANCEMENT = "noise-reduction-and-edge-enhancement";
    private static final String KEY_MULTI_ACCESS_COLOR_CORRECTION = "multi-access-color-correction";
    private static final String KEY_AE_MODE = "ae-mode";
    private static final String KEY_AE_METERING_MODE = "ae-metering-mode";
    private static final String KEY_SHUTTER = "shutter";
    private static final String KEY_APERTURE = "aperture";
    private static final String KEY_ISO = "iso";
    private static final String KEY_BACK_LIGHTING_CORRECTION = "back-lighting-correction-mode";
    private static final String KEY_AF_METERING_MODE = "af-metering-mode";
    private static final String KEY_AWB_MAPPING_MODE = "awb-mapping-mode";
    private static final String KEY_COLOR_TEMPERATURE = "color-temperature";
    private static final String KEY_RAW_DATA_FORMAT = "raw-data-format";
    private static final String KEY_CAPTURE_BRACKET = "capture-bracket";
    private static final String KEY_ROTATION_MODE = "rotation-mode";

    private static final String KEY_CONTRAST_MODE = "contrast-mode";
    private static final String KEY_SATURATION_MODE = "saturation-mode";
    private static final String KEY_SHARPNESS_MODE = "sharpness-mode";

    // HDR
    private static final String KEY_HDR_IMAGING = "hdr-imaging";
    private static final String KEY_HDR_SHARPENING = "hdr-sharpening";
    private static final String KEY_HDR_VIVIDNESS = "hdr-vividness";
    private static final String KEY_HDR_SAVE_ORIGINAL = "hdr-save-original";

    // Ultra low light
    private static final String KEY_ULL = "ull";

    // panorama
    private static final String KEY_PANORAMA = "panorama";

    // face detection and recognition
    private static final String KEY_FACE_DETECTION = "face-detection";
    private static final String KEY_FACE_RECOGNITION = "face-recognition";

    // scene detection
    private static final String KEY_SCENE_DETECTION = "scene-detection";

    // smart shutter
    private static final String KEY_SMILE_SHUTTER = "smile-shutter";
    private static final String KEY_SMILE_SHUTTER_THRESHOLD = "smile-shutter-threshold";
    private static final String KEY_BLINK_SHUTTER = "blink-shutter";
    private static final String KEY_BLINK_SHUTTER_THRESHOLD = "blink-shutter-threshold";

    // hw overlay rendering
    private static final String KEY_HW_OVERLAY_RENDERING = "overlay-render";

    // burst capture
    private static final String KEY_BURST_LENGTH = "burst-length";
    private static final String KEY_BURST_FPS = "burst-fps";

    // values for back light correction
    private static final String BACK_LIGHTING_CORRECTION_ON = "on";
    private static final String BACK_LIGHTING_CORRECTION_OFF = "off";

    // values for af metering mode
    private static final String AF_METERING_MODE_AUTO = "auto";
    private static final String AF_METERING_MODE_SPOT = "spot";

    private static final String KEY_FOCUS_DISTANCES = "focus-distances";
    private static final String KEY_ANTIBANDING = "antibanding";

    private static final String KEY_PANORAMA_LIVE_PREVIEW_SIZE = "panorama-live-preview-size";
    private static final String KEY_SUPPORTED_PANORAMA_LIVE_PREVIEW_SIZES = "panorama-live-preview-sizes";
    private static final String KEY_PANORAMA_MAX_SNAPSHOT_COUNT = "panorama-max-snapshot-count";

    // preview keep alive
    private static final String KEY_PREVIEW_KEEP_ALIVE = "preview-keep-alive";

    // continuous viewfinder
    public static final String KEY_CONTINUOUS_VIEWFINDER = "continuous-viewfinder";
    public static final String KEY_BURST_START_INDEX = "burst-start-index";

    // high speed recording, slow motion playback
    private static final String KEY_SLOW_MOTION_RATE = "slow-motion-rate";

    // Exif data
    public static final String KEY_EXIF_MAKER = "exif-maker-name";
    public static final String KEY_EXIF_MODEL = "exif-model-name";
    public static final String KEY_EXIF_SOFTWARE = "exif-software-name";

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    // values for ae mode setting.
    /** @hide */
    public static final String AE_MODE_AUTO = "auto";
    /** @hide */
    public static final String AE_MODE_MANUAL = "manual";
    /** @hide */
    public static final String AE_MODE_SHUTTER_PRIORITY = "shutter-priority";
    /** @hide */
    public static final String AE_MODE_APERTURE_PRIORITY = "aperture-priority";

    // Values for ae metering setting.
    /** @hide */
    public static final String AE_METERING_AUTO = "auto";
    /** @hide */
    public static final String AE_METERING_SPOT = "spot";
    /** @hide */
    public static final String AE_METERING_CENTER = "center";
    /** @hide */
    public static final String AE_METERING_CUSTOMIZED = "customized";

    // Values for awb mapping mode.
    /** @hide */
    public static final String AWB_MAPPING_AUTO = "auto";
    /** @hide */
    public static final String AWB_MAPPING_INDOOR = "indoor";
    /** @hide */
    public static final String AWB_MAPPING_OUTDOOR = "outdoor";

    /** @hide */
    public static final String FLASH_MODE_DAY_SYNC = "day-sync";
    /** @hide */
    public static final String FLASH_MODE_SLOW_SYNC = "slow-sync";

    /** @hide */
    public static final String FOCUS_MODE_MANUAL = "manual";

    /** @hide */
    public static final String FOCUS_MODE_TOUCH = "touch";

    /** @hide */
    public static final String SLOW_MOTION_RATE_1x = "1x";
    public static final String SLOW_MOTION_RATE_2x = "2x";
    public static final String SLOW_MOTION_RATE_3x = "3x";
    public static final String SLOW_MOTION_RATE_4x = "4x";

    // value of contrast mode
    /** @hide */
    public static final String CONTRAST_MODE_NORMAL = "normal";
    /** @hide */
    public static final String CONTRAST_MODE_SOFT = "soft";
    /** @hide */
    public static final String CONTRAST_MODE_HARD = "hard";

    // value of saturation mode
    /** @hide */
    public static final String SATURATION_MODE_NORMAL = "normal";
    /** @hide */
    public static final String SATURATION_MODE_LOW = "low";
    /** @hide */
    public static final String SATURATION_MODE_HIGH = "high";

    // value of sharpness mode
    /** @hide */
    public static final String SHARPNESS_MODE_NORMAL = "normal";
    /** @hide */
    public static final String SHARPNESS_MODE_SOFT = "soft";
    /** @hide */
    public static final String SHARPNESS_MODE_HARD = "hard";

    private Camera mCameraDevice = null;
    private Parameters mParameters;
    private EventHandler mEventHandler;
    private SceneModeListener mSceneListener;
    private PanoramaListener mPanoramaListener;
    private boolean mSceneDetectionRunning = false;
    private boolean mPanoramaRunning = false;
    private boolean mSmileShutterRunning = false;
    private boolean mBlinkShutterRunning = false;
    private int mNativeContext; //accessed by native methods

    private static final String TAG = "com.intel.cameraext.Camera";

    private native final void native_setup(Object camera_this, Camera cameraDevice);
    private native final void native_release();
    private native final boolean native_enableIntelCamera();
    private native final void native_startSceneDetection();
    private native final void native_stopSceneDetection();
    private native final void native_startPanorama();
    private native final void native_stopPanorama();
    private native final void native_startSmileShutter();
    private native final void native_stopSmileShutter();
    private native final void native_startBlinkShutter();
    private native final void native_stopBlinkShutter();
    private native final void native_cancelSmartShutterPicture();
    private native final void native_forceSmartShutterPicture();
    private native final void native_startFaceRecognition();
    private native final void native_stopFaceRecognition();

    // here need keep pace with native msgType
    private static final int CAMERA_MSG_SCENE_DETECT = 0x2001;
    private static final int CAMERA_MSG_PANORAMA_SNAPSHOT = 0x2003;
    private static final int CAMERA_MSG_PANORAMA_METADATA = 0x2005;

    static {
        System.loadLibrary("intelcamera_jni");
    }

    public IntelCamera(int cameraId) {
        mCameraDevice = android.hardware.Camera.open(cameraId);
        init();
    }

    public IntelCamera() {
        mCameraDevice = android.hardware.Camera.open();
        init();
    }

    public final void release() {
        native_release();
        if (mCameraDevice != null) {
            mCameraDevice.release();
            mCameraDevice = null;
        }
    }

    private void init() {
        native_setup(new WeakReference<IntelCamera>(this), mCameraDevice);

        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }

        native_enableIntelCamera();
    }


    public Camera getCameraDevice() {
        return mCameraDevice;
    }

    private static void postEventFromNative(Object camera_ref,
                                            int what, int arg1, int arg2, Object obj)
    {
        IntelCamera c = (IntelCamera)((WeakReference)camera_ref).get();
        if (c == null)
            return;

        if (c.mEventHandler != null) {
            Message m = c.mEventHandler.obtainMessage(what, arg1, arg2, obj);
            c.mEventHandler.sendMessage(m);
        }
    }
    private class EventHandler extends Handler
    {
        private IntelCamera mCamera;

        public EventHandler(IntelCamera c, Looper looper) {
            super(looper);
            mCamera = c;
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case CAMERA_MSG_SCENE_DETECT:
                Log.v(TAG, "SceneListener");
                if (mSceneListener != null) {
                    mSceneListener.onSceneChange(msg.arg1, msg.arg2 == 0 ? false : true);
                }
                return;
            case CAMERA_MSG_PANORAMA_METADATA:
                PanoramaMetadata metadata = (PanoramaMetadata) msg.obj;
                if (mPanoramaListener != null)
                    mPanoramaListener.onDisplacementChange(metadata);
                break;
            case CAMERA_MSG_PANORAMA_SNAPSHOT:
                PanoramaSnapshot snapshot = (PanoramaSnapshot) msg.obj;
                if (mPanoramaListener != null)
                    mPanoramaListener.onSnapshotTaken(snapshot);
                break;
            default:
                Log.e(TAG, "Unknown intel message type " + msg.what);
                return;
           }
        }
    }

    /**
     * The PanoramaSnapshot class is used to carry information in the PanoramaListener
     * callbacks.
     * @see IntelCamera.PanoramaListener
     * @see #setPanoramaListener(PanoramaListener listener)
     * @hide
     */
    public static class PanoramaSnapshot
    {
        public PanoramaSnapshot()
        {
        }

        /**
         * Metadata during the panorama snapshot. This includes the displacement of the live
         * preview image compared to the previous live preview image.
         */
        public PanoramaMetadata metadataDuringSnap;

        /**
         * Snapshot holds the live preview image which is NV12 format. Size can be set with
         * {@link #setPanoramaLivePreviewSize(int width, int height)}.
         * <p>
         * Note, that the live preview image size is typically small compared to the actual
         * preview size.
         */
        public byte[] snapshot;
    }

    /**
     * The PanoramaMetadata class carries metadata information during panorama mode via the
     * callbacks of the PanoramaListener.
     * @see #setPanoramaListener(PanoramaListener listener)
     * @see IntelCamera.PanoramaListener
     * @see IntelCamera.PanoramaSnapshot
     * @hide
     */
    public static class PanoramaMetadata
    {
        public PanoramaMetadata() {
        }
        /**
         * Direction tells what direction the panorama engine has selected for stitching
         * based on initial movement after first snapshot. Values are:
         * 1 - right
         * 2 - left
         * 3 - down
         * 4 - up
         */
        public int direction = 0;
        /**
         * Horizontal displacement as preview pixels compared to the preview location during
         * previous panorama snapshot. Positive values are to the right, meaning the viewfinder
         * is moved to right of the previous snapshot. Negative values similarly to the left.
         */
        public int horizontalDisplacement = 0;
        /**
         * Vertical displacement as preview pixels compared to the preview location during
         * previous panorama snapshot. Negative values are to up, meaning the viewfinder is
         * moved upwards of the previous snapshot. Positive values similarly to down.
         */
        public int verticalDisplacement = 0;
        /**
         * Motion blur indicates if the camera panning during panorama capturing is done too fast
         * and the end result of taking a panorama snapshot would be blurred.
         */
        public boolean motionBlur = false;
        /**
         * finalizationStarted signals the application whether finalization has begun automatically
         * due to reaching the maximum number of panorama snapshots. Only valid when metadata is 
         * carried with a onSnapshotTaken callback.
         */
        public boolean finalizationStarted = false;
    }

    /**
     * Sets the panorama listener for receiving panorama displacement callbacks
     * and live preview images.
     * @param listener the new PanoramaListener
     * @hide
     */
    public void setPanoramaListener(PanoramaListener listener)
    {
        mPanoramaListener = listener;
    }

    /**
     * The PanoramaListener interface is for receiving panorama callbacks.
     * @hide
     */
    public interface PanoramaListener
    {
        /**
         * The onDisplacementChangeNotify callback notifies of the viewfinder moving
         * during panorama capturing. The displacement pixel information is given in actual
         * viewfinder preview pixels.
         * @param metadata about the panorama capturing
         * @hide
         */
        void onDisplacementChange(PanoramaMetadata metadata);

        /**
         * The onSnapshotTaken notifies of a snapshot during panorama capturing. It contains
         * metadata of panorama displacement during time of snapshot and the NV12
         * format live preview image.
         * <p>
         * Note, that the associated displacement pixel information
         * is given in live preview image pixels to make it easier for the application to lay
         * the live preview images on top of each other, if needed.
         * @param snapshot live preview image of the snapshot and associated metadata
         * @hide
         */
        void onSnapshotTaken(PanoramaSnapshot snapshot);
    }

    public interface SceneModeListener
    {
        /**
        * Notify the listener of the detected scene mode.
        *
        * @param scene The string constant of scene mode detected.
        * The string provided is one of those being returned by getSceneMode()
        * @param hdrHint The detected HDR state in current scene. True, if HDR
        * is detected
        * @see #getSceneMode()
        */
        void onSceneChange(int scene, boolean hdrHint);
    };

    /**
    * @hide
    * Registers a listener to be notified about the scene detected in the
    * preview frames.
    *
    * @param listener the listener to notify
    * @see #startSceneDetection()
    */
    public final void setSceneModeListener(SceneModeListener listener)
    {
        mSceneListener = listener;
    }

    /**
     * @hide
     * Starts the smart scene detection.
     * After calling, the camera will notify {@link SceneModeListener} of the detected
     * scene modes.
     * Note that some scene modes (like "portrait") are not detected, unless
     * {@link #startFaceDetection()} has been called by the application.
     * If the scene detection has started, apps should not call this again
     * before calling {@link #stopSceneDetection()}.
     * @see #setSceneModeListener()
     */
    public final void startSceneDetection()
    {
        if (mSceneDetectionRunning) {
            throw new RuntimeException("Scene detection is already running");
        }
        native_startSceneDetection();
        mSceneDetectionRunning = true;
    }

    /**
     * @hide
     * Stops the smart scene detection.
     * @see #startSceneDetection()
     */
    public final void stopSceneDetection()
    {
        native_stopSceneDetection();
        mSceneDetectionRunning = false;
    }

    /**
     * Starts the panorama mode. Preview must be started before you can call this function.
     * <p>
     * In panorama mode the takePicture API will behave differently. The first takePicture call
     * will start the panorama capture sequence. During the capture sequence, images are captured
     * automatically while the camera is turned. At the same time, PanoramaListener
     * callbacks will be called for both giving displacement feedback during panning, and for the
     * taken snapshots.
     * <p>
     * The capturing will either end automatically after reaching the maximum count
     * of panorama snapshots, or by calling the takePicture API another time. At that point the
     * final JPEG is returned through the normal PictureCallback given to takePicture.
     * <p>
     * Raw and postview callback types are not supported.
     * <p>
     * Face detection will be automatically stopped as soon as the first image for the panorama has
     * been captured.
     * <p>
     * Flash will not fire during panorama regardless of the flash setting.
     * <p>
     * Exposure, focus and white balance should be locked by using setParameters before takePicture
     * is called first time, to get best image quality.
     * <p>
     * Smart scene detection and smart shutter functionality should be stopped with
     * {@link #stopSceneDetection()}, {@link #stopSmileShutter()} and {@link #stopBlinkShutter()}
     * before calling takePicture for the first time.
     * <p>
     * If the panorama mode has been started, apps should not call this again
     * before calling {@link #stopPanorama()}.
     *
     * @see #stopPanorama()
     * @see #setPanoramaListener(PanoramaListener listener)
     * @see #stopSceneDetection()
     * @hide
     */
    public final void startPanorama() {
        if(mPanoramaRunning) {
            throw new RuntimeException("Panorama is already running");
        }
        native_startPanorama();
        mPanoramaRunning = true;
    }

    /**
     * Stops the panorama mode.
     * @see #startPanorama()
     * @hide
     */
    public final void stopPanorama() {
        native_stopPanorama();
        mPanoramaRunning = false;
    }

    /**
     * @hide
     * Starts the smile detection Smart Shutter.
     * After calling, the camera will trigger on smile when user presses shutter
     * Note that smile detection doesnt work unless
     * {@link #startFaceDetection()} has been called by the application.
     * If the smile shutter has started, apps should not call this again
     * before calling {@link #stopSmileShutter()}.
     */
    public final void startSmileShutter()
    {
        if (mSmileShutterRunning) {
            throw new RuntimeException("Smile Shutter is already running");
        }
        native_startSmileShutter();
        mSmileShutterRunning = true;
    }

    /**
     * @hide
     * Stops the smile shutter trigger.
     * @see #startSmileShutter()
     */
    public final void stopSmileShutter()
    {
        native_stopSmileShutter();
        mSmileShutterRunning = false;
    }

    /**
     * @hide
     * Starts the blink shutter.
     * After calling, the camera will capture on eye blinking events
     * when the user press and hold the camera shutter key.
     * Note that this feasture is not working, unless
     * {@link #startFaceDetection()} has been called by the application.
     * If the blink shutter has started, apps should not call this again
     * before calling {@link #stopBlinkShutter()}.
     */
    public final void startBlinkShutter()
    {
        if (mBlinkShutterRunning) {
            throw new RuntimeException("Blink Shutter is already running");
        }
        native_startBlinkShutter();
        mBlinkShutterRunning = true;
    }

    /**
     * @hide
     * Stops the blink shutter.
     * @see #startBlinkShutter()
     */
    public final void stopBlinkShutter()
    {
        native_stopBlinkShutter();
        mBlinkShutterRunning = false;
    }

    /**
     * @hide
     * Cancel capture on smart shutter even when no smile are detected.
     * !!! This must be used only when takePicture() has been initiated
     * !!! during either smile or blink shutter is started.
     */
    public final void cancelSmartShutterPicture()
    {
        native_cancelSmartShutterPicture();
    }

    /**
     * @hide
     * Force capture on smart shutter even when no smile are detected.
     * !!! This must be used only when takePicture() has been initiated
     * !!! during either smile or blink shutter is started.
     */
    public final void forceSmartShutterPicture()
    {
        native_forceSmartShutterPicture();
    }

    /**
     * @hide
     * Starts face recognition.
     * Before starting face recognition the application must start
     * face detection by calling {@link #startFaceDetection()}.
     * If face recognition has been started, application should not call
     * this again before calling {@link #stopFaceRecognition()}.
     */
    public final void startFaceRecognition()
    {
        native_startFaceRecognition();
    }

    /**
     * @hide
     * Stops face recognition.
     * @see #startFaceRecognition()
     */
    public final void stopFaceRecognition()
    {
        native_stopFaceRecognition();
    }

    public Parameters getParameters() {
        mParameters = mCameraDevice.getParameters();
        return mParameters;
    }

    public void setParameters(Parameters param) {
        mCameraDevice.setParameters(param);
    }
    /**
    * @hide
    */
    public String getXNR() {
        return mParameters.get(KEY_XNR);
    }

    /**
     * @hide
     */
    public void setXNR(String value) {
        mParameters.set(KEY_XNR, value);
    }

    /**
     * @hide
     */
    public List<String> getSupportedXNR() {
        String str = mParameters.get(KEY_XNR + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    /**
     * @hide
     */
    public String getANR() {
        return mParameters.get(KEY_ANR);
    }

    /**
     * @hide
     */
    public void setANR(String value) {
        mParameters.set(KEY_ANR, value);
    }

    /**
     * @hide
     */
    public List<String> getSupportedANR() {
        String str = mParameters.get(KEY_ANR + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    /**
     * @hide
     */
    public String getGDC() {
        return mParameters.get(KEY_GDC);
    }

    /**
     * @hide
     */
    public void setGDC(String value) {
        mParameters.set(KEY_GDC, value);
    }

    /**
     * @hide
     */
    public List<String> getSupportedGDC() {
        String str = mParameters.get(KEY_GDC + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    /**
     * @hide
     */
    public String getTemporalNoiseReduction() {
        return mParameters.get(KEY_TEMPORAL_NOISE_REDUCTION);
    }

    /**
     * @hide
     */
    public void setTemporalNoiseReduction(String value) {
            mParameters.set(KEY_TEMPORAL_NOISE_REDUCTION, value);
    }

    /**
     * @hide
     */
    public List<String> getSupportedTemporalNoiseReduction() {
        String str = mParameters.get(KEY_TEMPORAL_NOISE_REDUCTION + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    /**
     * @hide
     */
    public String getNoiseReductionAndEdgeEnhancement() {
        return mParameters.get(KEY_NOISE_REDUCTION_AND_EDGE_ENHANCEMENT);
    }

    /**
     * @hide
     */
    public void setNoiseReductionAndEdgeEnhancement(String value) {
        mParameters.set(KEY_NOISE_REDUCTION_AND_EDGE_ENHANCEMENT, value);
    }

    /**
     * @hide
     */
    public List<String> getSupportedNoiseReductionAndEdgeEnhancement() {
        String str = mParameters.get(KEY_NOISE_REDUCTION_AND_EDGE_ENHANCEMENT + SUPPORTED_VALUES_SUFFIX);
       return split(str);
    }

    /**
     * @hide
     */
    public String getColorCorrection() {
        return mParameters.get(KEY_MULTI_ACCESS_COLOR_CORRECTION);
    }

   /**
    * @hide
    */
  public void setColorCorrection(String value) {
        mParameters.set(KEY_MULTI_ACCESS_COLOR_CORRECTION, value);
    }

    /**
    * @hide
    */
   public List<String> getSupportedColorCorrections() {
        String str = mParameters.get(KEY_MULTI_ACCESS_COLOR_CORRECTION + SUPPORTED_VALUES_SUFFIX);
       return split(str);
    }

     /**
     * @hide
     */
    public String getAEMeteringMode() {
        return mParameters.get(KEY_AE_METERING_MODE);
    }

    /**
     * @hide
     */
    public void setAEMeteringMode(String value) {
        mParameters.set(KEY_AE_METERING_MODE, value);
    }

    /**
    * @hide
    */
    public List<String> getSupportedAEMeteringModes() {
        String str = mParameters.get(KEY_AE_METERING_MODE + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    /**
     * @hide
     */
    public String getAEMode() {
        return mParameters.get(KEY_AE_MODE);
    }

    /**
     * @hide
     */
    public void setAEMode(String value) {
        mParameters.set(KEY_AE_MODE, value);
    }

    /**
     * @hide
     */
    public List<String> getSupportedAEModes() {
        String str = mParameters.get(KEY_AE_MODE + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    /**
     * @hide
     */
    public String getContrastMode() {
        return mParameters.get(KEY_CONTRAST_MODE);
    }

    /**
     * @hide
     */
    public void setContrastMode(String value) {
        mParameters.set(KEY_CONTRAST_MODE, value);
    }

    /**
     * @hide
     */
    public List<String> getSupportedContrastModes() {
        String str = mParameters.get(KEY_CONTRAST_MODE + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    /**
     * @hide
     */
    public String getSaturationMode() {
        return mParameters.get(KEY_SATURATION_MODE);
    }

    /**
     * @hide
     */
    public void setSaturationMode(String value) {
        mParameters.set(KEY_SATURATION_MODE, value);
    }

    /**
     * @hide
     */
    public List<String> getSupportedSaturationModes() {
        String str = mParameters.get(KEY_SATURATION_MODE + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    /**
     * @hide
     */
    public String getSharpnessMode() {
        return mParameters.get(KEY_SHARPNESS_MODE);
    }

    /**
     * @hide
     */
    public void setSharpnessMode(String value) {
        mParameters.set(KEY_SHARPNESS_MODE, value);
    }

    /**
     * @hide
     */
    public List<String> getSupportedSharpnessModes() {
        String str = mParameters.get(KEY_SHARPNESS_MODE + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    /**
     * @hide
     */
    public String getShutter() {
        return mParameters.get(KEY_SHUTTER);
    }

    /**
     * @hide
     */
    public void setShutter(String value) {
        mParameters.set(KEY_SHUTTER, value);
    }

    /**
     * @hide
     */
    public List<String> getSupportedShutter() {
        String str = mParameters.get(KEY_SHUTTER + SUPPORTED_VALUES_SUFFIX);
       return split(str);
    }

    /**
     * @hide
     */
    public String getAperture() {
        return mParameters.get(KEY_APERTURE);
    }

    /**
     * @hide
     */
    public void setAperture(String value) {
        mParameters.set(KEY_APERTURE, value);
    }

    /**
     * @hide
     */
    public List<String> getSupportedAperture() {
        String str = mParameters.get(KEY_APERTURE + SUPPORTED_VALUES_SUFFIX);
       return split(str);
    }

    /**
     * @hide
     */
    public String getISO() {
        return mParameters.get(KEY_ISO);
    }

    /**
     * @hide
     */
    public void setISO(String value) {
        mParameters.set(KEY_ISO, value);
    }

    /**
     * @hide
     */
    public List<String> getSupportedISO() {
        String str = mParameters.get(KEY_ISO + SUPPORTED_VALUES_SUFFIX);
       return split(str);
    }

    /**
     * @hide
     */
    public void setExifMaker(String value) {
        mParameters.set(KEY_EXIF_MAKER, value);
    }

    /**
     * @hide
     */
    public void setExifModel(String value) {
        mParameters.set(KEY_EXIF_MODEL, value);
    }

    /**
     * @hide
     */
    public void setExifSoftware(String value) {
        mParameters.set(KEY_EXIF_SOFTWARE, value);
    }

    /**
     * Gets the current awb mapping mode.
     *
     * @return current awb mapping mode. null if awb mapping mode is not supported.
     * @see #AWB_MAPPING_INDOOR
     * @see #AWB_MAPPING_OUTDOOR
     * @hide
     */
    public String getAWBMappingMode() {
        return mParameters.get(KEY_AWB_MAPPING_MODE);
    }

    /**
     * Sets the current awb mapping mode.
     *
     * @param value new awb mapping mode.
     * @see #get getAWBMappingMode()
     * @hide
     */
    public void setAWBMappingMode(String value) {
        mParameters.set(KEY_AWB_MAPPING_MODE, value);
    }

    /**
     * Gets the supported awb mapping mode.
     *
     * @return a list of supported awb mapping mode. null if awb mapping mode setting
     *         is not supported.
     * @hide
     */
    public List<String> getSupportedAWBMappingModes() {
        String str = mParameters.get(KEY_AWB_MAPPING_MODE + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    /**
     * Gets the current color temperature.
     *
     * @return current color temperature.
     * @hide
     */
    public String getColorTemperature() {
        return mParameters.get(KEY_COLOR_TEMPERATURE);
    }

    /**
     * Sets the current color temperature.
     *
     * @param value new color temperature.
     * @see #get getColorTemperature()
     * @hide
     */
    public void setColorTemperature(int value) {
        mParameters.set(KEY_COLOR_TEMPERATURE, value);
    }

    /**
     * Gets the current af metering mode.
     *
     * @return current af metering mode. null if af metering mode is not supported.
     * @see #AF_METERING_MODE_AUTO
     * @see #AF_METERING_MODE_SPOT
     * @hide
     */
    public String getAFMeteringMode() {
        return mParameters.get(KEY_AF_METERING_MODE);
    }

    /**
     * Sets the current af metering mode.
     *
     * @param value new af metering mode.
     * @see #get getAFMeteringMode()
     * @hide
     */
    public void setAFMeteringMode(String value) {
        mParameters.set(KEY_AF_METERING_MODE, value);
    }

    /**
     * Gets the supported af metering mode.
     *
     * @return a list of supported af metering mode. null if af metering mode setting
     *         is not supported.
     * @hide
     */
    public List<String> getSupportedAFMeteringModes() {
        String str = mParameters.get(KEY_AF_METERING_MODE + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    /**
     * Gets the current back light correction mode.
     *
     * @return current back light correction. null if back light correction is not supported.
     * @see #BACK_LIGHTING_CORRECTION_ON
     * @see #BACK_LIGHTING_CORRECTION_OFF
     * @hide
     */
    public String getBackLightCorrectionMode() {
        return mParameters.get(KEY_BACK_LIGHTING_CORRECTION);
    }

    /**
     * Sets the current back light correction mode.
     *
     * @param value new back light correction mode.
     * @see #get getBackLightCorrectionMode()
     * @hide
     */
    public void setBackLightCorrectionMode(String value) {
        mParameters.set(KEY_BACK_LIGHTING_CORRECTION, value);
    }

    /**
     * Gets the supported back light correction mode.
     *
     * @return a list of supported back light correction modes. null if back light correction mode setting
     *         is not supported.
     * @hide
     */
    public List<String> getSupportedBackLightCorrectionModes() {
        String str = mParameters.get(KEY_BACK_LIGHTING_CORRECTION + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    /**
     * Gets burst mode capture length.
     *
     * @return burst mode capture length.
     * @hide
     */
    public int getBurstLength() {
        String str = mParameters.get(KEY_BURST_LENGTH);
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    /**
     * Sets burst mode capture length.
     * NOTE: after starting burst capturing, application must wait
     * for all jpeg callbacks to return before starting preview.
     *
     * @param value burst mode capture length.
     * @hide
     */
    public void setBurstLength(int value) {
        mParameters.set(KEY_BURST_LENGTH, value);
    }

    /**
     * Gets the supported burst mode capture length.
     *
     * @return a list of supported burst mode capure length. null if this feature
     *         is not supported.
     * @hide
     */
    public List<String> getSupportedBurstLength() {
        String str = mParameters.get(KEY_BURST_LENGTH + SUPPORTED_VALUES_SUFFIX);
        if (str == null || str.equals("") || str.equals("1")) {
            Log.v(TAG, "Return null for key:" + KEY_BURST_LENGTH + SUPPORTED_VALUES_SUFFIX);
            return null;
        } else {
            return split(str);
        }
    }

    /**
     * Gets burst mode fps.
     *
     * @return burst mode fps.
     * @hide
     */
    public int getBurstFps() {
        String str = mParameters.get(KEY_BURST_FPS);
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    /**
     * Sets burst mode fps.
     *
     * @param value burst mode fps.
     * @hide
     */
    public void setBurstFps(int value) {
        mParameters.set(KEY_BURST_FPS, value);
    }

    /**
     * Gets the supported burst mode fps.
     *
     * @return a list of supported burst fps. null if this feature
     *         is not supported.
     * @hide
     */
    public List<String> getSupportedBurstFps() {
        String str = mParameters.get(KEY_BURST_FPS + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    /**
     * Gets start index for burst.
     *
     * @return burst mode capture length.
     * @hide
     */
    public int getBurstStartIndex() {
        return getInt(KEY_BURST_START_INDEX, 1);
    }

    /**
     * Sets start index for burst
     *
     * @param value burst mode capture length.
     * @hide
     */
    public void setBurstStartIndex(int value) {
        mParameters.set(KEY_BURST_START_INDEX, value);
    }

    /**
     * Gets the supported burst start indexes.
     *
     * @return a list of supported burst mode capure length. null if this feature
     *         is not supported.
     * @hide
     */
    public List<String> getSupportedBurstStartIndex() {
        return getSupportedValues(KEY_BURST_START_INDEX + SUPPORTED_VALUES_SUFFIX);
    }

    /**
     * Gets the supported continuous viewfinder.
     *
     * @return a list of supported values for continuous vf. null if this feature
     *         is not supported.
     * @hide
     */
    public List<String> getSupportedContinuousViewfinder() {
        return getSupportedValues(KEY_CONTINUOUS_VIEWFINDER + SUPPORTED_VALUES_SUFFIX);
    }

    /**
     * Sets state of Preview keep alive feature
     *
     * @param toggle switch for feature on/off
     * @hide
     */
    public void setPreviewKeepAlive(boolean toggle) {
        mParameters.set(KEY_PREVIEW_KEEP_ALIVE, toggle ? TRUE : FALSE);
    }

    /**
     * Sets state of Continuous Viewfinder feature
     * @hide
     */
    public void setContinuousViewfinder(boolean toggle) {
        mParameters.set(KEY_CONTINUOUS_VIEWFINDER, toggle ? TRUE : FALSE);
    }
    /**
     * Gets the current raw data format.
     *
     * @return current raw data format. null if this feature is not supported.
     * @hide
     */
    public String getRAWDataFormat() {
        return mParameters.get(KEY_RAW_DATA_FORMAT);
    }

    /**
     * Sets the current raw picture format.
     *
     * @param value new raw picture format
     * @see #get getRAWDataFormat()
     * @hide
     */
    public void setRAWDataFormat(String value) {
        mParameters.set(KEY_RAW_DATA_FORMAT, value);
    }

    /**
     * Gets the supported raw data formats.
    *
     * @return a list of supported af raw data formats. null if this feature
     *         is not supported.
     * @hide
     */
    public List<String> getSupportedRAWDataFormats() {
        String str = mParameters.get(KEY_RAW_DATA_FORMAT + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    /**
     * Gets the current capture bracket mode.
     *
     * @return current capture bracket mode. null if this feature is not supported.
     * @hide
     */
    public String getCaptureBracket() {
        return mParameters.get(KEY_CAPTURE_BRACKET);
    }

    /**
     * Sets the current capture bracket mode.
     * NOTE: after starting capturing with bracketing, application must wait
     * for all jpeg callbacks to return before starting preview.
     *
     * @param value new capture bracket mode
     * @hide
     */
    public void setCaptureBracket(String value) {
        mParameters.set(KEY_CAPTURE_BRACKET, value);
    }

    /**
     * Gets the supported capture bracket mode.
     *
     * @return a list of supported capture bracket mode. null if this feature
     *         is not supported.
     * @hide
     */
    public List<String> getSupportedCaptureBracket() {
        String str = mParameters.get(KEY_CAPTURE_BRACKET + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    /**
     * Get the supported hw overlay rendering modes.
     *
     * @return the supported hw overlay rendering modes. null if this feature
     *         is not supported.
     * @hide
     */
    public List<String> getSupportedHWOverlayRendering() {
        return split(mParameters.get(KEY_HW_OVERLAY_RENDERING + SUPPORTED_VALUES_SUFFIX));
    }

    /**
     * Set the hw overlay rendering mode.
     * This can conly be set before the preview is started
     * otherwise the command will be ignored. The application can check whether
     * the value of the parameter KEY_HW_OVERLAY_RENDERING changed to check
     * whether the command succeeded or not
     * @param value new overlay rendering mode
     * @hide
     */
    public void setHWOverlayRendering(String value) {
        mParameters.set(KEY_HW_OVERLAY_RENDERING, value);
    }

    /**
     * Gets the current rotation mode.
     *
     * @return current rotation mode. null if this feature is not supported.
     * @hide
     */
    public String getRotationMode() {
        return mParameters.get(KEY_ROTATION_MODE);
    }

    /**
     * Sets the current rotation mode.
     *
     * @param value new rotation mode
     * @hide
     */
    public void setRotationMode(String value) {
        mParameters.set(KEY_ROTATION_MODE, value);
    }

    /**
     * Gets the supported rotation modes.
     *
     * @return a list of supported af rotation modes. null if this feature
     *         is not supported.
     * @hide
     */
    public List<String> getSupportedRotationModes() {
        String str = mParameters.get(KEY_ROTATION_MODE + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    /**
     * Sets the focus distances
     * @hide
     */
    public void setFocusDistances(float[] input) {
        if (input == null || input.length != 3) {
            throw new IllegalArgumentException(
                    "output must be an float array with three elements.");
        }
        mParameters.set(KEY_FOCUS_DISTANCES, "" + input[Parameters.FOCUS_DISTANCE_NEAR_INDEX] + "," +
            input[Parameters.FOCUS_DISTANCE_OPTIMAL_INDEX] + "," + input[mParameters.FOCUS_DISTANCE_FAR_INDEX]);
    }

    /**
     * Sets the focus distances - for single input
     * @hide
     */
    public void setFocusDistance(float input) {
        mParameters.set(KEY_FOCUS_DISTANCES, "" + input + "," + input + "," + input);
    }

    /**
     * Gets the focus distances - for single output
     * @hide
     */
    public float getFocusDistance() {
        float[] output = new float[3];
       splitFloat(mParameters.get(KEY_FOCUS_DISTANCES), output);
        return output[Parameters.FOCUS_DISTANCE_OPTIMAL_INDEX];
    }

    /**
     * Sets the focus distances - for single input
     * @hide
     */
    public void setFocusWindow(int input[]) {
        if (input == null || input.length != 4) {
            throw new IllegalArgumentException(
                    "output must be an int array with four elements.");
        }
        mParameters.set(KEY_FOCUS_WINDOW, "" + input[0]
            + "," + input[1] + "," + input[2] + "," + input[3]);
    }

    /**
     * Gets the current Ultra-low light (ULL) status.
     *
     * @return current ULL status.
     * @hide
     */
    public String getULL() {
        return mParameters.get(KEY_ULL);
    }

    /**
     * Sets the Ultra-low light (ULL) status.
     *
     * @param value new Ultra-low light mode
     *
     * The behavior differs upon the set parameter value:
     * "on": forces ULL on, bypassing the 3A logic that normally would trigger the ULL processing.
     * NOTE: If the value is set to "on" when preview has already been started, a preview re-start is triggered. (for testing)
     * "off": ULL will not be used
     * "auto": ULL shooting is determined and triggered by the 3A logic (this is the normal mode)
     *
     * When the ULL image is taken (parameter values "on" and "auto"), the application will receive 2 JPEG images:
     * the first one is the normal snapshot via PictureCallback. Second one is the ULL image, received via the UllListener interface.
     *
     * @see IntelCamera.UllListener
     * @see IntelCamera.UllSnapshot
     * @hide
     */
    public void setULL(String value) {
        mParameters.set(KEY_ULL, value);
    }

    /**
     * Gets the supported Ultra-low light (ULL) values.
     *
     * @return supported ULL values.
     *
     * @see IntelCamera#setULL(String)
     * @hide
     */
    public List<String> getSupportedULL() {
        return getSupportedValues(KEY_ULL + SUPPORTED_VALUES_SUFFIX);
    }

    /**
     * Gets the current HDR Imaging mode.
     *
     * @return current HDR imaging mode. null if this feature is not supported.
     * @hide
     */
    public String getHDRImaging() {
        return mParameters.get(KEY_HDR_IMAGING);
    }

    /**
     * Sets the current HDR Imaging mode.
     *
     * @param value new HDR Imaging mode
     * @hide
     */
    public void setHDRImaging(String value) {
        mParameters.set(KEY_HDR_IMAGING, value);
    }

    /**
     * Gets the supported HDR Imaging mode.
     *
     * @return a list of supported HDR Imaging mode. null if this feature
     *         is not supported.
     * @hide
     */
    public List<String> getSupportedHDRImaging() {
        return getSupportedValues(KEY_HDR_IMAGING + SUPPORTED_VALUES_SUFFIX);
    }

    /**
     * Gets the current HDR Sharpening mode.
     *
     * @return current HDR Sharpening mode. null if this feature is not supported.
     * @hide
     */
    public String getHDRSharpening() {
        return mParameters.get(KEY_HDR_SHARPENING);
    }

    /**
     * Sets the current HDR Sharpening mode.
     *
     * @param value new HDR Sharpening mode
     * @hide
     */
    public void setHDRSharpening(String value) {
        mParameters.set(KEY_HDR_SHARPENING, value);
    }

    /**
     * Gets the supported HDR Sharpening mode.
     *
     * @return a list of supported HDR Sharpening mode. null if this feature
     *         is not supported.
     * @hide
     */
    public List<String> getSupportedHDRSharpening() {
        String str = mParameters.get(KEY_HDR_SHARPENING + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    /**
     * Gets the current HDR Vividness Enhancement mode.
     *
     * @return current HDR Vividness Enhancement mode. null if this feature is not supported.
     * @hide
     */
    public String getHDRVividness() {
        return mParameters.get(KEY_HDR_VIVIDNESS);
    }

    /**
     * Sets the current HDR Vividness Enhancement mode.
     *
     * @param value new HDR Vividness Enhancement mode
     * @hide
     */
    public void setHDRVividness(String value) {
        mParameters.set(KEY_HDR_VIVIDNESS, value);
    }

    /**
     * Gets the supported HDR Vividness Enhancement mode.
     *
     * @return a list of supported HDR Vividness Enhancement mode. null if this feature
     *         is not supported.
     * @hide
     */
    public List<String> getSupportedHDRVividness() {
        String str = mParameters.get(KEY_HDR_VIVIDNESS + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    /**
     * Gets the current HDR Save Original mode.
     *
     * @return current HDR Save Original mode. null if this feature is not supported.
     * @hide
     */
    public String getHDRSaveOriginal() {
        return mParameters.get(KEY_HDR_SAVE_ORIGINAL);
    }

    /**
     * Sets the current HDR Save Original mode.
     * NOTE: after starting HDR capturing with HDR Save Original on, application must wait
     * for all jpeg callbacks to return before starting preview.
     *
     * @param value new HDR Save Original mode
     * @hide
     */
    public void setHDRSaveOriginal(String value) {
        mParameters.set(KEY_HDR_SAVE_ORIGINAL, value);
    }

    /**
     * Gets the supported HDR Save Original mode.
     *
     * @return a list of supported HDR Save Original mode. null if this feature
     *         is not supported.
     * @hide
     */
    public List<String> getSupportedHDRSaveOriginal() {
        String str = mParameters.get(KEY_HDR_SAVE_ORIGINAL + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    private Camera.Size parseSize(String str) {
        StringTokenizer tokenizer = new StringTokenizer(str, "x");
        int width = 0, height = 0;
        if (tokenizer.hasMoreElements())
            width = Integer.parseInt(tokenizer.nextToken());
        if (tokenizer.hasMoreElements())
            height = Integer.parseInt(tokenizer.nextToken());

        return mCameraDevice.new Size(width, height);
    }

    /**
     * Gets if the scene detection (hdr hint) is supported
     *
     * @return on or off if the feature is supported null if this feature
               is not supported
     * @hide
     */
    public List<String> getSupportedSceneDetection() {
        return getSupportedValues(KEY_SCENE_DETECTION + SUPPORTED_VALUES_SUFFIX);
    }

    /**
     * Gets if the face detection is supported
     *
     * @return on or off if the feature is supported null if this feature
               is not supported
     * @hide
     */
    public List<String> getSupportedFaceDetection() {
        return getSupportedValues(KEY_FACE_DETECTION + SUPPORTED_VALUES_SUFFIX);
    }

    /**
     * Gets if the face recognition is supported
     *
     * @return on or off if the feature is supported null if this feature
               is not supported
     * @hide
     */
    public List<String> getSupportedFaceRecognition() {
        return getSupportedValues(KEY_FACE_RECOGNITION + SUPPORTED_VALUES_SUFFIX);
    }

    /**
     * Gets if the panorama mode is supported
     *
     * @return on or off if the feature is supported null if this feature
               is not supported
     * @hide
     */
    public List<String> getSupportedPanorama() {
        return getSupportedValues(KEY_PANORAMA + SUPPORTED_VALUES_SUFFIX);
    }

    /**
     * Gets the supported panorama live preview sizes.
     * @return a list of Size object.
     * @hide
     */
    public List<Camera.Size> getSupportedPanoramaLivePreviewSizes() {
        String str = mParameters.get(KEY_SUPPORTED_PANORAMA_LIVE_PREVIEW_SIZES);
        ArrayList<String> sizeStrings = split(str);
        List<Camera.Size> sizes = new ArrayList<Camera.Size>();
        if (sizeStrings == null)
            return sizes;

        Iterator<String> it = sizeStrings.iterator();
        while(it.hasNext()) {
            String size = it.next();
            sizes.add(parseSize(size));
        }
        return sizes;
    }

    /**
     * Gets the maximum panorama snapshot count.
     * @return the max count
     * @hide
     */
    public int getMaximumPanoramaSnapshotCount() {
        return getInt(KEY_PANORAMA_MAX_SNAPSHOT_COUNT, 0);
    }

    /**
     * Returns the current panorama live preview size
     * @return size of live preview images
     * @hide
     */
    public Camera.Size getPanoramaLivePreviewSize() {
        String str = mParameters.get(KEY_PANORAMA_LIVE_PREVIEW_SIZE);
        return parseSize(str);
    }

    /**
     * Sets the panorama live preview size. Live preview images are delivered via the
     * PanoramaListener. The size of the live preview size must be among the list of
     * resolutions in {@link #getSupportedPanoramaLivePreviewSizes()}
     * @hide
     */
    public void setPanoramaLivePreviewSize(int width, int height) {
        mParameters.set(KEY_PANORAMA_LIVE_PREVIEW_SIZE, "" + width + "x" + height);
    }

    /**
     * Sets the smile detection threshold for smile shutter.
     *
     * @param value for smile detection in smart shutter (0 = not-strict to 100 = strict)
     * @hide
     */
    public void setSmileShutterThreshold(String value) {
        mParameters.set(KEY_SMILE_SHUTTER_THRESHOLD, value);
    }

    /**
     * Gets if the smile detection smart shutter is supported
     *
     * @return on or off if the feature is supported null if this feature
               is not supported
     * @hide
     */
    public List<String> getSupportedSmileShutter() {
        return getSupportedValues(KEY_SMILE_SHUTTER + SUPPORTED_VALUES_SUFFIX);
    }

    /**
     * Sets the blink detection threshold for blink shutter.
     *
     * @param value for blink detection in smart shutter (0 = strict to 100 = not-strict)
     * @hide
     */
    public void setBlinkShutterThreshold(String value) {
        mParameters.set(KEY_BLINK_SHUTTER_THRESHOLD, value);
    }

    /**
     * Gets if the blink detection smart shutter is supported
     *
     * @return on or off if the feature is supported null if this feature
               is not supported
     * @hide
     */
    public List<String> getSupportedBlinkShutter() {
        return getSupportedValues(KEY_BLINK_SHUTTER + SUPPORTED_VALUES_SUFFIX);
    }


    /**
     * Gets the current slow motion rate value.
     *
     * @hide
     */
    public String getSlowMotionRate() {
        return mParameters.get(KEY_SLOW_MOTION_RATE);
    }

    /**
     * Sets the slow motion rate value.
     *
     * @hide
     */
    public void setSlowMotionRate(String value) {
        mParameters.set(KEY_SLOW_MOTION_RATE, value);
    }

    /**
     * Gets the support list for slow motion value
     *
     * @hide
     */
    public List<String> getSupportedSlowMotionRate() {
        String str = mParameters.get(KEY_SLOW_MOTION_RATE + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    /**
     * Splits a comma delimited string to an ArrayList of String.
     * @Return null if the passing string is null or the size is 0.
     */
    private ArrayList<String> split(String str) {
        if (str == null) return null;

        // Use StringTokenizer because it is faster than split.
        StringTokenizer tokenizer = new StringTokenizer(str, ",");
        ArrayList<String> substrings = new ArrayList<String>();
        while (tokenizer.hasMoreElements()) {
            substrings.add(tokenizer.nextToken());
        }
        return substrings;
    }

    // Get supported values for a key
    private List<String> getSupportedValues(String key) {
        String str = mParameters.get(key);
        if (str == null || str.equals("")) {
            Log.v(TAG, "Return null for key:" + key);
            return null;
        }
        return split(str);
    }

    // Splits a comma delimited string to an ArrayList of Float.
    private void splitFloat(String str, float[] output) {
        if (str == null) return;

        StringTokenizer tokenizer = new StringTokenizer(str, ",");
        int index = 0;
        while (tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();
            output[index++] = Float.parseFloat(token);
        }
    }

    // Returns the value of a integer parameter.
    private int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(mParameters.get(key));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
