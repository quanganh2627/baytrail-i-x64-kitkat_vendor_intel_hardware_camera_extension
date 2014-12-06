/*******************************************************************************
 * INTEL CORPORATION PROPRIETARY INFORMATION
 *
 * This software is supplied under the terms of a license agreement or nondisclosure
 * agreement with Intel Corporation and may not be copied or disclosed except in
 * accordance with the terms of that agreement
 * Copyright(c) 2014 Intel Corporation. All Rights Reserved.
 ********************************************************************************/

#include "DSResolutionMode.h"
//#include "DSError.h" //EMAN
#include <utils/Log.h>

DSResolutionMode DSResolutionMode::makeLRZ(const int2& nativeLRDims, int nativeFps, int modeIndex, PixelFormatUVC nativeLRFormat, DSPixelFormat userLRFormat)
{
    DSResolutionMode m;
    m.kind = Stereo;
    m.nativeDims = nativeLRDims;
    m.nativeFps = nativeFps;
    m.thirdIntrinsicsIndex = 0;
    m.rectifiedModeIndex = modeIndex;
    m.m_nativeFormat = nativeLRFormat;
    m.m_userFormat = userLRFormat;
    return m;
}

DSResolutionMode DSResolutionMode::makeNonRectifiedThird(const int2& nativeDims, int nativeFps, int thirdIndex, PixelFormatUVC nativeFormat, DSPixelFormat userFormat)
{
    DSResolutionMode m;
    m.kind = NativeThird;
    m.nativeDims = nativeDims;
    m.nativeFps = nativeFps;
    m.thirdIntrinsicsIndex = thirdIndex;
    m.rectifiedModeIndex = 0;
    m.m_nativeFormat = nativeFormat;
    m.m_userFormat = userFormat;
    return m;
}

DSResolutionMode DSResolutionMode::makeRectifiedThird(const int2& nativeDims, int nativeFps, int thirdIndex, int modeIndex, PixelFormatUVC nativeFormat, DSPixelFormat userFormat)
{
    DSResolutionMode m;
    m.kind = RectifiedThird;
    m.nativeDims = nativeDims;
    m.nativeFps = nativeFps;
    m.thirdIntrinsicsIndex = thirdIndex;
    m.rectifiedModeIndex = modeIndex;
    m.m_nativeFormat = nativeFormat;
    m.m_userFormat = userFormat;
    return m;
}

bool DSResolutionMode::operator==(const DSResolutionMode& r) const
{
    return kind == r.kind && 
        thirdIntrinsicsIndex == r.thirdIntrinsicsIndex && 
        rectifiedModeIndex == r.rectifiedModeIndex && 
        nativeFps == r.nativeFps &&
        m_nativeFormat == r.m_nativeFormat &&
        m_userFormat == r.m_userFormat;
}

bool DSResolutionMode::operator<(const DSResolutionMode& r) const
{
    if (kind < r.kind) return true;
    if (kind > r.kind) return false;
    if (thirdIntrinsicsIndex < r.thirdIntrinsicsIndex) return true;
    if (thirdIntrinsicsIndex > r.thirdIntrinsicsIndex) return false;
    if (rectifiedModeIndex < r.rectifiedModeIndex) return true;
    if (rectifiedModeIndex > r.rectifiedModeIndex) return false;
    if (nativeFps > r.nativeFps) return true;
    if (nativeFps < r.nativeFps) return false;
    if (m_userFormat < r.m_userFormat) return true;
    if (m_userFormat > r.m_userFormat) return false;
    return m_nativeFormat < r.m_nativeFormat;
}

const DSCalibIntrinsicsNonRectified& DSResolutionMode::getCalibIntrinsicsNonRect(const DSCalibRectParameters& calib) const
{
    switch (kind)
    {
    case Stereo:
        ALOGE(" %s Cannot ask for non-rectified intrinsics for left/right/Z resolution mode", __FUNCTION__);
        //throw DSError(DS_INVALID_CALL, "Cannot ask for non-rectified intrinsics for left/right/Z resolution mode");
    case NativeThird:
    case RectifiedThird:
        return calib.intrinsicsThird[thirdIntrinsicsIndex];
    default:
        ALOGE("%s Invalid DSResolutionMode", __FUNCTION__);
        //throw DSError(DS_INVALID_ARG, "Invalid DSResolutionMode");
    }
    return DSCalibIntrinsicsNonRectified(); //EMAN
}

DSCalibIntrinsicsRectified DSResolutionMode::getCalibIntrinsicsRect(const DSCalibRectParameters& calib, int cropPixels) const
{
    DSCalibIntrinsicsRectified intrinsics;

    switch (kind)
    {
    case Stereo:
        intrinsics = calib.modesLR[0][rectifiedModeIndex];
        break;
    case NativeThird:
        // throw DSError(DS_INVALID_CALL, "Cannot ask for rectified intrinsics for third image if third rectification is not enabled");
    case RectifiedThird:
        intrinsics = calib.modesThird[0][thirdIntrinsicsIndex][rectifiedModeIndex];
        break;
    default: 
        ALOGE("%s Invalid DSResolutionMode", __FUNCTION__);
        //throw DSError(DS_INVALID_ARG, "Invalid DSResolutionMode");
    }

    // Modify resolution and principle point to account for cropping (leave focal length unadjusted)
    intrinsics.rw -= cropPixels;
    intrinsics.rh -= cropPixels;
    intrinsics.rpx -= cropPixels * 0.5f;
    intrinsics.rpy -= cropPixels * 0.5f;
    return intrinsics;
}

int2 DSResolutionMode::getNativeDimensions(int crop) const
{ 
    return nativeDims - int2(crop,crop); 
}

int2 DSResolutionMode::getUserDimensions(const DSCalibRectParameters& calib, int crop) const
{
    switch (kind)
    {
    case Stereo:
    case RectifiedThird:
    {
        //auto intrinsics = getCalibIntrinsicsRect(calib, crop);
        DSCalibIntrinsicsRectified intrinsics = getCalibIntrinsicsRect(calib, crop);
        return int2(intrinsics.rw, intrinsics.rh);
    }
    case NativeThird:
        return getNativeDimensions(crop);
    default:
        ALOGE("%s Invalid DSResolutionMode", __FUNCTION__);
        //throw DSError(DS_INVALID_ARG, "Invalid DSResolutionMode");
    }
    return int2(-1, -1); //EMAN
}

PixelFormatUVC DSResolutionMode::getNativeFormat() const
{
    return m_nativeFormat;
}

DSPixelFormat DSResolutionMode::getUserFormat() const
{
    return m_userFormat;
}
