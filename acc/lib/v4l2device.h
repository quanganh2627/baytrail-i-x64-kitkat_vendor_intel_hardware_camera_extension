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

#ifndef _ACC_V4L2DEVICE_H_
#define _ACC_V4L2DEVICE_H_

#include <utils/RefBase.h>
#include <utils/String8.h>
#include <linux/atomisp.h>
#include <linux/videodev2.h>

namespace android {

/**
 * A class encapsulating simple V4L2 device operations
 *
 * Base class that contains common V4L2 operations used by video nodes and
 * subdevices. It provides a slightly higher interface than IOCTLS to access
 * the devices. It also stores:
 * - State of the device to protect from invalid usage sequence
 * - Name
 * - File descriptor
 */
class V4L2Device: public RefBase {
public:
    V4L2Device(const char *name);
    virtual ~V4L2Device();

    virtual status_t open();
    virtual status_t close();

    virtual int xioctl(int request, void *arg) const;
    virtual int poll(int timeout);
    /*
     * poll v4l2 device and pipe device, use pipe device to speed up poll()
     * return when switching camera
     */
    virtual int pollDevices(int timeout,  bool &pipeReturn, int pipeFd);

    virtual status_t setControl (int aControlNum, const int value, const char *name);
    virtual status_t getControl (int aControlNum, int *value);

    virtual int subscribeEvent(int event, int id = 0);
    virtual int unsubscribeEvent(int event, int id = 0);
    virtual int dequeueEvent(struct v4l2_event *event);

    bool isOpen() { return mFd != -1; };

protected:
    String8      mName;     /*!< path to device in file system, ex: /dev/video0 */
    int          mFd;       /*!< file descriptor obtained when device is open */

};


} /* namespace android */
#endif /* _ACC_V4L2DEVICE_H_ */
