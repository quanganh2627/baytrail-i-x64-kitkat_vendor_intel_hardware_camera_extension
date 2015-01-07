CAMERA_EXT_PATH:= $(call my-dir)
include $(CLEAR_VARS)

# Build camera1 extensions
include $(CAMERA_EXT_PATH)/camera1/Android.mk

# Build camera2 extensions
include $(CAMERA_EXT_PATH)/camera2/Android.mk

