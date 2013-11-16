package com.ssb.droidsound.plugins;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.ssb.droidsound.file.FileSource;

public class HTPlugin extends DroidSoundPlugin {

	static {
		System.loadLibrary("highlytheo");
	}

	private static String extension = "";
	
	private static final String TAG = "HTPlugin";
	
	private static Map<String, String> tagMap = new HashMap<String, String>();
	
	@Override
	public String getVersion() {
		return "Highly Theoretical Replay for DreamCast/Saturn formats";
	}
	
	private long songRef;
	@Override
	public boolean canHandle(FileSource fs) 
	{
		extension = fs.getExt().toUpperCase();
		return fs.getExt().equals("SSF") || 
				fs.getExt().equals("DSF") || 
				fs.getExt().equals("MINIDSF") || 
				fs.getExt().equals("MINISSF");
	}
	
	
	@Override
	public void getDetailedInfo(Map<String, Object> list)
	{
		list.put("plugin", "Highly Theoretical");
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
		if (tagMap != null)
			return -1;
		if(what == INFO_LENGTH)
		{
			return PSFFile.parseLength(tagMap.get("length"));
		}

		return -1;
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

		FileSource lib_fs = null;
		FileSource lib_fs2 = null;

		String libName = null;
		String libName2 = null;
		
		if (tagMap != null)
		{
			libName = tagMap.get("_lib");
			libName2 = tagMap.get("_lib2");
		}
		
		if(libName != null)
		{
			lib_fs = fs.getRelative(libName);
			Log.d(TAG,libName);
			
			File res = lib_fs.getFile();
			if (res == null)
			{
				lib_fs.close();
				return false;
			}
			lib_fs.close();
		}
		if(libName2 != null)
		{
			lib_fs2 = fs.getRelative(libName2);
			lib_fs2.getFile();
			lib_fs2.close();
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
