/*
 * Copyright 2012, Intel Corporation

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
#ifndef _INTEL_CAMERA_EXTENSIONS
#define _INTEL_CAMERA_EXTENSIONS

#include <system/camera.h>
namespace android {

/** cmdType in sendCommand functions */
enum {
    CAMERA_CMD_ENABLE_INTEL_PARAMETERS  = 0x1000,
    CAMERA_CMD_START_SCENE_DETECTION    = 0x1001,
    CAMERA_CMD_STOP_SCENE_DETECTION     = 0x1002,
    CAMERA_CMD_START_PANORAMA           = 0x1003,
    CAMERA_CMD_STOP_PANORAMA            = 0x1004,
    CAMERA_CMD_START_SMILE_SHUTTER      = 0x1005,
    CAMERA_CMD_STOP_SMILE_SHUTTER       = 0x1006,
    CAMERA_CMD_START_BLINK_SHUTTER      = 0x1007,
    CAMERA_CMD_STOP_BLINK_SHUTTER       = 0x1008,
    CAMERA_CMD_CANCEL_TAKE_PICTURE      = 0x1009,
    CAMERA_CMD_START_FACE_RECOGNITION   = 0x1010,
    CAMERA_CMD_STOP_FACE_RECOGNITION    = 0x1011
};

/**
 * Messagetypes
 *
 */
enum {
    // omitting 0x2000 because it has high probability of being used in the future by
    // official Android messages
    CAMERA_MSG_SCENE_DETECT             = 0x2001,
    CAMERA_MSG_PANORAMA_METADATA        = 0x2002,
    CAMERA_MSG_PANORAMA_SNAPSHOT        = 0x2003
};

/**
 * Panorama metadata
 */
typedef struct camera_panorama_metadata {
    int32_t direction;               /* enum will be in ia_types.h */
    int32_t horizontal_displacement; /* in pixels */
    int32_t vertical_displacement;   /* in pixels */
    bool    motion_blur;
    bool    finalization_started;    /* signals whether automatic finalization has started.
                                        Only valid when associated with a snapshot */
} camera_panorama_metadata_t;

}; // namespace android

#endif /* _INTEL_CAMERA_EXTENSIONS */

