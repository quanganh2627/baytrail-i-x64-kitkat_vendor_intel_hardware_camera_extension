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

#ifndef INTEL_CAMERA_METADATA_TAGS_H
#define INTEL_CAMERA_METADATA_TAGS_H
#include <system/camera_metadata.h>

/**
 * ! Do not edit this file directly !
 *
 * Generated automatically from intel_camera_metadata_tags.mako
 */

/** TODO: Nearly every enum in this file needs a description */

/**
 * Top level hierarchy definitions for camera metadata. *_INFO sections are for
 * the static metadata that can be retrived without opening the camera device.
 * New sections must be added right before INTEL_CAMERA_SECTION_COUNT to maintain
 * existing enumerations.
 */
typedef enum intel_camera_metadata_section {
    COM_INTEL_AIQ,
    COM_INTEL_FACE_ENGINE,
    COM_INTEL_FACE_ENGINE_INFO,
    INTEL_CAMERA_SECTION_COUNT,
} intel_camera_metadata_section_t;

/**
 * Hierarchy positions in enum space. All vendor extension tags must be
 * defined with tag >= VENDOR_SECTION_START
 */
typedef enum intel_camera_metadata_section_start {
    COM_INTEL_AIQ_START            = (COM_INTEL_AIQ            << 16 ) | VENDOR_SECTION_START,
    COM_INTEL_FACE_ENGINE_START    = (COM_INTEL_FACE_ENGINE    << 16 ) | VENDOR_SECTION_START,
    COM_INTEL_FACE_ENGINE_INFO_START
                                   = (COM_INTEL_FACE_ENGINE_INFO
                                                                << 16 ) | VENDOR_SECTION_START,
} intel_camera_metadata_section_start_t;

/**
 * Main enum for defining camera metadata tags.  New entries must always go
 * before the section _END tag to preserve existing enumeration values.  In
 * addition, the name and type of the tag needs to be added to
 * intel_camera_metadata_tag_info.c
 */
typedef enum intel_camera_metadata_tag {
    COM_INTEL_AIQ_ANALYSIS_MODE =                     // enum         | public
            COM_INTEL_AIQ_START,
    COM_INTEL_AIQ_COLOR_EFFECT,                       // enum         | public
    COM_INTEL_AIQ_MULTI_FRAME_HINT,                   // enum         | public
    COM_INTEL_AIQ_SCENE_DETECTED,                     // enum         | public
    COM_INTEL_AIQ_END,

    COM_INTEL_FACE_ENGINE_SMILE_DETECT_MODE =         // enum         | public
            COM_INTEL_FACE_ENGINE_START,
    COM_INTEL_FACE_ENGINE_BLINK_DETECT_MODE,          // enum         | public
    COM_INTEL_FACE_ENGINE_FACE_RECOGNIZE_MODE,        // enum         | public
    COM_INTEL_FACE_ENGINE_SMILE_DETECT_STATUS,        // byte[]       | public
    COM_INTEL_FACE_ENGINE_SMILE_DETECT_SCORES,        // int32[]      | public
    COM_INTEL_FACE_ENGINE_BLINK_DETECT_STATUS,        // byte[]       | public
    COM_INTEL_FACE_ENGINE_BLINK_DETECT_SCORES,        // int32[]      | public
    COM_INTEL_FACE_ENGINE_FACE_RECOGNIZE_PERSON_IDS,  // int32[]      | public
    COM_INTEL_FACE_ENGINE_FACE_RECOGNIZE_PERSON_SIMILARITIES,
                                                      // int32[]      | public
    COM_INTEL_FACE_ENGINE_FACE_DETECT_RIP_ANGLES,     // int32[]      | public
    COM_INTEL_FACE_ENGINE_FACE_DETECT_ROP_ANGLES,     // int32[]      | public
    COM_INTEL_FACE_ENGINE_END,

    COM_INTEL_FACE_ENGINE_INFO_AVAILABLE_SMILE_DETECT = 
                                                      // byte[]       | public
            COM_INTEL_FACE_ENGINE_INFO_START,
    COM_INTEL_FACE_ENGINE_INFO_AVAILABLE_BLINK_DETECT,// byte[]       | public
    COM_INTEL_FACE_ENGINE_INFO_AVAILABLE_FACE_RECOGNIZE,
                                                      // byte[]       | public
    COM_INTEL_FACE_ENGINE_INFO_AVAILABLE_OBJECT_TRACK,// byte[]       | public
    COM_INTEL_FACE_ENGINE_INFO_AVAILABLE_PANORAMA,    // byte[]       | public
    COM_INTEL_FACE_ENGINE_INFO_END,

} intel_camera_metadata_tag_t;

