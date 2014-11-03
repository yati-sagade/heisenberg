LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include $(OPENCV_ANDROID_DIR)/share/OpenCV/OpenCV.mk

# LOCAL_MODULE    := hello-jni
# LOCAL_SRC_FILES := hello-jni.c

include $(BUILD_SHARED_LIBRARY)
