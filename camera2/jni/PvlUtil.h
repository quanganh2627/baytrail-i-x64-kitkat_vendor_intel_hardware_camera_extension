/*
 * Copyright 2015, Intel Corporation
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
#ifndef __PVLUTIL_H__
#define __PVLUTIL_H__

#include "JNIUtil.h"
#include "pvl_types.h"

/*
 * PACKAGE was defined in Android.mk
 * --> LOCAL_CFLAGS := -DPACKAGE="\"com/intel/camera2/extensions\""
 */
#define CLASS_PVL_LIBRARY_LOADER        PACKAGE "/vision/PVLibraryLoader"
#define CLASS_PVL_VERSION               CLASS_PVL_LIBRARY_LOADER "$Version"
#define SIG_PVL_VERSION                 "L" CLASS_PVL_VERSION ";"

#define CLASS_FACE_DETECTION            PACKAGE "/vision/FaceDetectionJNI"
#define CLASS_FACE_DETECTION_CONFIG     CLASS_FACE_DETECTION "$Config"
#define SIG_FACE_DETECTION_CONFIG       "L" CLASS_FACE_DETECTION_CONFIG ";"
#define CLASS_FACE_DETECTION_PARAM      CLASS_FACE_DETECTION "$Param"
#define SIG_FACE_DETECTION_PARAM        "L" CLASS_FACE_DETECTION_PARAM ";"

#define CLASS_EYE_DETECTION             PACKAGE "/vision/EyeDetectionJNI"
#define CLASS_EYE_DETECTION_CONFIG      CLASS_EYE_DETECTION "$Config"
#define SIG_EYE_DETECTION_CONFIG        "L" CLASS_EYE_DETECTION_CONFIG ";"

#define CLASS_SMILE_DETECTION           PACKAGE "/vision/SmileDetectionJNI"
#define CLASS_SMILE_DETECTION_CONFIG    CLASS_SMILE_DETECTION "$Config"
#define SIG_SMILE_DETECTION_CONFIG      "L" CLASS_SMILE_DETECTION_CONFIG ";"
#define CLASS_SMILE_DETECTION_PARAM     CLASS_SMILE_DETECTION "$Param"
#define SIG_SMILE_DETECTION_PARAM       "L" CLASS_SMILE_DETECTION_PARAM ";"

#define CLASS_BLINK_DETECTION           PACKAGE "/vision/BlinkDetectionJNI"
#define CLASS_BLINK_DETECTION_CONFIG    CLASS_BLINK_DETECTION "$Config"
#define SIG_BLINK_DETECTION_CONFIG      "L" CLASS_BLINK_DETECTION_CONFIG ";"
#define CLASS_BLINK_DETECTION_PARAM     CLASS_BLINK_DETECTION "$Param"
#define SIG_BLINK_DETECTION_PARAM       "L" CLASS_BLINK_DETECTION_PARAM ";"

#define CLASS_FACE_RECOGNITION          PACKAGE "/vision/FaceRecognitionJNI"
#define CLASS_FACE_RECOGNITION_CONFIG   CLASS_FACE_RECOGNITION "$Config"
#define SIG_FACE_RECOGNITION_CONFIG     "L" CLASS_FACE_RECOGNITION_CONFIG ";"
#define CLASS_FACE_RECOGNITION_PARAM    CLASS_FACE_RECOGNITION "$Param"
#define SIG_FACE_RECOGNITION_PARAM      "L" CLASS_FACE_RECOGNITION_PARAM ";"

#define CLASS_FACE_RECOGNITION_WITH_DB         PACKAGE "/vision/FaceRecognitionWithDbJNI"
#define CLASS_FACE_RECOGNITION_WITH_DB_CONFIG  CLASS_FACE_RECOGNITION_WITH_DB "$Config"
#define SIG_FACE_RECOGNITION_WITH_DB_CONFIG    "L" CLASS_FACE_RECOGNITION_WITH_DB_CONFIG ";"
#define CLASS_FACE_RECOGNITION_WITH_DB_PARAM   CLASS_FACE_RECOGNITION_WITH_DB "$Param"
#define SIG_FACE_RECOGNITION_WITH_DB_PARAM     "L" CLASS_FACE_RECOGNITION_WITH_DB_PARAM ";"

