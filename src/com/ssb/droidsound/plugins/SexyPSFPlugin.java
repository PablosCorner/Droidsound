package com.ssb.droidsound.plugins;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.ssb.droidsound.file.FileSource;

public class SexyPSFPlugin extends DroidSoundPlugin {
	private static final String TAG = SexyPSFPlugin.class.getSimpleName();

	static {
		System.loadLibrary("sexypsf");
	}

	
	private long songFile = 0;
	
	private String [] info = null;
	private static Map<String, Integer> optMap = new HashMap<String, Integer>(); // never reset this, HEPlugin uses this for another PSF player
	

	@Override
	public boolean canHandle(FileSource fs) {
		String ext = fs.getExt();
		if (optMap.containsKey("isEnabled"))
		{
			Integer val = optMap.get("isEnabled");
			if (val == 1)
			{
				return ext.equals("PSF") || ext.equals("MINIPSF");
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
			if (v.equals("SexyPSF"))
				optMap.put("isEnabled", 1);
			else
				optMap.put("isEnabled", 0);
				
		}
	}

	
	@Override
	public void getDetailedInfo(Map<String, Object> info) {

		info.put("plugin", "PSF");
		
		String game = getStringInfo(INFO_GAME);
		String copyright = getStringInfo(INFO_COPYRIGHT);

		info.put("format", "PS1");
		if(game != null) {
			info.put("game", game);
		}
		if(copyright != null) {
			info.put("copyright", copyright);
		}
	}
	
	@Override
	public void unload() {
		if(songFile != 0)
			N_unload(songFile);
		else if(info != null)
			info = null;
	}

	@Override
	public boolean load(FileSource fs) {
		
		Map<String, String> tagMap = PSFFile.getTags(fs.getContents(), (int) fs.getLength());
		info = new String [128];
		if(tagMap != null) {
			info[INFO_TITLE] = tagMap.get("title");
			info[INFO_AUTHOR] = tagMap.get("artist");
			info[INFO_GAME] = tagMap.get("game");
			info[INFO_COPYRIGHT] = tagMap.get("copyright");
			info[INFO_LENGTH] = tagMap.get("length");
		}
		String libName = tagMap.get("_lib");
		FileSource fs2 = null;
		if(libName != null) {
			fs2 = fs.getRelative(libName);
			fs2.getFile();
		}
		libName = tagMap.get("_lib2");
		FileSource fs3 = null;
		if(libName != null) {
			fs3 = fs.getRelative(libName);
			fs3.getFile();
		}
		songFile = N_load(fs.getFile().getPath());
		fs.close();
		if(fs2 != null)
			fs2.close();
		if(fs3 != null)
			fs3.close();
		return true;
		
		
	}
		
	private static String fromData(byte [] data, int start, int len) throws UnsupportedEncodingException {
		int i = start;
		for(; i<start+len; i++) {
			if(data[i] == 0) {
				i++;
				break;
			}
		}
		return new String(data, start, i-start, "ISO-8859-1").trim();
	}
	
	@Override
	public boolean loadInfo(FileSource fs) {
		
		byte [] module = fs.getContents();
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
	
	//@Override
	//public boolean loadInfo(File file) throws IOException {
//		return load(file);
	//}

	@Override
	public int getSoundData(short[] dest, int size) {
		return N_getSoundData(songFile, dest, size);
	}

	@Override
	public String getStringInfo(int what) {
		
		if(info != null) {
			return info[what];
		}
		
		
		return N_getStringInfo(songFile, what);
	}

	@Override
	public int getIntInfo(int what) {
		if(info != null) {
			if(what == INFO_LENGTH) {				
				return PSFFile.parseLength(info[what]);
			}
			return 0;
		}
		return N_getIntInfo(songFile, what);
	}
	
	@Override
	public String getVersion() {
		return "sexyPSF 0.4.7";
	}

	native public long N_load(String fileName);	
	native public void N_unload(long song);
	native public int N_getSoundData(long song, short [] dest, int size);
	native public String N_getStringInfo(long song, int what);
	native public int N_getIntInfo(long song, int what);

}
