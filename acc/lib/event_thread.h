/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (c) 2015 Intel Corporation
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
#ifndef _ACC_EVENT_THREAD_
#define _ACC_EVENT_THREAD_
#include <utils/threads.h>

namespace android {

class AccControl;
class V4L2Device;

class EventThread : public Thread {
public:
    EventThread(AccControl* accControl);
    virtual ~EventThread();
    void exitThread();
    void SubscribeEvent(unsigned handle);
    void UnsubscribeEvent(unsigned handle);
private:
    bool mThreadRunning;
    virtual bool threadLoop();
    sp<V4L2Device> mEventSubdevice;
    AccControl* mAccControl;
    sp<V4L2Device> getSubDevice(int groupIdx);
};
}
#endif
