
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <types.h>
#include <endian.h>
#include <jni.h>
#include <android/log.h>
#include "com_ssb_droidsound_plugins_USFPlugin.h"

extern "C" {
}
#include "../common/Misc.h"
#include "lazyusf/misc.h"

JNIEXPORT jlong JNICALL Java_com_ssb_droidsound_plugins_USFPlugin_N_1load(JNIEnv *env, jobject obj, jstring fname)
{
  	
	usf_loader_state * state = new usf_loader_state;
	state->emu_state = malloc( usf_get_state_size() );
	usf_clear( state->emu_state );
		
	const char *filename = env->GetStringUTFChars(fname, NULL);
	char temp[260];
	strcpy(temp, filename);
				
	if ( psf_load( temp, &psf_file_system, 0x21, usf_loader, state, usf_info, state ) < 0 )
		return -1;
	
	usf_set_compare( state->emu_state, state->enable_compare );
	usf_set_fifo_full( state->emu_state, state->enable_fifo_full );
	usf_start(state->emu_state);
	
	return (long)state;
}

JNIEXPORT void JNICALL Java_com_ssb_droidsound_plugins_USFPlugin_N_1unload(JNIEnv *env, jobject obj, jlong song)
{
	usf_loader_state * usf_state = (usf_loader_state*)song;
	usf_shutdown(usf_state->emu_state);
	return;
}

JNIEXPORT jint JNICALL Java_com_ssb_droidsound_plugins_USFPlugin_N_1getSoundData(JNIEnv *env, jobject obj, jlong song, jshortArray sArray, jint size)
{
	int32_t sample_rate;
	usf_loader_state * usf_state = (usf_loader_state*)song;

	jshort *dest = env->GetShortArrayElements(sArray, NULL); 
	__android_log_print(ANDROID_LOG_VERBOSE, "USFPlugin", "going to create samples: %d", size/2); 
	usf_render(usf_state->emu_state, dest, size/2, &sample_rate);
	__android_log_print(ANDROID_LOG_VERBOSE, "USFPlugin", "created samples"); 
	env->ReleaseShortArrayElements(sArray, dest, 0);

	return size;
}

JNIEXPORT jint JNICALL Java_com_ssb_droidsound_plugins_USFPlugin_N_1getIntInfo(JNIEnv *env, jobject obj, jlong song, jint what)
{
	usf_loader_state * usf_state = (usf_loader_state*)song;
	switch(what)
	{
		case 11:
			{
				return usf_get_sample_rate( usf_state->emu_state);
			} 
	}

}