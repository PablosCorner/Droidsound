package com.ssb.droidsound.plugins;

import java.util.HashMap;
import java.util.Map;

import com.ssb.droidsound.file.FileSource;

public class HQPlugin extends DroidSoundPlugin {

	static {
		System.loadLibrary("highlyquix");
	}

	private static String extension = "";
	
	private static Map<String, String> tagMap = new HashMap<String, String>();
	
	@Override
	public String getVersion() {
		return "Highly Quixotic Replay for QSF/MiniQSF";
	}
	
	private long songRef;
	@Override
	public boolean canHandle(FileSource fs) 
	{
		extension = fs.getExt().toUpperCase();
		return fs.getExt().equals("QSF") || fs.getExt().equals("MINIQSF");
	}
	
	
	@Override
	public void getDetailedInfo(Map<String, Object> list)
	{
		list.put("plugin", "Highly Quixotic");
		list.put("format", extension);
		if (tagMap == null)
		{
			return;
		}

		if (tagMap.containsKey("artist"))
		{
			String composer = tagMap.get("artist");
			list.put("composer", composer);
			tagMap.remove("artist");
		}
		if (tagMap.containsKey("game"))
		{
			String title = tagMap.get("game");
			list.put("title", title);
			tagMap.remove("game");
		}

		for (Map.Entry<String, String> entry : tagMap.entrySet())
		{
			list.put(entry.getKey(), entry.getValue());
		}
	}
	
	@Override
	public int getIntInfo(int what)
	{
		if (tagMap == null)
		{
			return 0;
		}
		
		if(what == INFO_LENGTH)
		{
			return PSFFile.parseLength(tagMap.get("length"));				
		}

		return 0;
	}

	@Override
	public int getSoundData(short[] dest, int size) {
		return N_getSoundData(songRef, dest, size);
	}

	@Override
	public String getStringInfo(int what) {
		return null;
	}

	@Override
	public boolean load(FileSource fs)
	{
		tagMap = PSFFile.getTags(fs.getContents(), (int) fs.getLength());
		
		String libName = null;
		FileSource lib_fs = null;
		if (tagMap != null)
		{
			libName = tagMap.get("_lib");
		}

		if(libName != null)
		{
			lib_fs = fs.getRelative(libName);
			lib_fs.getFile();
			lib_fs.close();
		}
		
		songRef = N_load(fs.getFile().getPath());
		return ( songRef != 0);
	}

	@Override
	public void unload() {
		N_unload(songRef);
	}

	native public long N_load(String fileName);	
	native public void N_unload(long song);
	native public int N_getSoundData(long song, short [] dest, int size);	
}
