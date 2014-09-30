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

#define LOG_TAG "Acc_LogHelper"

#include <stdint.h>
#include <limits.h> // INT_MAX, INT_MIN
#include <stdlib.h> // atoi.h
#include <utils/Log.h>
#include <cutils/properties.h>

#include "LogHelper.h"

int32_t gAccLogLevel = 0;

using namespace android;

void LogHelper::setDebugLevel(void)
{
    const char* PROP_MEDIA_IPU_ACC_DEBUG = "media.ipu.acc.debug";
    char debugPropTmp [PROPERTY_VALUE_MAX];

    if (property_get(PROP_MEDIA_IPU_ACC_DEBUG, debugPropTmp, NULL)) {
        gAccLogLevel = atoi(debugPropTmp);
        LOGD("Debug level is 0x%x", gAccLogLevel);

        // Check that the property value is a valid integer
        if (gAccLogLevel >= INT_MAX || gAccLogLevel <= INT_MIN) {
            LOGD("Invalid %s property integer value: %s", PROP_MEDIA_IPU_ACC_DEBUG, debugPropTmp);
            gAccLogLevel = 0;
        }

        // "setprop camera.hal.debug 2" is expected
        // to enable both LOG1 and LOG2 traces
        if (gAccLogLevel & MEDIA_IPU_ACC_DEBUG_LOG_LEVEL2)
            gAccLogLevel |= MEDIA_IPU_ACC_DEBUG_LOG_LEVEL1;
    }
}
