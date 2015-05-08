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
package com.intel.camera2.extensions.photography;

import com.intel.camera2.extensions.IaFrame;

final class PanoramaJNI {
    private static boolean isSupported;

    static {
        try {
            System.loadLibrary("pvl_panorama_jni");
            isSupported = true;
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
            isSupported = false;
        }
    }

    public static boolean isSupported() {
        return isSupported;
    }

    public static final int DIRECTION_STILL = 0;
    public static final int DIRECTION_RIGHT = 1;
    public static final int DIRECTION_LEFT = 2;
    public static final int DIRECTION_DOWN = 3;
    public static final int DIRECTION_UP = 4;
    public static final int DIRECTION_PREVIEW_AUTO = 5;

    public native static long create();
    public native static void destroy(long instance);
    public native static void reset(long instance);
    public native static void setParam(long instance, int direction);
    public native static void stitch(long instance, IaFrame image, int index);  // index (0 ~ ...). for debugging.
//    public native static int compareWithStitched(long instance, IaFrame image, Param result);    // for preview.
    public native static IaFrame run(long instance);
    public native static void setDebug(long instance, int debug);  // debug == 1, dump image when panorama stitch & run.

    public native static void setParam(long instance, Param param);
    public native static Param getParam(long instance);
    public native static Config getConfig(long instance);

    public static class Param {
        /** The expected overlapping ratio between adjacent input images (in percent).
         *  Smaller value results in larger FoV and also larger output image and vice versa.
         *  In addition, however, larger value tends to result in more accurate alignments.
         *  The min/max and default values are defined in pvl_panorama structure in runtime.
         */
        public int overlapping_ratio;

        /** The assumed panning direction of input images. If pvl_panorama_direction_preview_auto is set
         *  the direction would be automatically estimated while processing preview.(See pvl_parnorama_detect_frame_to_stitch()
         *  Otherwise, the direction is assumed to be explicitly specified.
         */
        public int direction;

        public Param(int overlapping_ratio, int direction) {
            this.overlapping_ratio = overlapping_ratio;
            this.direction = direction;
        }

        @Override
        public String toString() {
            return "overlapping_ratio("+overlapping_ratio+") direction("+direction+")";
        }
    }

    public static class Version {
        public int major;
        public int minor;
        public int patch;
        public String description;

        public Version(int major, int minor, int patch, String description) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.description = description;
        }

        @Override
        public String toString() {
            return "major("+major+") minor("+minor+") patch("+patch+") desc("+description+")";
        }
    }

    public static class Config {
        /** The version information. */
        final Version version;
        /**< The maximum number of input images supported by this component. */
        final int max_supported_num_images;
        /**< The minimum configurable value of overlapping ratio (in percent) */
        final int min_overlapping_ratio;
        /**< The maximum configurable value of overlapping ratio. (in percent) */
        final int max_overlapping_ratio;
        /**< The default value of overlapping ratio. (in percent) */
        final int default_overlapping_ratio;

        public Config(Version version, int max_supported_num_images, int min_overlapping_ratio,
                                       int max_overlapping_ratio, int default_overlapping_ratio) {
            this.version = version;
            this.max_supported_num_images = max_supported_num_images;
            this.min_overlapping_ratio = min_overlapping_ratio;
            this.max_overlapping_ratio = max_overlapping_ratio;
            this.default_overlapping_ratio = default_overlapping_ratio;
        }

        @Override
        public String toString() {
            return "version("+version.toString()+") max_supported_num_images("+max_supported_num_images+") overlapping_ratio: min("+min_overlapping_ratio+")" +
                   "max("+max_overlapping_ratio+") default("+default_overlapping_ratio+")";
        }
    }
}
