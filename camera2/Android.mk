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

LOCAL_SRC_FILES := \
    $(call all-java-files-under, src) \
    $(call all-java-files-under, tools/java)

include $(BUILD_JAVA_LIBRARY)



################# MAKE_DOC ############################
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-subdir-java-files) $(call all-subdir-html-files)

LOCAL_MODULE:= camera2sdk_doc
LOCAL_DROIDDOC_OPTIONS := com.intel.camera2.extensions
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_DROIDDOC_USE_STANDARD_DOCLET := true

include $(BUILD_DROIDDOC)



################# TRIGGER_UNDER_MAKEFILE ############################
include $(call first-makefiles-under,$(LOCAL_PATH))

