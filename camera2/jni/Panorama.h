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

