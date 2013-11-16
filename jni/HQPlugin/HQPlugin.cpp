
#include <stdlib.h>
#include <stdio.h>

#include <jni.h>
#include <android/log.h>

#include "com_ssb_droidsound_plugins_HQPlugin.h"

#include "../common/Fifo.h"
#include "../common/Misc.h"

#include "hq/misc.h"


JNIEXPORT jlong JNICALL Java_com_ssb_droidsound_plugins_HQPlugin_N_1load(JNIEnv *env, jobject obj, jstring fname)
{
	int version;
	qsf_loader_state state;
    memset( &state, 0, sizeof(state) );

	qsf_loader_state * qsfinfo = (qsf_loader_state*)malloc(sizeof(qsf_loader_state));

	const char *filename = env->GetStringUTFChars(fname, NULL);
	char temp[1024];
	strcpy(temp, filename);

	int init_result = qsound_init();
	if (init_result != 0)
	{
		return 0;
	}

	version = psf_load( temp, &psf_file_system, 0, 0, 0, 0, 0);
	if (version < 0)
    {
		return 0;
	}
	
    if ( psf_load( temp, &psf_file_system, version, qsf_loader, &state, 0, 0 ) < 0 )
    {
		return 0;
	}

    void * qsound_state = malloc( qsound_get_state_size() );
    if ( !qsound_state )
    {
		return 0;
	}
    qsound_clear_state( qsound_state );

    if(state.key_size == 11)
	{
        uint8_t * ptr = state.key;
        uint32_t swap_key1 = get_be32( ptr +  0 );
        uint32_t swap_key2 = get_be32( ptr +  4 );
        uint32_t addr_key  = get_be16( ptr +  8 );
        uint8_t  xor_key   =        *( ptr + 10 );

        qsound_set_kabuki_key( qsound_state, swap_key1, swap_key2, addr_key, xor_key );
    } 
	else 
	{
        qsound_set_kabuki_key( qsound_state, 0, 0, 0, 0 );
    }

    qsound_set_z80_rom( qsound_state, state.z80_rom, state.z80_size );
    qsound_set_sample_rom( qsound_state, state.sample_rom, state.sample_size );

	qsfinfo->emu = qsound_state;
	qsfinfo->z80_rom = state.z80_rom;
	qsfinfo->sample_rom = state.sample_rom;
	qsfinfo->key = state.key;
	
	return (long)qsfinfo;
}

JNIEXPORT void JNICALL Java_com_ssb_droidsound_plugins_HQPlugin_N_1unload(JNIEnv *env, jobject obj, jlong song)
{
	qsf_loader_state *state = (qsf_loader_state*)song;

	free( state->emu );
	
    if ( state->key ) 
		free( state->key );
    
	if ( state->z80_rom ) 
		free( state->z80_rom );
    
	if ( state->sample_rom ) 
		free( state->sample_rom );

		
	return;
	
}

JNIEXPORT jint JNICALL Java_com_ssb_droidsound_plugins_HQPlugin_N_1getSoundData(JNIEnv *env, jobject obj, jlong song, jshortArray sArray, jint size)
{
	qsf_loader_state *state = (qsf_loader_state*)song;
	
	int ret = 0;			

	uint32_t samples_cnt = size/2;
	
	jshort *dest = env->GetShortArrayElements(sArray, NULL);
	
	ret = qsound_execute( (void*)state->emu, 0x7fffffff, dest, &samples_cnt ); 	
    if (samples_cnt < (size/2))	
	{
		size = samples_cnt * 2;
	}

	env->ReleaseShortArrayElements(sArray, dest, 0);
	
	return size;
}
