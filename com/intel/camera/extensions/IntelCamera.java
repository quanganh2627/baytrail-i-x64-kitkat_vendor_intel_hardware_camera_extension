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
    private static final String KEY_HDR_IMAGING = "hdr-imaging";
    private static final String KEY_HDR_SHARPENING = "hdr-sharpening";
    private static final String KEY_HDR_VIVIDNESS = "hdr-vividness";
    private static final String KEY_HDR_SAVE_ORIGINAL = "hdr-save-original";
    private static final String KEY_SMILE_SHUTTER_THRESHOLD = "smile-shutter-threshold";
    private static final String KEY_BLINK_SHUTTER_THRESHOLD = "blink-shutter-threshold";
    private static final String KEY_SUPPORTED_SMILE_SHUTTER = "smile-shutter";
    private static final String KEY_SUPPORTED_BLINK_SHUTTER = "blink-shutter";

    // for burst capture
    private static final String KEY_BURST_LENGTH = "burst-length";
    private static final String KEY_BURST_FPS = "burst-fps";

    //values for back light correction
    private static final String BACK_LIGHTING_CORRECTION_ON = "on";
    private static final String BACK_LIGHTING_CORRECTION_OFF = "off";

    //values for af metering mode
    private static final String AF_METERING_MODE_AUTO = "auto";
    private static final String AF_METERING_MODE_SPOT = "spot";

    private static final String KEY_FOCUS_DISTANCES = "focus-distances";
    private static final String KEY_ANTIBANDING = "antibanding";

    private static final String KEY_PANORAMA_LIVE_PREVIEW_SIZE = "panorama-live-preview-size";

    // continuous viewfinder
    public static final String KEY_CONTINUOUS_VIEWFINDER = "continuous-viewfinder";
    public static final String KEY_BURST_START_INDEX = "burst-start-index";

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    // Values for ae mode setting.
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

    private Camera mCameraDevice;
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
    private native final void native_cancelTakePicture();

    // here need keep pace with native msgType
    private static final int CAMERA_MSG_SCENE_DETECT = 0x2001;
    private static final int CAMERA_MSG_PANORAMA_METADATA = 0x2002;
    private static final int CAMERA_MSG_PANORAMA_SNAPSHOT = 0x2003;

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

    public static class PanoramaSnapshot
    {
        public PanoramaSnapshot()
        {
        }
        public PanoramaMetadata metadataDuringSnap;
        public byte[] snapshot;
    }

    public static class PanoramaMetadata
    {
        public PanoramaMetadata() {
        }
        public int direction = 0;
        public int horizontalDisplacement = 0;
        public int verticalDisplacement = 0;
        public boolean motionBlur = false;
    }

    /**
     * Sets the panorama listener
     * @param listener the new PanoramaListener
     */
    public void setPanoramaListener(PanoramaListener listener)
    {
        mPanoramaListener = listener;
    }

    public interface PanoramaListener
    {
        /**
         * Notify listener of the viewfinder moving during panorama capturing
         * @param metadata of panorama displacement change
         */
        void onDisplacementChange(PanoramaMetadata metadata);

        /**
         * Notify listener of a snapshot during panorama capturing
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
     * Stops the smart shutter capture.
     */
    public final void cancelTakePicture()
    {
        native_cancelTakePicture();
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
        return split(str);
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
        String str = mParameters.get(KEY_BURST_START_INDEX + SUPPORTED_VALUES_SUFFIX);
        return split(str);
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
        String str = mParameters.get(KEY_HDR_IMAGING + SUPPORTED_VALUES_SUFFIX);
        return split(str);
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

    public Camera.Size getPanoramaLivePreviewSize() {
        String str = mParameters.get(KEY_PANORAMA_LIVE_PREVIEW_SIZE);
        return parseSize(str);
    }

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
        String str = mParameters.get(KEY_SUPPORTED_SMILE_SHUTTER);
        return split(str);
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
        String str = mParameters.get(KEY_SUPPORTED_BLINK_SHUTTER);
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
