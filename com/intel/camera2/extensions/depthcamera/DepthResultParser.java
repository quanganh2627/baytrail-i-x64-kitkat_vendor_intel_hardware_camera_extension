/*******************************************************************************
 * INTEL CORPORATION PROPRIETARY INFORMATION
 *
 * This software is supplied under the terms of a license agreement or nondisclosure
 * agreement with Intel Corporation and may not be copied or disclosed except in
 * accordance with the terms of that agreement
 * Copyright(c) 2014 Intel Corporation. All Rights Reserved.
 ********************************************************************************/

package com.intel.camera2.extensions.depthcamera;

import android.hardware.camera2.CaptureResult;


public class DepthResultParser {
	
	public static boolean getAuxiliaryRectificationMode(CaptureResult result,int type) 
	{
		return false;
	} 
	
	public static int getDepthUnit(CaptureResult result) {
		return 0;
	}
	public static int getDepthFrameRate() {
		return 0;
	}	
	public static int getColorFrameRate(CaptureResult result) {
		return 0;
	}
	public static int getMinZValue(CaptureResult result) {
		return 0;
	}
	public static int getMaxZValue(CaptureResult result) {
		return 0;
	}
		
	public static int getAutoExposureMode(CaptureResult result,int which)
	{return 0;}
	
	public static float	getMinExposureTime(CaptureResult result,int which)
	{return 0;}
	public static float	getMaxExposureTime(CaptureResult result,int which)
	{return 0;}
	public static float	getExposureTime(CaptureResult result,int which)
	{return 0;}
	
	public static float	getMinGain(CaptureResult result,int which) 
	{return 0;}
	public static float	getMaxGain(CaptureResult result,int which)
	{return 0;}
	public static float	getGain(CaptureResult result,int which)
	{return 0;}
	
	public static int getPWMDisableLaserCount(CaptureResult result){
		return 0;
	}
	
	public static int getPWMEnableLaserCount(CaptureResult result)
	{
		return 0;
	}
	public static int getYUY2EnableEmbeddedCounterField(CaptureResult result)
	{
		return 0;
	}
}
