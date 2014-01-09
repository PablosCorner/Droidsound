LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := sidplayfp

LOCAL_ARM_MODE := arm

LOCAL_SRC_FILES :=  SidPlayfpplugin.cpp \
	builders/resid-builder/resid-builder.cpp \
	builders/resid-builder/resid-emu.cpp \
	builders/resid-builder/resid/dac.cc \
	builders/resid-builder/resid/sid.cc \
	builders/resid-builder/resid/voice.cc \
	builders/resid-builder/resid/wave.cc \
	builders/resid-builder/resid/envelope.cc \
	builders/resid-builder/resid/filter.cc \
	builders/resid-builder/resid/extfilt.cc \
	builders/resid-builder/resid/pot.cc \
	builders/resid-builder/resid/version.cc \
	builders/residfp-builder/residfp-builder.cpp \
	builders/residfp-builder/residfp-emu.cpp \
	builders/residfp-builder/residfp/Dac.cpp \
	builders/residfp-builder/residfp/EnvelopeGenerator.cpp \
	builders/residfp-builder/residfp/ExternalFilter.cpp \
	builders/residfp-builder/residfp/Filter.cpp \
	builders/residfp-builder/residfp/Filter6581.cpp \
	builders/residfp-builder/residfp/FilterModelConfig.cpp \
	builders/residfp-builder/residfp/OpAmp.cpp \
	builders/residfp-builder/residfp/SID.cpp \
	builders/residfp-builder/residfp/Spline.cpp \
	builders/residfp-builder/residfp/WaveformCalculator.cpp \
	builders/residfp-builder/residfp/WaveformGenerator.cpp \
	builders/residfp-builder/residfp/version.cc \
	builders/residfp-builder/residfp/resample/SincResampler.cpp \
	sidplayfp/config.cpp \
	sidplayfp/EventScheduler.cpp \
	sidplayfp/mixer.cpp \
	sidplayfp/player.cpp \
	sidplayfp/psiddrv.cpp \
	sidplayfp/reloc65.cpp \
	sidplayfp/sidbuilder.cpp \
	sidplayfp/SidConfig.cpp \
	sidplayfp/sidplayfp.cpp \
	sidplayfp/SidTune.cpp \
	sidplayfp/c64/c64.cpp \
	sidplayfp/c64/mmu.cpp \
	sidplayfp/c64/CIA/mos6526.cpp \
	sidplayfp/c64/CIA/timer.cpp \
	sidplayfp/c64/CPU/mos6510.cpp \
	sidplayfp/c64/CPU/mos6510debug.cpp \
	sidplayfp/c64/VIC_II/mos656x.cpp \
	sidplayfp/sidtune/MUS.cpp \
	sidplayfp/sidtune/p00.cpp \
	sidplayfp/sidtune/prg.cpp \
	sidplayfp/sidtune/PSID.cpp \
	sidplayfp/sidtune/SidTuneBase.cpp \
	sidplayfp/sidtune/SidTuneTools.cpp \
	utils/iniParser.cpp \
	utils/SidDatabase.cpp \
	utils/MD5/MD5.cpp

LOCAL_C_INCLUDES  = $(LOCAL_PATH)
LOCAL_C_INCLUDES  += $(LOCAL_PATH)/builders
LOCAL_C_INCLUDES  += $(LOCAL_PATH)/builders/resid-builder
LOCAL_C_INCLUDES  += $(LOCAL_PATH)/builders/resid-builder/resid
LOCAL_C_INCLUDES  += $(LOCAL_PATH)/builders/residfp-builder
LOCAL_C_INCLUDES  += $(LOCAL_PATH)/builders/residfp-builder/residfp
LOCAL_C_INCLUDES  += $(LOCAL_PATH)/builders/residfp-builder/residfp/resample
LOCAL_C_INCLUDES  += $(LOCAL_PATH)/sidplayfp/sidtune
LOCAL_C_INCLUDES  += $(LOCAL_PATH)/sidplayfp/c64
LOCAL_C_INCLUDES  += $(LOCAL_PATH)/sidplayfp
LOCAL_C_INCLUDES  += $(LOCAL_PATH)/utils
LOCAL_C_INCLUDES  += $(LOCAL_PATH)/utils/MD5
LOCAL_C_INCLUDES  += $(LOCAL_PATH)/utils/STILview

LOCAL_CFLAGS := -DHAVE_CONFIG_H -O3
LOCAL_LDLIBS := -llog -lstdc++
LOCAL_CPPFLAGS += -fexceptions -std=c++11

include $(BUILD_SHARED_LIBRARY)
