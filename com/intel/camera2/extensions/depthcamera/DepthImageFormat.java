/*
 * The source code contained or described herein and all documents related to the source code
 * ("Material") are owned by Intel Corporation or its suppliers or licensors. Title to the
 * Material remains with Intel Corporation or its suppliers and licensors. The Material may
 * contain trade secrets and proprietary and confidential information of Intel Corporation
 * and its suppliers and licensors, and is protected by worldwide copyright and trade secret
 * laws and treaty provisions. No part of the Material may be used, copied, reproduced,
 * modified, published, uploaded, posted, transmitted, distributed, or disclosed in any way
 * without Intel’s prior express written permission.
 * No license under any patent, copyright, trade secret or other intellectual property right
 * is granted to or conferred upon you by disclosure or delivery of the Materials, either
 * expressly, by implication, inducement, estoppel or otherwise. Any license under such
 * intellectual property rights must be express and approved by Intel in writing.
 * Copyright © 2015 Intel Corporation. All rights reserved.
 */

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
