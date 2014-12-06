/*******************************************************************************
 * INTEL CORPORATION PROPRIETARY INFORMATION
 *
 * This software is supplied under the terms of a license agreement or nondisclosure
 * agreement with Intel Corporation and may not be copied or disclosed except in
 * accordance with the terms of that agreement
 * Copyright(c) 2014 Intel Corporation. All Rights Reserved.
 ********************************************************************************/

package com.intel.camera2.extensions.depthcamera;

import android.hardware.camera2.CaptureRequest;

public class DepthRequestSetupDS extends DepthRequestSetup {

	/////////////////// DS4 Specific /////////////////////
	public static void setMedianThresh( CaptureRequest.Builder blder,long threshold){}
	public static long getMedianThresh( CaptureRequest.Builder blder )
	{return 0;}
	
	public static void setScoreMinThreshold(CaptureRequest.Builder blder,  long min){}
	public static long getScoreMinThreshold( CaptureRequest.Builder blder )
	{return 0;}
	
	public static void setScoreMaxThreshold( CaptureRequest.Builder blder, long max){}
	public static long getScoreMaxThreshold( CaptureRequest.Builder blder )
	{return 0;}
	
	public static void setNeighborThresh( CaptureRequest.Builder blder, long val){}
	public static long getNeighborThresh( CaptureRequest.Builder blder )
	{return 0;}
	
	public static void setLRagreeThresh( CaptureRequest.Builder blder, long val) {}
	public static long getLRagreeThresh( CaptureRequest.Builder blder )
	{return 0;}
	
	public static void setTextureCountThreshold( CaptureRequest.Builder blder, long textureCountThresh) {}
	public static long getTextureCountThreshold( CaptureRequest.Builder blder ) 
	{return 0;}
	
	public static void setTextureDifferenceThreshold( CaptureRequest.Builder blder, long textureDifferenceThresh){}
	public static long getTextureDifferenceThreshold(CaptureRequest.Builder blder )
	{return 0;}
	
	public static void setSecondPeakThresh( CaptureRequest.Builder blder, long val){}
	public static long getSecondPeakThresh( CaptureRequest.Builder blder ) 
	{return 0;}
	///////////////////////////////////////////
}
