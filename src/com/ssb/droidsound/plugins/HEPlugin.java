package com.ssb.droidsound.plugins;

import java.util.HashMap;
import java.util.Map;

import com.ssb.droidsound.file.FileSource;


public class HEPlugin extends DroidSoundPlugin {
	private static final String TAG = HEPlugin.class.getSimpleName();

	static {
		System.loadLibrary("highlyexp");
	}
	
	private String [] info = null;
	private static String extension = "";
	private static Map<String, String> tagMap = new HashMap<String, String>();
	private static Map<String, Integer> optMap = new HashMap<String, Integer>();
	
	
	@Override
	public String getVersion()
	{
		return "Highly Experimental Library for PSF/PSF2 formats by kode54";
	}
	
	private long songRef;
	@Override
	public boolean canHandle(FileSource fs)
	{
		//
		// check here from optMap if this engine is enabled
		//
		if (optMap.isEmpty())
		{
			optMap.put("isEnabled", 1); // default value, since HighlyEXP is enabled by default
		}
		
		
		if (optMap.containsKey("isEnabled"))
		{
			Integer enabled = optMap.get("isEnabled");
			if (enabled == 1)
			{
				extension = fs.getExt().toUpperCase();
				return fs.getExt().equals("PSF") || 
						fs.getExt().equals("MINIPSF") || 
						fs.getExt().equals("PSF2") || 
						fs.getExt().equals("MINIPSF2");
			}
			
		}
		return false;
		
	}
	
	@Override
	public void setOption(String opt, Object val)
	{
				
		if (opt.equals("psfengine"))
		{
			String v = val.toString();
			if (v.equals("HighlyEXP"))
				optMap.put("isEnabled", 1);
			else
				optMap.put("isEnabled", 0);
				
		}
	}
	
	
	@Override
	public void getDetailedInfo(Map<String, Object> list)
	{
		list.put("plugin", "Highly Experimental");
		list.put("format", extension);
		if (tagMap == null)
		{
			return;
		}
		
		if (tagMap.containsKey("title"))
		{
			list.put("title", tagMap.get("title"));
		}
		
		if (tagMap.containsKey("artist"))
		{
			list.put("composer", tagMap.get("artist"));
		}
		
		if (tagMap.containsKey("year"))
		{
			list.put("year", tagMap.get("year"));
		}

		if (tagMap.containsKey("copyright"))
		{
			list.put("copyright", tagMap.get("copyright"));
		}
		
		if (tagMap.containsKey("game"))
		{
			list.put("title", tagMap.get("game"));
		}

		if (tagMap.containsKey("psfby"))
		{
			list.put("psfby", tagMap.get("psfby"));
		}

	}
	
	@Override
	public int getIntInfo(int what)
	{
		if (tagMap == null)
			return 0;
		if(what == INFO_LENGTH)
		{
			return PSFFile.parseLength(tagMap.get("length"));
		}
		
		return 0;
	}

	@Override
	public int getSoundData(short[] dest, int size)
	{
		return N_getSoundData(songRef, dest, size);
	}

	@Override
	public String getStringInfo(int what) {
		
		if(info != null)
		{
			return info[what];
		}
		
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
			lib_fs.getFile();
			lib_fs.close();
		}
		if(libName2 != null)
		{
			lib_fs2 = fs.getRelative(libName2);
			lib_fs2.getFile();
			lib_fs2.close();
		}
		songRef = N_load(fs.getFile().getPath());
		return (songRef != 0);
	}

	@Override
	public boolean loadInfo(FileSource fs)
	{
		info = new String [128];
		
		tagMap = PSFFile.getTags(fs.getContents(), (int) fs.getLength());
		
		if(tagMap != null)
		{
			info[INFO_TITLE] = tagMap.get("title");
			info[INFO_AUTHOR] = tagMap.get("artist");
			info[INFO_GAME] = tagMap.get("game");
			info[INFO_COPYRIGHT] = tagMap.get("copyright");
			info[INFO_LENGTH] = tagMap.get("length");
			return true;
		}
		return false;
	}

	
	@Override
	public void unload()
	{
		if (songRef != 0)
			N_unload(songRef);
		return;
	}

	
	native public long N_load(String fileName);	
	native public void N_unload(long song);
	native public int N_getSoundData(long song, short [] dest, int size);	
}
