package com.intel.camera2.extensions.depthcamera;

import android.graphics.ImageFormat;

public class DepthImageFormat extends ImageFormat {

    /**
     * Depth Z format used for depth images, stored in 16 bit words. 
     */
    public static final int Z16 = 0x111;

    /**
    * UVMap format 64bit each inlcudes 2 floats- size is resolution of Depth
    */
    public static final int UVMAP = 0x112;

    public static int getBitsPerPixel(int format) {
        switch (format) {
            case UVMAP:
                return 64;
            case Z16:
                return 16;
        }
        return ImageFormat.getBitsPerPixel(format);
    }
    /**
     * Determine whether or not this is a public-visible {@code format}.
     *
     * <p>In particular, {@code @hide} formats will return {@code false}.</p>
     *
     * <p>Any other formats (including UNKNOWN) will return {@code false}.</p>
     *
     * @param format an integer format
     * @return a boolean
     *
     * @hide
     */
    public static boolean isPublicFormat(int format) {
        switch (format) {
            case UVMAP:
            case Z16:
                return true;
        }
        return ImageFormat.isPublicFormat(format);
    }
}
