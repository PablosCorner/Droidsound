
#include <stdlib.h>
#include <stdio.h>
#include <jni.h>
#include <android/log.h>

#include "com_ssb_droidsound_plugins_OpenMPTPlugin.h"

#include "libopenmpt/libopenmpt.h"
#include "libopenmpt/libopenmpt_stream_callbacks_file.h"

extern "C" {
}

#define INFO_TITLE 0
#define INFO_AUTHOR 1
#define INFO_LENGTH 2
#define INFO_TYPE 3
#define INFO_COPYRIGHT 4
#define INFO_GAME 5
#define INFO_SUBTUNES 6
#define INFO_STARTTUNE 7 

#define INFO_INSTRUMENTS 100
#define INFO_CHANNELS 101
#define INFO_PATTERNS 102 


static jstring NewString(JNIEnv *env, const char *str)
{
	static jchar *temp, *ptr;

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

JNIEXPORT jlong JNICALL Java_com_ssb_droidsound_plugins_OpenMPTPlugin_N_1load(JNIEnv *env, jobject obj, jstring fname)
{
	openmpt_module* mod = 0;
	FILE* file = NULL;
	
	const char *filename = env->GetStringUTFChars(fname, NULL);
	file = fopen(filename,"rb");
	mod = openmpt_module_create(openmpt_stream_get_file_callbacks(), file, NULL, NULL, NULL);
	fclose(file);
	return (long)mod;
}

JNIEXPORT void JNICALL Java_com_ssb_droidsound_plugins_OpenMPTPlugin_N_1unload(JNIEnv *env, jobject obj, jlong song)
{
	openmpt_module* mod = (openmpt_module*)song;
	openmpt_module_destroy(mod);
	return;
}

JNIEXPORT jint JNICALL Java_com_ssb_droidsound_plugins_OpenMPTPlugin_N_1getSoundData(JNIEnv *env, jobject obj, jlong song, jshortArray sArray, jint size)
{
	size_t samples_done = 0;
	openmpt_module* mod = (openmpt_module*)song;
	jshort *ptr = env->GetShortArrayElements(sArray, NULL);
	samples_done = openmpt_module_read_interleaved_stereo(mod, 44100, size/2, ptr);
    env->ReleaseShortArrayElements(sArray, ptr, 0); 
	return size;
}
JNIEXPORT jint JNICALL Java_com_ssb_droidsound_plugins_OpenMPTPlugin_N_1getIntInfo(JNIEnv *env, jobject obj, jlong song, jint what)
{
	openmpt_module* mod = (openmpt_module*)song;
	switch(what)
	{
		case INFO_LENGTH:
			return openmpt_module_get_duration_seconds(mod) * 1000;
		case INFO_SUBTUNES:
			return openmpt_module_get_num_subsongs(mod);
		case INFO_STARTTUNE:
			return 0;
		case INFO_CHANNELS:
			return openmpt_module_get_num_channels(mod);
		case INFO_PATTERNS:
			return openmpt_module_get_num_patterns(mod);
		
	}
	return -1; 
}
JNIEXPORT jstring JNICALL Java_com_ssb_droidsound_plugins_OpenMPTPlugin_N_1getStringInfo(JNIEnv *env, jobject obj, jlong song, jint what)
{
	openmpt_module* mod = (openmpt_module*)song;
	switch(what)
	{
		case INFO_INSTRUMENTS:
			{
						
			char instruments[2048];
			char *ptr = instruments;
			char *instEnd = instruments + sizeof(instruments) - 48;
			int pat = openmpt_module_get_num_patterns(mod);
			int ns = openmpt_module_get_num_samples(mod);
			memset(ptr,0,2048);
			if ((ns * 48) > 2048)
				return  NewString(env, instruments); 
			
			if(ns > 0)
			{
				for(int i=1; i<ns; i++)
				{
					const char* instr_name = openmpt_module_get_sample_name(mod, i);
					memcpy((void*)ptr, instr_name, strlen(instr_name));
					ptr += strlen(instr_name);
					if(ptr >= instEnd)
						break;
					*ptr++ = 0x0a;
					*ptr = 0;
				}
			}
			return  NewString(env, instruments); 
			}
		
		return NewString(env, ""); 
	
	}

}


