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

package com.intel.camera2.extensions.intelcamera;

import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.CaptureResult.Key;
import android.hardware.camera2.impl.CaptureResultExtras;
import android.hardware.camera2.impl.PublicKey;
import android.hardware.camera2.impl.SyntheticKey;
import android.hardware.camera2.utils.TypeReference;
import android.util.Log;
import android.util.Rational;

import java.util.List;

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
public class IntelCaptureResult extends CameraMetadata<CaptureResult.Key<?>> {

    /**
     * {@inheritDoc}
     * @hide
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <T> T getProtected(Key<?> key) {
        return null;
    }



    /**
     * {@inheritDoc}
     * @hide
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Key<?>> getKeyClass() {
        Object thisClass = Key.class;
        return (Class<Key<?>>)thisClass;
    }

    /*@O~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * The key entries below this point are generated from metadata
     * definitions in /system/media/camera/docs. Do not modify by hand or
     * modify the comment blocks at the start or end.
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~*/


    /**
     * <p>This reports the recommendation from the Intel 3A to use one of the multi-frame postprocessing algotithms.</p>
     * <p>To be added...</p>
     * @see #INTEL_AIQ_MULTI_FRAME_HINT_NONE
     * @see #INTEL_AIQ_MULTI_FRAME_HINT_ULL
     * @see #INTEL_AIQ_MULTI_FRAME_HINT_HDR
     */
    @PublicKey
    public static final Key<Integer> INTEL_AIQ_MULTI_FRAME_HINT =
            new Key<Integer>("com.intel.aiq.multiFrameHint", int.class);

    /**
     * <p>This reports the recommended scene mode the user should select for optimal quality.</p>
     * <p>This is the ouput of the Intel Discrete Scene Detection algorithm.</p>
     * @see #INTEL_AIQ_SCENE_DETECTED_NONE
     * @see #INTEL_AIQ_SCENE_DETECTED_CLOSE_UP_PORTRAIT
     * @see #INTEL_AIQ_SCENE_DETECTED_PORTRAIT
     * @see #INTEL_AIQ_SCENE_DETECTED_LOWLIGHT_PORTRAIT
     * @see #INTEL_AIQ_SCENE_DETECTED_LOWLIGHT
     * @see #INTEL_AIQ_SCENE_DETECTED_ACTION
     * @see #INTEL_AIQ_SCENE_DETECTED_BACKLIGHT
     * @see #INTEL_AIQ_SCENE_DETECTED_LANDSCAPE
     * @see #INTEL_AIQ_SCENE_DETECTED_DOCUMENT
     * @see #INTEL_AIQ_SCENE_DETECTED_FIREWORK
     * @see #INTEL_AIQ_SCENE_DETECTED_LOWLIGHT_ACTION
     * @see #INTEL_AIQ_SCENE_DETECTED_BABY
     * @see #INTEL_AIQ_SCENE_DETECTED_BARCODE
     */
    @PublicKey
    public static final Key<Integer> INTEL_AIQ_SCENE_DETECTED =
            new Key<Integer>("com.intel.aiq.sceneDetected", int.class);

    /**
     * <p>Control of Smile detection.</p>
     * <p>When ON, com.intel.faceEngine.smileDetectStatus (dynamic) and com.intel.faceEngine.smileDetectScores (dynamic) outputs are valid.</p>
     * @see #INTEL_FACE_ENGINE_SMILE_DETECT_MODE_OFF
     * @see #INTEL_FACE_ENGINE_SMILE_DETECT_MODE_ON
     */
    @PublicKey
    public static final Key<Integer> INTEL_FACE_ENGINE_SMILE_DETECT_MODE =
            new Key<Integer>("com.intel.faceEngine.smileDetectMode", int.class);

    /**
     * <p>This metadata tag will provide a list of smile status of faces. The order of this list is always aligned with the IDs produced by the android-defined metadata tagin.
     * android.statistics.faceIds.</p>
     * <p>True means smiling face.
     * False means non-smiling face</p>
     */
    @PublicKey
    public static final Key<byte[]> INTEL_FACE_ENGINE_SMILE_DETECT_STATUS =
            new Key<byte[]>("com.intel.faceEngine.smileDetectStatus", byte[].class);

    /**
     * <p>This metadata tag will provide an list of smile confidence scores. The scores are confidence level that the face is smiling.</p>
     * <p>0 means it’s no smile at all.
     * 100 means fully smiling face.</p>
     */
    @PublicKey
    public static final Key<int[]> INTEL_FACE_ENGINE_SMILE_DETECT_SCORES =
            new Key<int[]>("com.intel.faceEngine.smileDetectScores", int[].class);

