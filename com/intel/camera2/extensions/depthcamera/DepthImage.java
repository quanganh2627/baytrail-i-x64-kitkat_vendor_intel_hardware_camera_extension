/*******************************************************************************
 * INTEL CORPORATION PROPRIETARY INFORMATION
 *
 * This software is supplied under the terms of a license agreement or nondisclosure
 * agreement with Intel Corporation and may not be copied or disclosed except in
 * accordance with the terms of that agreement
 * Copyright(c) 2014 Intel Corporation. All Rights Reserved.
 ********************************************************************************/

package com.intel.camera2.extensions.depthcamera;
import java.nio.ByteBuffer;

import android.graphics.Point;
import android.media.Image;


public abstract class DepthImage extends Image {

	 /**
     * @hide
     */
	protected DepthImage() {		
	}

	public abstract Point projectWorldToImageCoordinates(int x, int y, int z);
	
	public abstract int[] projectImageToWorldCoordinates(int u, int v);
	
	public abstract Point[] mapDepthToColorCoordinates(Point[] depthCoordinates );
	
	public abstract Point[]  mapDepthToColorCoordinates(Point origin, int width, int hight ); //map part of the depth image (region)- might be used for optimization
	
	public abstract int getZ(int x, int y);

	public abstract Plane getUVMapPlane();
	
	public abstract void convertZ16ToRGB(ByteBuffer dest);
	
	public abstract void convertUVMapToRGB(ByteBuffer src, ByteBuffer rgbPixels, ByteBuffer dest,int colorWidth, int colorHeight);

}
