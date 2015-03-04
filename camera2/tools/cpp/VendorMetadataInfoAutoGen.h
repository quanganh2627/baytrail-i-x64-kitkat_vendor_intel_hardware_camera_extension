/*
 * Copyright (C) 2014 The Android Open Source Project
 *               2014 Intel Corporation
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
 * ! Do not edit this file directly !
 * ! Do not include this file directly !
 *
 * Generated automatically from metadata_info_autogen.mako
 * This file logically belongs to Metadata.h
 * check readme.txt to understand how this is used
 */


const metadata_value_t com_intel_cv_blinkDetectMode_values[] = {
                 {"OFF", COM_INTEL_CV_BLINK_DETECT_MODE_OFF },
                 {"ON", COM_INTEL_CV_BLINK_DETECT_MODE_ON },
         };

const metadata_value_t com_intel_cv_faceRecognizeMode_values[] = {
                 {"OFF", COM_INTEL_CV_FACE_RECOGNIZE_MODE_OFF },
                 {"ON", COM_INTEL_CV_FACE_RECOGNIZE_MODE_ON },
         };

const metadata_value_t com_intel_cv_smileDetectMode_values[] = {
                 {"OFF", COM_INTEL_CV_SMILE_DETECT_MODE_OFF },
                 {"ON", COM_INTEL_CV_SMILE_DETECT_MODE_ON },
         };

const metadata_value_t com_intel_device_dualCameraMode_values[] = {
                 {"OFF", COM_INTEL_DEVICE_DUAL_CAMERA_MODE_OFF },
                 {"ON", COM_INTEL_DEVICE_DUAL_CAMERA_MODE_ON },
         };

const metadata_value_t com_intel_imageEnhance_colorEffect_values[] = {
                 {"SKY_BLUE", COM_INTEL_IMAGE_ENHANCE_COLOR_EFFECT_SKY_BLUE },
                 {"GRASS_GREEN", COM_INTEL_IMAGE_ENHANCE_COLOR_EFFECT_GRASS_GREEN },
                 {"SKIN_WHITEN", COM_INTEL_IMAGE_ENHANCE_COLOR_EFFECT_SKIN_WHITEN },
                 {"SKIN_WHITEN_LOW", COM_INTEL_IMAGE_ENHANCE_COLOR_EFFECT_SKIN_WHITEN_LOW },
                 {"SKIN_WHITEN_HIGH", COM_INTEL_IMAGE_ENHANCE_COLOR_EFFECT_SKIN_WHITEN_HIGH },
                 {"VIVID", COM_INTEL_IMAGE_ENHANCE_COLOR_EFFECT_VIVID },
         };

const metadata_value_t com_intel_statistics_analysisMode_values[] = {
                 {"OFF", COM_INTEL_STATISTICS_ANALYSIS_MODE_OFF },
                 {"ON", COM_INTEL_STATISTICS_ANALYSIS_MODE_ON },
         };

const metadata_value_t com_intel_statistics_multiFrameHint_values[] = {
                 {"NONE", COM_INTEL_STATISTICS_MULTI_FRAME_HINT_NONE },
                 {"ULL", COM_INTEL_STATISTICS_MULTI_FRAME_HINT_ULL },
                 {"HDR", COM_INTEL_STATISTICS_MULTI_FRAME_HINT_HDR },
         };

const metadata_value_t com_intel_statistics_sceneDetected_values[] = {
                 {"NONE", COM_INTEL_STATISTICS_SCENE_DETECTED_NONE },
                 {"CLOSE_UP_PORTRAIT", COM_INTEL_STATISTICS_SCENE_DETECTED_CLOSE_UP_PORTRAIT },
                 {"PORTRAIT", COM_INTEL_STATISTICS_SCENE_DETECTED_PORTRAIT },
                 {"LOWLIGHT_PORTRAIT", COM_INTEL_STATISTICS_SCENE_DETECTED_LOWLIGHT_PORTRAIT },
                 {"LOWLIGHT", COM_INTEL_STATISTICS_SCENE_DETECTED_LOWLIGHT },
                 {"ACTION", COM_INTEL_STATISTICS_SCENE_DETECTED_ACTION },
                 {"BACKLIGHT", COM_INTEL_STATISTICS_SCENE_DETECTED_BACKLIGHT },
                 {"LANDSCAPE", COM_INTEL_STATISTICS_SCENE_DETECTED_LANDSCAPE },
                 {"DOCUMENT", COM_INTEL_STATISTICS_SCENE_DETECTED_DOCUMENT },
                 {"FIREWORK", COM_INTEL_STATISTICS_SCENE_DETECTED_FIREWORK },
                 {"LOWLIGHT_ACTION", COM_INTEL_STATISTICS_SCENE_DETECTED_LOWLIGHT_ACTION },
                 {"BABY", COM_INTEL_STATISTICS_SCENE_DETECTED_BABY },
                 {"BARCODE", COM_INTEL_STATISTICS_SCENE_DETECTED_BARCODE },
         };


