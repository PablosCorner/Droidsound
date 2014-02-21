package com.ssb.droidsound.plugins;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import com.ssb.droidsound.PlayerActivity;
import com.ssb.droidsound.file.FileSource;
import com.ssb.droidsound.utils.Log;
import com.ssb.droidsound.utils.Unzipper;

public class SidplayfpPlugin extends DroidSoundPlugin 
{
	static {
		System.loadLibrary("sidplayfp");
		extract_c64_roms();
		
	}

	private static final String TAG = SidplayfpPlugin.class.getSimpleName();
			
	private static class Info {
		protected String name = "Unknown";
		protected String composer = "Unknown";
		protected String copyright = "Unknown";
		protected int videoMode = 0;
		protected int sidModel = 0;
		protected int startSong = 1;
		protected int songs = 1;
		protected int psidversion = 0;
		protected int second_sid_addr = 0;
		protected String format;
		
	};
	
	final byte [] header = new byte [128];
	private byte[] mainHash;
	private short[] extraLengths;
	private int hashLen;
	private int loopMode = 0;
	private int currentFrames;
		
	private int songLengths[] = new int [256];
	private int currentTune;
	private static boolean isoptions_done = false;
	
	static class Option {
		String name;
		String description;
		Object defaultValue;
	}

	private Info songInfo;

	private int silence;
	
	private static final int OPT_FILTER = 1;
	private static final int OPT_SID_MODEL = 2;
	private static final int OPT_VIDEO_MODE = 3;
	private static final int OPT_FILTERBIAS = 4; // only for ReSID
	private static final int OPT_FILTERCURVE_6581 = 5;
	private static final int OPT_FILTERCURVE_8580 = 6;
	private static final int OPT_HAS_SECOND_SID = 7;
	private static final int OPT_SID_BUILDERMODE = 8;
	private static final int OPT_FORCED_SID_MODEL = 9;
	private static final int OPT_FORCED_VIDEO_MODE = 10;
	private static final int OPT_SID_RESAMPLING = 11;
	
	private static boolean forced_sid_options = false;
	
	@SuppressLint("UseSparseArrays")
	private static HashMap<Integer, Integer> optMap = new HashMap<Integer, Integer>();
	
	private static HashMap<String, Integer> sid_options = new HashMap<String, Integer>();
	
	private long currentSong = 0;



	private static Unzipper unzipper = null;
	private static File dataDir;
	private static boolean initialized = false;
		
	public static void setupOptions()
	{
	    sid_options.put("sid_filter", OPT_FILTER);
	    sid_options.put("video_mode", OPT_VIDEO_MODE);
	    sid_options.put("sid_model", OPT_SID_MODEL);
	    sid_options.put("buildermode", OPT_SID_BUILDERMODE);
	    sid_options.put("second_sid_addr", OPT_HAS_SECOND_SID);
	    sid_options.put("resampling_mode", OPT_SID_RESAMPLING);
	    sid_options.put("forced_video_mode",OPT_FORCED_VIDEO_MODE);
	    sid_options.put("forced_sid_model",OPT_FORCED_SID_MODEL);
	    sid_options.put("filter_bias",OPT_FILTERBIAS);
	    isoptions_done = true;
	}
	
