package com.ssb.droidsound.plugins;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.ssb.droidsound.file.FileSource;

public class USFPlugin extends DroidSoundPlugin {

	static {
		System.loadLibrary("lazyusf");
	}

	private static String extension = "";
	
	private static final String TAG = "USFPlugin";
	
	private static Map<String, String> tagMap = new HashMap<String, String>();
	private static HashMap<Integer, String> infoMap = new HashMap<Integer, String>();
	private long songRef;
	private int frequency;

	
	@Override
	public String getVersion() {
		return "LazyUSF Plugin [Kode54] for N64 format";
	}
	
	@Override
	public boolean canHandle(FileSource fs) {
		extension = fs.getExt().toUpperCase();
		return fs.getExt().equals("USF") || fs.getExt().equals("MINIUSF");
	}
	
	
	@Override
	public void getDetailedInfo(Map<String, Object> list)
	{
		list.put("plugin", "LazyUSF");
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
			String game = tagMap.get("game");
			list.put("game", game);
			tagMap.remove("game");
		}

		if (tagMap.containsKey("title"))
		{
			String title = tagMap.get("title");
			list.put("title", title);
			tagMap.remove("title");
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
			return -1;
		}
		
		else if (what == INFO_FREQUENCY)
		{
			return N_getIntInfo(songRef, INFO_FREQUENCY);
		}
			 
					
		else if(what == INFO_LENGTH)
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
		return infoMap.get(what);
	}
	
	@Override
	public boolean loadInfo(FileSource fs)
	{
		tagMap = PSFFile.getTags(fs.getData(), (int) fs.getLength());

		if (tagMap == null)
		{
			return false;
		}

		infoMap.put(INFO_TITLE, tagMap.get("title"));
		infoMap.put(INFO_GAME, tagMap.get("game"));
		infoMap.put(INFO_COPYRIGHT, tagMap.get("copyright"));
		infoMap.put(INFO_LENGTH, tagMap.get("length"));
		infoMap.put(INFO_AUTHOR, tagMap.get("artist"));
		infoMap.put(INFO_YEAR, tagMap.get("year"));
		return true;

	} 
	@Override
	public boolean load(FileSource fs)
	{
		tagMap = PSFFile.getTags(fs.getData(), (int) fs.getLength());

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
			
		}
		if(libName2 != null)
		{
			lib_fs2 = fs.getRelative(libName2);
			lib_fs2.getFile();
		}
		
		if (libName != null)
			lib_fs.close();
		if (libName2 != null)
			lib_fs2.close();
			
		songRef = N_load(fs.getFile().getPath());
		return ( songRef != 0);
	}

	@Override
	public void unload() 
	{
		if (songRef != 0)
		{
			N_unload(songRef);			
		}
		songRef = 0;
	}
	
	@Override
	public int getBufferSize(int frequency)
	{
		int bufSize = (int) Math.ceil( frequency / 100.0) * 100;
		return bufSize * 4;
	}
	
	native public int N_getIntInfo(long song, int what);
	native public long N_load(String fileName);	
	native public void N_unload(long song);
	native public int N_getSoundData(long song, short [] dest, int size);	
}
