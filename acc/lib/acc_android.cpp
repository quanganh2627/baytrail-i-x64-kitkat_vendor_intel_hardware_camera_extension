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
#define LOG_TAG "CameraAccLib"
#include <stdio.h>
#include <stdlib.h>
#include<stdbool.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <errno.h>
#include <unistd.h>
#include <stdint.h>
#include <sys/ioctl.h>

#include <linux/videodev2.h>
#include <linux/atomisp.h>
#include <linux/media.h>

#include "acc.h"
#include "LogHelper.h"

namespace android {

AccControl::AccControl() :
    mAccDevice("/dev/video4"),
    mCssMajorVersion(-1),
    mCssMinorVersion(-1),
    mIspHwMajorVersion(-1),
    mIspHwMinorVersion(-1),
    mAccDeviceHandle(-1)
{
    LogHelper::setDebugLevel();
    LOG2("AccControl instantiated on device: %s", mAccDevice);
}

AccControl::AccControl(char* accDevice) :
    mAccDevice(accDevice),
    mCssMajorVersion(-1),
    mCssMinorVersion(-1),
    mIspHwMajorVersion(-1),
    mIspHwMinorVersion(-1),
    mAccDeviceHandle(-1)
{
    LogHelper::setDebugLevel();
    LOG2("AccControl instantiated on device: %s", mAccDevice);
}

AccControl::~AccControl()
{
    LOG2("AccControl for device %s get destroyed", mAccDevice);
}


/***********************************************************
 * ACC internel misc utility functions.
 **********************************************************/
int AccControl::get_file_size(FILE *file)
{
    int len;

    if (file == NULL) return 0;

    if (fseek(file, 0, SEEK_END)) return 0;

    len = ftell(file);

    if (fseek(file, 0, SEEK_SET)) return 0;

    return len;
}

int AccControl::_xioctl(int request, void *arg, const char* req_name)
{
    int ret;

    if (mAccDeviceHandle == -1) {
        ALOGE("acc: device not opened\n");
        return -1;
    }

    LOG2("xioctl: request = %s\n", req_name);
    do {
        if (request == ATOMISP_IOC_ACC_START)
            LOG2 ("At %s(%d)\n", __FUNCTION__, __LINE__);
        ret = ioctl (mAccDeviceHandle, request, arg);
    } while (-1 == ret && EINTR == errno);

    if (ret < 0)
        ALOGE("%s: Request %s failed: %s\n", __FUNCTION__, req_name, strerror(errno));

    return ret;
}

/*********************************************************************
 * ACC memory related utils.
 *********************************************************************/

void* AccControl::acc_alloc(size_t size)
{
#if defined(_WIN32)
    return _aligned_malloc(size, 4096); // FIXME: properly get the page size
#else
    void *ptr;
    posix_memalign(&ptr, sysconf(_SC_PAGESIZE), size);
    return ptr;
#endif
}

void AccControl::acc_free(void *ptr)
{
    if (ptr) {
        free(ptr);
    }
}


uint32_t AccControl::acc_css_alloc(size_t size)
{
    (void)size;
    ALOGW("use alloc+map instead\n");
    return 0;
}

void AccControl::acc_css_free(uint32_t css_ptr)
{
    (void)css_ptr;
    ALOGW("use unmap+fre instead\n");
}

acc_buf* AccControl::acc_buf_create(void *cpu_ptr, size_t size)
{
    acc_buf *me = (acc_buf*)malloc(sizeof(*me));
    if (!me)
        return me;
    me->size = size;
    me->cpu_ptr = cpu_ptr;
    me->own_cpu_ptr = 0;
    acc_map(me->cpu_ptr, size, (unsigned long*)&me->css_ptr);
    return me;
}

acc_buf* AccControl::acc_buf_alloc(size_t size)
{
    acc_buf *me = (acc_buf*)malloc(sizeof(*me));
    if (!me)
        return me;
    me->size = size;
    me->cpu_ptr = acc_alloc(size);
    me->own_cpu_ptr = 1;
    acc_map(me->cpu_ptr, size, (unsigned long*)&me->css_ptr);
    return me;
}

void AccControl::acc_buf_free(acc_buf *buf)
{
    if (buf) {
        acc_unmap(buf->css_ptr, buf->size);
        if (buf->own_cpu_ptr)
            acc_free(buf->cpu_ptr);
        free(buf);
    }
}

void AccControl::acc_buf_sync_to_css(acc_buf *buf)
{
    // Android uses uncached pages, nothing to do
    (void)buf;
}

void AccControl::acc_buf_sync_to_cpu(acc_buf *buf)
{
    // Android uses uncached pages, nothing to do
    (void)buf;
}

/**************************************************************************
 * ACC internal functions to support async stage wait update.
 **************************************************************************/

// Drops pending stage updates for the given fw handle. Caller must take
// the mACCEventSystemLock itself!
void AccControl::dropACCStageUpdates(unsigned int handle)
{
    LOG2("@ %s handle: %d\n",__FUNCTION__, handle);
    int index = -1;
    do {
        index = mUpdates.indexOfKey(handle);
        if (index >= 0)
            mUpdates.removeItemsAt(index, 1);
    } while (index >= 0);
}

// Drops pending stage updates for the given fw handle. Takes the
// mACCEventSystemLock lock.
void AccControl::dropACCStageUpdatesLocked(unsigned int handle)
{
    LOG2("@ %s handle: %d\n", __FUNCTION__, handle);
    Mutex::Autolock lock(mACCEventSystemLock);
    dropACCStageUpdates(handle);
}

int AccControl::newAccPipeFw(unsigned int handle)
{
    LOG2("@%s: handle: %x", __FUNCTION__, handle);

    EventWaiter *waiter = new EventWaiter();

    Mutex::Autolock lock(mACCEventSystemLock);
    if (waiter) {// for code analyzers
        waiter->handle = handle;
        mAllWaiters.add(handle, waiter);
    } else {
        ALOGE("Not enough memory to allocate event waiter");
        return -1;
    }

    return 0;
}

/************************************************************
 * ACC newly refactored operation interface
 * **********************************************************/

void AccControl::fetch_isp_versions()
{
    LOG2("@%s", __FUNCTION__);

    mCssMajorVersion = -1;
    mCssMinorVersion = -1;
    mIspHwMajorVersion = -1;
    mIspHwMinorVersion = -1;

    // Sensor drivers have been registered to media controller
    const char *mcPathName = "/dev/media0";
    int fd = open(mcPathName, O_RDONLY);
    if (fd == -1) {
        ALOGE("Error in opening media controller: %s!", strerror(errno));
        return;
    } else {
        struct media_device_info info;
        memset(&info, 0, sizeof(info));

        if (ioctl(fd, MEDIA_IOC_DEVICE_INFO, &info) < 0) {
            ALOGE("Error in getting media controller info: %s", strerror(errno));
            close(fd);
            return;
        } else {
            int hw_version = (info.hw_revision & ATOMISP_HW_REVISION_MASK) >> ATOMISP_HW_REVISION_SHIFT;
            int hw_stepping = info.hw_revision & ATOMISP_HW_STEPPING_MASK;
            int css_version = info.driver_version & ATOMISP_CSS_VERSION_MASK;

            switch(hw_version) {
                case ATOMISP_HW_REVISION_ISP2300:
                    mIspHwMajorVersion = 23;
                    break;
                case ATOMISP_HW_REVISION_ISP2400:
                case ATOMISP_HW_REVISION_ISP2401_LEGACY:
                    mIspHwMajorVersion = 24;
                    break;
                case ATOMISP_HW_REVISION_ISP2401:
                    mIspHwMajorVersion = 2401;
                    break;
                default:
                    ALOGE("Unknown ISP HW version: %d", hw_version);
            }
            if (mIspHwMajorVersion > 0)
                LOG2("ISP HW version is: %d", mIspHwMajorVersion);

            switch(hw_stepping) {
                case ATOMISP_HW_STEPPING_A0:
                    mIspHwMinorVersion = 0;
                    break;
                case ATOMISP_HW_STEPPING_B0:
                    mIspHwMinorVersion = 1;
                    break;
                default:
                    ALOGE("Unknown ISP HW stepping: %d", hw_stepping);
            }
            if (mIspHwMinorVersion > 0)
                LOG2("ISP HW stepping is: %d", mIspHwMinorVersion);

            switch(css_version) {
                case ATOMISP_CSS_VERSION_15:
                    mCssMajorVersion = 1;
                    mCssMinorVersion = 5;
                    break;
                case ATOMISP_CSS_VERSION_20:
                    mCssMajorVersion = 2;
                    mCssMinorVersion = 0;
                    break;
                case ATOMISP_CSS_VERSION_21:
                    mCssMajorVersion = 2;
                    mCssMinorVersion = 1;
                    break;
                default:
                    ALOGE("Unknown CSS version: %d", css_version);
            }
            if (mCssMajorVersion > 0)
                LOG2("CSS version is: %d.%d", mCssMajorVersion, mCssMinorVersion);
        }
        close(fd);
    }
}

int AccControl::get_isp_hw_major_version()
{
    if (mIspHwMajorVersion == -1){
        fetch_isp_versions();
    }
    return mIspHwMajorVersion;
}

int AccControl::get_isp_hw_minor_version()
{
    if (mIspHwMinorVersion == -1){
        fetch_isp_versions();
    }
    return mIspHwMinorVersion;
}

int AccControl::get_css_major_version()
{
    if (mCssMajorVersion == -1){
        fetch_isp_versions();
    }
    return mCssMajorVersion;
}

int AccControl::get_css_minor_version()
{
    if (mCssMinorVersion == -1){
        fetch_isp_versions();
    }
    return mCssMinorVersion;
}

int AccControl::acc_init()
{
    if (mAccDeviceHandle == -1){
        mAccDeviceHandle = open(mAccDevice, O_RDWR);
        if (mAccDeviceHandle <= 0) {
            ALOGE("Error opening ACC device (%s)\n", strerror(errno));
            mAccDeviceHandle = -1;
            return -1;
        }
    }
    LOG2 ("Opened ACC Device %s\n", mAccDevice);

    fetch_isp_versions();
    return mAccDeviceHandle;
}

void AccControl::acc_deinit(int fd)
{
    if (fd >= 0) {
        close(fd);
    }
}

void * AccControl::acc_open_fw(const char *fw_path, unsigned *size)
{
    FILE *file;
    unsigned len;
    void *fw;

    if (!fw_path)
        return NULL;

    file = fopen(fw_path, "rb");
    if (!file)
        return NULL;

    len = get_file_size(file);

    if (!len) {
        fclose(file);
        return NULL;
    }

    fw = malloc(len);
    if (!fw) {
        fclose(file);
        return NULL;
    }

    if (fread(fw, 1, len, file) != len) {
        fclose(file);
        free(fw);
        return NULL;
    }

    *size = len;

    fclose(file);

    return fw;
}

int AccControl::acc_load_fw(void *fw, unsigned size, unsigned *handle)
{
    int ret;

    struct atomisp_acc_fw_load fwData;
    fwData.size      = size;
    fwData.fw_handle = 0;
    fwData.data      = fw;

    ret = xioctl(ATOMISP_IOC_ACC_LOAD, &fwData);

    //If IOCTRL call was returned successfully, get the firmware handle
    //from the structure and return it to the application.
    if (!ret)
        *handle = fwData.fw_handle;

    LOG2("%s: Loaded the ACC firmware handle = %d\n", __func__, *handle);
    return ret;
}

int AccControl::acc_unload_fw(unsigned handle)
{
    return xioctl(ATOMISP_IOC_ACC_UNLOAD, &handle);
}

int AccControl::acc_load_ex_fw(void *fw, unsigned size, unsigned *handle)
{
    return acc_load_fw_pipe(fw, size, handle, PREVIEW_VFPP);
}

int AccControl::acc_unload_ex_fw(unsigned handle)
{
    LOG2("@ %s handle: %d\n",__FUNCTION__, handle);
    int ret = -1;

    // remove acc event waiter for the stage, if it exists
    Mutex::Autolock lock(mACCEventSystemLock);
    int index = mAllWaiters.indexOfKey(handle);
    if (index >= 0) {
        EventWaiter *waiter = mAllWaiters.editValueAt(index);

        mAllWaiters.removeItem(handle);
        delete waiter;
        waiter = NULL;
        dropACCStageUpdates(handle);
    }

    ret = xioctl(ATOMISP_IOC_ACC_UNLOAD, &handle);
    LOG2("%s IOCTL ATOMISP_IOC_ACC_UNLOAD ret: %d \n", __FUNCTION__,ret);

    return ret;
}

int AccControl::acc_load_fw_pipe(void *fw, unsigned size, unsigned *handle, int destination)
{
    LOG2("@%s", __FUNCTION__);
    int ret = -1;

    struct atomisp_acc_fw_load_to_pipe fwDataPipe;
    memset(&fwDataPipe, 0, sizeof(fwDataPipe));

    switch(destination) {
        case CAPTURE_OUTPUT:
            fwDataPipe.flags = ATOMISP_ACC_FW_LOAD_FL_CAPTURE;
            fwDataPipe.type = ATOMISP_ACC_FW_LOAD_TYPE_OUTPUT;
            break;
        case CAPTURE_VFPP:
            fwDataPipe.flags = ATOMISP_ACC_FW_LOAD_FL_CAPTURE;
            fwDataPipe.type = ATOMISP_ACC_FW_LOAD_TYPE_VIEWFINDER;
            break;
        case PREVIEW_VFPP:
            fwDataPipe.flags = ATOMISP_ACC_FW_LOAD_FL_PREVIEW;
            fwDataPipe.type = ATOMISP_ACC_FW_LOAD_TYPE_VIEWFINDER;
            break;
        case ACC_QOS:
            fwDataPipe.flags = ATOMISP_ACC_FW_LOAD_FL_ACC;
            fwDataPipe.type = ATOMISP_ACC_FW_LOAD_TYPE_VIEWFINDER;
            break;
        default:
            ALOGE("@%s: Invalid acc destination", __FUNCTION__);
            return -1;
            break;
    }

    /*  fwDataPipe.fw_handle filled by kernel and returned to caller */
    fwDataPipe.size = size;
    fwDataPipe.data = fw;

    ret = xioctl(ATOMISP_IOC_ACC_LOAD_TO_PIPE, &fwDataPipe);
    LOG2("%s IOCTL ATOMISP_IOC_ACC_LOAD_TO_PIPE ret: %d fwDataPipe->fw_handle: %d "
         "flags: %d type: %d", __FUNCTION__, ret, fwDataPipe.fw_handle,
         fwDataPipe.flags, fwDataPipe.type);

    //If IOCTL call was returned successfully, get the firmware handle
    //from the structure and return it to the application.
    if(!ret){
        *handle = fwDataPipe.fw_handle;
        LOG2("%s IOCTL Call returned : %d Handle: %ud",
                __FUNCTION__, ret, *handle );

        if (fwDataPipe.flags == ATOMISP_ACC_FW_LOAD_FL_ACC) {
            ret = newAccPipeFw(fwDataPipe.fw_handle);
        }
    }

    return ret;
}

int AccControl::acc_map(void *user_ptr, size_t size, unsigned long *css_ptr)
{
    int ret;
    struct atomisp_acc_map map;

    LOG2 ("%s: Called for user_ptr %p\n", __func__, user_ptr);
    memset(&map, 0, sizeof(map));

    map.length = size;
    map.user_ptr = user_ptr;

    ret = xioctl(ATOMISP_IOC_ACC_MAP, &map);

    *css_ptr = map.css_ptr;
    if (ret == 0)
      LOG2 ("%s: user_ptr %p mapped to %p\n", __func__, user_ptr, (void *)css_ptr);
    return ret;
}

int AccControl::acc_unmap(unsigned long css_ptr, size_t size)
{
    struct atomisp_acc_map map;

    memset(&map, 0, sizeof(map));

    map.css_ptr = css_ptr;
    map.length = size;

    return xioctl(ATOMISP_IOC_ACC_UNMAP, &map);
}

int AccControl::acc_set_mapped_arg(unsigned handle, unsigned mem, unsigned long css_ptr, size_t size)
{
    struct atomisp_acc_s_mapped_arg arg;

    memset(&arg, 0, sizeof(arg));

    arg.fw_handle = handle;
    arg.memory = mem;
    arg.css_ptr = css_ptr;
    arg.length = size;

    return xioctl(ATOMISP_IOC_ACC_S_MAPPED_ARG, &arg);
}

int AccControl::acc_start_fw(unsigned handle)
{
    LOG2 ("At %s(%d)\n", __FUNCTION__, __LINE__);
    return xioctl(ATOMISP_IOC_ACC_START, &handle);
}

int AccControl::acc_wait_fw(unsigned handle)
{
    LOG2 ("At %s(%d)\n", __FUNCTION__, __LINE__);
    return xioctl(ATOMISP_IOC_ACC_WAIT, &handle);
}

int AccControl::acc_abort_fw(unsigned handle, unsigned timeout)
{
    int ret;
    struct atomisp_acc_fw_abort abort;

    abort.fw_handle = handle;
    abort.timeout = timeout;

    ret = xioctl(ATOMISP_IOC_ACC_ABORT, &abort);
    LOG2("%s IOCTL ATOMISP_IOC_ACC_ABORT ret: %d\n", __FUNCTION__, ret);
    return ret;
}

int AccControl::acc_set_fw_arg(unsigned handle, unsigned num, void *val, size_t size)
{
    LOG2("@ %s handle:%d\n", __FUNCTION__, handle);
    int ret = -1;

    struct atomisp_acc_fw_arg arg;
    arg.fw_handle = handle;
    arg.index = num;
    arg.value = val;
    arg.size = size;

    ret = xioctl(ATOMISP_IOC_ACC_S_ARG, &arg);
    LOG2("%s IOCTL ATOMISP_IOC_ACC_S_ARG ret: %d \n", __FUNCTION__, ret);

    return ret;
}

int AccControl::acc_set_stage_state(unsigned int handle, bool enable)
{
    LOG2("@%s", __FUNCTION__);

    // set the stage struct internal state first
    Mutex::Autolock lock(mACCEventSystemLock);
    int index = mAllWaiters.indexOfKey(handle);
    if (index >= 0) {
        EventWaiter *waiter = mAllWaiters.valueAt(index);
        if (waiter) {// to keep static code analyzers happy
            waiter->enabled = enable;
        }
    } else {
        ALOGE("Bad ACC handle given!");
        return BAD_VALUE;
    }

    atomisp_acc_state accState;
    accState.fw_handle = handle;
    accState.flags = enable ? ATOMISP_STATE_FLAG_ENABLE : 0;
    int ret = xioctl(ATOMISP_IOC_S_ACC_STATE, &accState);
    LOG2("@%s: ATOMISP_IOC_S_ACC_STATE handle:%x, enable:%d ret:%d",
         __FUNCTION__, handle, enable, ret);

    if (!enable)
        dropACCStageUpdates(handle);

    return ret;
}

int AccControl::acc_wait_stage_update(unsigned int handle)
{
    LOG2("@%s: ATOMISP_IOC_S_ACC_STATE handle: %x", __FUNCTION__, handle);
    status_t status = OK;

    mACCEventSystemLock.lock(); // this must be held while using the vectors
    int updateIndex = mUpdates.indexOfKey(handle);
    if (updateIndex >= 0) {
        // found pending update, remove it from vector, then unlock and return
        mUpdates.removeItemsAt(updateIndex, 1);
    } else {
        // find the waiter object and sleep against its condition
        int waiterIndex = mAllWaiters.indexOfKey(handle);
        if (waiterIndex >= 0) {
            EventWaiter *waiter = mAllWaiters.valueAt(waiterIndex);
            if (waiter) { // to keep static code analyzers happy
                // add this waiter object to sleeping waiter vector
                mSleepingWaiters.add(handle, waiter);
                waiter->mWaitLock.lock(); // to ensure no signals are too early
                mACCEventSystemLock.unlock(); // can't hold this during the cond wait
                waiter->mWaitCond.wait(waiter->mWaitLock);
                mACCEventSystemLock.lock(); // remove the sleeper from vector
                mSleepingWaiters.removeItem(handle);
                mACCEventSystemLock.unlock();
                waiter->mWaitLock.unlock();
                return OK;
            }
        } else {
            ALOGE("Unknown waiter handle %x given", handle);
            status = BAD_VALUE;
        }
    }
    mACCEventSystemLock.unlock();
    return status;
}

int AccControl::acc_stage_update(unsigned int handle)
{
    LOG2("@%s: update handle: %x", __FUNCTION__, handle);

    Mutex::Autolock lock(mACCEventSystemLock);

    // first check if stage is disabled, drop the update if it is
    int index = mAllWaiters.indexOfKey(handle);
    if (index >= 0) {
        EventWaiter *waiter = mAllWaiters.valueAt(index);
        if (waiter) {// to keep static code analyzers happy
            if (!waiter->enabled) {
                ALOGW("Stage update happened for a disabled stage. Dropping it, maybe scheduling was unlucky.");
                return OK; // drop the update
            }
        }
    } else {
        ALOGE("Unknown stage!");
        return BAD_VALUE;
    }

    index = mSleepingWaiters.indexOfKey(handle);
    if (index >= 0) {
        EventWaiter *waiter = mSleepingWaiters.valueAt(index);
        if (waiter) {// to keep static code analyzers happy
            Mutex::Autolock waiterLock(waiter->mWaitLock); // to ensure no signals are too early
            waiter->mWaitCond.signal(); // wake up the sleeping thread
        }
    } else {
        mUpdates.add(handle, handle);
        if (mUpdates.size() > MAX_NUMBER_PENDING_UPDATES) {
            ALOGW("Pending acc stage updates now already: %d", mUpdates.size());
        }
    }
    return OK;
}

}
