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

/**
 * Class provides interface for setting/getting properties of a request for a depth camera
 * Properties exposed in this interface are common to all cameras. 
 * Separate class is defined for properties specific to specific type of camera
 * 
 * @author ecopty
 *
 */
public class DepthRequestSetup
{
	
	/**
	 * Returns current rectification mode (Enabled/Disabled) for a given auxiliary stream Id (auxId)
	 * @param blder
	 * @param auxId
	 * @return
	 */
	public static boolean getAuxiliaryRectificationMode(CaptureRequest.Builder blder, int auxId) 
	{
		CheckBlder(blder);
		return false;
	} 
	
	/**
	 * sets rectification mode for a given auxiliary stream id to the given modeVal value 
	 * @param blder
	 * @param auxId
	 * @param modeVale
	 */
	public static void setAuxiliaryRectificationMode(CaptureRequest.Builder blder, int auxId, boolean modeVale) {
		CheckBlder(blder);
	}
	
	/**
	 * returns the current value of the Depth Unit in the request
	 * @param blder
	 * @return
	 */
	public static int getDepthUnit(CaptureRequest.Builder blder) {
		return 0;
	}
	
	/**
	 * sets value of the Depth Unit in the request to val
	 * @param blder
	 * @param val
	 * @return
	 */
	public static void setDepthUnit(CaptureRequest.Builder blder,int val) {
	}
 

	/**
	 * returns the current depth frame rate in the request
	 * @param blder
	 * @return
	 */
	public static int getDepthFrameRate(CaptureRequest.Builder blder) {
		return 0;
	}
	/**
	 * sets the depth frame rate in the request to fps
	 * @param blder
	 * @param fps
	 */
	public static void setDepthFrameRate(CaptureRequest.Builder blder,int fps) {
	}
	
	/**
	 * returns the current color frame rate in the request
	 * @param blder
	 * @return
	 */
	public static int getColorFrameRate(CaptureRequest.Builder blder) {
		return 0;
	}
	
	/**
	 * sets the color frame rate in the request to fps
	 * @param blder
	 * @param fps
	 */
	public static void setColorFrameRate(CaptureRequest.Builder blder,int fps) {
	}
	
	
	/**
	 * Returns the current Min Z value set in the requet
	 * @param blder
	 * @return
	 */
	public static int getMinZValue(CaptureRequest.Builder blder) {
		return 0;
	}
	
	/**
	 * Returns the current Max Z Value set in the request
	 * @param blder
	 * @return
	 */
	public static int getMaxZValue(CaptureRequest.Builder blder) {
		return 0;
	}
	
	/**
	 * sets The Min Z value in the request to val
	 * @param blder
	 * @param val
	 */
	public static void setMinZValue( CaptureRequest.Builder blder,int val ) {
	}
	
	/**
	 * sets the max Z value in the request to val
	 * @param blder
	 * @param val
	 */
	public static void setMaxZValue(CaptureRequest.Builder blder, int val ) {
	}
	
	public static void setAutoExposureMode(CaptureRequest.Builder blder,int state, int which) {}
	public static int getAutoExposureMode(CaptureRequest.Builder blder,int which)
	{return 0;}
	public static void	setExposureTime(CaptureRequest.Builder blder,float millisec, int which){}
	public static float	getMinExposureTime(CaptureRequest.Builder blder,int which)
	{return 0;}
	public static float	getMaxExposureTime(CaptureRequest.Builder blder,int which)
	{return 0;}
	public static float	getExposureTime(CaptureRequest.Builder blder,int which)
	{return 0;}
	
	public static void	setGain(CaptureRequest.Builder blder,float gainFactor, int which){}
	public static float	getMinGain(CaptureRequest.Builder blder,int which) 
	{return 0;}
	public static float	getMaxGain(CaptureRequest.Builder blder,int which)
	{return 0;}
	public static float	getGain(CaptureRequest.Builder blder,int which)
	{return 0;}

	// other third image parameters:
	public static void setColorUseAutoWhiteBalanceMode(CaptureRequest.Builder blder,int mode){}
	public static int getColorUseAutoWhiteBalanceMode(CaptureRequest.Builder blder)
	{return 0;}
	
	public static void setColorPowerLineFrequency(CaptureRequest.Builder blder,int freq){}
	public static int getColorPowerLineFrequency(CaptureRequest.Builder blder){return 0;}


	/** 
	 * Laser pulse width modulation (PWM) controls
	 * Laser will be on for "enable laser count" clock cyles, 
	 * and off for "disable laser count" clock cycles.  
	 * Clock is 24 Mhz so each count is 41.667 ns. 
	 * The total cycle is the sum of the two counts, which is a 17 bit value.
 	 * Default behavior at startup is on all the time.
 	 */  
	public static void setPWMDisableLaserCount(CaptureRequest.Builder blder,int disableCount)
	{}
	public static int getPWMDisableLaserCount(CaptureRequest.Builder blder){
		return 0;
	}
	public static void setPWMEnableLaserCount(CaptureRequest.Builder blder,int enableCount)
	{
		
	}
	public static int getPWMEnableLaserCount(CaptureRequest.Builder blder)
	{
		return 0;
	}
	
	public static void setYUY2EnableEmbeddedCounterField(CaptureRequest.Builder blder, int enableCount)
	{
		return;
	}
	
	public static int getYUY2EnableEmbeddedCounterField(CaptureRequest.Builder blder)
	{
		return 0;
	}

	///////////// Private
	private static void CheckBlder(CaptureRequest.Builder blder)
	{
		if ( blder == null )
			throw new IllegalArgumentException("CaptureRequest.Builder is null");
	}
}
