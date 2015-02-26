#ifndef __CPUTIL_H__
#define __CPUTIL_H__

#include "JNIUtil.h"
#include "ia_types.h"

/*
 * PACKAGE was defined in Android.mk
 * --> LOCAL_CFLAGS := -DPACKAGE="\"com/intel/camera2/extensions\""
 */
#define CLASS_CP                    PACKAGE "/photography/CPJNI"

#define CLASS_BLENDER_OPTION        PACKAGE "/photography/BlenderOption"
#define SIG_BLENDER_OPTION          "L" CLASS_BLENDER_OPTION ";"

#define CLASS_HDR_OPTION            PACKAGE "/photography/HdrOption"
#define SIG_HDR_OPTION              "L" CLASS_HDR_OPTION ";"

#define CLASS_ULL_OPTION            PACKAGE "/photography/UllOption"
#define SIG_ULL_OPTION              "L" CLASS_ULL_OPTION ";"


// convert Java IaFrame to ia_frame
void convert(JNIEnv* env, ia_frame* dst, jobject jIaFrameSrc);

void create_ia_frame(ia_frame* newFrame, ia_frame_format format, int stride, int width, int height, int rotation);
void destroy_ia_frame(ia_frame* pFrame);
void printFrameInfo(ia_frame* pFrame);
void downscaleFrame(ia_frame* src, ia_frame* dest);
void debugDumpData(const char* filename, unsigned char* data, int bytes);
jobject createIaFrame(JNIEnv* env, ia_frame* src);

#endif  /* __CPUTIL_H__ */

