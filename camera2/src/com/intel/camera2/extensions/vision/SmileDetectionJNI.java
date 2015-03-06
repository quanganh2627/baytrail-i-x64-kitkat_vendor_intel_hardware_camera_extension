package com.intel.camera2.extensions.vision;

import com.intel.camera2.extensions.IaFrame;
import com.intel.camera2.extensions.vision.FaceData.EyeInfo;
import com.intel.camera2.extensions.vision.FaceData.SmileInfo;

final class SmileDetectionJNI extends PVLibraryLoader {
    public native static long create();
    public native static void destroy(long instance);
    public native static SmileInfo[] runInImage(long instance, IaFrame frame, EyeInfo[] eyeInfo);

    public native static void setParam(long instance, Param param);
    public native static Param getParam(long instance);
    public native static Config getConfig(long instance);

    public static class Param {
        /** The threshold value that evaluates the status smile or not if the score greater than or equal to this threshold.
         *  The range of the value may be in [0..100], though, recommended to use the default value set in the structure pvl_smile_detection.
         */
        public int threshold;

        public Param(int threshold) {
            this.threshold = threshold;
        }

        public String toString() {
            return "threshold("+threshold+")";
        }
    }

    public static class Config {
        /** The version information. */
        public final Version version;
        /** The default threshold value recommended. */
        public final int default_threshold;
        /** The maximum range of ROP (Rotation Out of Plane) tolerance of the face.
         *  The accuracy may not be guaranteed if the ROP angle is out of range.
         */
        public final int rop_tolerance;

        public Config(Version version, int default_threshold, int rop_tolerance) {
            this.version = version;
            this.default_threshold = default_threshold;
            this.rop_tolerance = rop_tolerance;
        }

        public String toString() {
            return "version("+version.toString()+") default_threshold("+default_threshold+") rop_tolerance("+rop_tolerance+")";
        }
    }
}
