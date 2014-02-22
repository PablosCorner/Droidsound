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
LOCAL_MODULE := lazyusf
#LOCAL_ARM_MODE := arm

LOCAL_SRC_FILES := \
		USFPlugin.cpp \
		lazyusf/psflib.c \
		lazyusf/audio.c \
		lazyusf/cpu.c \
		lazyusf/dma.c \
		lazyusf/exception.c \
		lazyusf/interpreter_cpu.c \
		lazyusf/interpreter_ops.c \
		lazyusf/main.c \
		lazyusf/memory.c \
		lazyusf/pif.c \
		lazyusf/registers.c \
		lazyusf/tlb.c \
		lazyusf/usf.c \
		lazyusf/rsp/rsp.c

LOCAL_LDFLAGS = -Wl,--fix-cortex-a8
LOCAL_LDLIBS := -llog -lz
LOCAL_CFLAGS = -O3 -DHAVE_NEON=1 -DARCH_MIN_SSSE2 -mfpu=neon
LOCAL_STATIC_LIBRARIES := cpufeatures 
LOCAL_C_INCLUDES := \
                $(LOCAL_PATH)/ \
                $(LOCAL_PATH)/lazyusf \
		
include $(BUILD_SHARED_LIBRARY)
