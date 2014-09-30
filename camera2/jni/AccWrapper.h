/*
 * Copyright (C) 2015 Intel Corporation
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
#ifndef ACCWRAPPER_H
#define ACCWRAPPER_H

#include <utils/Errors.h>  // for status_t
#include <utils/RefBase.h>
#include <binder/IMemory.h>
#include <ia_types.h>
#include "AccClient.h"

using namespace android;

status_t CpIpuInit ();
void CpIpuUnInit();
status_t CpIpuHdrInit (const int width, const int height);
status_t CpIpuHdrUnInit ();
status_t CpIpuHdrCompose (const int inImageNum,
                            const acc_ia_frame* inBuf,
                            const acc_ia_frame* inPvBuf,
                            acc_ia_frame* outBuf,
                            acc_ia_frame* outPv);
status_t CpIpuUllInit (const int width, const int height);
status_t CpIpuUllUnInit();
status_t CpIpuUllCompose(const int inImageNum,
                      const acc_ia_frame* inBuf,
                      const acc_ia_frame* inPvBuf,
                      acc_ia_frame* outBuf,
                      acc_ia_frame* outPvBuf,
                      ia_cp_ull_cfg* cfg);

status_t initAcc();
void uninitAcc();


#endif // ACCWRAPPER_H