/**
 * Enumeration definitions for the various entries that need them
 */

// COM_INTEL_AIQ_ANALYSIS_MODE
typedef enum intel_camera_metadata_enum_com_intel_aiq_analysis_mode {
    COM_INTEL_AIQ_ANALYSIS_MODE_OFF,
    COM_INTEL_AIQ_ANALYSIS_MODE_ON,
} intel_camera_metadata_enum_com_intel_aiq_analysis_mode_t;

// COM_INTEL_AIQ_COLOR_EFFECT
typedef enum intel_camera_metadata_enum_com_intel_aiq_color_effect {
    COM_INTEL_AIQ_COLOR_EFFECT_SKY_BLUE,
    COM_INTEL_AIQ_COLOR_EFFECT_GRASS_GREEN,
    COM_INTEL_AIQ_COLOR_EFFECT_SKIN_WHITEN,
    COM_INTEL_AIQ_COLOR_EFFECT_SKIN_WHITEN_LOW,
    COM_INTEL_AIQ_COLOR_EFFECT_SKIN_WHITEN_HIGH,
    COM_INTEL_AIQ_COLOR_EFFECT_VIVID,
} intel_camera_metadata_enum_com_intel_aiq_color_effect_t;

// COM_INTEL_AIQ_MULTI_FRAME_HINT
typedef enum intel_camera_metadata_enum_com_intel_aiq_multi_frame_hint {
    COM_INTEL_AIQ_MULTI_FRAME_HINT_NONE,
    COM_INTEL_AIQ_MULTI_FRAME_HINT_ULL,
    COM_INTEL_AIQ_MULTI_FRAME_HINT_HDR,
} intel_camera_metadata_enum_com_intel_aiq_multi_frame_hint_t;

// COM_INTEL_AIQ_SCENE_DETECTED
typedef enum intel_camera_metadata_enum_com_intel_aiq_scene_detected {
    COM_INTEL_AIQ_SCENE_DETECTED_NONE,
    COM_INTEL_AIQ_SCENE_DETECTED_CLOSE_UP_PORTRAIT,
    COM_INTEL_AIQ_SCENE_DETECTED_PORTRAIT,
    COM_INTEL_AIQ_SCENE_DETECTED_LOWLIGHT_PORTRAIT,
    COM_INTEL_AIQ_SCENE_DETECTED_LOWLIGHT,
    COM_INTEL_AIQ_SCENE_DETECTED_ACTION,
    COM_INTEL_AIQ_SCENE_DETECTED_BACKLIGHT,
    COM_INTEL_AIQ_SCENE_DETECTED_LANDSCAPE,
    COM_INTEL_AIQ_SCENE_DETECTED_DOCUMENT,
    COM_INTEL_AIQ_SCENE_DETECTED_FIREWORK,
    COM_INTEL_AIQ_SCENE_DETECTED_LOWLIGHT_ACTION,
    COM_INTEL_AIQ_SCENE_DETECTED_BABY,
    COM_INTEL_AIQ_SCENE_DETECTED_BARCODE,
} intel_camera_metadata_enum_com_intel_aiq_scene_detected_t;


// COM_INTEL_FACE_ENGINE_SMILE_DETECT_MODE
typedef enum intel_camera_metadata_enum_com_intel_face_engine_smile_detect_mode {
    COM_INTEL_FACE_ENGINE_SMILE_DETECT_MODE_OFF,
    COM_INTEL_FACE_ENGINE_SMILE_DETECT_MODE_ON,
} intel_camera_metadata_enum_com_intel_face_engine_smile_detect_mode_t;

// COM_INTEL_FACE_ENGINE_BLINK_DETECT_MODE
typedef enum intel_camera_metadata_enum_com_intel_face_engine_blink_detect_mode {
    COM_INTEL_FACE_ENGINE_BLINK_DETECT_MODE_OFF,
    COM_INTEL_FACE_ENGINE_BLINK_DETECT_MODE_ON,
} intel_camera_metadata_enum_com_intel_face_engine_blink_detect_mode_t;

// COM_INTEL_FACE_ENGINE_FACE_RECOGNIZE_MODE
typedef enum intel_camera_metadata_enum_com_intel_face_engine_face_recognize_mode {
    COM_INTEL_FACE_ENGINE_FACE_RECOGNIZE_MODE_OFF,
    COM_INTEL_FACE_ENGINE_FACE_RECOGNIZE_MODE_ON,
} intel_camera_metadata_enum_com_intel_face_engine_face_recognize_mode_t;



#endif
