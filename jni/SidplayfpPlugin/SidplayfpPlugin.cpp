//#include <stdlib.h>
#include <stdio.h>

#include <android/log.h>
#include "com_ssb_droidsound_plugins_SidplayfpPlugin.h"


#include "builders/resid-builder/resid.h"
#include "builders/residfp-builder/residfp.h"
#include "sidplayfp/sidplayfp.h"
#include "sidplayfp/sidtuneinfo.h"
#include "sidplayfp/sidconfig.h"
#include "sidplayfp/SidTune.h"

#define OPT_FILTER 1
#define OPT_SID_MODEL 2
#define OPT_VIDEO_MODE 3
#define OPT_FILTERBIAS 4
#define OPT_FILTERCURVE_6581 5
#define OPT_FILTERCURVE_8580 6
#define OPT_HAS_SECOND_SID 7
#define OPT_SID_BUILDERMODE 8
#define OPT_FORCED_SID_MODEL 9
#define OPT_FORCED_VIDEO_MODE 10
#define OPT_SID_RESAMPLING 11
#define OPT_LOOP_MODE 12

struct Player
{
	SidTune *sidtune;
	ReSIDBuilder *residbuilder;
	ReSIDfpBuilder *residfpbuilder;
	sidplayfp *sidemu;
	SidTuneInfo *sidInfo;
	bool silent;
};

static bool use_filter = false;
static int use_playback = 2;   //default to STEREO
static int resampling_mode = 0;
static int defaultSidModel = 0; //MOS6581
static bool defaultSidModel_forced = false;
static int defaultC64Model = 0; //PAL
static bool defaultC64Model_forced = false;
static bool use_resid = false;
static bool use_residfp = false;
static double filterCurve6581 = 0.5;
static double filterCurve8580 = 12500.0;
static double filterbias = 0.0;
static unsigned short second_sid_addr = 0;

static jstring NewString(JNIEnv *env, const char *str)
{
	static jchar *temp, *ptr;

	temp = (jchar *) malloc((strlen(str) + 1) * sizeof(jchar));

	ptr = temp;
	while(*str) {
		unsigned char c = (unsigned char)*str++;
		*ptr++ = (c < 0x7f && c >= 0x20) || c >= 0xa0 || c == 0xa ? c : '?';
	}
	jstring j = env->NewString(temp, ptr - temp);

	free(temp);

	return j;
}

JNIEXPORT void JNICALL Java_com_ssb_droidsound_plugins_SidplayfpPlugin_N_1setOption(JNIEnv *env, jclass cl, jint what, jint val)
{
	switch(what)
	{

     	case OPT_FILTER:
			use_filter = val ? true : false;
			break;

		case OPT_SID_MODEL: // UNKNOWN = 0 6581=1  8580 = 2
			defaultSidModel = val;
			break;
			
		case OPT_VIDEO_MODE: // UNKNOWN = 0 PAL=1 NTSC=2 OLD_NTSC=3 DREAN=4
			defaultC64Model = val;
			break;

		case OPT_HAS_SECOND_SID:
			second_sid_addr = (uint16_t)val;
			break;
		
		case OPT_SID_BUILDERMODE:
			use_resid = (val == 0) ? true : false;
			use_residfp = (val == 1) ? true : false;
			__android_log_print(ANDROID_LOG_VERBOSE, "Sidplay2fpPlugin", "setting builderoption: %d", val);
			break;

		case OPT_FORCED_SID_MODEL:
			defaultSidModel_forced = (val == 1) ? true : false;
			break;
		
		case OPT_FORCED_VIDEO_MODE:
			defaultC64Model_forced = (val == 1) ? true : false;
			break;

		case OPT_SID_RESAMPLING:
			resampling_mode = val;
			break;

		case OPT_FILTERBIAS:
			filterbias = (double) val;
			break;
			
			
	}
}

JNIEXPORT jboolean JNICALL Java_com_ssb_droidsound_plugins_SidplayfpPlugin_N_1canHandle(JNIEnv *, jobject, jstring)
{
	return true;
}

