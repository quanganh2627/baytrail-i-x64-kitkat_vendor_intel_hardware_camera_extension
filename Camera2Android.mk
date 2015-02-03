LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)


LOCAL_SRC_FILES:= \
    libdepthimageutils/Devel/Source/DSAPI/Implementations/DSConfig.cpp \
    libdepthimageutils/Devel/Source/DSAPI/Implementations/DSResolutionMode.cpp \
    libdepthimageutils/Devel/Source/DSAPI/Implementations/DSHelpers.cpp \
    jni/com_intel_camera2_extensions_DepthCameraCalibrationDataMap.cpp \
    jni/com_intel_camera2_extensions_DepthSurfaceConfiguration.cpp \
    jni/com_intel_camera2_extensions_SurfaceQuery.cpp \
    jni/com_intel_camera2_extensions_DepthCameraImageReader.cpp

LOCAL_SHARED_LIBRARIES := \
    libandroid_runtime \
    libnativehelper \
    libutils \
    libbinder \
    libmedia \
    libui \
    liblog \
    libcutils \
    libgui \
    libcamera_client

LOCAL_C_INCLUDES += \
    system/core/include/ \
    frameworks/base/core/jni \
    frameworks/av/media/libmedia \
    frameworks/native/include \
    frameworks/native/opengl/include \
    system/media/camera/include \
    $(call include-path-for, libhardware)/hardware \
    frameworks/base/include/ \
    libnativehelper/include/ \
    libnativehelper/include/nativehelper/  \
    vendor/intel/hardware/PRIVATE/libds4/v4l_camera/ \
    vendor/intel/hardware/PRIVATE/libds4/v4l_camera/service \
    $(LOCAL_PATH)/libdepthimageutils/Include/DSAPI/ \
    $(LOCAL_PATH)/libdepthimageutils/Include/ \
    $(LOCAL_PATH)/libdepthimageutils/Devel/Source/DSAPI/Implementations/ \
    $(LOCAL_PATH)/libdepthimageutils/Devel/Source/DSUVC/API/ \
    $(LOCAL_PATH)/libdepthimageutils/Devel/Source/DSDevice/ \
    $(PV_INCLUDES) \
    $(JNI_H_INCLUDE)

LOCAL_CFLAGS += -g -ggdb -O0

LOCAL_SHARED_LIBRARIES += \
    libstlport \
    libv4lcamera_client

LOCAL_C_INCLUDES += \
    external/stlport/stlport \
    bionic

LOCAL_MODULE:= libinteldepthcamera_jni

include $(BUILD_SHARED_LIBRARY)

include $(call all-makefiles-under,$(LOCAL_PATH))

# ================= make jar file =============
include $(CLEAR_VARS)

LOCAL_MODULE:= com.intel.camera2.extensions.depthcamera
LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := \
            $(call all-subdir-java-files) 
            
include $(BUILD_JAVA_LIBRARY)


# ====  com.intel.camera2.extensions.depthcamera.xml lib def  ========================
include $(CLEAR_VARS)

LOCAL_MODULE := com.intel.camera2.extensions.depthcamera.xml
LOCAL_MODULE_TAGS := optional

LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_OWNER := intel

# This will install the file in /system/etc/permissions
#
LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/permissions

LOCAL_SRC_FILES := $(LOCAL_MODULE)

include $(BUILD_PREBUILT)
