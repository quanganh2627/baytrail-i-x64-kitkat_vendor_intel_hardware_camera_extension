/*
 * Copyright (C) 2012-2014 Intel Corporation
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

#ifndef _LOGHELPER_H_
#define _LOGHELPER_H_

#include <inttypes.h> // For PRId64 used to print int64_t for both 64bits and 32bits
#include <utils/Log.h>
#include <cutils/atomic.h>
#include <cutils/compiler.h>
#include <utils/KeyedVector.h>
#include <utils/Timers.h>
#include <utils/String8.h>
#include <cutils/trace.h>

extern int32_t gAccLogLevel;

enum  {
    /* verbosity level of general traces */
    MEDIA_IPU_ACC_DEBUG_LOG_LEVEL1 = 1,
    MEDIA_IPU_ACC_DEBUG_LOG_LEVEL2 = 1 << 1,
};

#define LOG1(...) ALOGD_IF(gAccLogLevel & MEDIA_IPU_ACC_DEBUG_LOG_LEVEL1, __VA_ARGS__)
#define LOG2(...) ALOGD_IF(gAccLogLevel & MEDIA_IPU_ACC_DEBUG_LOG_LEVEL2, __VA_ARGS__)

#define LOGE    ALOGE
#define LOGD    ALOGD
#define LOGW    ALOGW
#define LOGV    ALOGV

namespace android {

namespace LogHelper {

/**
 * Runtime selection of debugging level.
 */
void setDebugLevel(void);

} // namespace LogHelper
} // namespace android;
#endif // _LOGHELPER_H_
