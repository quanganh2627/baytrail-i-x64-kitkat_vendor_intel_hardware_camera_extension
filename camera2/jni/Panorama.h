#ifndef __PVL_PANORAMA_JNI_H__
#define __PVL_PANORAMA_JNI_H__

/*
 * PACKAGE was defined in Android.mk
 * --> LOCAL_CFLAGS := -DPACKAGE="\"com/intel/camera2/extensions\""
 */
#define CLASS_PANORAMA_LIBRARY_LOADER        PACKAGE "/photography/PanoramaJNI"
#define CLASS_PANORAMA_VERSION               CLASS_PANORAMA_LIBRARY_LOADER "$Version"
#define SIG_PANORAMA_VERSION                 "L" CLASS_PANORAMA_VERSION ";"

#define CLASS_PANORAMA                  PACKAGE "/photography/PanoramaJNI"
#define CLASS_PANORAMA_CONFIG           CLASS_PANORAMA "$Config"
#define SIG_PANORAMA_CONFIG             "L" CLASS_PANORAMA_CONFIG ";"
#define CLASS_PANORAMA_PARAM            CLASS_PANORAMA "$Param"
#define SIG_PANORAMA_PARAM              "L" CLASS_PANORAMA_PARAM ";"

#endif  /* __PVL_PANORAMA_JNI_H__ */

