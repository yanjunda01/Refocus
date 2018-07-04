LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := libarcsoft_dualcam_refocus
LOCAL_SRC_FILES_32 := lib32/libarcsoft_dualcam_refocus.so
LOCAL_SRC_FILES_64 := lib64/libarcsoft_dualcam_refocus.so
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
include $(BUILD_PREBUILT)