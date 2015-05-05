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
#define LOG_TAG "CameraAccLib_EventThread"
#include <dirent.h>
#include <fcntl.h>
#include "acc.h"
#include "v4l2device.h"
#include "event_thread.h"
#include "LogHelper.h"

namespace android {

#define EVENT_POLL_TIMEOUT 500
#define MAX_THREAD_FAILURE_TIMES 60
#define SYS_INFO_DEVICE_DIR "/sys/class/video4linux/"
#define ATOMISP_SUBDEV_NAME_PREFIX "ATOMISP_SUBDEV_"
#define CLEAR(x) memset (&(x), 0, sizeof (x))

EventThread::EventThread(AccControl* accControl) : Thread(false),
                             mAccControl(accControl) {

    LOG2("Construct event thread");
    mEventSubdevice = getSubDevice(0);
    mThreadRunning = false;
    int ret = 0;
    if (!mEventSubdevice->isOpen())
        ret = mEventSubdevice->open();

    if (ret < 0) {
        ALOGE("Failed to open ACC event device!");
        return;
    }

    for (unsigned int i = 0; i < mAccControl->getAllWaiters().size(); i++) {
        EventWaiter *waiter = mAccControl->getAllWaiters().valueAt(i);
        status_t status = mEventSubdevice->subscribeEvent(V4L2_EVENT_ATOMISP_ACC_COMPLETE, waiter->handle);
        if (status < 0) {
            ALOGE("Failed to subscribe to ACC complete event!");
            mEventSubdevice->close();
            return;
        }
    }
    LOG2("Construct event thread done");
}


EventThread::~EventThread() {

    if (mEventSubdevice != NULL && mEventSubdevice->isOpen()) {
        for (unsigned int i = 0; i < mAccControl->getAllWaiters().size(); i++) {
            EventWaiter *waiter = mAccControl->getAllWaiters().valueAt(i);
            status_t status = mEventSubdevice->unsubscribeEvent(V4L2_EVENT_ATOMISP_ACC_COMPLETE, waiter->handle);
            if (status < 0) {
                ALOGE("Failed to unsubscribe to ACC complete event!");
            }
        }
        mEventSubdevice->close();
    }

    exitThread();
}

void EventThread::SubscribeEvent(unsigned handle) {

    mEventSubdevice->subscribeEvent(V4L2_EVENT_ATOMISP_ACC_COMPLETE, handle);
}

void EventThread::UnsubscribeEvent(unsigned handle) {

    mEventSubdevice->unsubscribeEvent(V4L2_EVENT_ATOMISP_ACC_COMPLETE, handle);
}

bool EventThread::threadLoop() {

    mThreadRunning = true;
    int failureCounter = 0;

    while (mThreadRunning) {
        LOG2("Thread looping...");
        usleep(500);
        if (mEventSubdevice->isOpen()) {

           bool pipeReturn;
           int pipeFd = -1;
           struct v4l2_event event;
           CLEAR(event);
           failureCounter = 0;
           int ret = mEventSubdevice->pollDevices(EVENT_POLL_TIMEOUT, pipeReturn, pipeFd);
           if(pipeReturn == true) {
               LOG2("poll returns from flush pipe!");
           } else if (ret <= 0) {
               ALOGE("Stats sync poll failed (%s), waiting recovery", (ret == 0) ? "timeout" : "error");
               ret = -1;
            } else {
                // poll was successful, dequeue the event right away
                ret = mEventSubdevice->dequeueEvent(&event);
                if (ret < 0) {
                    ALOGE("Dequeue stats event failed");
                }
            }

            if (ret < 0) {
                usleep(500);
                continue;
            }

            if (event.type == V4L2_EVENT_ATOMISP_ACC_COMPLETE) {
                mAccControl->acc_stage_update(event.id);
            }
        } else {
            failureCounter ++;

            if (failureCounter > MAX_THREAD_FAILURE_TIMES) {
                break;
            }
        }
    }
    return false;
}

void EventThread::exitThread() {
    mThreadRunning = false;
}

sp<V4L2Device> EventThread::getSubDevice(int groupIdx)
{
    const char *sysInfoDeviceDir = SYS_INFO_DEVICE_DIR;
    const char *ISP_SUBDEV_NAME_PREFIX = ATOMISP_SUBDEV_NAME_PREFIX;
    dirent *entry;
    sp<V4L2Device> subDevice = NULL;

    DIR *dir = opendir(sysInfoDeviceDir);
    if (!dir) {
        ALOGE("ERROR in opening subDeviceDir folder %s", sysInfoDeviceDir);
        return NULL;
    }

    while ((entry = readdir(dir)) != NULL) {
        if ((strcmp(entry->d_name, ".") == 0) ||
            (strcmp(entry->d_name, "..") == 0)) {
            continue;  // Skip self and parent
        }

        String8 sysInfoDevicePath(sysInfoDeviceDir);
        /* length of sub device name, the name prefix is ATOMISP_SUBDEV_,
         * please check here, if the prefix is changed.
         */
        const int deviceNameLen = 32;
        char deviceName[deviceNameLen] = {'\0'};
        char expSubdeviceName[deviceNameLen] = {'\0'};
        int fd = 0;
        int rcount = 0;

        sysInfoDevicePath.appendPath(entry->d_name);
        sysInfoDevicePath.appendPath("name");

        char* buf = sysInfoDevicePath.lockBuffer(sysInfoDevicePath.size());
        if (CC_UNLIKELY(buf == NULL)) {
            LOGW("Failed to lock buffer for %s", sysInfoDevicePath.string());
            continue;
        }
        fd = open(buf, O_RDONLY);
        if (fd < 0) {
            LOGW("Failed to open file %s", buf);
            continue;
        }
        rcount = read(fd, deviceName, deviceNameLen-1);
        if (rcount < 0) {
            LOGW("Failed to read file %s rcount %d", buf, rcount);
            close(fd);
            continue;
        }
        close(fd);

        snprintf(expSubdeviceName, deviceNameLen-1, "%s%d", ISP_SUBDEV_NAME_PREFIX, groupIdx);
        if (strncmp(deviceName, expSubdeviceName, strlen(expSubdeviceName)) == 0) {
            String8 subDevicePath("/dev");
            subDevicePath.appendPath(entry->d_name);
            buf = subDevicePath.lockBuffer(subDevicePath.size());
            if (CC_LIKELY(buf != NULL)) {
                LOG2("Create event device %s", buf);
                subDevice = new V4L2Device(buf);
                break;
            } else {
                LOGE("Failed to lock buffer for %s", subDevicePath.string());
            }
        } else {
            LOGE("Failed to detect event device.");
        }
    }

    closedir(dir);

    return subDevice;
}

}
