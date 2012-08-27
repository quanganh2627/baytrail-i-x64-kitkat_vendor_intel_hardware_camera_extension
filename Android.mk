LOCAL_PATH:= $(call my-dir)

################# MAKE_LIB ############################
include $(CLEAR_VARS)
LOCAL_MODULE := libintelcamera_jni
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES:= \
	jni/com_intel_camera_extensions_IntelCamera.cpp
LOCAL_SHARED_LIBRARIES := \
	libandroid_runtime \
	libnativehelper \
	libutils \
	libcamera_client
LOCAL_C_INCLUDES += \
	$(JNI_H_INCLUDES) \
	$(LOCAL_PATH)/include \
	frameworks/base/core/jni
include $(BUILD_SHARED_LIBRARY)

################# MAKE_XML ############################
include $(CLEAR_VARS)
LOCAL_MODULE := com.intel.camera.extensions.xml
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_OWNER := intel
# This will install the file in /system/etc/permissions
LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/permissions
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)

################# MAKE_JAR ############################
include $(CLEAR_VARS)
LOCAL_MODULE := com.intel.camera.extensions
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := \
	com/intel/camera/extensions/IntelCamera.java
include $(BUILD_JAVA_LIBRARY)

################# COPY_HEADERS #######################
include $(CLEAR_VARS)
LOCAL_COPY_HEADERS_TO := cameralibs
LOCAL_COPY_HEADERS := \
	include/intel_camera_extensions.h
include $(BUILD_COPY_HEADERS)

################# burst capture sound ################
include $(CLEAR_VARS)
LOCAL_MODULE := fast_click.pcm
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_OWNER := intel
# This will install the file in /system/media/audio/ui
LOCAL_MODULE_PATH := $(TARGET_OUT)/media/audio/ui
LOCAL_SRC_FILES := data/sounds/effects/$(LOCAL_MODULE)
include $(BUILD_PREBUILT)