    /**
     * <p>Control of Blink detection.</p>
     * <p>When ON, com.intel.faceEngine.blinkDetectStatus (dynamic) and com.intel.faceEngine.blinkDetectScores (dynamic) outputs are valid.</p>
     * @see #INTEL_FACE_ENGINE_BLINK_DETECT_MODE_OFF
     * @see #INTEL_FACE_ENGINE_BLINK_DETECT_MODE_ON
     */
    @PublicKey
    public static final Key<Integer> INTEL_FACE_ENGINE_BLINK_DETECT_MODE =
            new Key<Integer>("com.intel.faceEngine.blinkDetectMode", int.class);

    /**
     * <p>This metadata tag will provide a list of blink status of faces. The order of this list is always aligned with the IDs produced by the android-defined metadata tagin.
     * android.statistics.faceIds.</p>
     * <p>True means closed eye.
     * False means open eye.
     * The meaning of array should be like:
     * "0th face lefe eye|0th face right eye|1th face left eye|1th face right eye|2th face left eye|2th face  right eye"</p>
     */
    @PublicKey
    public static final Key<byte[]> INTEL_FACE_ENGINE_BLINK_DETECT_STATUS =
            new Key<byte[]>("com.intel.faceEngine.blinkDetectStatus", byte[].class);

    /**
     * <p>This metadata tag will provide an list of blink confidence scores. The scores are confidence level that the face is blinking.</p>
     * <p>0 means closed eye.
     * 100 means wide-open eye.</p>
     */
    @PublicKey
    public static final Key<int[]> INTEL_FACE_ENGINE_BLINK_DETECT_SCORES =
            new Key<int[]>("com.intel.faceEngine.blinkDetectScores", int[].class);

    /**
     * <p>Control of Face recognition.</p>
     * <p>When ON, com.intel.faceEngine.faceRecognizePersonIds (dynamic) and com.intel.faceEngine.faceRecognizePersonSimilarities (dynamic) outputs are valid.</p>
     * @see #INTEL_FACE_ENGINE_FACE_RECOGNIZE_MODE_OFF
     * @see #INTEL_FACE_ENGINE_FACE_RECOGNIZE_MODE_ON
     */
    @PublicKey
    public static final Key<Integer> INTEL_FACE_ENGINE_FACE_RECOGNIZE_MODE =
            new Key<Integer>("com.intel.faceEngine.faceRecognizeMode", int.class);

    /**
     * <p>This metadata tag will provide a list of person Ids from face recognition. Each person is identified as unique positive integer. This is different from android.statistics.faceIds, which is unique among the tracking session</p>
     * <p>-10000 means unkown face.
     * Positive integer means each person id. 0 is illegal.</p>
     */
    @PublicKey
    public static final Key<int[]> INTEL_FACE_ENGINE_FACE_RECOGNIZE_PERSON_IDS =
            new Key<int[]>("com.intel.faceEngine.faceRecognizePersonIds", int[].class);

    /**
     * <p>This metadata tag will provide a list of person similarities from face recognition. It is the biggest one of the estimated levels between the input face and the faces in databases. It is good to be tread as a kind of score.</p>
     * <p>0 is not likely the person
     * 100 is quite sure of the person</p>
     */
    @PublicKey
    public static final Key<int[]> INTEL_FACE_ENGINE_FACE_RECOGNIZE_PERSON_SIMILARITIES =
            new Key<int[]>("com.intel.faceEngine.faceRecognizePersonSimilarities", int[].class);

    /**
     * <p>This metadata tag will provide an list of RIP (Rotation In-Plane) angles from face detection. Represented in degree. Also known as 'rolling'.</p>
     * <p>The angle is represented as degree. The upright face is 0 degree, clock wise is positive, CC wise is negatives.</p>
     */
    @PublicKey
    public static final Key<int[]> INTEL_FACE_ENGINE_FACE_DETECT_RIP_ANGLES =
            new Key<int[]>("com.intel.faceEngine.faceDetectRipAngles", int[].class);

    /**
     * <p>This metadata tag will provide an list of ROP (Rotation out-of-plane) angles from face detection. Represented in degree. Also known as 'yawing'. Piching is not supported</p>
     * <p>The frontal face is 0, left profile face is -1, right profile face is 1.</p>
     */
    @PublicKey
    public static final Key<int[]> INTEL_FACE_ENGINE_FACE_DETECT_ROP_ANGLES =
            new Key<int[]>("com.intel.faceEngine.faceDetectRopAngles", int[].class);

    /*~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~
     * End generated code
     *~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~@~O@*/

}
