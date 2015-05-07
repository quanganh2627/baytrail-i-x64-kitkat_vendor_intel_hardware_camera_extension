LOCAL_PATH := $(call my-dir)

####################### Copy headers ######################
include $(CLEAR_VARS)
LOCAL_COPY_HEADERS := acc.h
LOCAL_COPY_HEADERS_TO := acc
include $(BUILD_COPY_HEADERS)

####################### Make shared lib ###################
include $(CLEAR_VARS)
LOCAL_SRC_FILES := acc_android.cpp \
                   v4l2device.cpp \
                   event_thread.cpp \
                   ../common/LogHelper.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../common
LOCAL_STATIC_LIBRARIES :=
LOCAL_SHARED_LIBRARIES += libutils libcutils liblog
LOCAL_CFLAGS += -Wunused-variable -Werror
LOCAL_MODULE := libacc
LOCAL_MODULE_OWNER := intel
LOCAL_MULTILIB := 32
#LOCAL_MODULE_PATH := $(TARGET_OUT_SHARED_LIBRARIES)
LOCAL_MODULE_TAGS := optional
include $(BUILD_SHARED_LIBRARY)

####################### Make static lib ###################
include $(CLEAR_VARS)
LOCAL_SRC_FILES := acc_android.cpp \
                   v4l2device.cpp \
                   event_thread.cpp \
                   ../common/LogHelper.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../common
LOCAL_STATIC_LIBRARIES :=
LOCAL_SHARED_LIBRARIES :=
LOCAL_CFLAGS += -Wunused-variable -Werror
LOCAL_MODULE := libacc
LOCAL_MODULE_PATH := $(TARGET_OUT_SHARED_LIBRARIES)
LOCAL_MODULE_TAGS := optional
include $(BUILD_STATIC_LIBRARY)
