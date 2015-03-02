package com.intel.camera2.extensions.vision;

import android.graphics.Bitmap;

import com.intel.camera2.extensions.IaFrame;
import com.intel.camera2.extensions.vision.FaceData.FaceInfo;

public class FaceDetectionJNI extends PVLibraryLoader {
    public native static long create();  // If config value is null, it will initialize with default config.
    public native static void destroy(long instance);
    public native static FaceInfo[] runInImage(long instance, IaFrame frame);

    public native static void setParam(long instance, Param param);
    public native static Param getParam(long instance);
    public native static Config getConfig(long instance);

    public native static byte[] convertToGray(Bitmap bitmap);

    public static class Param {
        /** The maximum number of detectable faces in one frame.
         *  max_supported_num_faces in pvl_face_detection structure represents the maximum allowable value, and minimum allowable value set to 1.
         *  The default value is set to maximum when the component is created.
         */
        public int max_num_faces;
        /** The ratio of minimum detectable face size to the shorter side of the input image.
         *  The maximum allowable value is 1.0 (100%) and there is no limitation on the minimum allowable value.
         *  However, the faces smaller than min_face_size set in pvl_face_detection may not be detected.
         */
        public float min_face_ratio;  // The maximum allowable value is 1.0 (100%)
        /** The degree of RIP (Rotation In-Plane) ranges, representing [-rip_range, +rip_range]. */
        public int rip_range;
        /** The degree of ROP (Rotation Out-of-Plane) ranges, representing [-rop_range, +rop_range]. */
        public int rop_range;
        /** The number of rollover frames indicating how many frames the entire scanning will be distributed.
         *  The value works in the preview mode, only.
         */
        public int num_rollover_frames;

        public Param(int max_num_faces, float min_face_ratio, int rip_range, int rop_range, int num_rollover_frames) {
            this.max_num_faces = max_num_faces;
            this.min_face_ratio = min_face_ratio;
            this.rip_range = rip_range;
            this.rop_range = rop_range;
            this.num_rollover_frames = num_rollover_frames;
        }

        public String toString() {
            return "maxNumFaces("+max_num_faces+") minFaceRatio("+min_face_ratio+")"+
                   "ripRange("+rip_range+") ropRange("+rop_range+") numRolloverFrames("+num_rollover_frames+")";
        }
    }

    public static class Config {
        /** The version information. */
        public final Version version;
        /** The maximum number of faces supported by this component. */
        public final int max_supported_num_faces;
        /** The minimum size in pixel of detectable faces of this component. */
        public final int min_face_size;
        /** The maximum value of RIP range (in degree). */
        public final int rip_range_max;
        /** The resolution of RIP range value. RIP range should be multiple of this value. */
        public final int rip_range_resolution;
        /** The maximum value of ROP range (in degree). */
        public final int rop_range_max;
        /** The resolution of ROP range value. ROP range should be multiple of this value. */
        public final int rop_range_resolution;

        public Config(Version version, int max_supported_num_faces, int min_face_size, int rip_range_max, int rip_range_resolution, int rop_range_max, int rop_range_resolution) {
            this.version = version;
            this.max_supported_num_faces = max_supported_num_faces;
            this.min_face_size = min_face_size;
            this.rip_range_max = rip_range_max;
            this.rip_range_resolution = rip_range_resolution;
            this.rop_range_max = rop_range_max;
            this.rop_range_resolution = rop_range_resolution;
        }

        public String toString() {
            return "Version("+version.toString()+") maxNum("+max_supported_num_faces+") minSize("+min_face_size+")" +
                   "ripMax("+rip_range_max+") ripRes("+rip_range_resolution+")" +
                   "ropMax("+rop_range_max+") ropRes("+rop_range_resolution+")";
        }
    }
}
