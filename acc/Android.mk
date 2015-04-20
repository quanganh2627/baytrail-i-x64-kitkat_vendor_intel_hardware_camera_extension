ifeq ($(PLATFORM_ACC_SUPPORT), true)
ACC_PATH:= $(call my-dir)
include $(CLEAR_VARS)

# the acc lib which will call acc V4l2 IOCTRL
include $(ACC_PATH)/lib/Android.mk

# the acc client and acc service
include $(ACC_PATH)/client_service/Android.mk

endif
