LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#LOCAL_C_INCLUDES += $(TOP)/$(MTK_PATH_SOURCE)/hardware/gralloc_extra/include

LOCAL_SHARED_LIBRARIES += liblog
LOCAL_SHARED_LIBRARIES += libarcsoft_dualcam_refocus
LOCAL_SHARED_LIBRARIES += libmpbase

LOCAL_SRC_FILES += ./src/ArcImageRefocus.cpp
LOCAL_SRC_FILES += ./src/ArcVideoRefocus.cpp

LOCAL_MODULE_TAGS := optional 
LOCAL_MODULE := libcam.arcsoftrefocus
LOCAL_PROPRIETARY_MODULE := true
LOCAL_MODULE_OWNER := mtk
#LOCAL_MULTILIB := 32
include $(MTK_SHARED_LIBRARY)

include $(CLEAR_VARS)
include $(call all-makefiles-under,$(LOCAL_PATH))
