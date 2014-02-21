package com.ssb.droidsound.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.*;

import com.ssb.droidsound.PlayerActivity;
import com.ssb.droidsound.file.FileSource;

public class GMEPlugin extends DroidSoundPlugin {
	private static final String TAG = GMEPlugin.class.getSimpleName();
	
	static
	{
		System.loadLibrary("gme");
	}
	private static String extension = "";
	private static HashMap<String, String> infoMap = new HashMap<String, String>();
	private Set<String> extensions;
	
	static String [] ex = { "SGC", "SFM", "SPC", "GYM", "NSF", "NSFE", "GBS", "AY", "SAP", "HES", "KSS" };
	
	long currentSong = -1;
	private int currentFrames;
	private int songLength;
	private boolean use_vgm_vgz = false;
	private Integer loopMode = 0;

	public GMEPlugin()
	{
		extensions = new HashSet<String>();
		for(String s : ex)
		{			
			extensions.add(s);
		}
	}
		
	@Override
	public boolean canHandle(FileSource fs)
	{
		extension = fs.getExt();

		boolean res = PlayerActivity.prefs.getBoolean("use_vgmplay", false);
		if (!res)
		{
			extensions.add("VGM");
			extensions.add("VGZ");
		}
		else
		{
			if (extensions.contains("VGM"))
				extensions.remove("VGM");
			if (extensions.contains("VGZ"))
				extensions.remove("VGZ");

		}
		return extensions.contains(fs.getExt());
	}

	
	@Override
	public void getDetailedInfo(Map<String, Object> list)
	{
		list.put("plugin", "GME");
		if (infoMap == null)
			return;

		if (extension.startsWith("VG")) {
			list.put("format", infoMap.get("SystemNameE"));
			list.put("copyright", infoMap.get("ReleaseDate"));
			list.put("game", infoMap.get("GameNameE"));
			list.put("title", infoMap.get("TrackNameE"));
			list.put("composer", infoMap.get("AuthorNameE"));
		}
	}
	
	@Override
	public boolean load(FileSource fs) {
		currentFrames = 0;
		
		if (extension.contains("VGM") || extension.contains("VGZ"))
			infoMap = GD3Parser.getTags(fs.getFile().getPath());
						
		if (isGZIPPED(fs)) // is packed with zlib
		{
			currentSong = N_loadFile(fs.getFile().getPath());
		}
		
		else if (fs.getExt().equals("GYM"))
		{
			//check if its packed, if packed then unpack it with zlib
			
			byte[] contents = new byte[(int)fs.getLength()];
			contents = fs.getData();
			
			if (contents[428] != 0)
			{
				int decompress_size = 0;
				decompress_size = ( ((contents[426] & 0xff)<<16) | ((contents[425] & 0xff)<<8) | (contents[424] & 0xff)) & 0x00ffffff;
								
				byte[] decompress_buffer = new byte[(int)decompress_size];
				int bytestodecompress = (int)fs.getLength() - 428; // 428 is size of GYM header
				
				Inflater inflater = new Inflater();
				inflater.setInput(contents, 428, bytestodecompress);
				
				try
				{
					inflater.inflate(decompress_buffer);
				} 
				catch (DataFormatException e)
				{
					e.printStackTrace();
					currentSong = 0;
					return false;
					
				}
				byte[] decompressed_contents =  new byte[(int)decompress_size + 428];
				byte[] nullbuf =  new byte[4];
				
				System.arraycopy(contents, 0, decompressed_contents, 0, 428);
				System.arraycopy(decompress_buffer, 0, decompressed_contents, 428, decompress_size);
				System.arraycopy(nullbuf, 0, decompressed_contents, 424, 4);
				
				currentSong = N_load(decompressed_contents, decompressed_contents.length);
			}
				
				
			}
			else
			{
				currentSong = N_load(fs.getData(), (int) fs.getLength());
			}
		
		
		if(currentSong != 0)
			songLength = N_getIntInfo(currentSong, INFO_LENGTH);
		
		return (currentSong != 0);
	}
	
	public boolean isGZIPPED(FileSource fs)
	{
		
		byte[] data = new byte[4];
		File file = fs.getFile();
		try 
		{
			FileInputStream filereader = new FileInputStream(file);
			filereader.read(data, 0, 4);
			filereader.close();
		} 
		catch (FileNotFoundException e1)
		{
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (data[0] == 0x1f && ((data[1] & 0xff) == 0x8b) && data[2] == 0x08) // is packed with zlib
		{
			return true;
		}
		else
		{
			return false;	
		}
		
		
	}
	@Override
	public boolean loadInfo(FileSource fs) 
	{
		if (isGZIPPED(fs))
		{
			currentSong = N_loadFile(fs.getFile().getPath());
		}

		else
			currentSong = N_load(fs.getData(), (int) fs.getLength());
		return (currentSong != 0);
	}

	@Override
	public void unload() {
		if(currentSong != 0)
		{
			N_unload(currentSong);
			currentSong = 0;
		}
		
	}
	
	@Override
	public String getVersion() {
		return "Game Music Emu v0.60 [Kode54 version]";
	}
	
	
	@Override
	public int getSoundData(short [] dest, int size)
	{
		int len = N_getSoundData(currentSong, dest, size);
		
		currentFrames += len/2;
		if(loopMode == 0 && songLength > 0 && currentFrames / 44100 >= (songLength/1000))
			return -1;
		
		return len;
	}
	
	@Override
	public void setOption(String opt, Object val)
	{
		// this is because VGMPlay also plays VGM/VGZ files.
		boolean res = PlayerActivity.prefs.getBoolean("use_vgmplay", false);
		if (res)
		{
			use_vgm_vgz = true;			
		}
		
		if(opt.equals("loop"))
		{
			loopMode  = (Integer)val;
		}
	}

	
	@Override
	public boolean seekTo(int seconds)
	{ 
		return N_seekTo(currentSong, seconds); 
	}
	
	@Override
	public boolean setTune(int tune)
	{
		
		boolean rc = N_setTune(currentSong, tune);
		if(rc)
		{
			currentFrames = 0;
			songLength = N_getIntInfo(currentSong, INFO_LENGTH);
		}
		return rc;	

	}
	@Override
	public String getStringInfo(int what)
	{
		if(currentSong == -1)
		{
			return null;
		}
		return N_getStringInfo(currentSong, what);
	}
	
	@Override
	public int getIntInfo(int what)
	{
		if(currentSong == -1)
		{
			return 0;
		}
		return N_getIntInfo(currentSong, what);
	}

	native public boolean N_canHandle(String name);
	native public long N_load(byte [] module, int size);
	native public long N_loadFile(String name);
	native public void N_unload(long song);
	
	// Expects Stereo, 44.1Khz, signed, big-endian shorts
	native public int N_getSoundData(long song, short [] dest, int size);	
	native public boolean N_seekTo(long song, int seconds);
	native public boolean N_setTune(long song, int tune);
	native public String N_getStringInfo(long song, int what);
	native public int N_getIntInfo(long song, int what);

}
