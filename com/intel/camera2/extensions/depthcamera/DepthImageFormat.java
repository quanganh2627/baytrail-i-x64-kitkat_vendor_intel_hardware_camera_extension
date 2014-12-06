/*******************************************************************************
 * INTEL CORPORATION PROPRIETARY INFORMATION
 *
 * This software is supplied under the terms of a license agreement or nondisclosure
 * agreement with Intel Corporation and may not be copied or disclosed except in
 * accordance with the terms of that agreement
 * Copyright(c) 2014 Intel Corporation. All Rights Reserved.
 ********************************************************************************/

package com.intel.camera2.extensions.depthcamera;

import android.graphics.ImageFormat;

public class DepthImageFormat extends ImageFormat {
	
    /**
     * Depth Z format used for depth images, stored in 16 bit words. 
     */
    public static final int Z16 = 0x1F0;
    
    /**
     * Y12 is a YUV planar format comprised of a WxH Y plane only, with each pixel
     * being represented by 12 bits.
     */
    public static final int Y12 = 0x1F1;
    
    public static int getBitsPerPixel(int format) {
        switch (format) {
            case RGB_565:
            case NV16:
            case YUY2:
            case YV12:
            case Y8:
            case Y16:
            case NV21:
            case YUV_420_888:
            case RAW_SENSOR:
            case BAYER_RGGB:
            	return ImageFormat.getBitsPerPixel(format);
            case Y12:
            	return 16;
            case Z16:
            	return 16;
        }
        return -1;
    }
}
