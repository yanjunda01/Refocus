LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

# only for debug
# $(info $(LOCAL_PATH))
# $(info $(NDK_ROOT))
# $(info $(TARGET_ARCH))

# sdk头文件目录
LOCAL_C_INCLUDES += $(LOCAL_PATH)/input/inc

# sdk库文件目录
LOCAL_LDLIBS += -L$(LOCAL_PATH)/input/lib

# 编译
LOCAL_SRC_FILES += src/ArcImageRefocus.cpp
LOCAL_SRC_FILES += src/ArcVideoRefocus.cpp
LOCAL_SRC_FILES += src/jni_imagerefocus.cpp

# 系统库
LOCAL_LDLIBS += -llog

# 算法库、不需要平台库
LOCAL_LDLIBS += -larcsoft_dualcam_refocus

LOCAL_CFLAGS += -DDUMPVIDEO
LOCAL_CFLAGS += -DDUMPIMAGE
LOCAL_CFLAGS += -DLOG

# 输出文件名
LOCAL_MODULE := arcsoft_dualcam_refocus_wrap
include $(BUILD_SHARED_LIBRARY)