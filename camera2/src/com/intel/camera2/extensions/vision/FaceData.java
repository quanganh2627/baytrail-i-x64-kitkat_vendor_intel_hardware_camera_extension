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
package com.intel.camera2.extensions.vision;

import android.graphics.Point;
import android.graphics.Rect;

/**
 * The result info class of analyzing.
 */
public class FaceData {

    /**
     * The result info class of face detection.
     */
    public static class FaceInfo {
        /** The rectangular region of the detected face. */
        public Rect bound;
        /** The confidence value of the detected face [0, 100]. */
        public int confidence;
        /** The approximated value of RIP angle (in degree) of the detected face. */
        public int ripAngle;
        /** The approximated value of ROP angle (in degree) of the detected face. */
        private int ropAngle;
        /** The tracking id of the face. Only valid in preview mode.
         *  The value will be unique throughout the component life cycle. */
        private int trackingId;

        public FaceInfo(Rect rect, int confidence, int ripAngle, int ropAngle, int trackingId) {
            this.bound = rect;
            this.confidence = confidence;
            this.ripAngle = ripAngle;
            this.ropAngle = ropAngle;
            this.trackingId = trackingId;
        }

        @Override
        public String toString() {
            return "rect("+bound.toShortString()+") confidence("+confidence+") rip("+ripAngle+") rop("+ropAngle+") trackingId("+trackingId+")";
        }
    }

    /**
     * The result info class of eye detection.
     */
    public static class EyeInfo {
        /** The center position on the left eye */
        public Point leftEyePosition;
        /** The center position on the right eye */
        public Point rightEyePosition;
        /**< The confidence value of the detected eyes. The value is negative if the eye detection failed. */
        public int confidence;

        public EyeInfo(Point left, Point right, int confidence) {
            this.leftEyePosition = left;
            this.rightEyePosition = right;
            this.confidence = confidence;
        }

        @Override
        public String toString() {
            return "left("+leftEyePosition.toString()+") right("+rightEyePosition.toString()+") confidence("+confidence+")";
        }
    }

    /**
     * The result info class of smile detection.
     */
    public static class SmileInfo {
        public static final int STATE_NO_SMILE = 0;
        public static final int STATE_SMILE = 1;

        /** The smile score of the face in the range of 0 to 100, where 0 means non-smile and 100 means full smile. */
        public int score;

        /**
         * The state of the smile of the face.<br>
         * - 0 ({@link #STATE_NO_SMILE}): Person is not smiling.<br>
         * - 1 ({@link #STATE_SMILE}): Person is smiling.
         */
        public int state;

        public SmileInfo(int score, int state) {
            this.score = score;
            this.state = state;
        }

        @Override
        public String toString() {
            return "score("+score+") state("+state+")";
        }
    }

    /**
     * The result info class of blink detection.
     */
    public static class BlinkInfo {
        public static final int STATE_OPEN = 0;
        public static final int STATE_CLOSE = 1;

        /** The blink score on the left eye in range between 0 and 100, where 0 means wide opened eye and 100 means fully closed eye. */
        public int leftEyeScore;

        /** The blink state on the left eye. */
        public int leftEyeState;

        /** The blink score on the right eye in range between 0 and 100, where 0 means wide opened eye and 100 means fully closed eye. */
        public int rightEyeScore;

        /** The blink state on the right eye. */
        public int rightEyeState;

        public BlinkInfo(int left_score, int left_state, int right_score, int right_state) {
            this.leftEyeScore = left_score;
            this.leftEyeState = left_state;
            this.rightEyeScore = right_score;
            this.rightEyeState = right_state;
        }

        @Override
        public String toString() {
            return "left_score("+leftEyeScore+") left_state("+leftEyeState+") right_score("+rightEyeScore+") right_state("+rightEyeState+")";
        }
    }

    /**
     * The result info class of face recognition.
     */
    public static class RecognitionInfo {
        /** It's default person id which the recognition library couldn't search the person in database. */
        public static final int UNKOWN_PERSON_ID = -10000;

        /** The estimated similarity between the input face and the faces in the database. The biggest value will be assigned. */
        public int similarity;
        /** The unique id of the face data. */
        public long faceId;
        /** The unique id of the person associated with the face data. The valid person will have positive person_id (i.e. person_id > 0). */
        public int personId;
        /** The timestamp when the face data was generated. */
        public long timeStamp;
        /** The environmental information of the face. Reserved for future use. */
        public int condition;
        /** The checksum value of the face data. */
        private int checksum;
        /** The pointer to the actual face data. Face data is essentially a binary encoded representation of the face generated from the gray face image. */
        private byte[] feature;

        public RecognitionInfo(byte[] feature, int similarity, long faceId, int personId, long timeStamp, int condition, int checksum) {
            this.similarity = similarity;
            this.faceId = faceId;
            this.personId = personId;
            this.timeStamp = timeStamp;
            this.condition = condition;
            this.checksum = checksum;
            this.feature = feature;
        }

        @Override
        public String toString() {
            return "similarity("+similarity+") faceId("+faceId+") personId("+personId+") timeStamp("+timeStamp+")" +
                   " condition("+condition+") checksum("+checksum+") feature("+feature+" / size: "+(feature!=null?feature.length:0)+")";
        }
    }
}