	public static void extract_c64_roms()
	{
		if (!initialized) {
			dataDir = new File(Environment.getExternalStorageDirectory(), "droidsound");
			if (!dataDir.exists()) {
				dataDir.mkdir();
			}
			
			File viceDir = new File(dataDir, "c64roms");
			synchronized (lock) {					
				if(!viceDir.exists()) {
					unzipper = Unzipper.getInstance();
					unzipper.unzipAssetAsync(getContext(), "c64roms.zip", dataDir);
				}
			}
			initialized = true;
		}
	}
	
	
	private byte[] calculateMD5(byte[] module, int size)
	{
		ByteBuffer src = ByteBuffer.wrap(module, 0, size);		
		src.order(ByteOrder.BIG_ENDIAN);
		
		byte[] id = new byte[4];
		src.get(id);
		int version = src.getShort();
		src.position(8);
		/*short loadAdress =*/ src.getShort();
		short initAdress = src.getShort();
		short playAdress = src.getShort();
		short songs = src.getShort();
		/*short startSong =*/ src.getShort();
		int speedBits = src.getInt();
		src.position(0x76);
		int flags = src.getShort();
		
		int offset = 120;
		if (version == 2) {
			offset = 126;
		}
		
		int speed = 0;
		if (id[0] == 'R') {
			speed = 60;
		}
		
		Log.d(TAG, "speed %08x, flags %04x left %d songs %d init %x", speed, flags, size - offset, songs, initAdress);
		
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			
			md.update(module, offset, size - offset);
			
			ByteBuffer dest = ByteBuffer.allocate(128);
			dest.order(ByteOrder.LITTLE_ENDIAN);
			
			dest.putShort(initAdress);
			dest.putShort(playAdress);
			dest.putShort(songs);
			
			for (int i = 0; i < songs; i ++) {
				if ((speedBits & (1 << i)) != 0) {
					dest.put((byte) 60);
				} else {
					dest.put((byte) speed);
				}
			}
			
			if ((flags & 0x8) != 0) {
				dest.put((byte) 2);
			}

			byte[] d = dest.array();
			md.update(d, 0, dest.position());
			
			byte[] md5 = md.digest();
			Log.d(TAG, "%d %02x %02x DIGEST %02x %02x %02x %02x", d.length, d[0], d[1], md5[0], md5[1], md5[2], md5[3]);
			return md5;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	
	private void findLength(byte [] module, int size) {
		
		for (int i=0; i < 256; i++)
		{
			songLengths[i] = 60*60*1000;
		}
		
		Context context = getContext();
		if (context != null) {		
			if (mainHash == null) {
				try {
					InputStream is = context.getAssets().open("songlengths.dat");
					if(is != null) {						
						DataInputStream dis = new DataInputStream(is);
						hashLen = dis.readInt();
						mainHash = new byte [hashLen * 6];
						dis.read(mainHash);
						int l = is.available()/2;
						Log.d(TAG, "We have %d lengths and %d hashes", l, hashLen);
						extraLengths = new short [l];
						for(int i=0; i<l; i++) {
							extraLengths[i] = dis.readShort();
						}
						is.close();
						
					}
				} catch (IOException e) {
				}
			}
		}
		
		if (mainHash != null) {		
			byte[] md5 = calculateMD5(module, size);
			int first = 0;
			int upto = hashLen;
			
			int found = -1;
			
			long key = ((md5[0]&0xff) << 24) | ((md5[1]&0xff) << 16) | ((md5[2]&0xff) << 8) | (md5[3] & 0xff);
			key &= 0xffffffffL;
			
			//short lens [] = new short [128];
			
	    	Log.d(TAG, "MD5 %08x", key);
			while (first < upto) {
		        int mid = (first + upto) / 2;  // Compute mid point.
	    		long hash = ((mainHash[mid*6]&0xff) << 24) | ((mainHash[mid*6+1]&0xff) << 16) | ((mainHash[mid*6+2]&0xff) << 8) | (mainHash[mid*6+3] & 0xff);
	    		hash &= 0xffffffffL;
	
	        	//Log.d(TAG, "offs %x, hash %08x", mid, hash);
		        if (key < hash) {
		            upto = mid;     // repeat search in bottom half.
		        } else if (key > hash) {
		            first = mid + 1;  // Repeat search in top half.
		        } else {
		        	found = mid;
		        	int len = ((mainHash[mid*6+4]&0xff)<<8) | (mainHash[mid*6+5]&0xff);
	    			Log.d(TAG, "LEN: %x", len);
		        	if((len & 0x8000) != 0) {
		        		len &= 0x7fff;
		        		int xl = 0;
		        		int n = 0;
		        		while((xl & 0x8000) == 0) {
		        			xl = extraLengths[len++] & 0xffff;
		        			songLengths[n++] = (xl & 0x7fff) * 1000;
		        		}
		        		
		        		//for(int i=0; i<n; i++) {
		        		//	Log.d(TAG, "LEN: %02d:%02d", songLengths[i]/60, songLengths[i]%60);
		        		//}
		        	} else {
		        		Log.d(TAG, "SINGLE LEN: %02d:%02d", len/60, len%60);
		        		songLengths[0] = (len * 1000);
		        	}
		        	break;
		        }
		    }
			
			Log.d(TAG, "Found md5 at offset %d", found);
		}
	}
	
	
	public boolean setInfo(Info songinfo)
	{
		return true;
	}
	
	@Override
	public boolean loadInfo(FileSource fs)
	{
		final byte[] header = new byte[128];
		songInfo = null;
		
		songInfo = new Info();
		
		if(fs.getExt().equals("PRG")) {
			songInfo.name = fs.getName();
			Log.d(TAG, "######################## PRG LOAD OK");
			songInfo.format = "PRG";
			return true;
		}
		
		try {
			fs.read(header);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
				
		try 
		{
			String s = new String(header, 0, 4, "ISO-8859-1");
			
			if (!(s.equals("PSID") || s.equals("RSID")))
			{
				return false;
			}
			
			songInfo.format = s;
			songInfo.name = new String(header, 0x16, 0x20, "ISO-8859-1").replaceAll("\0", "");
			songInfo.composer = new String(header, 0x36, 0x20, "ISO-8859-1").replaceAll("\0", "");
			songInfo.copyright = new String(header, 0x56, 0x20, "ISO-8859-1").replaceAll("\0", "");
			songInfo.psidversion = header[0x5];

			songInfo.second_sid_addr = (Integer)((header[0x7a] & 0xff) << 4);
			if (songInfo.second_sid_addr != 0)
				songInfo.second_sid_addr = (Integer) (0x0000d000 | (header[0x7a] & 0xff) << 4);

			
			forced_sid_options = PlayerActivity.prefs.getBoolean("SidplayfpPlugin.force_options", false);
			
			setOption("second_sid_addr", songInfo.second_sid_addr);

			String resampling_mode = PlayerActivity.prefs.getString("SidplayfpPlugin.resampling_mode", "1");
			setOption("resampling_mode", Integer.valueOf(resampling_mode));
			
			String filter_bias = PlayerActivity.prefs.getString("SidplayfpPlugin.filter_bias", "0");
			setOption("filter_bias", Integer.valueOf(filter_bias));

			String buildermode = PlayerActivity.prefs.getString("SidplayfpPlugin.buildermode", "1");
 			setOption("buildermode", Integer.valueOf(buildermode));
			
			setOption("forced_video_mode", 0);
			setOption("forced_sid_model", 0);

 			if (forced_sid_options)
 			{
 				String sid_model = PlayerActivity.prefs.getString("SidplayfpPlugin.sid_model", "0");
 				String video_mode = PlayerActivity.prefs.getString("SidplayfpPlugin.video_mode", "0");

 				songInfo.videoMode = Integer.valueOf(video_mode) + 1;
 				songInfo.sidModel = Integer.valueOf(sid_model) + 1;

 				setOption("forced_video_mode", 1);
 				setOption("forced_sid_model", 1);
 			}
 			else
 			{
 				songInfo.videoMode = (header[0x77] >> 2) & 3;
 				songInfo.sidModel = (header[0x77] >> 4) & 3;

 			}
			setOption("sid_model", songInfo.sidModel);
			setOption("video_mode", songInfo.videoMode);
				
			songInfo.songs = ((header[0xe] << 8) & 0xff00) | (header[0xf] & 0xff);
			songInfo.startSong = ((header[0x10] << 8) & 0xff00) | (header[0x11] & 0xff) - 1;
			
			Log.i(TAG, "startSong=" + songInfo.startSong + ", songs=" + songInfo.songs);
			
			return true;
		
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}
		return false;	
	}

	@Override
	public boolean canHandle(FileSource fs)
	{
		String ext = fs.getExt();
		return ext.equals("SID") || ext.equals("PRG") || ext.equals("PSID") || ext.equals("RSID");
	}
	

	@Override
	public void getDetailedInfo(Map<String, Object> list)
	{
		final String sid_model[] = { "UNKNOWN", "MOS6581", "MOS8580", "6581 & 8580" };
		final String videoModes[] = { "UNKNOWN", "PAL", "NTSC", "PAL & NTSC" };
		
		list.put("plugin", "Sidplay2fp");
		list.put("format", songInfo.format);
		list.put("copyright", songInfo.copyright);
		list.put("sidmodel", sid_model[songInfo.sidModel]);
		list.put("videomode", videoModes[songInfo.videoMode]);
		list.put("psidversion", songInfo.psidversion);
	}
	
	@Override
	public int getIntInfo(int what) {
		if (songInfo == null)
		{
			return 0;
		}
		
		if (what == INFO_LENGTH)
		{
			return songLengths[currentTune];
		}
		
		if (what == INFO_SUBTUNE_COUNT) {
			return songInfo.songs;
		}
		
		if (what == INFO_SUBTUNE_NO) {
			return currentTune;
		}
		
		if (what == INFO_STARTTUNE)
		{
			return songInfo.startSong;
		}
		
		return 0;
	}
	
	@Override
	public String getStringInfo(int what)
	{
		if (songInfo == null)
		{
			return null;
		}
		
		switch(what) {
		case INFO_AUTHOR:
			return songInfo.composer;
		case INFO_COPYRIGHT:
			return songInfo.copyright;
		case INFO_TITLE:
			return songInfo.name;
		default:
			return null;
		}
	}


	@Override
	public int getSoundData(short[] dest, int size)
	{
		int len = N_getSoundData(currentSong, dest, size);  

		currentFrames += len/2;
		
		int played = currentFrames * 10 / 441;
				
		if(loopMode == 0 && songLengths[currentTune] > 0 && (played + silence >= songLengths[currentTune]))
			return -1;
		return len;
	}
	
	@Override
	public boolean load(FileSource fs) {
		
		silence = 0;
		
		currentTune = 0;
		songInfo = null;
		int type = -1;
		currentFrames = 0;
		
		byte [] module = fs.getData();
		String name = fs.getName();
		int size = (int) fs.getLength();
		
		if(size < 0x80)
			return false;
		
		String s = new String(module, 0, 4);
		if((s.equals("PSID") || s.equals("RSID")))
			type = 0;
 
		else if(name.toUpperCase().endsWith(".PRG")) 
			type = 1;
		
		else if(module[0] == 0x01 && module[1] == 0x08)
			type = 1;
		
		if (type < 0)
			return false;

		if (type == 1)
		{
			byte rsid[] = new byte [] {
					0x52, 0x53, 0x49, 0x44, 0x00, 0x02, 0x00, 0x7c,
					0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,
					0x00, 0x01, 0x00, 0x00, 0x00, 0x00
			}; 

			byte [] oldm = module;
			module = new byte [oldm.length + 0x7c];
			System.arraycopy(rsid, 0, module, 0, rsid.length);

			module[0x77] = 2;

			System.arraycopy(oldm, 0, module, 0x7c, oldm.length);
			size += 0x7c;

			fs = FileSource.fromData(name, module);
			songInfo = new Info();
			songInfo.name = name;
			Log.d(TAG, "######################## PRG LOAD OK");
			songInfo.format = "PRG";
			currentTune = songInfo.startSong = 0;
			loadInfo(fs);
			
			for(Entry<Integer, Integer> e : optMap.entrySet())
			{
				N_setOption(e.getKey(), e.getValue());
			}
			currentSong = N_load(fs.getData(), (int) fs.getLength());
			
			if(currentSong == 0)
			{
				return false;
			}
			findLength(module, size);
			optMap.clear();	

			fs.close();
			module = null;
						
			return true;
			
		} 
		// get/set options from sid file directly
		loadInfo(fs);
		currentTune = songInfo.startSong;
		
		for(Entry<Integer, Integer> e : optMap.entrySet())
		{
			N_setOption(e.getKey(), e.getValue());
		}
		
		currentSong = N_load(fs.getData(), (int) fs.getLength());
				
		if(currentSong == 0)
		{
			module = null;
			return false;
			
		}
		
		findLength(module, size);
		optMap.clear();	
		fs.close();
		module = null;
		return true; 
		
		
	}
	@Override
	public void setSilence(int msec)
	{
		silence = msec;
	}

	
	@Override
	public boolean setTune(int tune)
	{
		boolean ok = N_setTune(currentSong, tune);
		if(ok) 
			currentTune = tune;
		currentFrames = 0;
		return ok;
	}

	
	@Override
	public void unload()
	{
		if(currentSong != 0)
		{
			N_unload(currentSong);
			currentSong = 0;
		}
	}

	@Override
	public void setOption(String opt, Object val)
	{
		Log.d(TAG, opt, val);
		if (!isoptions_done)
			setupOptions();

		if (!sid_options.containsKey(opt))
			return;
		
		int k = sid_options.get(opt);
		int v = -1;
		
		if (val instanceof Boolean)
			v = (Boolean)val ? 1 : 0;
		
		else if (val instanceof Integer)
			v = (Integer)val;
		
		if (opt.equals("sid_model"))
		{
			if (v==0)
			{
				v = 1;
			}
			
			else
			{
				v+=1;
			}
				
				
		}

		if (opt.equals("video_mode"))
		{
			if (v == 0)
			{
				v = 1;
			}
			else
			{
				v+=1;
			}
		}


		optMap.put(k, v);
		
	}
	
	@Override
	public boolean seekTo(int seconds) 
	{ 
		return N_seekTo(currentSong, seconds);
	}


	@Override
	public boolean isSilent()
	{
		
		return (N_getIntInfo(currentSong, 100) != 0);
	}
	
	@Override
	public String getVersion()
	{
		return "libsidplay2fp 1.2.2 beta";
	}
		
	@Override
	public boolean isEndless()
	{
		return true;
	}

	native public long N_load(byte [] module, int size);
	native public void N_unload(long song);

	native public int N_getSoundData(long song, short [] dest, int size);
	native public boolean N_seekTo(long song, int seconds);
	native public boolean N_setTune(long song, int tune);
	native public String N_getStringInfo(long song, int what);
	native public int N_getIntInfo(long song, int what);
	native public static void N_setOption(int what, int val);
	
	
}
