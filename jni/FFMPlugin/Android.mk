# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := libavcodec
LOCAL_SRC_FILES := ffmpeg/lib/libavcodec.a 
LOCAL_EXPORT_C_INCLUDE := $(LOCAL_PATH)/ffmpeg/include/libavcodec
include $(PREBUILT_STATIC_LIBRARY)

LOCAL_MODULE := libavformat
LOCAL_SRC_FILES := ffmpeg/lib/libavformat.a 
LOCAL_EXPORT_C_INCLUDE := $(LOCAL_PATH)/ffmpeg/include/libavformat
include $(PREBUILT_STATIC_LIBRARY)

LOCAL_MODULE := libavutil
LOCAL_SRC_FILES := ffmpeg/lib/libavutil.a 
LOCAL_EXPORT_C_INCLUDE := $(LOCAL_PATH)/ffmpeg/include/libavutil
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := ffmpeg

LOCAL_ARM_MODE := arm
LOCAL_SRC_FILES := FFMPlugin.cpp

#LOCAL_LDFLAGS = -Wl,--fix-cortex-a8

LOCAL_CFLAGS = -O3 -D__STDC_CONSTANT_MACROS 

LOCAL_C_INCLUDES := $(LOCAL_PATH) \
					$(LOCAL_PATH)/ffmpeg \
					$(LOCAL_PATH)/ffmpeg/include

LOCAL_STATIC_LIBRARIES := libavcodec libavformat libavutil libswscale

include $(BUILD_SHARED_LIBRARY)
