
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <jni.h>
#include <android/log.h>
#include "com_ssb_droidsound_plugins_FFMPlugin.h"

extern "C" {
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libavutil/file.h>
}

JNIEXPORT jlong JNICALL Java_com_ssb_droidsound_plugins_FFMPlugin_N_1load(JNIEnv *env, jobject obj, jstring fname)
{
		
	AVFormatContext     *pFormatCtx;
	AVCodecContext      *aCodecCtx;
	AVCodecContext      *c = NULL;
	AVCodec             *aCodec;
	AVPacket            packet;
			
	av_register_all();				
	avcodec_register_all();
	
	
	aCodec = avcodec_find_decoder(AV_CODEC_ID_ATRAC3P);
	if (aCodec == NULL)
		return -1;
	
	c = avcodec_alloc_context3(aCodec);
	
	int res = avcodec_open2(c, aCodec, NULL);

	const char *filename = env->GetStringUTFChars(fname, NULL);
	FILE * f = fopen(filename, "rb");
    if (!f)
        return -1;



	
	return 1;
}
JNIEXPORT jint JNICALL Java_com_ssb_droidsound_plugins_FFMPlugin_N_1getSoundData(JNIEnv *env, jobject obj, jlong song, jshortArray sArray, jint size)
{

}
