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
