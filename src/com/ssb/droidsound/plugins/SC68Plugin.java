package com.ssb.droidsound.plugins;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import java.util.regex.*;

import android.annotation.SuppressLint;
import android.os.Environment;

import com.ssb.droidsound.file.FileSource;
import com.ssb.droidsound.utils.Log;
import com.ssb.droidsound.utils.Unzipper;

public class SC68Plugin extends DroidSoundPlugin
{
	private static final String TAG = SC68Plugin.class.getSimpleName();
	static {
		System.loadLibrary("sc68");
	}
	
	public static final int SC68_OPT_ASID = 1;
	public static final int SC68_OPT_LOOPING = 2;
	
	private long currentSong;
	private static Object lock = new Object();
	private static boolean inited = false;
	
	@SuppressLint("UseSparseArrays")
	private static HashMap<Integer, Integer> optMap = new HashMap<Integer, Integer>();
	private static HashMap<String, String> infoMap = new HashMap<String, String>();
	
	private File sc68Dir;
	private long pluginRef;
	
	private static String title = null;
	private static String composer = null;
	private static String year = null;
	private static String type = null;
	
	private Unzipper unzipper = null;

	private boolean done_fileinfo = false;
	
	public SC68Plugin() {

		File droidDir = new File(Environment.getExternalStorageDirectory(), "droidsound");
		sc68Dir = new File(droidDir, "sc68data");
		synchronized (lock) {					
			if(!sc68Dir.exists()) {
				droidDir.mkdir();
				unzipper = Unzipper.getInstance();
				unzipper.unzipAssetAsync(getContext(), "sc68data.zip", droidDir);
			}
			if(!inited) {
				N_setDataDir(sc68Dir.getPath());
				inited = true;
			}
		}
	}
	
	
	//
	// collect all the options to one place
	//
	static String [] sc68options = new String [] { "aSIDfilter", "looping" };
	static int [] optvals = new int [] { SC68_OPT_ASID, SC68_OPT_LOOPING };
	
	@Override
	public void setOption(String opt, Object val)
	{
		int v  = -1;
		if(val instanceof Boolean)
		{
			v = (Boolean)val ? 1 : 0;
		} 
		else if(val instanceof Integer)
		{
			v = (Integer)val;
		}
		
		for(int i=0; i < sc68options.length; i++)
		{
			if(sc68options[i].equals(opt))
			{
				optMap.put(optvals[i], v);
				break;
			}
		}
	}
	
	@Override
	public boolean canHandle(FileSource fs)
	{
		String ext = fs.getExt();
		return(ext.equals("SNDH") || ext.equals("SC68") || ext.equals("SND"));
	}
	
	@Override
	public void unload()
	{
		Log.d(TAG, "Unloading");
		if(currentSong != 0)
			N_unload(currentSong);		
	}

