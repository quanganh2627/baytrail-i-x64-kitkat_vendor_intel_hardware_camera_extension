LOCAL_PATH:= $(call my-dir)

################# MAKE_XML ############################
include $(CLEAR_VARS)
LOCAL_MODULE := com.intel.camera2.extensions.xml
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_OWNER := intel

# This will install the file in /system/etc/permissions
LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/permissions

LOCAL_SRC_FILES := $(LOCAL_MODULE)

include $(BUILD_PREBUILT)



################# MAKE_JAR ############################
include $(CLEAR_VARS)
LOCAL_MODULE := com.intel.camera2.extensions
LOCAL_MODULE_TAGS := optional
LOCAL_JAR_MANIFEST := etc/manifest.txt

LOCAL_SRC_FILES := \
    $(call all-java-files-under, src) \
    $(call all-java-files-under, tools/java)

include $(BUILD_JAVA_LIBRARY)



################# MAKE_DOC ############################
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, tools) \
                   src/com/intel/camera2/extensions/photography/MultiFrameBlender.java \
                   src/com/intel/camera2/extensions/photography/Panorama.java \
                   src/com/intel/camera2/extensions/photography/ZSLCaptureManager.java \
                   src/com/intel/camera2/extensions/vision/FaceAnalyzer.java \
                   src/com/intel/camera2/extensions/vision/FaceData.java

LOCAL_MODULE:= camera2sdk_doc
LOCAL_DROIDDOC_OPTIONS := com.intel.camera2.extensions
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_DROIDDOC_USE_STANDARD_DOCLET := true
#LOCAL_DROIDDOC_USE_STANDARD_DOCLET := false
#LOCAL_DROIDDOC_HTML_DIR := doc
#LOCAL_DROIDDOC_OPTIONS := -title "Camera2 SDK Add-on"

include $(BUILD_DROIDDOC)



################# TRIGGER_UNDER_MAKEFILE ############################
include $(call first-makefiles-under,$(LOCAL_PATH))

