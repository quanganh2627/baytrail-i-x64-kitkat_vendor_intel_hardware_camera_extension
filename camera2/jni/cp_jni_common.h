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
#ifndef __CPJNICOMMON_H__
#define __CPJNICOMMON_H__

#include "ia_cp_types.h"
#include "ia_types.h"

struct CPEngine {
    ia_cp_target target;
    ia_cp_context* pIaCpContext;
    ia_acceleration iaAcc;
    ia_env iaEnv;
    ia_cp_hdr* pIaCpHdr;
    ia_cp_ull* pIaCpUll;
};

typedef enum
{
    ACC_STANDALONE,
    ACC_EXTENSION,
    ERROR_MODE
} Acc_Mode;
#endif  /* __CPJNICOMMON_H__ */

