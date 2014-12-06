/*******************************************************************************
 * INTEL CORPORATION PROPRIETARY INFORMATION
 *
 * This software is supplied under the terms of a license agreement or nondisclosure
 * agreement with Intel Corporation and may not be copied or disclosed except in
 * accordance with the terms of that agreement
 * Copyright(c) 2014 Intel Corporation. All Rights Reserved.
 ********************************************************************************/

#pragma once

#include "DSAPI/DSCalibRectParameters.h"
#include "DSHelpers.h"
#include "DSMath.h"

/// Represents an available resolution mode that will be advertised through DSAPI
/// Knows about the corresponding native resolution/FPS to configure DSDevice
/// Also knows how to find its intrinsics inside of DSCalibRectParameters
class DSResolutionMode
{
public:
    static DSResolutionMode makeLRZ(const int2& nativeLRDims, int nativeFps, int modeIndex, PixelFormatUVC nativeLRFormat, DSPixelFormat userLRFormat);
    static DSResolutionMode makeNonRectifiedThird(const int2& nativeDims, int nativeFps, int thirdIndex, PixelFormatUVC nativeFormat, DSPixelFormat userFormat);
    static DSResolutionMode makeRectifiedThird(const int2& nativeDims, int nativeFps, int thirdIndex, int modeIndex, PixelFormatUVC nativeFormat, DSPixelFormat userFormat);

    bool operator==(const DSResolutionMode& r) const;
    bool operator<(const DSResolutionMode& r) const;

    const DSCalibIntrinsicsNonRectified& getCalibIntrinsicsNonRect(const DSCalibRectParameters& calib) const; ///< Get non-rectified intrinsics corresponding to this mode (not available for Stereo modes)
    DSCalibIntrinsicsRectified getCalibIntrinsicsRect(const DSCalibRectParameters& calib, int cropPixels) const;       ///< Get rectified intrinsics corresponding to this mode (not available for NativeThird modes)

    int getNativeFramerate() const { return nativeFps; }                              ///< Get framerate of images produced by DSDevice. Receives FPS may be lower based on bandwidth issues.
    int2 getNativeDimensions(int cropPixels) const;                                   ///< Get dimensions of images produced by DSDevice. Set cropPixels = 0 for left/right and third images, and 12 for depth images
    int2 getUserDimensions(const DSCalibRectParameters& calib, int cropPixels) const; ///< Get dimensions of the images provided to the user. Set cropPixels = 0 for uncropped left/right and third images, and 12 for cropped left/right and depth images

    PixelFormatUVC getNativeFormat() const;
    DSPixelFormat getUserFormat() const;

private:
    enum Kind
    {
        Stereo,        ///< Left/right/Z images, may or may not be rectified, always rectified if Z is present
        NativeThird,   ///< Unrectified color images in RAW10 or YUY2 format
        RectifiedThird ///< Wire data is the same as NativeThird, but we rectify the image in software before giving it to the user
    };

    Kind kind;       ///< What kind of video mode is this?
    int2 nativeDims; ///< Size of the left/right or third image produced by DSDevice. Actual image data may be located in a subrect, due to firmware issues.
    int nativeFps;   ///< Intended frequency of images produced by DSDevice. Received FPS may be lower based on USB bandwidth issues.

    PixelFormatUVC m_nativeFormat; ///< What native format will be set
    DSPixelFormat m_userFormat;   ///< What user format will be

    uint16_t thirdIntrinsicsIndex; ///< For third camera video modes, the index of the native resolution of the third camera
    uint16_t rectifiedModeIndex;  ///< For both stereo and third camera modes, the index of the rectified resolution to use
};