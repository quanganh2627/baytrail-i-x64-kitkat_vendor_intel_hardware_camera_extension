/*******************************************************************************
 * INTEL CORPORATION PROPRIETARY INFORMATION
 *
 * This software is supplied under the terms of a license agreement or nondisclosure
 * agreement with Intel Corporation and may not be copied or disclosed except in
 * accordance with the terms of that agreement
 * Copyright(c) 2014 Intel Corporation. All Rights Reserved.
 ********************************************************************************/

#pragma once

#include "DSAPI/DSAPITypes.h"
#include "DSResolutionMode.h"
#include "DSUVCTypes.h"

/// Represents a configuration of a DS device
/// Knows how to answer questions like "what is the current focal length?", etc., WITHOUT dispatching calls
/// to an actual DSDevice instance. Therefore, this struct can be used to represent the device configuration
/// even on platforms without a live device (such as the FILE platform)
/// This type should always be DEFAULT CONSTRUCTIBLE and COPYABLE
class DSConfig
{
public:
    DSConfig()
    {
        // Default most values to 0/false
        memset(this, 0, sizeof(*this));
        m_isStereoRectificationEnabled = true;
        m_isStereoCroppingEnabled = true;
        m_zUnits = 1000;
        m_minZ = 0x0000;
        m_maxZ = 0xffff;
        m_disparityMultiplier = 32;
        m_cropPixelCount = 12;
    }

    // Enumeration and selection of modes
    int getStereoModeCount(bool rectified) const;
    void getStereoMode(bool rectified, int index, int& depthWidth, int& depthHeight, int& stereoFps, DSPixelFormat& lrPixelFormat) const;
    void setStereoMode(bool rectified, int depthWidth, int depthHeight, int stereoFps, DSPixelFormat lrPixelFormat);

    int getThirdModeCount(bool rectified) const;
    void getThirdMode(bool rectified, int index, int& thirdWidth, int& thirdHeight, int& thirdFps, DSPixelFormat& thirdPixelFormat) const;
    void setThirdMode(bool rectified, int thirdWidth, int thirdHeight, int thirdFps, DSPixelFormat thirdPixelFormat);

    // Get information about which streams are enabled
    void setZEnabled(bool enable) { m_isZEnabled = enable; } //Eman
    bool isZEnabled() const { return m_isZEnabled; }
    bool isLREnabled() const { return m_isLeftEnabled || m_isRightEnabled; }
    bool isThirdEnabled() const { return m_isThirdEnabled; }

    // Get information about the currently selected hardware mode
    const DSResolutionMode& getStereoVideoMode() const { return m_deviceStereoModes[m_stereoMode]; }
    const DSResolutionMode& getThirdVideoMode() const { return m_isThirdRectificationEnabled ? m_deviceThirdRectModes[m_thirdMode] : m_deviceThirdModes[m_thirdMode]; }

    // Get the current calibration intrinsics
    DSCalibIntrinsicsRectified getCalibIntrinsicsZ() const { return getStereoVideoMode().getCalibIntrinsicsRect(m_calib, m_cropPixelCount); }
    DSCalibIntrinsicsRectified getCalibIntrinsicsRectLeftRight() const { return getStereoVideoMode().getCalibIntrinsicsRect(m_calib, m_isStereoCroppingEnabled ? m_cropPixelCount : 0); }
    DSCalibIntrinsicsRectified getCalibIntrinsicsRectThird() const { return getThirdVideoMode().getCalibIntrinsicsRect(m_calib, 0); }
    const DSCalibIntrinsicsNonRectified& getCalibIntrinsicsNonRectLeft() const { return m_calib.intrinsicsLeft; }
    const DSCalibIntrinsicsNonRectified& getCalibIntrinsicsNonRectRight() const { return m_calib.intrinsicsRight[0]; }
    const DSCalibIntrinsicsNonRectified& getCalibIntrinsicsNonRectThird() const { return getThirdVideoMode().getCalibIntrinsicsNonRect(m_calib); }

    void getCalibExtrinsicsNonRectLeftToNonRectRight(double rotation[9], double translation[3]) const;
    double getZToDisparityConstant() const { return getCalibIntrinsicsZ().rfx * m_calib.B[0]; }
    void getCalibZToWorldTransform(double rotation[9], double translation[3]) const;
    void setCalibZToWorldTransform(double rotation[9], double translation[3]);
    void getCalibExtrinsicsZToRectThird(double translation[3]) const;
    void getCalibExtrinsicsZToNonRectThird(double rotation[9], double translation[3]) const;
    void getCalibExtrinsicsRectThirdToNonRectThird(double rotation[9]) const;

    // Get the "on the wire" dimensions of a particular image (Note: May contain excess garbage at the end of each scanline)
    int2 getNativeZDimensions() const { return getStereoVideoMode().getNativeDimensions(m_cropPixelCount); }
    int2 getNativeLRDimensions() const { return getStereoVideoMode().getNativeDimensions(0); }
    int2 getNativeThirdDimensions() const { return getThirdVideoMode().getNativeDimensions(0); }

    // Get the "on the wire" format of a particular image
    PixelFormatUVC getNativeZFormat() const { return PixelFormatZ16_2_1; }
    uint32_t getUsedStereoBits() const { return 10; }

    int getNativeLRZFramerate() const { return getStereoVideoMode().getNativeFramerate(); }
    int getNativeThirdFramerate() const { return getThirdVideoMode().getNativeFramerate(); }

    // Get information about software postprocessing
    int2 getUserZDimensions() const { return getStereoVideoMode().getUserDimensions(m_calib, m_cropPixelCount); }
    int2 getUserLRDimensions() const { return getStereoVideoMode().getUserDimensions(m_calib, m_isStereoCroppingEnabled ? m_cropPixelCount : 0); }
    int2 getUserThirdDimensions() const { return getThirdVideoMode().getUserDimensions(m_calib, 0); }

    // Properties which are cached during probeConfiguration
    DSCalibRectParameters m_calib;
    std::vector<DSResolutionMode> m_deviceStereoModes;
    std::vector<DSResolutionMode> m_deviceThirdModes;
    std::vector<DSResolutionMode> m_deviceThirdRectModes;

    // Settings which affect which hardware mode we will be in
    size_t m_stereoMode;                    ///< Which left/right/Z mode to use (index into m_deviceStereoModes)
    size_t m_thirdMode;                     ///< Which third camera mode to use (index into m_deviceThirdModes)
    bool m_isZEnabled;                      ///< True if the Z image will be streamed
    bool m_isLeftEnabled, m_isRightEnabled; ///< True if the left/right images should be available, if either are true, packed L/R will be streamed
    bool m_isThirdEnabled;                  ///< True if the third image will be streamed
    bool m_isStereoRectificationEnabled;    ///< True if the left/right images will be rectified in hardware (REQUIRED FOR THE PRODUCTION OF DEPTH!)

    // Hardware settings which can probably be adjusted while the camera is running
    uint32_t m_zUnits;         ///< How many micrometers should one unit of Z refer to
    uint16_t m_minZ, m_maxZ;   ///< Minimum and maximum Z values which will be produced by the chip
    bool m_useDisparityOutput; ///< True if the hardware should produce pixel disparity values instead of depth
    double m_disparityMultiplier;

    // Settings which affect which software post-processing we will apply
    bool m_isStereoCroppingEnabled;     ///< True if left/right images should be cropped to match the Z image
    bool m_isThirdRectificationEnabled; ///< True if third image should be rectified to the same image plane as Z image in software
    int m_cropPixelCount;
};