JNIEXPORT jlong JNICALL Java_com_ssb_droidsound_plugins_SidplayfpPlugin_N_1load(JNIEnv *env, jobject obj, jbyteArray bArray, jint size)
{
	unsigned int sid_count = 1;
	bool load_result = false;
	bool conf_result = false;
		
	jbyte * ptr = env->GetByteArrayElements(bArray, NULL);
	
	Player *player = new Player();

	player->sidtune = new SidTune((uint_least8_t*)ptr, size);
	
	if(!player->sidtune->getStatus()) 
	{
		delete player->sidtune;
		delete player;
		return 0;
	}
	player->sidtune->selectSong(0);

	player->sidemu = new sidplayfp();
	FILE * fp = NULL;

	fp = fopen("/mnt/sdcard/droidsound/c64roms/kernal", "rb");
	uint8_t * kernal_rom = (uint8_t *) malloc( 8192 );
    fread(kernal_rom, 1, 8192, fp);
    fclose(fp);

	fp = fopen("/mnt/sdcard/droidsound/c64roms/chargen", "rb");
	uint8_t * chargen_rom = (uint8_t *) malloc( 4096 );
    fread(chargen_rom, 1, 4096, fp);
    fclose(fp);

	fp = fopen("/mnt/sdcard/droidsound/c64roms/basic", "rb");
	uint8_t * basic_rom = (uint8_t *) malloc( 8192 );
    fread(basic_rom, 1, 8192, fp);
    fclose(fp);
	
	player->sidemu->setRoms(kernal_rom, basic_rom, chargen_rom);
	delete [] kernal_rom;
    delete [] basic_rom;
    delete [] chargen_rom; 
	//free(kernal_rom);
	//free(basic_rom);
	//free(chargen_rom);

	SidConfig cfg = player->sidemu->config();
	
	//we need to config the player manually
	cfg.frequency        = 44100;
	cfg.playback         = cfg.STEREO;
	cfg.secondSidAddress = second_sid_addr;

	cfg.forceC64Model = defaultC64Model_forced;
	cfg.forceSidModel = defaultSidModel_forced;

	if (second_sid_addr != 0)
		sid_count = 2;
		
	if (defaultC64Model == 1)
	{
		cfg.defaultC64Model = cfg.PAL;
		__android_log_print(ANDROID_LOG_VERBOSE, "Sidplay2fpPlugin", "C64Model PAL");
	}
	
	if (defaultC64Model == 2)
	{
		cfg.defaultC64Model = cfg.NTSC;
		__android_log_print(ANDROID_LOG_VERBOSE, "Sidplay2fpPlugin", "C64Model NTSC");
	}
	
	if (defaultC64Model == 3)
	{
		cfg.defaultC64Model = cfg.OLD_NTSC;
		__android_log_print(ANDROID_LOG_VERBOSE, "Sidplay2fpPlugin", "C64Model NTSC OLD");
	}
	
	if (defaultC64Model == 4)
	{
		cfg.defaultC64Model = cfg.DREAN;
		__android_log_print(ANDROID_LOG_VERBOSE, "Sidplay2fpPlugin", "C64Model DREAN");		
	}
	
	if (defaultSidModel == 1)
	{
		cfg.defaultSidModel = cfg.MOS6581;
		__android_log_print(ANDROID_LOG_VERBOSE, "Sidplay2fpPlugin", "C64SidModel MOS6581");
	}	
	if (defaultSidModel == 2)
	{
		cfg.defaultSidModel = cfg.MOS8580;
		__android_log_print(ANDROID_LOG_VERBOSE, "Sidplay2fpPlugin", "C64SidModel MOS8580");
	}	
	if (resampling_mode == 0)
	{
		cfg.samplingMethod = cfg.INTERPOLATE;
		__android_log_print(ANDROID_LOG_VERBOSE, "Sidplay2fpPlugin", "C64Samplingmethod Interpolate");
	}
	
	if (resampling_mode == 1)
	{
		cfg.samplingMethod = cfg.RESAMPLE_INTERPOLATE;
		__android_log_print(ANDROID_LOG_VERBOSE, "Sidplay2fpPlugin", "C64Samplingmethod Resample");	
	}		

	if (use_resid)
	{
		__android_log_print(ANDROID_LOG_VERBOSE, "Sidplay2fpPlugin", "Using ReSID builder");

		player->residbuilder = new ReSIDBuilder("ReSID");

		cfg.fastSampling = false;

		player->residbuilder->create(sid_count);
		player->residbuilder->filter(use_filter);
		player->residbuilder->bias(filterbias);
		cfg.sidEmulation  = player->residbuilder;
	}

	if (use_residfp)
	{
		__android_log_print(ANDROID_LOG_VERBOSE, "Sidplay2fpPlugin", "Using ReSIDfp builder");
		
		player->residfpbuilder = new ReSIDfpBuilder("ReSIDfp");
		
		player->residfpbuilder->create(sid_count);
		player->residfpbuilder->filter(use_filter);
		player->residfpbuilder->filter6581Curve(filterCurve6581);
		player->residfpbuilder->filter8580Curve(filterCurve8580);
		cfg.sidEmulation  = player->residfpbuilder;		
	}
	
	load_result = player->sidemu->load(player->sidtune);

	if(load_result == false)
	{
		delete player->sidemu;
		delete player->residbuilder;
		delete player->residfpbuilder;
		delete player->sidtune;
		delete player;
		return 0;
	}

	conf_result = player->sidemu->config(cfg);

	if (conf_result == false)
	{
		delete player->sidemu;
		delete player->residbuilder;
		delete player->residfpbuilder;
		delete player->sidtune;
		delete player;
		return 0;
	}

	env->ReleaseByteArrayElements(bArray, ptr, 0);
	
	return (long)player;

}

