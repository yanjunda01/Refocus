LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_C_INCLUDES += $(TOP)/$(MTK_PATH_SOURCE)/hardware/mtkcam/utils/arcbokeh/input/inc

LOCAL_SHARED_LIBRARIES += liblog
LOCAL_SHARED_LIBRARIES += libarcsoft_dualcam_refocus
#LOCAL_SHARED_LIBRARIES += libmpbase

LOCAL_SRC_FILES += ./src/ArcImageRefocus.cpp
LOCAL_SRC_FILES += ./src/ArcVideoRefocus.cpp

LOCAL_MODULE_TAGS := optional 
LOCAL_MODULE := libcam.arcsoftrefocus
LOCAL_PROPRIETARY_MODULE := true
LOCAL_MODULE_OWNER := mtk
LOCAL_CFLAGS += -DLOG
#LOCAL_CFLAGS += -DDUMPVIDEO
LOCAL_MULTILIB := both
include $(MTK_SHARED_LIBRARY)

include $(call all-makefiles-under,$(LOCAL_PATH))
