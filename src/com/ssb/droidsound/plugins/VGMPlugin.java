package com.ssb.droidsound.plugins;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import com.ssb.droidsound.PlayerActivity;
import com.ssb.droidsound.file.FileSource;

public class VGMPlugin extends DroidSoundPlugin {

	static {
		System.loadLibrary("vgmplay");
	}
	private long songRef;
	private static HashMap<String, String> infoMap = new HashMap<String, String>();
	private static String extension = "";
	
	private boolean getGD3TAGs(String filename)
	{
		try {
			
            FileInputStream vgzfile = new FileInputStream(filename);
            GZIPInputStream gZIPInputStream = new GZIPInputStream(vgzfile);

            int bytes_read = 0;
            char[] buffer = new char[1024 * 1024 * 10];

            BufferedReader bf = new BufferedReader(new InputStreamReader(gZIPInputStream));
            bytes_read = bf.read(buffer);
            gZIPInputStream.close();
            bf.close();
            
            int i=0;
            // find the "GD3\x20" tag
            while (true)
            {
                if (buffer[i] == 'G' && buffer[i+1] == 'd' && buffer[i+2] == '3' && buffer[i+3] == 0x20)
                {
                    i += 12;
                    break;
                }
                i++;
            }

            String[] items = new String[] {"TrackNameE", "GameNameE", "SystemNameE", "AuthorNameE",
                                    	   "ReleaseDate", "Creator", "Notes"};    //keep the order, its static
            
            String[] patterns = new String[] {"([.][.]){2,64})\\u0000\\u0000"};

            String str_buffer = new String(buffer);
            String GD3_TAG_str = String.valueOf(str_buffer.subSequence(i,bytes_read));

            Pattern p = Pattern.compile(patterns[0],Pattern.DOTALL);
            Matcher m = p.matcher(GD3_TAG_str);
            String val = "";
            int counter = 0;
            while (m.find())
            {
                val = m.group(0);
                String result = val.toString().replace("\00","");
                if (!result.isEmpty())
                {
                	infoMap.put(items[counter], result);              
                	counter += 1;
                }
            }
            
            bf = null;
            str_buffer = null;
            GD3_TAG_str = null;
            items = null;
            patterns = null;
            vgzfile = null;
            gZIPInputStream = null;
            
            return true;

        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            return false;
        }
		
	}
	
	
	@Override
	public String getVersion() {
		return "VGMPlay 0.40_4 by Valley Bell";
	}
		
	@Override
	public boolean canHandle(FileSource fs) {
		boolean res = PlayerActivity.prefs.getBoolean("use_vgmplay", false);
		if (!res)
			return false;
			
		
		extension = fs.getExt().toUpperCase();
		return fs.getExt().equals("VGM") || fs.getExt().equals("VGZ");
	}
	
	
	@Override
	public void getDetailedInfo(Map<String, Object> list)
	{
		list.put("plugin", "VGMPLAY");
		list.put("format", extension);
		/*
		list.put("copyright", release_date);
		list.put("title", game_name);
		list.put("composer", author);
		list.put("system_name", system_name);
		list.put("track_name", track_name);
		list.put("creator", creator);
		list.put("notes", notes);
		*/
		
	}
	
	@Override
	public int getIntInfo(int what) {
		return 0;
	}

	@Override
	public int getSoundData(short[] dest, int size)
	{
		return N_getSoundData(songRef, dest, size);
	}

	@Override
	public String getStringInfo(int what) {
		return null;
	}

	@Override
	public boolean load(FileSource fs) 
	{
		//getGD3TAGs(fs.getFile().getPath());
		
		songRef = N_load(fs.getFile().getPath());

		return songRef != 0;
	}

	@Override
	public void unload() {
		N_unload(songRef);
	}

	native public long N_load(String filename);
	native public void N_unload(long song);
	native public int N_getSoundData(long song, short [] dest, int size);	
}
