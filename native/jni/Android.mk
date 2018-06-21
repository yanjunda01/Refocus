LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

# only for debug
# $(info $(LOCAL_PATH))
# $(info $(NDK_ROOT))
# $(info $(TARGET_ARCH))

# sdkͷ�ļ�Ŀ¼
LOCAL_C_INCLUDES += $(LOCAL_PATH)/input/inc

# sdk���ļ�Ŀ¼
LOCAL_LDLIBS += -L$(LOCAL_PATH)/input/lib

# ����
LOCAL_SRC_FILES += src/ArcImageRefocus.cpp
LOCAL_SRC_FILES += src/ArcVideoRefocus.cpp
LOCAL_SRC_FILES += src/jni_imagerefocus.cpp

# ϵͳ��
LOCAL_LDLIBS += -llog

# �㷨�⡢����Ҫƽ̨��
LOCAL_LDLIBS += -larcsoft_dualcam_refocus

LOCAL_CFLAGS += -DDUMPVIDEO
LOCAL_CFLAGS += -DDUMPIMAGE
LOCAL_CFLAGS += -DLOG

# ����ļ���
LOCAL_MODULE := arcsoft_dualcam_refocus_wrap
include $(BUILD_SHARED_LIBRARY)