# Droidsound Application.mk, using gnustl_shared from now on since it has more features
#APP_STL := stlport_shared
APP_STL := gnustl_shared
APP_ABI := armeabi-v7a

APP_CFLAGS := -mfloat-abi=softfp
APP_CPPFLAGS := -mfloat-abi=softfp
APP_LDFLAGS := -O3 -mfloat-abi=softfp