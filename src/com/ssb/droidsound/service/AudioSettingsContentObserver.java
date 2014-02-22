package com.ssb.droidsound.service;

import com.ssb.droidsound.PlayerActivity;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import com.ssb.droidsound.utils.Log;

public class AudioSettingsContentObserver extends ContentObserver
{
    static boolean fadingOut = false;
    static boolean usefadeOut = false;
	static int previousVolume;
    static int originalVolume;
    static AudioManager audio;
    Context context;

    public AudioSettingsContentObserver(Context ctx, Handler handler) {
        super(handler);
        context = ctx;

        audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        originalVolume = previousVolume;
        //Log.d("AudioSettingsContentObserver", "Initial volume level on background: %d", previousVolume);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }
    
    public static void setfadingOut(boolean val) {
    	fadingOut = val;
    }
    
    public static void usefadingOut(boolean val) {
    	usefadeOut = val;
    }

    public static int getOriginalVolume() {
    	return originalVolume;
    }

    public static void setOriginalVolume(int volume) {
    	originalVolume = volume;
    	return;
    }

    public static void decreaseCurrentVolume(int level) {
		if (previousVolume > 0)
		{
			previousVolume -= level;
			audio.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolume, 0);
			Log.d("AudioSettingContentObserver", "Decreaed volume to level: "+previousVolume);
		}
    	return;
    }
    
    public static int getCurrentVolume() {
    	return previousVolume;
    }    
       
    @Override
    public void onChange(boolean selfChange)
    {
        super.onChange(selfChange);
        //Log.d("AudioSettingsContentObserver", "onChange triggered");
        
    	usefadeOut = PlayerActivity.prefs.getBoolean("fadeout", false);        		

        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
       
        int delta = previousVolume - currentVolume;
               
        if(delta>0)
            previousVolume = currentVolume;

        else if(delta<0)
            previousVolume = currentVolume;

        if (usefadeOut == true && fadingOut == false && delta != 0)
        	originalVolume = previousVolume;
        
        if (usefadeOut == false && delta != 0)
           	originalVolume = previousVolume;
        
    }
}