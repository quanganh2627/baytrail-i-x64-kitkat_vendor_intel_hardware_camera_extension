package com.intel.camera2.extensions.vision;

import com.intel.camera2.extensions.IaFrame;
import com.intel.camera2.extensions.vision.FaceData.BlinkInfo;
import com.intel.camera2.extensions.vision.FaceData.EyeInfo;

final class BlinkDetectionJNI extends PVLibraryLoader {
    public native static long create();
    public native static void destroy(long instance);
    public native static BlinkInfo[] runInImage(long instance, IaFrame frame, EyeInfo[] eyeInfo);

    public native static void setParam(long instance, Param param);
    public native static Param getParam(long instance);
    public native static Config getConfig(long instance);

    public static class Param {
        /** A threshold value which is the determinant of the blink. If the given blink score on the eye is
         *  greater than or equal to this value, the state of the eye will be estimated as 'closed'.
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
