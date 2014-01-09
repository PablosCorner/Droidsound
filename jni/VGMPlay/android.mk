LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := vgmplay

LOCAL_CFLAGS = -DDISABLE_HW_SUPPORT -DENABLE_ALL_CORES -DADDITIONAL_FORMATS -fno-builtin -O3 -funroll-all-loops

LOCAL_ARM_MODE := arm

LOCAL_SRC_FILES :=  VGMPlugin.cpp \
	vgm/ChipMapper.c \
	vgm/Stream.c \
	vgm/VGMPlay.c \
	vgm/VGMPlayUI.c \
	vgm/VGMPlay_AddFmts.c \
	vgm/chips/2151intf.c \
	vgm/chips/2203intf.c \
	vgm/chips/2413intf.c \
	vgm/chips/2608intf.c \
	vgm/chips/2610intf.c \
	vgm/chips/2612intf.c \
	vgm/chips/262intf.c \
	vgm/chips/3526intf.c \
	vgm/chips/3812intf.c \
	vgm/chips/8950intf.c \
	vgm/chips/adlibemu_opl2.c \
	vgm/chips/adlibemu_opl3.c \
	vgm/chips/ay8910.c \
	vgm/chips/ay8910_opl.c \
	vgm/chips/ay_intf.c \
	vgm/chips/c140.c \
	vgm/chips/c6280.c \
	vgm/chips/c6280intf.c \
	vgm/chips/dac_control.c \
	vgm/chips/emu2149.c \
	vgm/chips/emu2413.c \
	vgm/chips/fm.c \
	vgm/chips/fm2612.c \
	vgm/chips/fmopl.c \
	vgm/chips/gb.c \
	vgm/chips/k051649.c \
	vgm/chips/k053260.c \
	vgm/chips/k054539.c \
	vgm/chips/multipcm.c \
	vgm/chips/nes_apu.c \
	vgm/chips/nes_intf.c \
	vgm/chips/np_nes_apu.c \
	vgm/chips/np_nes_dmc.c \
	vgm/chips/np_nes_fds.c \
	vgm/chips/okim6258.c \
	vgm/chips/okim6295.c \
	vgm/chips/Ootake_PSG.c \
	vgm/chips/panning.c \
	vgm/chips/pokey.c \
	vgm/chips/pwm.c \
	vgm/chips/qsound.c \
	vgm/chips/rf5c68.c \
	vgm/chips/scd_pcm.c \
	vgm/chips/scsp.c \
	vgm/chips/scspdsp.c \
	vgm/chips/segapcm.c \
	vgm/chips/sn76489.c \
	vgm/chips/sn76496.c \
	vgm/chips/sn76496_opl.c \
	vgm/chips/sn764intf.c \
	vgm/chips/upd7759.c \
	vgm/chips/ym2151.c \
	vgm/chips/ym2413.c \
	vgm/chips/ym2413hd.c \
	vgm/chips/ym2413_opl.c \
	vgm/chips/ym2612.c \
	vgm/chips/ymdeltat.c \
	vgm/chips/ymf262.c \
	vgm/chips/ymf271.c \
	vgm/chips/ymf278b.c \
	vgm/chips/ymz280b.c


LOCAL_C_INCLUDES  += $(LOCAL_PATH)
LOCAL_C_INCLUDES  += $(LOCAL_PATH)/vgm
LOCAL_C_INCLUDES  += $(LOCAL_PATH)/vgm/chips

LOCAL_LDLIBS := -llog -lz

include $(BUILD_SHARED_LIBRARY)
