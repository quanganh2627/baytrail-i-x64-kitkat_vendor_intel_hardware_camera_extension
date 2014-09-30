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
#ifndef __CPIPUUTIL_H__
#define __CPIPUUTIL_H__


#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include "ia_types.h"
#include "AccClient.h"

using namespace android;

/*
 * create ia frame for IPU ACC mode, allocate memory for the frame
*/
void create_ia_frame(acc_ia_frame* newFrame,
                              ia_frame_format format,
                              int stride, int width, int height, int rotation);
/*
 * free memory of the frame
*/
void destroy_ia_frame(acc_ia_frame* pFrame);

/*
 * print base frame information
 */
void printFrameInfo(acc_ia_frame* pFrame);

/*
 * downscale frame from input stream to input postview frame
 */
void downscaleAccFrame(acc_ia_frame* src, acc_ia_frame* dest);

/*
 * write the out frame after post process to the object
 */
jobject createIaFrame(JNIEnv* env, acc_ia_frame* src);

#endif  /* __CPIPUUTIL_H__ */

