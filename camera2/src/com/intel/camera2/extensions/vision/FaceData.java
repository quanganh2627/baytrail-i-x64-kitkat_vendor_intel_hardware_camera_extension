package com.intel.camera2.extensions.vision;

import android.graphics.Point;
import android.graphics.Rect;

/**
 *
 */
public class FaceData {

    public static class FaceInfo {
        public Rect faceRect;
        public int confidence;  // [0, 100]
        public int ripAngle;    // degree
        public int ropAngle;    // degree
        public int trackingId;

        public FaceInfo(Rect rect, int confidence, int ripAngle, int ropAngle, int trackingId) {
            this.faceRect = rect;
            this.confidence = confidence;
            this.ripAngle = ripAngle;
            this.ropAngle = ropAngle;
            this.trackingId = trackingId;
        }

        public String toString() {
            return "rect("+faceRect.toShortString()+") confidence("+confidence+") rip("+ripAngle+") rop("+ropAngle+") trackingId("+trackingId+")";
        }
    }

    public static class EyeInfo {
        public Point leftEye;
        public Point rightEye;
        public int confidence;  // [0, 100]

        public EyeInfo(Point left, Point right, int confidence) {
            this.leftEye = left;
            this.rightEye = right;
            this.confidence = confidence;
        }

        public String toString() {
            return "left("+leftEye.toString()+") right("+rightEye.toString()+") confidence("+confidence+")";
        }
    }

    public static class SmileInfo {
        /** The smile score of the face in the range of 0 to 100, where 0 means non-smile and 100 means full smile. */
        public int score;

        /** The state of the smile of the face. */
        public int state;

        public SmileInfo(int score, int state) {
            this.score = score;
            this.state = state;
        }

        public String toString() {
            return "score("+score+") state("+state+")";
        }
    }

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

        public String toString() {
            return "left_score("+left_score+") left_state("+left_state+") right_score("+right_score+") right_state("+right_state+")";
        }
    }

    public static class RecognitionInfo {
        public int similarity;
        public long faceId;
        public int personId;
        public long timeStamp;
        public int condition;
        public int checksum;
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

        public String toString() {
            return "similarity("+similarity+") faceId("+faceId+") personId("+personId+") timeStamp("+timeStamp+") condition("+condition+") checksum("+checksum+") feature("+feature+" / size: "+feature.length+")";
        }
    }
}
