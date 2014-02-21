
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdarg.h>
#include <ctype.h>
#include <termios.h>
#include <unistd.h>

#include <jni.h>
#include <android/log.h>

#include "nds/vio2sf/vio2sf.h" 
 
#include "com_ssb_droidsound_plugins_NDSPlugin.h"

JNIEXPORT jlong JNICALL Java_com_ssb_droidsound_plugins_NDSPlugin_N_1load(JNIEnv *env, jobject obj, jstring fname)
{
	int result;
		
	const char *filename = env->GetStringUTFChars(fname, NULL);
	char temp[260];
	strcpy(temp, filename);

    result = xsf_start(temp);
	return (long)result;
}

JNIEXPORT void JNICALL Java_com_ssb_droidsound_plugins_NDSPlugin_N_1unload(JNIEnv *env, jobject obj, jlong song)
{
	xsf_term();
	return;
}

//
// getSoundData() is the function that is being called all the time from Java side
// It is the place where you put the function that produces samples.
// song is the pointer the sound instance usually casted to void*, sArray is in/out
// buffer for samples, size is wanted amount of samples.
//

JNIEXPORT jint JNICALL Java_com_ssb_droidsound_plugins_NDSPlugin_N_1getSoundData(JNIEnv *env, jobject obj, jlong song, jshortArray sArray, jint size)
{	
	int ret = 0;			
	signed short sample_buffer[22050 * 2]; 

	uint32_t samples_cnt = 22050;
	
	ret = xsf_gen( sample_buffer, samples_cnt ); 		
	
	jshort *dest = env->GetShortArrayElements(sArray, NULL);
	memcpy((char *)dest,(char *)sample_buffer, 22050 * 4); // 22050 * 2 * sizeof(short)
	env->ReleaseShortArrayElements(sArray, dest, 0);
	
	return 44100;
}
