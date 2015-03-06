package com.intel.camera2.extensions.vision;

import android.graphics.Point;
import android.graphics.Rect;

/**
 * The result info of detection or recognition.
 */
public class FaceData {

    /**
     * 
     */
    public static class FaceInfo {
        /** The rectangular region of the detected face. */
        public Rect faceRect;
        /** The confidence value of the detected face [0, 100]. */
        public int confidence;
        /** The approximated value of RIP angle (in degree) of the detected face. */
        public int ripAngle;
        /** The approximated value of ROP angle (in degree) of the detected face. */
        public int ropAngle;
        /** The tracking id of the face. Only valid in preview mode.
         *  The value will be unique throughout the component life cycle. */
        public int trackingId;

        public FaceInfo(Rect rect, int confidence, int ripAngle, int ropAngle, int trackingId) {
            this.faceRect = rect;
            this.confidence = confidence;
            this.ripAngle = ripAngle;
            this.ropAngle = ropAngle;
            this.trackingId = trackingId;
        }

        @Override
        public String toString() {
            return "rect("+faceRect.toShortString()+") confidence("+confidence+") rip("+ripAngle+") rop("+ropAngle+") trackingId("+trackingId+")";
        }
    }

    /**
     * 
     */
    public static class EyeInfo {
        /** The center position on the left eye */
        public Point leftEye;
        /** The center position on the right eye */
        public Point rightEye;
        /**< The confidence value of the detected eyes. The value is negative if the eye detection failed. */
        public int confidence;

        public EyeInfo(Point left, Point right, int confidence) {
            this.leftEye = left;
            this.rightEye = right;
            this.confidence = confidence;
        }

        @Override
        public String toString() {
            return "left("+leftEye.toString()+") right("+rightEye.toString()+") confidence("+confidence+")";
        }
    }

    /**
     *
     */
    public static class SmileInfo {
        /** The smile score of the face in the range of 0 to 100, where 0 means non-smile and 100 means full smile. */
        public int score;

        /** The state of the smile of the face. */
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
     *
     */
    public static class BlinkInfo {
        /** The blink score on the left eye in range between 0 and 100, where 0 means wide opened eye and 100 means fully closed eye. */
        public int left_score;

        /** The blink state on the left eye. */
        public int left_state;

        /** The blink score on the right eye in range between 0 and 100, where 0 means wide opened eye and 100 means fully closed eye. */
        public int right_score;

        /** The blink state on the right eye. */
        public int right_state;

        public BlinkInfo(int left_score, int left_state, int right_score, int right_state) {
            this.left_score = left_score;
            this.left_state = left_state;
            this.right_score = right_score;
            this.right_state = right_state;
        }

        @Override
        public String toString() {
            return "left_score("+left_score+") left_state("+left_state+") right_score("+right_score+") right_state("+right_state+")";
        }
    }

    /**
     *
     */
    public static class RecognitionInfo {
        public static final int UNKOWN_PERSON_ID = -1;

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
        public int checksum;
        /** The pointer to the actual face data. Face data is essentially a binary encoded representation of the face generated from the gray face image. */
        public byte[] feature;

        public RecognitionInfo(int similarity, long faceId, int personId, long timeStamp, int condition, int checksum, byte[] feature) {
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
            return "similarity("+similarity+") faceId("+faceId+") personId("+personId+") timeStamp("+timeStamp+") condition("+condition+") checksum("+checksum+") feature("+feature+" / size: "+feature.length+")";
        }
    }
}
