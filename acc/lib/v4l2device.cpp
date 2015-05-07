/*
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

#define LOG_TAG "CameraAccLib_V4L2Dev"
#include "LogHelper.h"
#include "v4l2device.h"

#include <sys/stat.h>
#include <fcntl.h>
#include <poll.h>

namespace android {

#define CLEAR(x) memset (&(x), 0, sizeof (x))

////////////////////////////////////////////////////////////////////
//                          PUBLIC METHODS
////////////////////////////////////////////////////////////////////

V4L2Device::V4L2Device(const char *name): mName(name),
                                          mFd(-1)
{
}

V4L2Device::~V4L2Device()
{
    LOG1("@%s", __FUNCTION__);
    if (mFd != -1) {
        ALOGW("Destroying a device object not closed, closing first");
        close();
    }
}

status_t V4L2Device::open()
{
    LOG1("@%s %s", __FUNCTION__, mName.string());
    status_t ret = NO_ERROR;
    struct stat st;
    CLEAR(st);

    if (mFd != -1) {
        ALOGE("Trying to open a device already open");
        return INVALID_OPERATION;
    }

    if (stat (mName.string(), &st) == -1) {
        ALOGE("Error stat video device %s: %s",
             mName.string(), strerror(errno));
        return UNKNOWN_ERROR;
    }

    if (!S_ISCHR (st.st_mode)) {
        ALOGE("%s is not a device", mName.string());
        return UNKNOWN_ERROR;
    }

    mFd = ::open(mName.string(), O_RDWR);

    if (mFd < 0) {
        ALOGE("Error opening video device %s: %s",
              mName.string(), strerror(errno));
        return UNKNOWN_ERROR;
    }

    return ret;
}

status_t V4L2Device::close()
{
    LOG1("@%s device : %s", __FUNCTION__, mName.string());

    if (mFd == -1) {
        ALOGW("Device not opened!");
        return INVALID_OPERATION;
    }

    if (::close(mFd) < 0) {
        ALOGE("Close video device failed: %s", strerror(errno));
        return UNKNOWN_ERROR;
    }

    mFd = -1;
    return NO_ERROR;
}

int V4L2Device::xioctl(int request, void *arg) const
{
    int ret(0);
    if (mFd == -1) {
        ALOGE("%s invalid device closed!",__FUNCTION__);
        return INVALID_OPERATION;
    }

    do {
        ret = ioctl (mFd, request, arg);
    } while (-1 == ret && EINTR == errno);

    if (ret < 0) {
        ALOGW ("%s: Request 0x%x failed: %s", __FUNCTION__, request, strerror(errno));
        ret = -errno;
    }

    return ret;
}

/**
 * Waits for frame data or read end of pipe device to be available
 *
 * \param timeout time to wait for data (in ms), timeout of -1
 *        means to wait indefinitely for data.
 * \param pipeReturn[OUT] flag to mark if pollDevices() returns from pipe device.
 * \param pipeFd stands for the file descriptor of pipe read end,
 *        pipeFd of -1 means only poll v4l2 device.
 *
 * \return 0: timeout, -1: error happened, positive number: success
 */
int V4L2Device::pollDevices(int timeout, bool &pipeReturn, int pipeFd)
{
    LOG2("@%s", __FUNCTION__);
    //Tweaks to make KW happy.
    uint32_t totalNumFds = 2;
    if (pipeFd == -1)  totalNumFds -= 1;
    struct pollfd pollFds[totalNumFds];
    int ret(0);
    if (mFd == -1) {
        LOG1("Device %s already closed. Do nothing.", mName.string());
        return -1;
    }
    pipeReturn = false;
    pollFds[0].fd = mFd;
    pollFds[0].events = POLLPRI | POLLIN | POLLERR;
    pollFds[0].revents = 0;
    if (pipeFd != -1) {
        pollFds[totalNumFds-1].fd = pipeFd;
        pollFds[totalNumFds-1].events = POLLPRI | POLLIN;
        pollFds[totalNumFds-1].revents = 0;
    }

    ret = ::poll(pollFds, totalNumFds, timeout);

    if (pipeFd != -1) {
        if (pollFds[totalNumFds-1].revents & POLLIN || pollFds[totalNumFds-1].revents & POLLPRI) {
            pipeReturn = true;
            LOG1("%s: Poll returning from flush", __FUNCTION__);
        }
    }

    return ret;
}

/**
 * Waits for frame data to be available
 *
 * \param timeout time to wait for data (in ms), timeout of -1
 *        means to wait indefinitely for data
 *
 * \return 0: timeout, -1: error happened, positive number: success
 */
