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

LOCAL_MODULE    := highlytheo

LOCAL_ARM_MODE := arm

LOCAL_SRC_FILES := HTPlugin.cpp \
	ht/arm.c \
	ht/dcsound.c \
	ht/psflib.c \
	ht/satsound.c \
	ht/sega.c \
	ht/yam.c \
	ht/m68k/m68kops.c \
	ht/m68k/m68kcpu.c

LOCAL_LDFLAGS = -Wl,--fix-cortex-a8
LOCAL_LDLIBS := -llog -lz
LOCAL_CFLAGS = -DEMU_COMPILE -DEMU_LITTLE_ENDIAN -DUSE_M68K -DHAVE_STDINT_H -DLSB_FIRST -DHAVE_MPROTECT -O3

include $(BUILD_SHARED_LIBRARY)