JNIEXPORT void JNICALL Java_com_ssb_droidsound_plugins_SidplayfpPlugin_N_1unload(JNIEnv *, jobject, jlong song)
{
	Player *player = (Player*)song;
	if (player->sidemu->isPlaying() == true)
		player->sidemu->stop();

	delete player->sidemu;
	delete player->residbuilder;
	delete player->residfpbuilder;
	delete player->sidtune;
	delete player;
	
	__android_log_print(ANDROID_LOG_VERBOSE, "Sidplay2fpPlugin", "unloaded");

}

JNIEXPORT jint JNICALL Java_com_ssb_droidsound_plugins_SidplayfpPlugin_N_1getSoundData(JNIEnv *env, jobject obj, jlong song, jshortArray sArray, jint size)
{
	Player *player = (Player*)song;
	int samples_created = 0;
	jshort *ptr = env->GetShortArrayElements(sArray, NULL);
	samples_created = player->sidemu->play(ptr, size);
    env->ReleaseShortArrayElements(sArray, ptr, 0);
	return samples_created;
	
}

JNIEXPORT jboolean JNICALL Java_com_ssb_droidsound_plugins_SidplayfpPlugin_N_1seekTo(JNIEnv *, jobject, jlong, jint)
{
	return false;
}

JNIEXPORT jboolean JNICALL Java_com_ssb_droidsound_plugins_SidplayfpPlugin_N_1setTune(JNIEnv *env, jobject obj, jlong song, jint tune)
{
	bool res = false;
	
	Player *player = (Player*)song;
	player->sidemu->stop();
	player->sidtune->selectSong(tune+1);
	res = player->sidemu->load(player->sidtune);
	return true;
}

JNIEXPORT jstring JNICALL Java_com_ssb_droidsound_plugins_SidplayfpPlugin_N_1getStringInfo(JNIEnv *env, jobject obj, jlong song, jint what)
{
	return NewString(env, "");
}

JNIEXPORT jint JNICALL Java_com_ssb_droidsound_plugins_SidplayfpPlugin_N_1getIntInfo(JNIEnv *env, jobject obj, jlong song, jint what)
{
	return -1;
}
