/*
 * Copyright (C) 2014 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * !! Do not reference this file directly !!
 *
 * It is logically a part of camera_metadata.c.  It is broken out for ease of
 * maintaining the tag info.
 *
 * Array assignments are done using specified-index syntax to keep things in
 * sync with intel_camera_metadata_tags.h
 */

/**
 * ! Do not edit this file directly !
 *
 * Generated automatically from intel_camera_metadata_tag_info.mako
 */

const char *intel_camera_metadata_section_names[INTEL_CAMERA_SECTION_COUNT] = {
    [COM_INTEL_AIQ]                = "com.intel.aiq",
    [COM_INTEL_FACE_ENGINE]        = "com.intel.faceEngine",
    [COM_INTEL_FACE_ENGINE_INFO]   = "com.intel.faceEngine.info",
};

static tag_info_t com_intel_aiq_tags[COM_INTEL_AIQ_END -
        COM_INTEL_AIQ_START] = {
    [ COM_INTEL_AIQ_ANALYSIS_MODE - COM_INTEL_AIQ_START ] =
    { "analysisMode",                  TYPE_BYTE   },
    [ COM_INTEL_AIQ_COLOR_EFFECT - COM_INTEL_AIQ_START ] =
    { "colorEffect",                   TYPE_BYTE   },
    [ COM_INTEL_AIQ_MULTI_FRAME_HINT - COM_INTEL_AIQ_START ] =
    { "multiFrameHint",                TYPE_BYTE   },
    [ COM_INTEL_AIQ_SCENE_DETECTED - COM_INTEL_AIQ_START ] =
    { "sceneDetected",                 TYPE_BYTE   },
};

static tag_info_t com_intel_face_engine_tags[COM_INTEL_FACE_ENGINE_END -
        COM_INTEL_FACE_ENGINE_START] = {
    [ COM_INTEL_FACE_ENGINE_SMILE_DETECT_MODE - COM_INTEL_FACE_ENGINE_START ] =
    { "smileDetectMode",               TYPE_BYTE   },
    [ COM_INTEL_FACE_ENGINE_BLINK_DETECT_MODE - COM_INTEL_FACE_ENGINE_START ] =
    { "blinkDetectMode",               TYPE_BYTE   },
    [ COM_INTEL_FACE_ENGINE_FACE_RECOGNIZE_MODE - COM_INTEL_FACE_ENGINE_START ] =
    { "faceRecognizeMode",             TYPE_BYTE   },
    [ COM_INTEL_FACE_ENGINE_SMILE_DETECT_STATUS - COM_INTEL_FACE_ENGINE_START ] =
    { "smileDetectStatus",             TYPE_BYTE   },
    [ COM_INTEL_FACE_ENGINE_SMILE_DETECT_SCORES - COM_INTEL_FACE_ENGINE_START ] =
    { "smileDetectScores",             TYPE_INT32  },
    [ COM_INTEL_FACE_ENGINE_BLINK_DETECT_STATUS - COM_INTEL_FACE_ENGINE_START ] =
    { "blinkDetectStatus",             TYPE_BYTE   },
    [ COM_INTEL_FACE_ENGINE_BLINK_DETECT_SCORES - COM_INTEL_FACE_ENGINE_START ] =
    { "blinkDetectScores",             TYPE_INT32  },
    [ COM_INTEL_FACE_ENGINE_FACE_RECOGNIZE_PERSON_IDS - COM_INTEL_FACE_ENGINE_START ] =
    { "faceRecognizePersonIds",        TYPE_INT32  },
    [ COM_INTEL_FACE_ENGINE_FACE_RECOGNIZE_PERSON_SIMILARITIES - COM_INTEL_FACE_ENGINE_START ] =
    { "faceRecognizePersonSimilarities",
                                        TYPE_INT32  },
    [ COM_INTEL_FACE_ENGINE_FACE_DETECT_RIP_ANGLES - COM_INTEL_FACE_ENGINE_START ] =
    { "faceDetectRipAngles",           TYPE_INT32  },
    [ COM_INTEL_FACE_ENGINE_FACE_DETECT_ROP_ANGLES - COM_INTEL_FACE_ENGINE_START ] =
    { "faceDetectRopAngles",           TYPE_INT32  },
};

static tag_info_t com_intel_face_engine_info_tags[COM_INTEL_FACE_ENGINE_INFO_END -
        COM_INTEL_FACE_ENGINE_INFO_START] = {
    [ COM_INTEL_FACE_ENGINE_INFO_AVAILABLE_SMILE_DETECT - COM_INTEL_FACE_ENGINE_INFO_START ] =
    { "availableSmileDetect",          TYPE_BYTE   },
    [ COM_INTEL_FACE_ENGINE_INFO_AVAILABLE_BLINK_DETECT - COM_INTEL_FACE_ENGINE_INFO_START ] =
    { "availableBlinkDetect",          TYPE_BYTE   },
    [ COM_INTEL_FACE_ENGINE_INFO_AVAILABLE_FACE_RECOGNIZE - COM_INTEL_FACE_ENGINE_INFO_START ] =
    { "availableFaceRecognize",        TYPE_BYTE   },
    [ COM_INTEL_FACE_ENGINE_INFO_AVAILABLE_OBJECT_TRACK - COM_INTEL_FACE_ENGINE_INFO_START ] =
    { "availableObjectTrack",          TYPE_BYTE   },
    [ COM_INTEL_FACE_ENGINE_INFO_AVAILABLE_PANORAMA - COM_INTEL_FACE_ENGINE_INFO_START ] =
    { "availablePanorama",             TYPE_BYTE   },
};



