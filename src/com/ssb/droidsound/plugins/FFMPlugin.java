package com.ssb.droidsound.plugins;

import java.util.HashMap;
import java.util.Map;
import com.ssb.droidsound.file.FileSource;

public class FFMPlugin extends DroidSoundPlugin {

	static {
		System.loadLibrary("ffmpeg");
	}

	private static String extension = "";
	
	private static Map<String, String> tagMap = new HashMap<String, String>();
	
	@Override
	public String getVersion() {
		return "FFmpeg 2.1";
	}
	
	private long songRef;
	@Override
	public boolean canHandle(FileSource fs) 
	{
		extension = fs.getExt().toUpperCase();
		return fs.getExt().equals("AT3");
	}
	
	
	@Override
	public void getDetailedInfo(Map<String, Object> list)
	{
		list.put("plugin", "FFmpeg");
		list.put("format", extension);
	}
	
	@Override
	public int getIntInfo(int what)
	{
	    return N_getIntInfo(songRef, what);
	} 
	
	@Override
	public int getSoundData(short[] dest, int size)
	{
		return N_getSoundData(songRef, dest, size);
	}

	
	@Override
	public String getStringInfo(int what)
	{
		return null;
	}

	@Override
	public boolean load(FileSource fs)
	{
		
		songRef = N_loadFile(fs.getFile().getPath());
		return ( songRef != 0);
	}

	@Override
	public void unload()
	{
		N_unload(songRef);
	}

	native public long N_loadFile(String fileName);	
	native public void N_unload(long song);
	native public int N_getSoundData(long song, short [] dest, int size);	
	native public int N_getIntInfo(long song, int what);
 
}
