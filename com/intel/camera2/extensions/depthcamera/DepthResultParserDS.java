/*******************************************************************************
 * INTEL CORPORATION PROPRIETARY INFORMATION
 *
 * This software is supplied under the terms of a license agreement or nondisclosure
 * agreement with Intel Corporation and may not be copied or disclosed except in
 * accordance with the terms of that agreement
 * Copyright(c) 2014 Intel Corporation. All Rights Reserved.
 *******************************************************************************/

package com.intel.camera2.extensions.depthcamera;

import android.hardware.camera2.CaptureResult;


public class DepthResultParserDS extends DepthResultParser 
{	/////////////////// DS4 Specific /////////////////////	
	public static long getMedianThresh( CaptureResult result )
	{return 0;}
	
	
	public static long getScoreMinThreshold( CaptureResult result )
	{return 0;}
	
	
	public static long getScoreMaxThreshold( CaptureResult result )
	{return 0;}
	
	
	public static long getNeighborThresh( CaptureResult result )
	{return 0;}
	
	
	public static long getLRagreeThresh( CaptureResult result )
	{return 0;}
	
	
	public static long getTextureCountThreshold( CaptureResult result ) 
	{return 0;}
	
	
	public static long getTextureDifferenceThreshold(CaptureResult result )
	{return 0;}
	
	
	public static long getSecondPeakThresh( CaptureResult result ) 
	{return 0;}
}
