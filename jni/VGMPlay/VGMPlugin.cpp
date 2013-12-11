
#include <stdlib.h>
#include <stdio.h>
#include <jni.h>
#include <android/log.h>

#include "com_ssb_droidsound_plugins_VGMPlugin.h"

extern "C" {
#include "vgm/chips/mamedef.h"
#include "vgm/VGMFile.h"
#include "vgm/VGMPlay_Intf.h"
}

#define INFO_TITLE 0
#define INFO_AUTHOR 1
#define INFO_LENGTH 2
#define INFO_TYPE 3
#define INFO_COPYRIGHT 4
#define INFO_GAME 5
#define INFO_SUBTUNES 6
#define INFO_STARTTUNE 7
#define INFO_SUBTUNE_TITLE 8

static jstring NewString(JNIEnv *env, const char *str)
{
	static jchar *temp, *ptr;
	UINT8 paska;

	temp = (jchar *) malloc((strlen(str) + 1) * sizeof(jchar));

	ptr = temp;
	while(*str) {
		unsigned char c = (unsigned char)*str++;
		*ptr++ = (c < 0x7f && c >= 0x20) || c >= 0xa0 || c == 0xa ? c : '?';
	}
	//*ptr++ = 0;
	jstring j = env->NewString(temp, ptr - temp);

	free(temp);

	return j;
}

JNIEXPORT jlong JNICALL Java_com_ssb_droidsound_plugins_VGMPlugin_N_1load(JNIEnv *env, jobject obj, jstring fname)
{
	bool res = false;
		
	const char *filename = env->GetStringUTFChars(fname, NULL);
	
	char temp[260];
	strcpy(temp, filename); 
			
	VGMPlay_Init();
	
	// load configuration file here
	
	VGMPlay_Init2();

	res = OpenVGMFile(temp);
	if (!res)
		return 0;
			
	PlayVGM();
	
	return 1;
}


JNIEXPORT void JNICALL Java_com_ssb_droidsound_plugins_VGMPlugin_N_1unload(JNIEnv *env, jobject obj, jlong song)
{
	StopVGM();
	CloseVGMFile();
	VGMPlay_Deinit();
	return;
}

JNIEXPORT jint JNICALL Java_com_ssb_droidsound_plugins_VGMPlugin_N_1getSoundData(JNIEnv *env, jobject obj, jlong song, jshortArray bArray, int size)
{
	jshort *dest = env->GetShortArrayElements(bArray, NULL);
	
	uint32_t created_samples = 0;
	created_samples = FillBuffer((WAVE_16BS *)dest, size / 2);
	env->ReleaseShortArrayElements(bArray, dest, 0);
	return created_samples * 2;
}

