
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_COPY_HEADERS := \
    AccService.h \
    IAccService.h \
    AccClient.h
LOCAL_COPY_HEADERS_TO := acc
LOCAL_C_INCLUDES := $(TARGET_OUT_HEADERS)/acc \
                    $(TARGET_OUT_HEADERS)/libmfldadvci
# acc service
LOCAL_SRC_FILES += \
     AccService.cpp \
     IAccService.cpp

LOCAL_MULTILIB := 32
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := libacc_service

LOCAL_SHARED_LIBRARIES := \
    libcutils \
    libutils \
    libbinder \
    libiacp \
    libacc
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_C_INCLUDES := $(TARGET_OUT_HEADERS)/acc \
                    $(TARGET_OUT_HEADERS)/libmfldadvci
# acc client
LOCAL_SRC_FILES += \
     IAccService.cpp \
     AccClient.cpp

LOCAL_MULTILIB := both
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := libacc_client

LOCAL_SHARED_LIBRARIES := \
    libcutils \
    libutils \
    libbinder
include $(BUILD_SHARED_LIBRARY)
