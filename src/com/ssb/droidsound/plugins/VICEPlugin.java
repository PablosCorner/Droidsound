package com.ssb.droidsound.plugins;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.os.Environment;

import com.ssb.droidsound.file.FileSource;
import com.ssb.droidsound.utils.Log;
import com.ssb.droidsound.utils.Unzipper;

public class VICEPlugin extends DroidSoundPlugin 
{
	private static final String TAG = VICEPlugin.class.getSimpleName();
	
	private static boolean libraryLoaded = false;
	
	//private int currentTune;

	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, Integer> optMap = new HashMap<Integer, Integer>();

	private Unzipper unzipper = null;

	private static File dataDir;

	private static boolean isActive = false;

	private static boolean initialized = false;
	
	public VICEPlugin() {
		if (! initialized) {
			/* Store basic, kernal & chargen for C++ code to find. */
			dataDir = new File(Environment.getExternalStorageDirectory(), "droidsound");
			if (!dataDir.exists()) {
				dataDir.mkdir();
			}
			
			File viceDir = new File(dataDir, "VICE");
			synchronized (lock) {					
				if(!viceDir.exists()) {
					unzipper = Unzipper.getInstance();
					unzipper.unzipAssetAsync(getContext(), "vice.zip", dataDir);
				}
			}
			initialized = true;
		}
	}

	@Override
	public void setOption(String opt, Object val) {
		final int k, v;
		if (opt.equals("active")) {
			isActive = (Boolean)val;
			Log.d(TAG, ">>>>>>>>>> VICEPLUGIN IS " + (isActive ? "ACTIVE" : "NOT ACTIVE"));
			return;
		} else if (opt.equals("sid_filter")) {
			k = OPT_FILTER;
			v = (Boolean) val ? 1 : 0;
		} else if (opt.equals("ntsc")) {
			k = OPT_NTSC;
			v = Integer.valueOf(String.valueOf(val));
		} else if (opt.equals("resampling_mode")) {
			k = OPT_RESAMPLING;
			v = Integer.valueOf(String.valueOf(val));
		} else if (opt.equals("filter_bias")) {
			k = OPT_FILTER_BIAS;
			v = Integer.valueOf(String.valueOf(val)); 
		} else if (opt.equals("sid_model")) {
			k = OPT_SID_MODEL;
			v = Integer.valueOf(String.valueOf(val));
		} else {
			return;
		}
		
		if(!libraryLoaded)
		{
			optMap.put(k, v);
		} 
		else
		{		
			N_setOption(k, v);
		}
	}
	
	@Override
	public void unload() {
		N_unload();
	}
	
	@Override
	public int getSoundData(short[] dest, int size) {
		return N_getSoundData(dest, size);
	}
	
	@Override
	public boolean seekTo(int seconds) {
		return false;
	}

	@Override
	public boolean setTune(int tune) {
		boolean ok = N_setTune(tune + 1);
		return ok;
	}

	@Override
	public boolean load(FileSource fs)
	{
		if(!libraryLoaded)
		{
			try {
				System.loadLibrary("vice");
				
			}
			catch (UnsatisfiedLinkError u)
			{
				return false;
			}
			
			
			if(unzipper != null) 
			{
				while(!unzipper.checkJob("vice.zip"))
				{
					try
					{
						Thread.sleep(500);
					} 
					catch (InterruptedException e)
					{
						e.printStackTrace();
						return false;
					}
				}
				unzipper = null;
			}
			
			N_setDataDir(new File(dataDir, "VICE").getAbsolutePath());
			
			for(Entry<Integer, Integer> e : optMap.entrySet())
			{
				N_setOption(e.getKey(), e.getValue());
			}
			optMap.clear();						
			libraryLoaded = true;
		}

		final String error;
		error = N_loadFile(fs.getFile().getAbsolutePath());
			
		
		if (error != null) {
			Log.i(TAG, "Native code error: " + error);
			return false;
		}
		
		fs.close();

		return true; 
	}

	@Override
	public int getIntInfo(int what) {
		throw new RuntimeException();
	}

	@Override
	public String getStringInfo(int what) {
		throw new RuntimeException();
	}
	
	@Override
	public String getVersion() {
		return "VICE 2.4.5-r27760";
	}

	native private static String N_loadFile(String name);
	native private static void N_unload();
	native private static int N_getSoundData(short[] dest, int size);
	native private static boolean N_setTune(int tune);
	native private static void N_setOption(int what, int val);
	native private static void N_setDataDir(String path);

}
