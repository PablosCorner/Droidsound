package com.ssb.droidsound.plugins;

import java.util.HashMap;
import java.util.Map;

import com.ssb.droidsound.file.FileSource;

public class PSFPlugin extends DroidSoundPlugin
{
	private static Map<String, String> psfoptMap = new HashMap<String, String>();

	//private static SexyPSFPlugin sexypsfPlugin = new SexyPSFPlugin();
	private static HEPlugin hePlugin = new HEPlugin();
	
	@Override
	public void setOption(String opt, Object val)
	{
		
		if (opt.equals("psfengine"))
		{
			String v = val.toString();
			psfoptMap.put("psfengine", v);
			hePlugin.setOption(opt, val);
			//sexypsfPlugin.setOption(opt, val);
		}
	}

	@Override
	public boolean load(FileSource fs) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getSoundData(short[] dest, int size) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getStringInfo(int what) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIntInfo(int what) {
		// TODO Auto-generated method stub
		return 0;
	}


}
