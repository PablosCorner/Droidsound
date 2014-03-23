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

LOCAL_MODULE    := highlyexp

LOCAL_SRC_FILES := HEPlugin.cpp \
	he/psx.c \
	he/ioptimer.c \
	he/iop.c \
	he/bios.c \
	he/r3000dis.c \
	he/r3000asm.c \
	he/r3000.c \
	he/vfs.c \
	he/spucore.c \
	he/spu.c \
	he/mkhebios.c \
	he/psf2fs.c \
	he/psflib.c
	
LOCAL_C_INCLUDES := $(LOCAL_PATH)/he
LOCAL_C_INCLUDES += $(LOCAL_PATH)/

LOCAL_LDLIBS := -llog -lz
LOCAL_CFLAGS = -DEMU_COMPILE -DEMU_LITTLE_ENDIAN

include $(BUILD_SHARED_LIBRARY)
