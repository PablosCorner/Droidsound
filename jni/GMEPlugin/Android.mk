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

LOCAL_CFLAGS += -DBLARGG_LITTLE_ENDIAN -DHAVE_ZLIB_H -DHAVE_STDINT_H

LOCAL_CPPFLAGS += -std=gnu++0x -fexceptions -fpermissive

LOCAL_MODULE := gme

LOCAL_SRC_FILES :=  \
	GMEPlugin.cpp \
	gme/Ay_Apu.cpp \
	gme/Ay_Core.cpp \
	gme/Ay_Cpu.cpp \
	gme/Ay_Emu.cpp \
	gme/blargg_common.cpp \
	gme/blargg_errors.cpp \
	gme/Blip_Buffer.cpp \
	gme/Bml_Parser.cpp \
	gme/C140_Emu.cpp \
	gme/Classic_Emu.cpp \
	gme/dbopl.cpp \
	gme/Downsampler.cpp \
	gme/Dual_Resampler.cpp \
	gme/Effects_Buffer.cpp \
	gme/Fir_Resampler.cpp \
	gme/fmopl.cpp \
	gme/Gbs_Core.cpp \
	gme/Gbs_Cpu.cpp \
	gme/Gbs_Emu.cpp \
	gme/Gb_Apu.cpp \
	gme/Gb_Cpu.cpp \
	gme/Gb_Oscs.cpp \
	gme/gme.cpp \
	gme/Gme_File.cpp \
	gme/Gme_Loader.cpp \
	gme/Gym_Emu.cpp \
	gme/Hes_Apu.cpp \
	gme/Hes_Apu_Adpcm.cpp \
	gme/Hes_Core.cpp \
	gme/Hes_Cpu.cpp \
	gme/Hes_Emu.cpp \
	gme/K051649_Emu.cpp \
	gme/K053260_Emu.cpp \
	gme/K054539_Emu.cpp \
	gme/Kss_Core.cpp \
	gme/Kss_Cpu.cpp \
	gme/Kss_Emu.cpp \
	gme/Kss_Scc_Apu.cpp \
	gme/M3u_Playlist.cpp \
	gme/Multi_Buffer.cpp \
	gme/Music_Emu.cpp \
	gme/Nes_Apu.cpp \
	gme/Nes_Cpu.cpp \
	gme/Nes_Fds_Apu.cpp \
	gme/Nes_Fme7_Apu.cpp \
	gme/Nes_Namco_Apu.cpp \
	gme/Nes_Oscs.cpp \
	gme/Nes_Vrc6_Apu.cpp \
	gme/Nes_Vrc7_Apu.cpp \
	gme/Nsfe_Emu.cpp \
	gme/Nsf_Core.cpp \
	gme/Nsf_Cpu.cpp \
	gme/Nsf_Emu.cpp \
	gme/Nsf_Impl.cpp \
	gme/Okim6258_Emu.cpp \
	gme/Okim6295_Emu.cpp \
	gme/Opl_Apu.cpp \
	gme/Pwm_Emu.cpp \
	gme/Qsound_Apu.cpp \
	gme/Resampler.cpp \
	gme/Rf5C164_Emu.cpp \
	gme/Rf5C68_Emu.cpp \
	gme/Rom_Data.cpp \
	gme/Sap_Apu.cpp \
	gme/Sap_Core.cpp \
	gme/Sap_Cpu.cpp \
	gme/Sap_Emu.cpp \
	gme/SegaPcm_Emu.cpp \
	gme/Sgc_Core.cpp \
	gme/Sgc_Cpu.cpp \
	gme/Sgc_Emu.cpp \
	gme/Sgc_Impl.cpp \
	gme/Sms_Apu.cpp \
	gme/Sms_Fm_Apu.cpp \
	gme/Spc_Emu.cpp \
	gme/Spc_Filter.cpp \
	gme/Spc_Sfm.cpp \
	gme/Track_Filter.cpp \
	gme/Upsampler.cpp \
	gme/Vgm_Core.cpp \
	gme/Vgm_Emu.cpp \
	gme/Ym2151_Emu.cpp \
	gme/Ym2203_Emu.cpp \
	gme/Ym2413_Emu.cpp \
	gme/Ym2608_Emu.cpp \
	gme/Ym2610b_Emu.cpp \
	gme/Ym2612_Emu.cpp \
	gme/Ym3812_Emu.cpp \
	gme/ymdeltat.cpp \
	gme/Ymf262_Emu.cpp \
	gme/Ymz280b_Emu.cpp \
	gme/Z80_Cpu.cpp \
	gme/higan/smp/smp.cpp \
	gme/higan/processor/spc700/spc700.cpp \
	gme/higan/dsp/spc_dsp.cpp \
	gme/higan/dsp/dsp.cpp \
	gme/c140.c \
	gme/dac_control.c \
	gme/fm.c \
	gme/fm2612.c \
	gme/k051649.c \
	gme/k053260.c \
	gme/k054539.c \
	gme/okim6258.c \
	gme/okim6295.c \
	gme/pwm.c \
	gme/qmix.c \
	gme/rf5c68.c \
	gme/scd_pcm.c \
	gme/segapcm.c \
	gme/s_deltat.c \
	gme/s_logtbl.c \
	gme/s_opl.c \
	gme/s_opltbl.c \
	gme/ym2151.c \
	gme/ym2413.c \
	gme/ymz280b.c \
	
LOCAL_C_INCLUDES := $(LOCAL_PATH)/gme

LOCAL_STATIC_LIBRARIES := libfex

LOCAL_LDLIBS := -llog -lz
 
include $(BUILD_SHARED_LIBRARY)
