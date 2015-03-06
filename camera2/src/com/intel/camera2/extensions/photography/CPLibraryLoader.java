package com.intel.camera2.extensions.photography;

class CPLibraryLoader {

    private static boolean isSupported;

    static {
        try {
            System.loadLibrary("iacp_jni");
            isSupported = true;
        } catch (UnsatisfiedLinkError e) {
            isSupported = false;
            e.printStackTrace();
        }
    }

    public static boolean isSupported() {
        return isSupported;
    }

    public class Version {
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
    }
}
