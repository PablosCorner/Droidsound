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
	private static HashMap<Integer, String> infoMap = new HashMap<Integer, String>(); 
	private static HashMap<String, String> tagMap = new HashMap<String, String>();
	
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

		if (tagMap == null)
			return;

		list.put("copyright", tagMap.get("ReleaseDate"));
		list.put("game", tagMap.get("GameNameE"));
		list.put("composer", tagMap.get("AuthorNameE"));
		list.put("system_name", tagMap.get("SystemNameE"));
		list.put("title", tagMap.get("TrackNameE"));
	
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
		return infoMap.get(what);
	}

	@Override
	public boolean loadInfo(FileSource fs)
	{
		
		tagMap = GD3Parser.getTags(fs.getFile().getPath());
		if (tagMap == null)
		{
			return false;
		}
		
		infoMap.put(INFO_TITLE, tagMap.get("TrackNameE"));
		infoMap.put(INFO_GAME, tagMap.get("GameNameE"));
		infoMap.put(INFO_COPYRIGHT, tagMap.get("ReleaseDate"));
		infoMap.put(INFO_AUTHOR, tagMap.get("AuthorNameE"));
		return true;

	} 
	
	@Override
	public boolean load(FileSource fs) 
	{
		tagMap = GD3Parser.getTags(fs.getFile().getPath());

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
