# Droidsound Application.mk
APP_STL := stlport_shared
APP_ABI := armeabi-v7a

APP_CFLAGS := -mfloat-abi=softfp
APP_CPPFLAGS := -mfloat-abi=softfp
APP_LDFLAGS := -O3 -mfloat-abi=softfp 