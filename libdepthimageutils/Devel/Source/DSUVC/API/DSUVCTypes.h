/*******************************************************************************
 * INTEL CORPORATION PROPRIETARY INFORMATION
 *
 * This software is supplied under the terms of a license agreement or nondisclosure
 * agreement with Intel Corporation and may not be copied or disclosed except in
 * accordance with the terms of that agreement
 * Copyright(c) 2014 Intel Corporation. All Rights Reserved.
 ********************************************************************************/

#pragma once

#include <stdint.h>
//#include <guiddef.h>

#define DATA_RECORD           0x00
#define LINEAR_ADDRESS_RECORD 0x04
#define IRAM_ADDRESS          0x40000000
#define FLASH_OFFSET          0
#define STUB_OFFSET           0


// DS4deviceLabel
enum DS4deviceLabel
{
	DS4RangeDevice		= 0,
	DS4IntensityDevice	= 1,
	DS4WebCamDevice		= 2,
	NumDS4Devices		= 3
};


// DS4FriendlyNames
static const char * DS4FriendlyNames[NumDS4Devices] = 
{
	"DS4 Depth Camera", 
	"DS4 LyRy Camera", 
	"DS4 WebCam"
};


// PixelFormatUVC
enum PixelFormatUVC
{
	PixelFormatLY_8_4_1		= 0, 
	PixelFormatLY12_2_1		= 1, 
	PixelFormatRY8LY8_2_1	= 2, 
	PixelFormatRY12LY12_1_1	= 3, 
	PixelFormatRY12LY12_4_3 = 4,

	PixelFormatZ16_2_1		= 10, 

	PixelFormatYUY2			= 20, 
	PixelFormatRAW10		= 21, 
};

// DS4StreamType
struct DS4StreamType
{
	DS4StreamType() : format(PixelFormatLY_8_4_1), width(0), height(0), fps(0) {}
	~DS4StreamType() {}
	PixelFormatUVC format;
	int width;
	int height;
	int fps;
};


// CamCapsFlags
enum CamCapsFlag
{ 
  FlagAuto		= 1, 
  FlagManual	= 2
};


// Properties, value -1 means auto
enum CamProp
{
	Exposure, 
	Gain, 
	Brightness, 
	Contrast, 
	Saturation, 
	Hue, 
	Gamma, 
	WhiteBalance, 
	Sharpness, 
	BacklightCompensation
};