const metadata_value_t vendorMetadataNames[] = {
        {"com.intel.statistics.analysisMode", COM_INTEL_STATISTICS_ANALYSIS_MODE},
        {"com.intel.statistics.multiFrameHint", COM_INTEL_STATISTICS_MULTI_FRAME_HINT},
        {"com.intel.statistics.sceneDetected", COM_INTEL_STATISTICS_SCENE_DETECTED},
        {"com.intel.cv.smileDetectMode", COM_INTEL_CV_SMILE_DETECT_MODE},
        {"com.intel.cv.blinkDetectMode", COM_INTEL_CV_BLINK_DETECT_MODE},
        {"com.intel.cv.faceRecognizeMode", COM_INTEL_CV_FACE_RECOGNIZE_MODE},
        {"com.intel.cv.smileDetectStatus", COM_INTEL_CV_SMILE_DETECT_STATUS},
        {"com.intel.cv.smileDetectScores", COM_INTEL_CV_SMILE_DETECT_SCORES},
        {"com.intel.cv.blinkDetectStatus", COM_INTEL_CV_BLINK_DETECT_STATUS},
        {"com.intel.cv.blinkDetectScores", COM_INTEL_CV_BLINK_DETECT_SCORES},
        {"com.intel.cv.faceRecognizePersonIds", COM_INTEL_CV_FACE_RECOGNIZE_PERSON_IDS},
        {"com.intel.cv.faceRecognizePersonSimilarities", COM_INTEL_CV_FACE_RECOGNIZE_PERSON_SIMILARITIES},
        {"com.intel.cv.faceDetectRipAngles", COM_INTEL_CV_FACE_DETECT_RIP_ANGLES},
        {"com.intel.cv.faceDetectRopAngles", COM_INTEL_CV_FACE_DETECT_ROP_ANGLES},
        {"com.intel.device.dualCameraMode", COM_INTEL_DEVICE_DUAL_CAMERA_MODE},
        {"com.intel.imageEnhance.colorEffect", COM_INTEL_IMAGE_ENHANCE_COLOR_EFFECT},
};

const metadata_tag_t static_vendor_tags_table[] = {
    // STATISTICS
    // CV
    {"cv.info.availableSmileDetect", COM_INTEL_CV_INFO_AVAILABLE_SMILE_DETECT, TYPE_BYTE, com_intel_cv_smileDetectMode_values, 2, true, {-1,0,0}, ENUM_LIST },
    {"cv.info.availableBlinkDetect", COM_INTEL_CV_INFO_AVAILABLE_BLINK_DETECT, TYPE_BYTE, com_intel_cv_blinkDetectMode_values, 2, true, {-1,0,0}, ENUM_LIST },
    {"cv.info.availableFaceRecognize", COM_INTEL_CV_INFO_AVAILABLE_FACE_RECOGNIZE, TYPE_BYTE, com_intel_cv_faceRecognizeMode_values, 2, true, {-1,0,0}, ENUM_LIST },
    {"cv.info.availableObjectTrack", COM_INTEL_CV_INFO_AVAILABLE_OBJECT_TRACK, TYPE_BYTE, NULL, 0, true, {-1,0,0}, ENUM_LIST },
    {"cv.info.availablePanorama", COM_INTEL_CV_INFO_AVAILABLE_PANORAMA, TYPE_BYTE, NULL, 0, true, {-1,0,0}, ENUM_LIST },
    // DEVICE
    {"device.info.availableDualCameraMode", COM_INTEL_DEVICE_INFO_AVAILABLE_DUAL_CAMERA_MODE, TYPE_BYTE, com_intel_device_dualCameraMode_values, 2, true, {-1,0,0}, ENUM_LIST },
    // IMAGE_ENHANCE
};

#define STATIC_VENDOR_TAGS_TABLE_SIZE 6

