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

LOCAL_CFLAGS += -DBLARGG_LITTLE_ENDIAN

LOCAL_CPPFLAGS += -std=gnu++0x

LOCAL_MODULE := fex

LOCAL_SRC_FILES :=  \
	fex/Zlib_Inflater.cpp \
    fex/Zip7_Extractor.cpp \
    fex/Zip_Extractor.cpp \
    fex/Rar_Extractor.cpp \
    fex/Gzip_Reader.cpp \
    fex/Gzip_Extractor.cpp \
    fex/File_Extractor.cpp \
    fex/fex.cpp \
    fex/Data_Reader.cpp \
    fex/blargg_errors.cpp \
    fex/blargg_common.cpp \
    fex/Binary_Extractor.cpp \
    7z_C/LzmaLib.c \
    7z_C/LzmaEnc.c \
    7z_C/LzmaDec.c \
    7z_C/Lzma86Enc.c \
    7z_C/Lzma86Dec.c \
    7z_C/Lzma2Enc.c \
    7z_C/Lzma2Dec.c \
    7z_C/LzFind.c \
    7z_C/Delta.c \
    7z_C/CpuArch.c \
    7z_C/BraIA64.c \
    7z_C/Bra86.c \
    7z_C/Bra.c \
    7z_C/Bcj2.c \
    7z_C/Alloc.c \
    7z_C/7zStream.c \
    7z_C/7zIn.c \
    7z_C/Ppmd7Enc.c \
    7z_C/Ppmd7Dec.c \
    7z_C/Ppmd7.c \
    7z_C/7zDec.c \
    7z_C/7zCrcOpt.c \
    7z_C/7zCrc.c \
    7z_C/7zBuf2.c \
    7z_C/7zBuf.c \
    7z_C/7zAlloc.c \
    7z_C/MtCoder.c \
    7z_C/LzFindMt.c \
    7z_C/posix/Threads.c \
    unrar/unrar.cpp \
    unrar/unrar_open.cpp \
    unrar/unrar_misc.cpp \
    unrar/unpack20.cpp \
    unrar/unpack15.cpp \
    unrar/unpack.cpp \
    unrar/unicode.cpp \
    unrar/suballoc.cpp \
    unrar/rawread.cpp \
    unrar/rarvmtbl.cpp \
    unrar/rarvm.cpp \
    unrar/model.cpp \
    unrar/getbits.cpp \
    unrar/extract.cpp \
    unrar/encname.cpp \
    unrar/crc.cpp \
    unrar/coder.cpp \
    unrar/arcread.cpp \
    unrar/archive.cpp
	
LOCAL_C_INCLUDES := $(LOCAL_PATH)/fex $(LOCAL_PATH)/7z_C $(LOCAL_PATH)/unrar

LOCAL_LDLIBS := -llog -lz

include $(BUILD_STATIC_LIBRARY)
