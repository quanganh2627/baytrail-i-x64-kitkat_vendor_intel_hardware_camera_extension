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

