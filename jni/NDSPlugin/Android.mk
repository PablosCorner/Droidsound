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

LOCAL_MODULE    := vio2sf

LOCAL_ARM_MODE := arm

LOCAL_SRC_FILES :=  ndsplugin.cpp \
	nds/vio2sf/vio2sf.c \
	nds/vio2sf/desmume/arm_instructions.c \
	nds/vio2sf/desmume/armcpu.c \
	nds/vio2sf/desmume/bios.c \
	nds/vio2sf/desmume/cp15.c \
	nds/vio2sf/desmume/FIFO.c \
	nds/vio2sf/desmume/GPU.c \
	nds/vio2sf/desmume/matrix.c \
	nds/vio2sf/desmume/mc.c \
	nds/vio2sf/desmume/MMU.c \
	nds/vio2sf/desmume/NDSSystem.c \
	nds/vio2sf/desmume/SPU.c \
	nds/vio2sf/desmume/thumb_instructions.c
	
 
LOCAL_LDLIBS := -llog -lz
LOCAL_CFLAGS := -DLSB_FIRST -DHAVE_STDINT_H -D_strnicmp=strncasecmp -O3 -funroll-all-loops
LOCAL_C_INCLUDES := $(LOCAL_PATH)/nds/vio2sf
LOCAL_C_INCLUDES += $(LOCAL_PATH)/nds
LOCAL_C_INCLUDES += $(LOCAL_PATH)/nds/vio2sf/desmume
  

include $(BUILD_SHARED_LIBRARY)
