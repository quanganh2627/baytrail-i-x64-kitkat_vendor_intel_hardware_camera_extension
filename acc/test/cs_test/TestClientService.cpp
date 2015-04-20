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

#include <stdio.h>
#include <utils/Log.h>
#include "AccClient.h"

using namespace android;
namespace android {
void test()
{
    ALOGD("@%s, line:%d", __FUNCTION__, __LINE__);
    AccClient* accClient = AccClient::connect();
    ALOGD("@%s, line:%d", __FUNCTION__, __LINE__);
    int CssMajor, CssMinor, IspMajor, IspMinor;
    accClient->getHwVersion(CssMajor, CssMinor, IspMajor, IspMinor);
    ALOGD("@%s, line:%d, CssMajor:%d, CssMinor:%d, IspMajor:%d, IspMinor:%d", __FUNCTION__, __LINE__, CssMajor, CssMinor, IspMajor, IspMinor);
    accClient->disConnect();
}
}

int main()
{
    test();
    return 0;
}
