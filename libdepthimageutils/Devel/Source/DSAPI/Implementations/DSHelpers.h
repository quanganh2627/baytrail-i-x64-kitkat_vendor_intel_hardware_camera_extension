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
#include "DSUVCTypes.h"

#include <cstdint>
#include <cassert>
#include <vector>
#include <algorithm>

// This is just a small bit of hackery to help us know what we've implemented. Using NOT_IMPLEMENTED in a function will cause the function, its filename and line number to show
// up on stderr, followed by the program exiting. You can use "return NOT_IMPLEMENTED;" to satisfy the return type requirement of a function, to ensure that it will compile without
// warnings. Over time, we will remove all references to this macro and its underlying helper type, at which point we can delete it.
struct DSNotImplementedHelper
{
    DSNotImplementedHelper(const char* filename, int line, const char* func);
    template <class T>
    operator T() const { return T(); }
};
#define NOT_IMPLEMENTED DSNotImplementedHelper(__FILE__, __LINE__, __FUNCTION__) // Warning: __FUNCTION__ is non-standard. This is just a temporary hack, though, so it should be fine.

inline DSPixelFormat DSGetCorrespondingFormat(PixelFormatUVC format)
{
    switch(format)
    {
    case PixelFormatRY12LY12_4_3: return DS_PF_NATIVE_RL_LUMINANCE12;
    case PixelFormatRAW10: return DS_PF_NATIVE_RAW10;
    case PixelFormatYUY2: return DS_PF_NATIVE_YUY2;
    default: assert(false); return DS_PF_LUMINANCE8;
    }
}

inline size_t DSComputeImageSize(DSPixelFormat format, size_t numPixels)
{
    switch (format)
    {
    default:
        assert(false);
        return 0;

    case DS_PF_LUMINANCE8:
        return numPixels;
    case DS_PF_LUMINANCE16:
        return 2 * numPixels;
    case DS_PF_RGB8:
        return 3 * numPixels;
    case DS_PF_BGRA8:
        return 4 * numPixels;

    case DS_PF_NATIVE_L_LUMINANCE8:
        return 1 * numPixels;
    case DS_PF_NATIVE_L_LUMINANCE16:
        return 2 * numPixels;
    case DS_PF_NATIVE_RL_LUMINANCE8:
        return 2 * numPixels; // 2 bytes per R/L pixel pair
    case DS_PF_NATIVE_RL_LUMINANCE12:
        return 3 * numPixels; // 3 bytes per R/L pixel pair
    case DS_PF_NATIVE_RL_LUMINANCE16:
        return 4 * numPixels; // 4 bytes per R/L pixel pair

    case DS_PF_NATIVE_YUY2:
        return 4 * (numPixels + 1) / 2; // 4 bytes per 2x1 pixel patch
    case DS_PF_NATIVE_RAW10:
        return 5 * (numPixels + 3) / 4; // 5 bytes per 4x1 pixel patch
    }
}

template<class T> bool Contains(const std::vector<T> & elems, const T & elem)
{
   return std::find(begin(elems), end(elems), elem) != end(elems);
}