#define CLASS_PANORAMA                  PACKAGE "/photography/PanoramaJNI"
#define CLASS_PANORAMA_CONFIG           CLASS_PANORAMA "$Config"
#define SIG_PANORAMA_CONFIG             "L" CLASS_PANORAMA_CONFIG ";"
#define CLASS_PANORAMA_PARAM            CLASS_PANORAMA "$Param"
#define SIG_PANORAMA_PARAM              "L" CLASS_PANORAMA_PARAM ";"



/////////////////////////////////////////////////
// class FaceData
#define CLASS_FACE_DATA                 PACKAGE "/vision/FaceData"

// class FaceData.FaceInfo
#define CLASS_FACE_DETECTION_INFO       CLASS_FACE_DATA "$FaceInfo"
#define SIG_FACE_DETECTION_INFO         "L" CLASS_FACE_DETECTION_INFO ";"

#define STR_FD_rect                     "bound"
#define STR_FD_confidence               "confidence"
#define STR_FD_rip_angle                "ripAngle"
#define STR_FD_rop_angle                "ripAngle"
#define STR_FD_tracking_id              "trackingId"

// class FaceData.EyeInfo
#define CLASS_EYE_DETECTION_INFO        CLASS_FACE_DATA "$EyeInfo"
#define SIG_EYE_DETECTION_INFO          "L" CLASS_EYE_DETECTION_INFO ";"

#define STR_ED_left_eye                 "leftEyePosition"
#define STR_ED_right_eye                "rightEyePosition"
#define STR_ED_confidence               "confidence"

// class FaceData.RecognitionInfo
#define CLASS_FACE_RECOGNITION_INFO     CLASS_FACE_DATA "$RecognitionInfo"
#define SIG_FACE_RECOGNITION_INFO       "L" CLASS_FACE_RECOGNITION_INFO ";"

#define STR_FR_similarity               "similarity"
#define STR_FR_face_id                  "faceId"
#define STR_FR_person_id                "personId"
#define STR_FR_time_stamp               "timeStamp"
#define STR_FR_condition                "condition"
#define STR_FR_checksum                 "checksum"
#define STR_FR_data                     "feature"

// class FaceData.SmileInfo
#define CLASS_SMILE_DETECTION_INFO      CLASS_FACE_DATA "$SmileInfo"
#define SIG_SMILE_DETECTION_INFO        "L" CLASS_SMILE_DETECTION_INFO ";"

#define STR_SD_score                    "score"
#define STR_SD_state                    "state"

// class FaceData.BlinkInfo
#define CLASS_BLINK_DETECTION_INFO      CLASS_FACE_DATA "$BlinkInfo"
#define SIG_BLINK_DETECTION_INFO        "L" CLASS_BLINK_DETECTION_INFO ";"

#define STR_BD_left_score               "leftEyeScore"
#define STR_BD_left_state               "leftEyeState"
#define STR_BD_right_score              "rightEyeScore"
#define STR_BD_right_state              "rightEyeState"

void mapImage(JNIEnv* env, pvl_image* dst, jobject src);
jobject createIaFrame(JNIEnv* env, pvl_image* src);

void print(pvl_image *img);
void dump(char* fileName, pvl_image *image);
jobject createJRect(JNIEnv* env, pvl_rect* rect);
jobject createJPoint(JNIEnv* env, pvl_point* point);
jobject createJVersion(JNIEnv* env, const pvl_version* version);
void getValueRect(JNIEnv* env, pvl_rect* out, jobject obj, const char* field_name);
void getValuePoint(JNIEnv* env, pvl_point* out, jobject obj, const char* field_name);

#endif  /* __PVLUTIL_H__ */

