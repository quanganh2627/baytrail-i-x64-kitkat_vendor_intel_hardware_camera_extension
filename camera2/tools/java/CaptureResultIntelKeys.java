/*
 * Copyright (C) 2014 Intel Corporation
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

package com.intel.camera2.extensions;

import android.hardware.camera2.CaptureResult.Key;

/**
 * <p>The subset of the results of a single image capture from the image sensor.</p>
 *
 * <p>Contains a subset of the final configuration for the capture hardware (sensor, lens,
 * flash), the processing pipeline, the control algorithms, and the output
 * buffers.</p>
 *
 * <p>CaptureResults are produced by a {@link CameraDevice} after processing a
 * {@link CaptureRequest}. All properties listed for capture requests can also
 * be queried on the capture result, to determine the final values used for
 * capture. The result also includes additional metadata about the state of the
 * camera device during the capture.</p>
 *
 * <p>Not all properties returned by {@link IntelCameraCharacteristics#getAvailableCaptureResultKeys()}
 * are necessarily available. Some results are {@link IntelCaptureResult partial} and will
 * not have every key set. Only {@link TotalCaptureResult total} results are guaranteed to have
 * every key available that was enabled by the request.</p>
 *
 * <p>{@link IntelCaptureResult} objects are immutable.</p>
 *
 */
public class CaptureResultIntelKeys extends CameraMetadataIntel
{

    /*@O~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * The key entries below this point are generated from metadata
     * definitions in /system/media/camera/docs. Do not modify by hand or
     * modify the comment blocks at the start or end.
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~*/


    /**
     * <p>Whether the extended AIQ analysis results are provided for each capture request.</p>
     * <p>This is the list of extra tags that will be present in the result:</p>
     * <ul>
     * <li>multiframeHint</li>
     * <li>sceneDected</li>
     * <li>hdr.preferedExposures</li>
     * </ul>
     * @see #INTEL_STATISTICS_ANALYSIS_MODE_OFF
     * @see #INTEL_STATISTICS_ANALYSIS_MODE_ON
     */
    public static final Key<Integer> INTEL_STATISTICS_ANALYSIS_MODE =
            new Key<Integer>("com.intel.statistics.analysisMode", int.class);

    /**
     * <p>This reports the recommendation from the Intel 3A to use one of the multi-frame postprocessing algotithms.</p>
     * <p>To be added...</p>
     * @see #INTEL_STATISTICS_MULTI_FRAME_HINT_NONE
     * @see #INTEL_STATISTICS_MULTI_FRAME_HINT_ULL
     * @see #INTEL_STATISTICS_MULTI_FRAME_HINT_HDR
     */
    public static final Key<Integer> INTEL_STATISTICS_MULTI_FRAME_HINT =
            new Key<Integer>("com.intel.statistics.multiFrameHint", int.class);

    /**
     * <p>This reports the recommended scene mode the user should select for optimal quality.</p>
     * <p>This is the ouput of the Intel Discrete Scene Detection algorithm.</p>
     * @see #INTEL_STATISTICS_SCENE_DETECTED_NONE
     * @see #INTEL_STATISTICS_SCENE_DETECTED_CLOSE_UP_PORTRAIT
     * @see #INTEL_STATISTICS_SCENE_DETECTED_PORTRAIT
     * @see #INTEL_STATISTICS_SCENE_DETECTED_LOWLIGHT_PORTRAIT
     * @see #INTEL_STATISTICS_SCENE_DETECTED_LOWLIGHT
     * @see #INTEL_STATISTICS_SCENE_DETECTED_ACTION
     * @see #INTEL_STATISTICS_SCENE_DETECTED_BACKLIGHT
     * @see #INTEL_STATISTICS_SCENE_DETECTED_LANDSCAPE
     * @see #INTEL_STATISTICS_SCENE_DETECTED_DOCUMENT
     * @see #INTEL_STATISTICS_SCENE_DETECTED_FIREWORK
     * @see #INTEL_STATISTICS_SCENE_DETECTED_LOWLIGHT_ACTION
     * @see #INTEL_STATISTICS_SCENE_DETECTED_BABY
     * @see #INTEL_STATISTICS_SCENE_DETECTED_BARCODE
     */
    public static final Key<Integer> INTEL_STATISTICS_SCENE_DETECTED =
            new Key<Integer>("com.intel.statistics.sceneDetected", int.class);

