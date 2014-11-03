LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include $(OPENCV_ANDROID_DIR)/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := heisenberg
LOCAL_SRC_FILES := heisenberg.cpp
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)
