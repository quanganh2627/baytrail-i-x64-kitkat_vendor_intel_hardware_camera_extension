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

import com.intel.camera2.extensions.IaFrame;
import com.intel.camera2.extensions.vision.FaceData.EyeInfo;
import com.intel.camera2.extensions.vision.FaceData.FaceInfo;

final class EyeDetectionJNI extends PVLibraryLoader {
    public native static long create();
    public native static void destroy(long instance);
    public native static EyeInfo[] runInImage(long instance, IaFrame frame, FaceInfo[] fdInfo);

    public native static Config getConfig(long instance);

    public static class Config {
        /** The version information. */
        public final Version version;
        /** The maximum supported ratio of the width of the face to the eye distance,
         *  s.t. max_face_with_ratio = (the width of the face)/(the distance between two eyes).
         *  The eye detection accuracy will be dropped if the estimated face region (as the parameter of pvl_eye_detection_run) is too large compared to the actual face size.
         */
        public final float max_face_width_ratio;
        /** The maximum supported value (in degree) of the difference between the actual RIP degree and the input RIP degree.
         *  The eye detection accuracy will be dropped if the actual RIP degree is greater than (input RIP angle)(max_rip_error_tolerance).
         */
        public final float max_rip_error_tolerance;

        public Config(Version version, float max_face_width_ratio, float max_rip_error_tolerance) {
            this.version = version;
            this.max_face_width_ratio = max_face_width_ratio;
            this.max_rip_error_tolerance = max_rip_error_tolerance;
        }

        public String toString() {
            return "version("+version.toString()+") maxFaceWidthRatio("+max_face_width_ratio+") maxRipErrorTolerance("+max_rip_error_tolerance+")";
        }
    }
}
