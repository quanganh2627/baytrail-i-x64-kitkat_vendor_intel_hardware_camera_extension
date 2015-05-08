LOCAL_PATH:= $(call my-dir)

MY_BUILD_FLAG := false

ifeq ($(USE_CAMERA_STUB),false)
ifeq ($(USE_INTEL_CAMERA_EXTRAS),true)
ifneq ($(USE_CSS_1_5),true)
MY_BUILD_FLAG := true
endif #ifneq ($(USE_CSS_1_5),true)
endif #ifeq ($(USE_INTEL_CAMERA_EXTRAS),true)
endif #ifeq ($(USE_CAMERA_STUB),false)

################# MAKE_JNI_LIBRARY (libiacp_jni) ############################
include $(CLEAR_VARS)

LOCAL_MODULE := libiacp_jni

ifeq ($(MY_BUILD_FLAG),true)
LOCAL_MODULE_TAGS := optional
LOCAL_MULTILIB := both
#LOCAL_PRELINK_MODULE := false   # Prevent from prelink error
LOCAL_CFLAGS := -funsigned-char -Wno-unused-parameter -DPACKAGE="\"com/intel/camera2/extensions\"" -DTAG="\"CP_JNI\""
LOCAL_ALLOW_UNDEFINED_SYMBOLS := true

LOCAL_SHARED_LIBRARIES := \
    liblog \
    libjnigraphics \
    libiacp

LOCAL_C_INCLUDES := \
    $(TARGET_OUT_HEADERS)/libmfldadvci \
    $(TARGET_OUT_HEADERS)/libtbd \
    $(TARGET_OUT_HEADERS)/acc

LOCAL_SRC_FILES := \
    JNIUtil.cpp \
    CpUtil.cpp \
    cp_onload.cpp \
    cp_jni.cpp \
    cp_ipu_jni.cpp \
    cp_cpu_jni.cpp

ifeq ($(PLATFORM_ACC_SUPPORT), true)
LOCAL_SHARED_LIBRARIES += \
    libcutils \
    libutils \
    libbinder \
    libacc_client

LOCAL_SRC_FILES += \
    AccWrapper.cpp \
    CpIpuUtil.cpp \
    CpMemUtil.cpp
LOCAL_CFLAGS += -DPLATFORM_ACC_SUPPORT
endif #ifeq ($(PLATFORM_ACC_SUPPORT), true)

endif #ifeq ($(MY_BUILD_FLAG),true)

include $(BUILD_SHARED_LIBRARY)



################# MAKE_JNI_LIBRARY (libpvl_jni) ############################
include $(CLEAR_VARS)

LOCAL_MODULE := libpvl_jni

ifeq ($(MY_BUILD_FLAG),true)
LOCAL_MODULE_TAGS := optional
LOCAL_MULTILIB := 32
#LOCAL_PRELINK_MODULE := false   # Prevent from prelink error
LOCAL_CFLAGS := -funsigned-char -Wno-unused-parameter -DPACKAGE="\"com/intel/camera2/extensions\"" -DTAG="\"PVL_JNI\""

LOCAL_SHARED_LIBRARIES := \
    liblog \
    libjnigraphics \
    libpvl_blink_detection \
    libpvl_eye_detection \
    libpvl_face_detection \
    libpvl_face_recognition \
    libpvl_face_recognition_with_db \
    libpvl_smile_detection

LOCAL_C_INCLUDES := \
    $(TARGET_OUT_HEADERS)/libmfldadvci \
    $(TARGET_OUT_HEADERS)/libtbd

LOCAL_SRC_FILES := \
    JNIUtil.cpp \
    PvlUtil.cpp \
    pvl_onload.cpp \
    FaceDetection.cpp \
    EyeDetection.cpp \
    SmileDetection.cpp \
    BlinkDetection.cpp \
    FaceRecognition.cpp \
    FaceRecognitionWithDb.cpp

endif #ifeq ($(MY_BUILD_FLAG),true)

include $(BUILD_SHARED_LIBRARY)



################# MAKE_JNI_LIBRARY (libpvl_panorama_jni) ############################
include $(CLEAR_VARS)

LOCAL_MODULE := libpvl_panorama_jni

ifeq ($(MY_BUILD_FLAG),true)
LOCAL_MODULE_TAGS := optional
LOCAL_MULTILIB := both
#LOCAL_PRELINK_MODULE := false   # Prevent from prelink error
LOCAL_CFLAGS := -funsigned-char -Wno-unused-parameter -DPACKAGE="\"com/intel/camera2/extensions\"" -DTAG="\"PVL_PANORAMA_JNI\""

LOCAL_SHARED_LIBRARIES := \
    liblog \
    libdl \
    libjnigraphics \
    libpvl_panorama

LOCAL_C_INCLUDES := \
    $(TARGET_OUT_HEADERS)/libmfldadvci \
    $(TARGET_OUT_HEADERS)/libtbd

LOCAL_SRC_FILES := \
    JNIUtil.cpp \
    PvlUtil.cpp \
    Panorama.cpp

endif #ifeq ($(MY_BUILD_FLAG),true)

include $(BUILD_SHARED_LIBRARY)

