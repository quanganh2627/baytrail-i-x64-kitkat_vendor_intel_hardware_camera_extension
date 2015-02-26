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

#define CLASS_PANORAMA                  PACKAGE "/photography/PanoramaJNI"
#define CLASS_PANORAMA_CONFIG           CLASS_PANORAMA "$Config"
#define SIG_PANORAMA_CONFIG             "L" CLASS_PANORAMA_CONFIG ";"
#define CLASS_PANORAMA_PARAM            CLASS_PANORAMA "$Param"
#define SIG_PANORAMA_PARAM              "L" CLASS_PANORAMA_PARAM ";"

#define CLASS_FACE_DATA                 PACKAGE "/vision/FaceData"

#define CLASS_FACE_DETECTION_INFO       CLASS_FACE_DATA "$FaceInfo"
#define SIG_FACE_DETECTION_INFO         "L" CLASS_FACE_DETECTION_INFO ";"

#define CLASS_EYE_DETECTION_INFO        CLASS_FACE_DATA "$EyeInfo"
#define SIG_EYE_DETECTION_INFO          "L" CLASS_EYE_DETECTION_INFO ";"

#define CLASS_FACE_RECOGNITION_INFO     CLASS_FACE_DATA "$RecognitionInfo"
#define SIG_FACE_RECOGNITION_INFO       "L" CLASS_FACE_RECOGNITION_INFO ";"

#define CLASS_SMILE_DETECTION_INFO      CLASS_FACE_DATA "$SmileInfo"
#define SIG_SMILE_DETECTION_INFO        "L" CLASS_SMILE_DETECTION_INFO ";"

#define CLASS_BLINK_DETECTION_INFO      CLASS_FACE_DATA "$BlinkInfo"
#define SIG_BLINK_DETECTION_INFO        "L" CLASS_BLINK_DETECTION_INFO ";"

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

