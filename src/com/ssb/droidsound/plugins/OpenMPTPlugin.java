package com.ssb.droidsound.plugins;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ssb.droidsound.PlayerActivity;
import com.ssb.droidsound.file.FileSource;

public class OpenMPTPlugin extends DroidSoundPlugin {

	static {
		System.loadLibrary("openmpt");
	}
	
	private static String extension = "";
	private long songRef;	
	private boolean loopmode = false;
	
	@Override
	public void setOption(String opt, Object val)
	{
		if (opt == "genericLoop")
			loopmode = (Boolean)val;
		
		return;
	}

	@Override
	public String getVersion() {
		return "libOpenMPT v0.2.3596";
	}
	
	private static final Set<String> EXTENSIONS = new HashSet<String>(Arrays.asList(
			"MOD", "S3M", "XM", "IT", "MPTM", "STM", "NST", "M15", "STK", "WOW", "ULT", "669",
			"MTM", "MED", "FAR", "MDL", "AMS", "DSM", "AMF", "OKT", "DMF", "PTM", "PSM", "MT2",
			"DBM", "DIGI", "IMF", "J2B", "GDM", "UMX")); 

	@Override
	public boolean canHandle(FileSource fs) {
		extension = fs.getExt().toUpperCase();
		if (EXTENSIONS.contains(extension))
			return true;
		return false;

	}
		
	@Override
	public void getDetailedInfo(Map<String, Object> list) {
		
		if (songRef == 0)
			return;
		
		int channels = N_getIntInfo(songRef, 101);
		String instruments = N_getStringInfo(songRef, 100);
		
		list.put("plugin", "OpenMPT");
		list.put("format", extension);
		list.put("channels", channels);
		if (instruments != null && instruments.length() > 0) {
			String[] instrArray = instruments.split("\\n");
			list.put("instruments", instrArray);
		}
		
	}
	
	@Override
	public int getIntInfo(int what) {
		if (songRef == 0)
			return -1;
		return N_getIntInfo(songRef, what); 
		
	}

	@Override
	public int getSoundData(short[] dest, int size) {
		return N_getSoundData(songRef, dest, size);
	}

	@Override
	public String getStringInfo(int what) {
		return null;
		//return N_getStringInfo(songRef, what);
	}

	@Override
	public boolean load(FileSource fs) {
		loopmode = PlayerActivity.prefs.getBoolean("generic_loop", false);
		songRef = N_load(fs.getFile().getPath(), loopmode);
		return songRef != 0;
	}

	@Override
	public void unload() {
		if (songRef != 0)
		{
			N_unload(songRef);
		}
		songRef = 0;
	}
	
	@Override
	public boolean seekTo(int seconds) {
		return N_seekTo(songRef, seconds);
	}

	native public boolean N_seekTo(long song, int seconds);
	native public boolean N_setTune(long song, int tune);
	native public String N_getStringInfo(long song, int what); 
	native public int N_getIntInfo(long song, int what);
	native public long N_load(String filename, boolean loopmode);
	native public void N_unload(long song);
	native public int N_getSoundData(long song, short [] dest, int size);	
}
