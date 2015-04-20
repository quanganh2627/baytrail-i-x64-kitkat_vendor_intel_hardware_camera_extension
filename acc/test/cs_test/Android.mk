LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SHARED_LIBRARIES := \
    libcutils \
    libutils \
    libbinder \
    libacc_client
LOCAL_C_INCLUDES := $(TARGET_OUT_HEADERS)/acc
LOCAL_MODULE    := TestClientService
LOCAL_SRC_FILES := \
    TestClientService.cpp
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)
