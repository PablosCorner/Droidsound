package com.ssb.droidsound.plugins;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.io.File;

import com.ssb.droidsound.file.FileSource;

public class GSFPlugin extends DroidSoundPlugin {
	
	static {
		System.loadLibrary("playgsf");
	}

	private Set<String> extensions;
	
	static String [] ex = { "GSF", "MINIGSF" };
	private static Map<String, String> tagMap = new HashMap<String, String>();
	long currentSong = 0;

	private String[] info;
	private static String extension = "";
	private FileSource libFile;

	public GSFPlugin() {
		extensions = new HashSet<String>();
		for(String s : ex) {			
			extensions.add(s);
		}
	}
	
	@Override
	public boolean canHandle(FileSource fs) {
		extension = fs.getExt().toUpperCase();
		return extensions.contains(fs.getExt());
	}

	
	@Override
	public void getDetailedInfo(Map<String, Object> list) {
		
		list.put("plugin", "GSFPlugin");
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
	public boolean load(FileSource fs) {
				
		tagMap = PSFFile.getTags(fs.getData(), (int) fs.getLength());
		info = new String [128];
		if(tagMap != null) {			
			info[INFO_TITLE] = tagMap.get("title");
			info[INFO_AUTHOR] = tagMap.get("artist");
			info[INFO_GAME] = tagMap.get("game");
			info[INFO_COPYRIGHT] = tagMap.get("copyright");
			info[INFO_LENGTH] = tagMap.get("length");
		}
		
		String libName = tagMap.get("_lib");

		//
		// check the lib name and use matching filename '_' -> '-' problem
		//
		
		
		if(libName != null)
		{
			FileSource fs2 = null;
			fs2 = FileSource.create(fs.getrefPath() + libName);
			File file = fs2.getFile();
			fs2.close();
			if (file == null)
			{
				// try to get it by extension
				
				return false;
			}
		}
	
		 		
		currentSong = N_loadFile(fs.getFile().getPath());
		fs.close();
			
		return (currentSong != 0);
	}
	
	@Override
	public boolean loadInfo(FileSource fs) {
		
		byte [] module = fs.getData();
		int size = (int) fs.getLength();
		
		//ByteBuffer src = ByteBuffer.wrap(module, 0, size);		
		//src.order(ByteOrder.LITTLE_ENDIAN);		
		//byte[] id = new byte[4];
		//src.get(id);
		
		//for(int i=0; i<128; i++)
		//	info[i] = null;
		
		info = new String [128];
		 
		Map<String, String> tagMap = PSFFile.getTags(module, size);
		if(tagMap != null) {
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
	public String getStringInfo(int what) {
		
		if(info != null) {
			return info[what];
		}
		return null;
				
	}

	@Override
	public int getIntInfo(int what) {
		if(info != null) {
			if(what == INFO_LENGTH) {				
				return PSFFile.parseLength(info[what]);
			}
		}
		return 0;

	}

	@Override
	public void unload() {
		if (currentSong != 0){
			N_unload(currentSong);
		}
		currentSong = 0;
	}
	
	@Override
	public String getVersion() {
		return "playgsf 0.7.1";
	}
	
	// Expects Stereo, 44.1Khz, signed, big-endian shorts
	@Override
	public int getSoundData(short [] dest, int size) { return N_getSoundData(currentSong, dest, size); }	

	// NATIVE
	
	native public long N_loadFile(String name);
	native public void N_unload(long song);
	native public int N_getSoundData(long song, short [] dest, int size);	
}
