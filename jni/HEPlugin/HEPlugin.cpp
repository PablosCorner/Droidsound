
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>

#include <jni.h>
#include <android/log.h>

#include "com_ssb_droidsound_plugins_HEPlugin.h"

#include "../common/Misc.h"

#include "../HEPlugin/he/misc.h"

JNIEXPORT jlong JNICALL Java_com_ssb_droidsound_plugins_HEPlugin_N_1load(JNIEnv *env, jobject obj, jstring fname)
{
	
	int result;
	int psf_version;
	
	FILE * f = fopen( "/mnt/sdcard/droidsound/hebios.bin", "rb" );
	if (f == NULL)
	{
		return 0;
	}
    fseek(f, 0, SEEK_END);
    int bios_size = ftell(f);
    fseek(f, 0, SEEK_SET);

    uint8_t * bios = (uint8_t *) malloc( bios_size );
    fread(bios, 1, bios_size, f);
    fclose(f);

    bios_set_image( (uint8 *) bios, bios_size ); 
	
	int init_result = psx_init();
	if (init_result != 0)
	{
		return 0; // means init failed
	}

	const char *filename = env->GetStringUTFChars(fname, NULL);
	
	char temp[1024];
	strcpy(temp, filename);

	psf_version = psf_load(temp, &psf_file_system,0,0,0,0,0);
	
	if (psf_version == 1)
	{
		__android_log_print(ANDROID_LOG_VERBOSE, "HEPlugin", "Successfully loaded song, using version 1");
	}

	if (psf_version == 2)
	{
		__android_log_print(ANDROID_LOG_VERBOSE, "HEPlugin", "Successfully loaded song, using version 2");
	}

	void * psx_state = malloc( psx_get_state_size( psf_version ) ); 
	psx_clear_state( psx_state, psf_version );
			
	psf1_load_state state;
	
	state.emu = psx_state;
	state.first = true;
	state.refresh = 50;

	psf1_load_state * psinfo = (psf1_load_state*)malloc(sizeof(psf1_load_state));

	if (psf_version == 1)
	{
		if ( psf_load( temp, &psf_file_system, psf_version, psf1_load, &state, psf1_info, &state ) < 0 )
		{
			return 0;
		}
	}
	if (psf_version == 2)
	{
		void * psf2fs = psf2fs_create();
		if (!psf2fs)
		{
			return 0;
		}

		psf1_load_state state; 

		if ( psf_load( temp, &psf_file_system, psf_version, psf2fs_load_callback, psf2fs, psf1_info, &state ) < 0 )
		{
			return 0;
		}

	   	psx_set_readfile( psx_state, virtual_readfile, psf2fs );

		psinfo->psf2fs = psf2fs;
				
	}
	psinfo->emu = psx_state;
	psinfo->version = psf_version;
	psinfo->first = state.first;
	psinfo->refresh = state.refresh;

	return (long)psinfo;
}

JNIEXPORT void JNICALL Java_com_ssb_droidsound_plugins_HEPlugin_N_1unload(JNIEnv *env, jobject obj, jlong song)
{
	psf1_load_state *state = (psf1_load_state*)song;
	
	if (state->version == 2)
	{
		psf2fs_delete( state->psf2fs ); 
	}
	free ((void *)song);
	return;
	
}

JNIEXPORT jint JNICALL Java_com_ssb_droidsound_plugins_HEPlugin_N_1getSoundData(JNIEnv *env, jobject obj, jlong song, jshortArray sArray, jint size)
{
	psf1_load_state *state = (psf1_load_state*)song;
	void * psx_state = state->emu;

	int rtn = 0;			

	uint32_t samples_cnt = size / 2;
	
	jshort *dest = env->GetShortArrayElements(sArray, NULL);
	
	rtn = psx_execute( (void*)psx_state, 0x7FFFFFFF, dest, &samples_cnt, 0 );
	if (samples_cnt < (size / 2))
	{
		size = samples_cnt * 2;
	}
	
	env->ReleaseShortArrayElements(sArray, dest, 0);
	
	return size;
}


JNIEXPORT jstring JNICALL Java_com_ssb_droidsound_plugins_HEPlugin_N_1getStringInfo(JNIEnv *env, jobject obj, jlong song, jint what)
 {
	return NewString(env, "Hello Highly Exp.");
}

JNIEXPORT jint JNICALL Java_com_ssb_droidsound_plugins_HEPlugin_N_1getIntInfo(JNIEnv *env, jobject obj, jlong song, jint what)
{
	return 0;
}
