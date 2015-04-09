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

class PVLibraryLoader {

    private static boolean isSupported;

    static {
        try {
            System.loadLibrary("pvl_jni");
            isSupported = true;
        } catch (UnsatisfiedLinkError e) {
            isSupported = false;
        }
    }

    public static boolean isSupported() {
        return isSupported;
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

        public String toString() {
            return major + "/" + minor + "patch("+patch+") desc("+description+")";
        }
    }
}
