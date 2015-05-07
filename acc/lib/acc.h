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
#ifndef _LIB_ACC_H_
#define _LIB_ACC_H_

#include <utils/Mutex.h>
#include <utils/Condition.h>
#include <utils/KeyedVector.h>
#include <utils/RefBase.h>

namespace android {

class EventThread;

/* Buffer shared between CPU and CSS.
   Depending on what the platform supports, this is implemented with
   shared physical memory (SPM) or by a shadow buffer and read/writes
   to sync. */

typedef struct {
    void    *cpu_ptr;
    uint32_t css_ptr;
    int32_t  size;
    int32_t  own_cpu_ptr; // indicate whether cpu_ptr was allocated by us or not
} acc_buf;

enum AccelerationFwDst {
    CAPTURE_OUTPUT,
    CAPTURE_VFPP,
    PREVIEW_VFPP,
    ACC_QOS
};

struct EventWaiter {
        Condition mWaitCond;
        Mutex mWaitLock;
        unsigned int handle;
        bool enabled;
};

class AccControl {

public:
    AccControl();
    AccControl(char* accDevice);
    ~AccControl();

private:
    // ACC event polling thread
    sp<EventThread> mEventThread;
    /********************************************************
     * ACC interface internal data.
     ********************************************************/
    Mutex mACCEventSystemLock;
    KeyedVector<unsigned int, EventWaiter *> mAllWaiters;
    KeyedVector<unsigned int, EventWaiter *> mSleepingWaiters;
    KeyedVector<unsigned int, unsigned int>  mUpdates;

    static const unsigned int MAX_NUMBER_PENDING_UPDATES = 20;
    const char* mAccDevice;

    int mCssMajorVersion;
    int mCssMinorVersion;
    int mIspHwMajorVersion;
    int mIspHwMinorVersion;

    int mAccDeviceHandle;

private:
    /***********************************************************
     * ACC internel misc utility functions.
     **********************************************************/
    int get_file_size(FILE *file);
    int _xioctl(int request, void *arg, const char*);
    /**************************************************************************
     * ACC internal functions to support async stage wait update.
     **************************************************************************/
    void dropACCStageUpdates(unsigned int handle);
    void dropACCStageUpdatesLocked(unsigned int handle);
    int newAccPipeFw(unsigned int handle);

public:
    /* Allocate aligned memory, can be mapped into ISP address space */
    void* acc_alloc(size_t size);

    void acc_free(void *ptr);

    uint32_t acc_css_alloc(size_t size);

    void acc_css_free(uint32_t css_ptr);

    void acc_css_load(uint32_t css_ptr, void *dst_ptr, size_t size);

    void acc_css_store(uint32_t css_ptr, void *src_ptr, size_t size);

    acc_buf* acc_buf_alloc(size_t size);

    /* Create buf and use cpu_ptr from argument rather than allocating
       one. */
    acc_buf* acc_buf_create(void *cpu_ptr, size_t size);

    void acc_buf_free(acc_buf *buf);

    const KeyedVector<unsigned int, EventWaiter *> getAllWaiters();

    /* Buffer sychronization. These functions make sure buffers are properly
       synced before being passed from CPU to CSS or vice versa.
       The implementation of these functions depends on the platform. Platforms
       that do not support PSM will copy into or from the shadow buffers,
       platforms that do support PSM AND use cached CPU memory will perform
       a cache flush. For platform with PSM support and uncached memory these
       functions may be empty. */
    void acc_buf_sync_to_css(acc_buf *buf);

    void acc_buf_sync_to_cpu(acc_buf *buf);


    /***********************************************************
     * ACC refactored operation interface
     ***********************************************************/

    void fetch_isp_versions();
    int get_isp_hw_major_version();
    int get_isp_hw_minor_version();
    int get_css_major_version();
    int get_css_minor_version();

    int acc_init();
    void acc_deinit(int fd);
    void * acc_open_fw (const char *fw_path, unsigned *size);
    int acc_load_fw(void *fw, unsigned size, unsigned *handle);
    int acc_unload_fw(unsigned handle);
    int acc_load_ex_fw(void *fw, unsigned size, unsigned *handle);
    int acc_unload_ex_fw(unsigned handle);
    int acc_load_fw_pipe(void *fw, unsigned size, unsigned *handle, int destination);
    int acc_map(void *ptr, size_t size, unsigned long *ispPtr);
    int acc_unmap(unsigned long ispPtr, size_t size);
    int acc_set_mapped_arg(unsigned handle, unsigned mem, unsigned long val,
                               size_t size);
    int acc_start_fw(unsigned handle);
    int acc_wait_fw(unsigned handle);
    int acc_abort_fw(unsigned handle, unsigned timeout);
    int acc_set_fw_arg(unsigned handle, unsigned num, void *val, size_t size);
    int acc_set_stage_state(unsigned int handle, bool enable);
    int acc_wait_stage_update(unsigned int handle);
    int acc_stage_update(unsigned int handle);
};

}

#endif /* _LIB_ACC_H_ */
