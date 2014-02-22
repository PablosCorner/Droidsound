#include <android/log.h> 
#include <jni.h>

#include "fex/fex.h"
#include "fex/File_Extractor.h"

#include "com_ssb_droidsound_utils_NativeArcFile.h"


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

JNIEXPORT void JNICALL Java_com_ssb_droidsound_utils_NativeArcFile_setPosition(JNIEnv *env, jobject obj, jlong fd, jlong position)
{
	fex_t *arc = (fex_t*)fd;
	fex_seek_arc(arc, position);
	return;
}
JNIEXPORT jlong JNICALL Java_com_ssb_droidsound_utils_NativeArcFile_getPositionByIndex(JNIEnv *env, jobject obj, jlong fd, jint index)
{
	int counter = 0;
	fex_t *arc = (fex_t*)fd;
	fex_rewind(arc);	 // roll back to start
	while(!arc->done())
	{
		if (counter == index)
		{
			break;
		}
		counter += 1;
		arc->next();
	}
	
	fex_pos_t position = fex_tell_arc(arc);
	return (long)position;
}


JNIEXPORT jint JNICALL Java_com_ssb_droidsound_utils_NativeArcFile_getcurrentSize(JNIEnv *env, jobject obj, jlong fd)
{
	fex_t *arc = (fex_t*)fd;
	fex_pos_t position;
	position = fex_tell_arc(arc);
	fex_stat(arc);
	int length = arc->size();
	fex_seek_arc(arc, position);
	return length;
}
JNIEXPORT jstring JNICALL Java_com_ssb_droidsound_utils_NativeArcFile_getcurrentName(JNIEnv *env, jobject obj, jlong fd)
{
	fex_t *arc = (fex_t*)fd;
	fex_pos_t position;
	position = fex_tell_arc(arc);
	jstring name = env->NewStringUTF(arc->name()); 
	fex_seek_arc(arc, position);
	return name;
}
JNIEXPORT jlong JNICALL Java_com_ssb_droidsound_utils_NativeArcFile_openFile(JNIEnv *env, jobject obj, jstring fname)
{
	fex_t *arc;
	const char *filename = env->GetStringUTFChars(fname, NULL);
	fex_open(&arc, filename);
	return (long)arc;
}

JNIEXPORT void JNICALL Java_com_ssb_droidsound_utils_NativeArcFile_closeFile(JNIEnv *env, jobject obj, jlong fd)
{
	fex_t *arc = (fex_t*)fd;
	fex_close(arc);
}

JNIEXPORT jint JNICALL Java_com_ssb_droidsound_utils_NativeArcFile_getTotalCount(JNIEnv *env, jobject obj, jlong fd)
{
	int counter = 0;
	fex_t *arc = (fex_t*)fd;

	fex_rewind(arc); // roll back to start
	while (!arc->done())
	{
		counter +=1;
		arc->next();
	}
	fex_rewind(arc); // roll back to start
	return counter;
}
JNIEXPORT void JNICALL Java_com_ssb_droidsound_utils_NativeArcFile_rewind(JNIEnv *env, jobject obj, jlong fd)
{
	fex_t *arc = (fex_t*)fd;
	fex_rewind(arc);
}

JNIEXPORT jint JNICALL Java_com_ssb_droidsound_utils_NativeArcFile_findEntry(JNIEnv *env, jobject obj, jlong fd, jstring fname)
{
	int counter = 0;
	jboolean iscopy; 

	fex_t *arc = (fex_t*)fd;
	const char *filename = env->GetStringUTFChars(fname, &iscopy); 

	fex_rewind(arc); 
	while (!arc->done())
	{
		if (strcmp((const char *)filename,arc->name()) == 0)
		{
			return counter;
		}
			
		counter += 1;
		arc->next();
	}

	return -1;
}

JNIEXPORT jint JNICALL Java_com_ssb_droidsound_utils_NativeArcFile_readfd(JNIEnv *env, jobject obj, jlong fd, jbyteArray ba, jint size)
{
	fex_t *arc = (fex_t*)fd;
	jbyte *ptr = env->GetByteArrayElements(ba, NULL);
	fex_stat(arc);
	fex_read(arc, ptr, (long)size);
	env->ReleaseByteArrayElements(ba, ptr, 0); 
	return size;
}
