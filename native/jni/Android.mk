LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

# $(info $(LOCAL_PATH))
# $(info $(NDK_ROOT))
# $(info $(TARGET_ARCH))

LOCAL_C_INCLUDES += $(LOCAL_PATH)/input/inc

LOCAL_SRC_FILES += src/jni_imagerefocus.cpp
LOCAL_SRC_FILES += src/ArcImageRefocus.cpp
LOCAL_SRC_FILES += src/ArcVideoRefocus.cpp

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
	LOCAL_LDLIBS += -L$(LOCAL_PATH)/input/lib32
else ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
	LOCAL_LDLIBS += -L$(LOCAL_PATH)/input/lib64
endif

LOCAL_LDLIBS += -llog
LOCAL_LDLIBS += -larcsoft_dualcam_refocus

#LOCAL_CFLAGS += -DDUMPVIDEO
#LOCAL_CFLAGS += -DDUMPIMAGE
LOCAL_CFLAGS += -DLOG

# 输出文件名
LOCAL_MODULE := arcsoft_dualcam_refocus_wrap
include $(BUILD_SHARED_LIBRARY)