    /**
     * <p>Control of Smile detection.</p>
     * <p>When ON, com.intel.cv.smileDetectStatus (dynamic) and com.intel.cv.smileDetectScores (dynamic) outputs are valid.</p>
     * @see #INTEL_CV_SMILE_DETECT_MODE_OFF
     * @see #INTEL_CV_SMILE_DETECT_MODE_ON
     */
    public static final Key<Integer> INTEL_CV_SMILE_DETECT_MODE =
            new Key<Integer>("com.intel.cv.smileDetectMode", int.class);

    /**
     * <p>This metadata tag will provide a list of smile status of faces. The order of this list is always aligned with the IDs produced by the android-defined metadata tagin.
     * android.statistics.faceIds.</p>
     * <p>True means smiling face.
     * False means non-smiling face</p>
     */
    public static final Key<byte[]> INTEL_CV_SMILE_DETECT_STATUS =
            new Key<byte[]>("com.intel.cv.smileDetectStatus", byte[].class);

    /**
     * <p>This metadata tag will provide an list of smile confidence scores. The scores are confidence level that the face is smiling.</p>
     * <p>A person's smile intensity is able to be detected. Along with detecting features of a face, the changes and movements of the face are also analyzed in order to index smile intensity.</p>
     */
    public static final Key<int[]> INTEL_CV_SMILE_DETECT_SCORES =
            new Key<int[]>("com.intel.cv.smileDetectScores", int[].class);

    /**
     * <p>Control of Blink detection.</p>
     * <p>When ON, com.intel.cv.blinkDetectStatus (dynamic) and com.intel.cv.blinkDetectScores (dynamic) outputs are valid.</p>
     * @see #INTEL_CV_BLINK_DETECT_MODE_OFF
     * @see #INTEL_CV_BLINK_DETECT_MODE_ON
     */
    public static final Key<Integer> INTEL_CV_BLINK_DETECT_MODE =
            new Key<Integer>("com.intel.cv.blinkDetectMode", int.class);

    /**
     * <p>This metadata tag will provide a list of blink status of faces. The order of this list is always aligned with the IDs produced by the android-defined metadata tagin.
     * android.statistics.faceIds.</p>
     * <p>True means closed eye.
     * False means open eye.
     * The meaning of array should be like:
     * "0th face lefe eye|0th face right eye|1th face left eye|1th face right eye|2th face left eye|2th face  right eye"</p>
     */
    public static final Key<byte[]> INTEL_CV_BLINK_DETECT_STATUS =
            new Key<byte[]>("com.intel.cv.blinkDetectStatus", byte[].class);

    /**
     * <p>This metadata tag will provide an list of blink confidence scores. The scores are confidence level that the face is blinking.</p>
     * <p>A person's blink intensity is able to be detected. Along with detecting features of eyes, the changes and movements of eyes are also analyzed in order to index blink intensity.</p>
     */
    public static final Key<int[]> INTEL_CV_BLINK_DETECT_SCORES =
            new Key<int[]>("com.intel.cv.blinkDetectScores", int[].class);

    /**
     * <p>Control of Face recognition.</p>
     * <p>When ON, com.intel.cv.faceRecognizePersonIds (dynamic) and com.intel.cv.faceRecognizePersonSimilarities (dynamic) outputs are valid.</p>
     * @see #INTEL_CV_FACE_RECOGNIZE_MODE_OFF
     * @see #INTEL_CV_FACE_RECOGNIZE_MODE_ON
     */
    public static final Key<Integer> INTEL_CV_FACE_RECOGNIZE_MODE =
            new Key<Integer>("com.intel.cv.faceRecognizeMode", int.class);

    /**
     * <p>This metadata tag will provide a list of person Ids from face recognition. Each person is identified as unique positive integer. This is different from android.statistics.faceIds, which is unique among the tracking session</p>
     * <p>-10000 means unkown face.
     * Positive integer means each person id. 0 is illegal.</p>
     */
    public static final Key<int[]> INTEL_CV_FACE_RECOGNIZE_PERSON_IDS =
            new Key<int[]>("com.intel.cv.faceRecognizePersonIds", int[].class);

    /**
     * <p>This metadata tag will provide a list of person similarities from face recognition. It is the biggest one of the estimated levels between the input face and the faces in databases. It is good to be tread as a kind of score.</p>
     * <p>0 is not likely the person
     * 100 is quite sure of the person</p>
     */
    public static final Key<int[]> INTEL_CV_FACE_RECOGNIZE_PERSON_SIMILARITIES =
            new Key<int[]>("com.intel.cv.faceRecognizePersonSimilarities", int[].class);