static tag_section_t section_com_intel_aiq = {
    "com.intel.aiq",
    (uint32_t) COM_INTEL_AIQ_START,
    (uint32_t) COM_INTEL_AIQ_END,
    com_intel_aiq_tags
};

static tag_section_t section_com_intel_face_engine = {
    "com.intel.faceEngine",
    (uint32_t) COM_INTEL_FACE_ENGINE_START,
    (uint32_t) COM_INTEL_FACE_ENGINE_END,
    com_intel_face_engine_tags
};

static tag_section_t section_com_intel_face_engine_info = {
    "com.intel.faceEngine.info",
    (uint32_t) COM_INTEL_FACE_ENGINE_INFO_START,
    (uint32_t) COM_INTEL_FACE_ENGINE_INFO_END,
    com_intel_face_engine_info_tags
};


tag_section_t intel_tag_sections[INTEL_CAMERA_SECTION_COUNT] = {
    section_com_intel_aiq,
    section_com_intel_face_engine,
    section_com_intel_face_engine_info,
};

int intel_camera_metadata_enum_snprint(uint32_t tag,
                                 uint32_t value,
                                 char *dst,
                                 size_t size) {
    const char *msg = "error: not an enum";
    int ret = -1;

    switch(tag) {
        case COM_INTEL_AIQ_ANALYSIS_MODE: {
            switch (value) {
                case COM_INTEL_AIQ_ANALYSIS_MODE_OFF:
                    msg = "OFF";
                    ret = 0;
                    break;
                case COM_INTEL_AIQ_ANALYSIS_MODE_ON:
                    msg = "ON";
                    ret = 0;
                    break;
                default:
                    msg = "error: enum value out of range";
            }
            break;
        }
        case COM_INTEL_AIQ_COLOR_EFFECT: {
            switch (value) {
                case COM_INTEL_AIQ_COLOR_EFFECT_SKY_BLUE:
                    msg = "SKY_BLUE";
                    ret = 0;
                    break;
                case COM_INTEL_AIQ_COLOR_EFFECT_GRASS_GREEN:
                    msg = "GRASS_GREEN";
                    ret = 0;
                    break;
                case COM_INTEL_AIQ_COLOR_EFFECT_SKIN_WHITEN:
                    msg = "SKIN_WHITEN";
                    ret = 0;
                    break;
                case COM_INTEL_AIQ_COLOR_EFFECT_SKIN_WHITEN_LOW:
                    msg = "SKIN_WHITEN_LOW";
                    ret = 0;
                    break;
                case COM_INTEL_AIQ_COLOR_EFFECT_SKIN_WHITEN_HIGH:
                    msg = "SKIN_WHITEN_HIGH";
                    ret = 0;
                    break;
                case COM_INTEL_AIQ_COLOR_EFFECT_VIVID:
                    msg = "VIVID";
                    ret = 0;
                    break;
                default:
                    msg = "error: enum value out of range";
            }
            break;
        }
        case COM_INTEL_AIQ_MULTI_FRAME_HINT: {
            switch (value) {
                case COM_INTEL_AIQ_MULTI_FRAME_HINT_NONE:
                    msg = "NONE";
                    ret = 0;
                    break;
                case COM_INTEL_AIQ_MULTI_FRAME_HINT_ULL:
                    msg = "ULL";
                    ret = 0;
                    break;
                case COM_INTEL_AIQ_MULTI_FRAME_HINT_HDR:
                    msg = "HDR";
                    ret = 0;
                    break;
                default:
                    msg = "error: enum value out of range";
            }
            break;
        }
        case COM_INTEL_AIQ_SCENE_DETECTED: {
            switch (value) {
                case COM_INTEL_AIQ_SCENE_DETECTED_NONE:
                    msg = "NONE";
                    ret = 0;
                    break;
                case COM_INTEL_AIQ_SCENE_DETECTED_CLOSE_UP_PORTRAIT:
                    msg = "CLOSE_UP_PORTRAIT";
                    ret = 0;
                    break;
                case COM_INTEL_AIQ_SCENE_DETECTED_PORTRAIT:
                    msg = "PORTRAIT";
                    ret = 0;
                    break;
                case COM_INTEL_AIQ_SCENE_DETECTED_LOWLIGHT_PORTRAIT:
                    msg = "LOWLIGHT_PORTRAIT";
                    ret = 0;
                    break;
                case COM_INTEL_AIQ_SCENE_DETECTED_LOWLIGHT:
                    msg = "LOWLIGHT";
                    ret = 0;
                    break;
                case COM_INTEL_AIQ_SCENE_DETECTED_ACTION:
                    msg = "ACTION";
                    ret = 0;
                    break;
                case COM_INTEL_AIQ_SCENE_DETECTED_BACKLIGHT:
                    msg = "BACKLIGHT";
                    ret = 0;
                    break;
                case COM_INTEL_AIQ_SCENE_DETECTED_LANDSCAPE:
                    msg = "LANDSCAPE";
                    ret = 0;
                    break;
                case COM_INTEL_AIQ_SCENE_DETECTED_DOCUMENT:
                    msg = "DOCUMENT";
                    ret = 0;
                    break;
                case COM_INTEL_AIQ_SCENE_DETECTED_FIREWORK:
                    msg = "FIREWORK";
                    ret = 0;
                    break;
                case COM_INTEL_AIQ_SCENE_DETECTED_LOWLIGHT_ACTION:
                    msg = "LOWLIGHT_ACTION";
                    ret = 0;
                    break;
                case COM_INTEL_AIQ_SCENE_DETECTED_BABY:
                    msg = "BABY";
                    ret = 0;
                    break;
                case COM_INTEL_AIQ_SCENE_DETECTED_BARCODE:
                    msg = "BARCODE";
                    ret = 0;
                    break;
                default:
                    msg = "error: enum value out of range";
            }
            break;
        }

        case COM_INTEL_FACE_ENGINE_SMILE_DETECT_MODE: {
            switch (value) {
                case COM_INTEL_FACE_ENGINE_SMILE_DETECT_MODE_OFF:
                    msg = "OFF";
                    ret = 0;
                    break;
                case COM_INTEL_FACE_ENGINE_SMILE_DETECT_MODE_ON:
                    msg = "ON";
                    ret = 0;
                    break;
                default:
                    msg = "error: enum value out of range";
            }
            break;
        }
        case COM_INTEL_FACE_ENGINE_BLINK_DETECT_MODE: {
            switch (value) {
                case COM_INTEL_FACE_ENGINE_BLINK_DETECT_MODE_OFF:
                    msg = "OFF";
                    ret = 0;
                    break;
                case COM_INTEL_FACE_ENGINE_BLINK_DETECT_MODE_ON:
                    msg = "ON";
                    ret = 0;
                    break;
                default:
                    msg = "error: enum value out of range";
            }
            break;
        }
        case COM_INTEL_FACE_ENGINE_FACE_RECOGNIZE_MODE: {
            switch (value) {
                case COM_INTEL_FACE_ENGINE_FACE_RECOGNIZE_MODE_OFF:
                    msg = "OFF";
                    ret = 0;
                    break;
                case COM_INTEL_FACE_ENGINE_FACE_RECOGNIZE_MODE_ON:
                    msg = "ON";
                    ret = 0;
                    break;
                default:
                    msg = "error: enum value out of range";
            }
            break;
        }
        case COM_INTEL_FACE_ENGINE_SMILE_DETECT_STATUS: {
            break;
        }
        case COM_INTEL_FACE_ENGINE_SMILE_DETECT_SCORES: {
            break;
        }
        case COM_INTEL_FACE_ENGINE_BLINK_DETECT_STATUS: {
            break;
        }
        case COM_INTEL_FACE_ENGINE_BLINK_DETECT_SCORES: {
            break;
        }
        case COM_INTEL_FACE_ENGINE_FACE_RECOGNIZE_PERSON_IDS: {
            break;
        }
        case COM_INTEL_FACE_ENGINE_FACE_RECOGNIZE_PERSON_SIMILARITIES: {
            break;
        }
        case COM_INTEL_FACE_ENGINE_FACE_DETECT_RIP_ANGLES: {
            break;
        }
        case COM_INTEL_FACE_ENGINE_FACE_DETECT_ROP_ANGLES: {
            break;
        }

        case COM_INTEL_FACE_ENGINE_INFO_AVAILABLE_SMILE_DETECT: {
            break;
        }
        case COM_INTEL_FACE_ENGINE_INFO_AVAILABLE_BLINK_DETECT: {
            break;
        }
        case COM_INTEL_FACE_ENGINE_INFO_AVAILABLE_FACE_RECOGNIZE: {
            break;
        }
        case COM_INTEL_FACE_ENGINE_INFO_AVAILABLE_OBJECT_TRACK: {
            break;
        }
        case COM_INTEL_FACE_ENGINE_INFO_AVAILABLE_PANORAMA: {
            break;
        }

    }

    strncpy(dst, msg, size - 1);
    dst[size - 1] = '\0';

    return ret;
}


#define CAMERA_METADATA_ENUM_STRING_MAX_SIZE 18
