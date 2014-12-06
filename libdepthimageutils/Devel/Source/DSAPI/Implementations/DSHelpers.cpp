/*******************************************************************************
 * INTEL CORPORATION PROPRIETARY INFORMATION
 *
 * This software is supplied under the terms of a license agreement or nondisclosure
 * agreement with Intel Corporation and may not be copied or disclosed except in
 * accordance with the terms of that agreement
 * Copyright(c) 2014 Intel Corporation. All Rights Reserved.
 ********************************************************************************/

#include "DSHelpers.h"
#include <iostream>

DSNotImplementedHelper::DSNotImplementedHelper(const char* filename, int line, const char* func)
{
    std::cerr << "Unimplemented function " << func << " was called! (" << filename << ':' << line << ")" << std::endl;
    exit(EXIT_FAILURE);
}

#define DSID(n) case n: return #n;

const char* DSPixelFormatString(DSPixelFormat pixelFormat)
{
    switch (pixelFormat)
    {
        DSID(DS_PF_LUMINANCE8)
        DSID(DS_PF_LUMINANCE16)
        DSID(DS_PF_RGB8)
        DSID(DS_PF_BGRA8)
        DSID(DS_PF_NATIVE_L_LUMINANCE8)
        DSID(DS_PF_NATIVE_L_LUMINANCE16)
        DSID(DS_PF_NATIVE_RL_LUMINANCE8)
        DSID(DS_PF_NATIVE_RL_LUMINANCE12)
        DSID(DS_PF_NATIVE_RL_LUMINANCE16)
        DSID(DS_PF_NATIVE_YUY2)
        DSID(DS_PF_NATIVE_RAW10)
    default:
        return "{bad DSPixelFormat}";
    }
}

const char* DSStatusString(DSStatus status)
{
    switch (status)
    {
        DSID(DS_NO_ERROR)
        DSID(DS_FAILURE)
        DSID(DS_INVALID_CALL)
        DSID(DS_INVALID_ARG)
        DSID(DS_HARDWARE_MISSING)
        DSID(DS_HARDWARE_ERROR)
        DSID(DS_HARDWARE_IN_USE)
        DSID(DS_FILE_ERROR)
        DSID(DS_MEMORY_ERROR)
        DSID(DS_UNSUPPORTED_CONFIG)
    default:
        return "{bad DSStatus}";
    }
}

#undef DSID
