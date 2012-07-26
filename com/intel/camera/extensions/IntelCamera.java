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

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.util.Log;

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

    private static final String TAG = "com.intel.cameraext.Camera";

    private native final boolean enableIntelCamera(Camera cameraDevice);

    static {
        System.loadLibrary("intelcamera_jni");
    }

    public IntelCamera(int cameraId) {
        mCameraDevice = android.hardware.Camera.open(cameraId);
        enableIntelCamera(mCameraDevice);
    }

    public IntelCamera() {
        mCameraDevice = android.hardware.Camera.open();
        enableIntelCamera(mCameraDevice);
    }

    public Camera getCameraDevice() {
        return mCameraDevice;
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
}
