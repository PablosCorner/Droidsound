package com.ssb.droidsound.plugins;

import java.util.HashMap;
import java.util.Map;

import com.ssb.droidsound.PlayerActivity;
import com.ssb.droidsound.file.FileSource;

public class VGMPlugin extends DroidSoundPlugin {

	static 
	{
		System.loadLibrary("vgmplay");
	}
	
	private long songRef;
	private static String extension = "";
	private static HashMap<String, String> infoMap = new HashMap<String, String>();

	@Override
	public String getVersion() {
		return "VGMPlay 0.40_4 by Valley Bell";
	}
		
	@Override
	public boolean canHandle(FileSource fs) {
		boolean res = PlayerActivity.prefs.getBoolean("use_vgmplay", false);
		if (!res)
			return false;
					
		extension = fs.getExt().toUpperCase();
		return fs.getExt().equals("VGM") || fs.getExt().equals("VGZ");
	}
	
	
	@Override
	public void getDetailedInfo(Map<String, Object> list)
	{
		list.put("plugin", "VGMPlay");
		list.put("format", extension);

		if (infoMap == null)
			return;

		list.put("copyright", infoMap.get("ReleaseDate"));
		list.put("game", infoMap.get("GameNameE"));
		list.put("composer", infoMap.get("AuthorNameE"));
		list.put("system_name", infoMap.get("SystemNameE"));
		list.put("title", infoMap.get("TrackNameE"));
		/*
		list.put("platform", infoMap.get("platform"));
		list.put("creator", creator);
		list.put("notes", notes);
		*/
		
	}
	
	@Override
	public int getIntInfo(int what) {
		return 0;
	}

	@Override
	public int getSoundData(short[] dest, int size)
	{
		return N_getSoundData(songRef, dest, size);
	}

	@Override
	public String getStringInfo(int what) {
		return null;
	}

	@Override
	public boolean load(FileSource fs) 
	{
		
		infoMap = GD3Parser.getTags(fs.getFile().getPath());
		
		songRef = N_load(fs.getFile().getPath());

		return songRef != 0;
	}

	@Override
	public void unload() {
		if (songRef != 0)
		{
			N_unload(songRef);
			songRef = 0;
		}
		
	}

	native public long N_load(String filename);
	native public void N_unload(long song);
	native public int N_getSoundData(long song, short [] dest, int size);	
}