int V4L2Device::poll(int timeout)
{
    LOG2("@%s", __FUNCTION__);
    struct pollfd pfd = {0,0,0};
    int ret(0);

    if (mFd == -1) {
        LOG1("Device %s already closed. Do nothing.", mName.string());
        return -1;
    }

    pfd.fd = mFd;
    pfd.events = POLLPRI | POLLIN | POLLERR;

    ret = ::poll(&pfd, 1, timeout);

    if (pfd.revents & POLLERR) {
        LOG1("%s received POLLERR", __FUNCTION__);
        return -1;
    }

    return ret;
}

int V4L2Device::subscribeEvent(int event, int id)
{
    LOG1("@%s event %x:", __FUNCTION__, event);
    int ret(0);
    struct v4l2_event_subscription sub;

    if (mFd == -1) {
        LOG1("Device %s already closed. cannot subscribe.", mName.string());
        return -1;
    }

    CLEAR(sub);
    sub.type = event;
    sub.id = id;

    ret = ioctl(mFd, VIDIOC_SUBSCRIBE_EVENT, &sub);
    if (ret < 0) {
        ALOGE("error subscribing event %x: %s", event, strerror(errno));
        return ret;
    }

    return ret;
}

int V4L2Device::unsubscribeEvent(int event, int id)
{
    LOG1("@%s", __FUNCTION__);
    int ret(0);
    struct v4l2_event_subscription sub;

    if (mFd == -1) {
        LOG1("Device %s closed. cannot unsubscribe.", mName.string());
        return -1;
    }

    CLEAR(sub);
    sub.type = event;
    sub.id = id;

    ret = ioctl(mFd, VIDIOC_UNSUBSCRIBE_EVENT, &sub);
    if (ret < 0) {
        ALOGE("error unsubscribing event %x :%s",event,strerror(errno));
        return ret;
    }

    return ret;
}

int V4L2Device::dequeueEvent(struct v4l2_event *event)
{
    LOG2("@%s", __FUNCTION__);
    int ret(0);

    if (mFd == -1) {
        LOG1("Device %s closed. cannot dequeue event.", mName.string());
        return -1;
    }

    ret = ioctl(mFd, VIDIOC_DQEVENT, event);
    if (ret < 0) {
        ALOGE("error dequeuing event");
        return ret;
    }

    return ret;
}

status_t V4L2Device::setControl (int aControlNum, const int value, const char *name)
{
    LOG2("@%s", __FUNCTION__);

    struct v4l2_control control;
    struct v4l2_ext_controls controls;
    struct v4l2_ext_control ext_control;

    CLEAR(control);
    CLEAR(controls);
    CLEAR(ext_control);

    LOG1("setting attribute [%s] to %d", name, value);

    if (mFd == -1) {
        ALOGE("Invalid state (CLOSED) to set an attribute");
        return UNKNOWN_ERROR;
    }

    control.id = aControlNum;
    control.value = value;
    controls.ctrl_class = V4L2_CTRL_ID2CLASS(control.id);
    controls.count = 1;
    controls.controls = &ext_control;
    ext_control.id = aControlNum;
    ext_control.value = value;

    if (ioctl(mFd, VIDIOC_S_EXT_CTRLS, &controls) == 0)
        return NO_ERROR;

    if (ioctl(mFd, VIDIOC_S_CTRL, &control) == 0)
        return NO_ERROR;

    ALOGE("Failed to set value %d for control %s (%d) on device '%s', %s",
        value, name, aControlNum, mName.string(), strerror(errno));

    return UNKNOWN_ERROR;
}

status_t V4L2Device::getControl (int aControlNum, int *value)
{
    LOG2("@%s", __FUNCTION__);

    struct v4l2_control control;
    struct v4l2_ext_controls controls;
    struct v4l2_ext_control ext_control;

    CLEAR(control);
    CLEAR(controls);
    CLEAR(ext_control);

    if (mFd == -1) {
        ALOGE("Invalid state (CLOSED) to set an attribute");
        return UNKNOWN_ERROR;
    }

    control.id = aControlNum;
    controls.ctrl_class = V4L2_CTRL_ID2CLASS(control.id);
    controls.count = 1;
    controls.controls = &ext_control;
    ext_control.id = aControlNum;

    if (ioctl(mFd, VIDIOC_G_EXT_CTRLS, &controls) == 0) {
       *value = ext_control.value;
       return NO_ERROR;
    }

    if (ioctl(mFd, VIDIOC_G_CTRL, &control) == 0) {
       *value = control.value;
       return NO_ERROR;
    }

    ALOGE("Failed to get value for control (%d) on device '%s', %s",
            aControlNum, mName.string(), strerror(errno));
    return UNKNOWN_ERROR;
}

} // namespace android
