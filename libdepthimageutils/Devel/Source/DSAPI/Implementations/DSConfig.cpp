/*******************************************************************************
 * INTEL CORPORATION PROPRIETARY INFORMATION
 *
 * This software is supplied under the terms of a license agreement or nondisclosure
 * agreement with Intel Corporation and may not be copied or disclosed except in
 * accordance with the terms of that agreement
 * Copyright(c) 2014 Intel Corporation. All Rights Reserved.
 ********************************************************************************/

#include "DSConfig.h"
//#include "DSError.h"
#include <utils/Log.h>


void DSConfig::getCalibExtrinsicsNonRectLeftToNonRectRight(double rotation[9], double translation[3]) const
{
    //EMAN
    //const auto& Rright = m_calib.Rright[0];
    const double* Rright = m_calib.Rright[0];
    DSMul_3x3_3x3t(rotation, Rright, m_calib.Rleft[0]);

    //EMAN auto
    float B = m_calib.B[0];
    translation[0] = Rright[0] * B;
    translation[1] = Rright[3] * B;
    translation[2] = Rright[6] * B;
}

void DSConfig::getCalibExtrinsicsZToRectThird(double translation[3])  const
{
    //const auto& T = m_calib.T[0];
    const float* T = m_calib.T[0];
    translation[0] = T[0];
    translation[1] = T[1];
    translation[2] = T[2];
}

void DSConfig::getCalibExtrinsicsZToNonRectThird(double rotation[9], double translation[3]) const
{
    memcpy(rotation, m_calib.Rthird[0], sizeof(double) * 9);

    const double T[3] = {m_calib.T[0][0], m_calib.T[0][1], m_calib.T[0][2]};
    DSMul_3x3_3x1(translation, m_calib.Rthird[0], T);
}

void DSConfig::getCalibExtrinsicsRectThirdToNonRectThird(double rotation[9]) const
{
    memcpy(rotation, m_calib.Rthird[0], sizeof(double) * 9);
}

void DSConfig::getCalibZToWorldTransform(double rotation[9], double translation[3]) const
{
    DSMul_3x3_3x3(rotation, m_calib.Rworld, m_calib.Rleft[0]);
    translation[0] = m_calib.Tworld[0];
    translation[1] = m_calib.Tworld[1];
    translation[2] = m_calib.Tworld[2];
}

void DSConfig::setCalibZToWorldTransform(double rotation[9], double translation[3])
{
    DSMul_3x3_3x3t(m_calib.Rworld, rotation, m_calib.Rleft[0]);
    m_calib.Tworld[0] = static_cast<float>(translation[0]);
    m_calib.Tworld[1] = static_cast<float>(translation[1]);
    m_calib.Tworld[2] = static_cast<float>(translation[2]);
}

int DSConfig::getStereoModeCount(bool rectified) const
{
    return static_cast<int>(m_deviceStereoModes.size());
}

void DSConfig::getStereoMode(bool rectified, int index, int& depthWidth, int& depthHeight, int& stereoFps, DSPixelFormat& lrPixelFormat) const
{
//EMAN
    if (index < 0 || index >= getStereoModeCount(rectified))
        ALOGE("%s Invalid left/right/Z resolution mode index: %d", __FUNCTION__,index);
        // throw DSError(DS_INVALID_ARG, "Invalid left/right/Z resolution mode index: %d", index);

    m_deviceStereoModes[index].getUserDimensions(m_calib, m_cropPixelCount).writeTo(depthWidth, depthHeight);
    stereoFps = m_deviceStereoModes[index].getNativeFramerate();
    lrPixelFormat = m_deviceStereoModes[index].getUserFormat();
}

void DSConfig::setStereoMode(bool rectified, int depthWidth, int depthHeight, int stereoFps, DSPixelFormat lrPixelFormat)
{
    for (int mode = 0; mode < getStereoModeCount(rectified); ++mode)
    {
        int w, h, f;
        DSPixelFormat p;
        getStereoMode(rectified, mode, w, h, f, p);
        if (w == depthWidth && h == depthHeight && f == stereoFps && p == lrPixelFormat)
        {
            m_stereoMode = mode;
            m_isStereoRectificationEnabled = rectified;
            return;
        }
    }
    ALOGE("%s Cannot set Z %s resolution to %d x %d at %d FPS with format %s", __FUNCTION__,
                rectified ? "rectified" : "non-rectified", depthWidth, depthHeight, stereoFps, DSPixelFormatString(lrPixelFormat));
    //throw DSError(DS_INVALID_ARG, "Cannot set Z %s resolution to %d x %d at %d FPS with format %s", 
    //    rectified ? "rectified" : "non-rectified", depthWidth, depthHeight, stereoFps, DSPixelFormatString(lrPixelFormat));
}

int DSConfig::getThirdModeCount(bool rectified) const
{
    return static_cast<int>(rectified ? m_deviceThirdRectModes.size() : m_deviceThirdModes.size());
}

void DSConfig::getThirdMode(bool rectified, int index, int& thirdWidth, int& thirdHeight, int& thirdFps, DSPixelFormat &thirdPixelFormat) const
{
    if (index < 0 || index >= getThirdModeCount(rectified)) 
        ALOGE("%s Invalid third resolution mode index: %d", __FUNCTION__,index);
        //throw DSError(DS_INVALID_ARG, "Invalid third resolution mode index: %d", index);
    //auto& videoMode = rectified ? m_deviceThirdRectModes[index] : m_deviceThirdModes[index];
    DSResolutionMode videoMode = rectified ? m_deviceThirdRectModes[index] : m_deviceThirdModes[index];

    videoMode.getUserDimensions(m_calib, 0).writeTo(thirdWidth, thirdHeight);
    thirdFps = videoMode.getNativeFramerate();
    thirdPixelFormat = videoMode.getUserFormat();
}

void DSConfig::setThirdMode(bool rectified, int thirdWidth, int thirdHeight, int thirdFps, DSPixelFormat thirdPixelFormat)
{
    for (int mode = 0; mode < getThirdModeCount(rectified); ++mode)
    {
        int w, h, f;
        DSPixelFormat p;
        getThirdMode(rectified, mode, w, h, f, p);
        if (w == thirdWidth && h == thirdHeight && f == thirdFps && p == thirdPixelFormat)
        {
            m_thirdMode = mode;
            m_isThirdRectificationEnabled = rectified;
            return;
        }
    }
    ALOGE("%s Cannot set third %s resolution to %d x %d at %d FPS with format %s", __FUNCTION__,
    //throw DSError(DS_INVALID_ARG, "Cannot set third %s resolution to %d x %d at %d FPS with format %s", 
        rectified ? "rectified" : "non-rectified", thirdWidth, thirdHeight, thirdFps, DSPixelFormatString(thirdPixelFormat));
}