    /**
     * <p>This metadata tag will provide an list of RIP (Rotation In-Plane) angles from face detection. Represented in degree. Also known as 'rolling'.</p>
     * <p>The angle is represented as degree. The upright face is 0 degree, clock wise is positive, CC wise is negatives.</p>
     */
    public static final Key<int[]> INTEL_CV_FACE_DETECT_RIP_ANGLES =
            new Key<int[]>("com.intel.cv.faceDetectRipAngles", int[].class);

    /**
     * <p>This metadata tag will provide an list of ROP (Rotation out-of-plane) angles from face detection. Represented in degree. Also known as 'yawing'. Piching is not supported</p>
     * <p>The frontal face is 0, left profile face is -1, right profile face is 1.</p>
     */
    public static final Key<int[]> INTEL_CV_FACE_DETECT_ROP_ANGLES =
            new Key<int[]>("com.intel.cv.faceDetectRopAngles", int[].class);

    /**
     * <p>Control of Dual camera.</p>
     * <p>When ON, the camera HAL can support dual camera mode.  </p>
     * @see #INTEL_DEVICE_DUAL_CAMERA_MODE_OFF
     * @see #INTEL_DEVICE_DUAL_CAMERA_MODE_ON
     */
    public static final Key<Integer> INTEL_DEVICE_DUAL_CAMERA_MODE =
            new Key<Integer>("com.intel.device.dualCameraMode", int.class);

    /**
     * <p>This contains the color effect modes supported by Intel 3A that can be applied to images.</p>
     * <p>Intel color effect can be used like as an extension to the standard control(android.control.effectMode).
     * But due to range checks in android standard API, those keys can't be sent by android.control.effectMode,
     * it's right way to send by separate control tag.
     * If a request with both tags arrives at the HAL, the Android standard metadata should take precedence
     * over the Intel's.</p>
     * @see #INTEL_IMAGE_ENHANCE_COLOR_EFFECT_OFF
     * @see #INTEL_IMAGE_ENHANCE_COLOR_EFFECT_SKY_BLUE
     * @see #INTEL_IMAGE_ENHANCE_COLOR_EFFECT_GRASS_GREEN
     * @see #INTEL_IMAGE_ENHANCE_COLOR_EFFECT_SKIN_WHITEN
     * @see #INTEL_IMAGE_ENHANCE_COLOR_EFFECT_SKIN_WHITEN_LOW
     * @see #INTEL_IMAGE_ENHANCE_COLOR_EFFECT_SKIN_WHITEN_HIGH
     * @see #INTEL_IMAGE_ENHANCE_COLOR_EFFECT_VIVID
     */
    public static final Key<Integer> INTEL_IMAGE_ENHANCE_COLOR_EFFECT =
            new Key<Integer>("com.intel.imageEnhance.colorEffect", int.class);

    /**
     * <p>value of brightness, its range is [-10, 10]; 10 is maximum brightness, 0 is neutral. </p>
     */
    public static final Key<Integer> INTEL_IMAGE_ENHANCE_BRIGHTNESS =
            new Key<Integer>("com.intel.imageEnhance.brightness", int.class);

    /**
     * <p>value of contrast, its range is [-10, 10]; 10 is maximum contrast, 0 is neutral. </p>
     */
    public static final Key<Integer> INTEL_IMAGE_ENHANCE_CONTRAST =
            new Key<Integer>("com.intel.imageEnhance.contrast", int.class);

    /**
     * <p>value of saturation, its range is [-10, 10]; 10 is maximum saturation, 0 is neutral. </p>
     */
    public static final Key<Integer> INTEL_IMAGE_ENHANCE_SATURATION =
            new Key<Integer>("com.intel.imageEnhance.saturation", int.class);

    /**
     * <p>value of hue, its range is [-10, 10]; 10 is maximum hue, 0 is neutral. </p>
     */
    public static final Key<Integer> INTEL_IMAGE_ENHANCE_HUE =
            new Key<Integer>("com.intel.imageEnhance.hue", int.class);

    /**
     * <p>value of sharpness, its range is [-10, 10]; 10 is maximum sharpness, 0 is neutral. </p>
     */
    public static final Key<Integer> INTEL_IMAGE_ENHANCE_SHARPNESS =
            new Key<Integer>("com.intel.imageEnhance.sharpness", int.class);

    /*~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * End generated code
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~O@*/

}