	@Override
	public boolean load(FileSource fs) {
		
		// fill the usual infos from SNDH header
		loadInfo(fs);
				
		int loop_mode = 1;
		if(unzipper != null) {
			while(!unzipper.checkJob("sc68data.zip")) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return false;
				}
			}
			unzipper = null;
		}
		
		Log.d(TAG, "Trying to load '%s' -> %d", fs.getName(), currentSong);
		currentSong = N_load(fs.getContents(), (int) fs.getLength());
			
		
		// set the options here
		for(Entry<Integer, Integer> e : optMap.entrySet())
		{
			
			if (e.getKey() == SC68_OPT_LOOPING && e.getValue() == 1)
			{
				loop_mode = -1; //loop forever
			}
			else if(e.getKey() == SC68_OPT_LOOPING && e.getValue() == 0)
			{
				loop_mode = 0; //loop once
			}
			
			else
			{
				N_setOption(currentSong, e.getKey(), e.getValue());
			}
			
		}
		
		
		N_PlaySong(currentSong, 1, loop_mode);
		
		return (currentSong != 0);
	}
	
	private static byte[] targetBuffer;
	
	
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
	
	private static final String [] hws = { "?", "ST", "STE", "YM+STE", "Amiga", "Amiga+ST", "Amiga+STE", "Amiga++" }; 
	
	@Override
	public void getDetailedInfo(Map<String, Object> info) {
		
		String replay = getStringInfo(52);
		String hwname = getStringInfo(51);
		int hwbits = getIntInfo(50);
		if(replay == null) replay = "?";
		if(hwname == null) hwname = "?";

		int can_asid = getIntInfo(101);
		String ripper = getStringInfo(102);
		String converter = getStringInfo(103);
		String format = getStringInfo(105);
		String rate = getStringInfo(104);
		
		String asid = can_asid == 1 ? "Yes" : "No";
		
		info.put("plugin", "SC68");
		info.put("replay", replay);
		info.put("format", format.toUpperCase());
		info.put("replayrate", rate);
		info.put("hardware", hwname);		
		info.put("platform", hws[hwbits]);
		info.put("can_asid", asid);
		if (ripper != null)
		{
			info.put("ripper", ripper);
		}
		if (converter != null)
		{
			info.put("converter", converter);
		}
		String timer = "";
		if (infoMap.containsKey("TIMER"))
		{
			timer = infoMap.get("TIMER");
			timer = timer.replace("T", "Timer-");
			info.put("timer", timer);
		}
		String copyright = "";
		if (infoMap.containsKey("YEAR"))
		{
			copyright = infoMap.get("YEAR");
			info.put("copyright", copyright);
		}
		infoMap.clear();
		
		
	}

	@Override
	public boolean loadInfo(FileSource fs)
	{

		currentSong = 0;
		
		title = null;
		composer = null;
		year = null;
		type = null;
		infoMap.clear();

		byte module [] = fs.getContents();
		int size = (int) fs.getLength();
		byte data [] = module;
		String head = new String(module, 0, 4);
		
		if(head.equals("ICE!"))
		{
			
			Log.d(TAG, "Unicing");
			
			if(targetBuffer == null)
			{
				targetBuffer = new byte [1024*1024];
			}

			module = N_unice(module);
			if(module == null)
				return false;
			size = module.length;
			data = module;
		}
		
		String header = new String(data, 12, 4);
		String header2 = new String(data, 0, 16);
		String sndh_data = new String(data,16,1024);
		
		if(header.equals("SNDH"))
		{
			Log.d(TAG, "Found SNDH");
			type = "SNDH";
				
	        String[] patterns = new String[] {"YEAR\\d+", "CONV[\\x20-\\x80]+", "RIPP[\\x20-\\x80]+", "COMM[\\x20-\\x80]+", "TITL[\\x20-\\x80]+", "T[ABCD]\\d+"};
	        String[] pattnames = new String[] {"YEAR", "CONVERTER", "RIPPER", "COMPOSER", "TITLE", "TIMER"};

	        for (int i=0; i<patterns.length; i++)
	        {
	            Pattern p = Pattern.compile(patterns[i]);
	            Matcher m = p.matcher(sndh_data);
	            String val = "";
	            if (m.find())
	            {
	                String field_name = pattnames[i];
	                if (field_name.contains("TIMER"))
	                {
	                    val = m.group(0).substring(0, 2);
	                    infoMap.put(field_name, val);
	                }
	                else
	                {
	                    val = m.group(0).substring(4);
	                    infoMap.put(field_name, val);
	                }

	                if (field_name.contains("COMPOSER"))
	                	composer = val;
	                else if (field_name.contains("TITLE"))
	                	title = val;
	                else if (field_name.contains("YEAR"))
	                	year = val;

	            }
	        }
			return true;
			
		} 
		else if(header2.equals("SC68 Music-file "))
		{
			int offset = 56;
			type = "SC68";
			while(offset < 1024) {
				String tag = new String(data, offset, 4);
				int tsize = data[offset+4] | (data[offset+5]<<8) | (data[offset+6]<<16) | (data[offset+7]<<24);
				offset += 8;
				Log.d(TAG, "TAG: %s, size %d", tag, tsize);
				if(tsize < 0 || tsize > size) {
					break;
				}
				try {
					if(tag.equals("SCMN")) {
						title = fromData(data, offset, tsize);
						Log.d(TAG, "TITLE: %s", title);
					} else if(tag.equals("SCFN")) {
						if(title != null) {
							title = fromData(data, offset, tsize);
							Log.d(TAG, "TITLE: %s", title);
						}
					} else if(tag.equals("SCAN")) {
						composer = fromData(data, offset, tsize);
					} else if(tag.equals("SC68")) {
						tsize = 0;
					} else if(tag.equals("SCEF") || tag.equals("SCDA")) {
						Log.d(TAG, "END");
						break;
					} else {			
					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				offset += tsize;
			}			
			return true;
		}

		return false;
	}
	

	@Override
	public int getSoundData(short[] dest, int size)
	{
		return N_getSoundData(currentSong, dest, size);
	}

	@Override
	public String getStringInfo(int what)
	{
		if(currentSong == 0)
		{
			switch(what)
			{
				case INFO_TITLE:
					return title;
				case INFO_AUTHOR:
					return composer;
				case INFO_COPYRIGHT:
					return year;
				case INFO_TYPE:
					return type;
			}			
			return null;
		}
		return N_getStringInfo(currentSong, what);
	}

	@Override
	public int getIntInfo(int what)
	{
		if(currentSong == 0)
		{
			return -1;
		}
		int rc = N_getIntInfo(currentSong, what);
		//if(what == INFO_LENGTH && rc == 90*1000)
		//	rc = -1;
		return rc;
	}
	
	@Override
	public boolean setTune(int tune)
	{		
		Log.d(TAG, "Set tune %d", tune);
		return N_setTune(currentSong, tune);
	}
	
	@Override
	public boolean seekTo(int msec) {
		return N_seekTo(currentSong, msec);
	}
	
	@Override
	public String getVersion()
	{
		return "Version 3.0.0b [r409]\nCopyright (C) 2013 Benjamin Gerard";
	}
	
	native public static int N_setOption(long song, int what, int val); 
	
	native public long N_PlaySong(long song, int track, int loop_mode);
	native public long N_load(byte [] module, int size);
	native public long N_loadInfo(byte [] module, int size);
	native public void N_unload(long song);
	
	// Expects Stereo, 44.1Khz, signed, big-endian shorts
	native public int N_getSoundData(long song, short [] dest, int size);	
	native public boolean N_seekTo(long song, int seconds);
	native public boolean N_setTune(long song, int tune);
	native public String N_getStringInfo(long song, int what);
	native public int N_getIntInfo(long song, int what);
	
	native public void N_setDataDir(String dataDir);
	native public byte[] N_unice(byte [] data);
}
