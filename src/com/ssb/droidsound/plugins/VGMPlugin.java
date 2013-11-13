package com.ssb.droidsound.plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
 
import com.ssb.droidsound.file.FileSource;

public class VGMPlugin extends DroidSoundPlugin {

	static {
		System.loadLibrary("vgmstream");
	}

	private static String extension = "";
	
	private static Map<String, String> tagMap = new HashMap<String, String>();
	private static Map<String, String> MULTI_EXTENSIONS = new HashMap<String, String>();
	@Override
	public String getVersion()
	{
		return "VGMStream [r1014]";
	}
	
	private long songRef;
	private boolean multi_file;
	
	private static final Set<String> SINGLE_EXTENSIONS = new HashSet<String>(Arrays.asList(
			"AAAP", "AAX", "ADX", "ADP", "AFC", "AGSC", "AIX", "AMTS", "ASS", "ASF", "ASR", 
			"AST", 
			"AUS", "BAF", "BG00", "BMDX", "BGW", "BNS", "BRSTM", "CAF", "CAPDSP", "CCC", 
			"CFN", "CNK", "DE2", 
			"DSP", "DXH", "EAM", "ENTH", "EXST", "FAG", "FILP",	"FSB", "GCA", "GCM", "GCSW", 
			"GCW", "GENH", 
			"GMS", "GSB", "HGC1", "HPS", "IDSP", "IKM", 
			"ILD", "INT", "ISD", "IVB", "JOE", "KCES", "KCEY", "KHV", "LEG", "LOGG", "MATX", "MCG", 
			"MIB", 
			"MIHB",	"MIC", "MPDSP", "MSA", "MSS", "MSVP", "MUSC", "MUSX", "NPSF", "PDT", 
			"PNB", "PSH", 
			"PSW", "RIFF", 
			"RKV", "RND", "RRDS", "RSD", "RSF", "RSTM", "RWS", "RXW", "SAD", "SEG", "SFS", 
			"SL3", 
			"SPM", "SS2", "STR", "STS", "STRM", "STER", "STX", "STS", "SVAG", "SVS", "SWAV", "THP",   
			"VAS", "VAG", "VIG", "VOI", "VPK", "VSF", "WAA", "WAM", "WAVM", "WP2", "WSI", "WVS",
			"XMU", 
			"XA", "XA2", "XSS", "XWB", "WVAS", "WWAV", "YMF")); 

	@Override
	public boolean canHandle(FileSource fs) 
	{
		MULTI_EXTENSIONS.put("SPD", "SPT"); // SPD needs also SPT to be included
		MULTI_EXTENSIONS.put("ISH", "ISD"); // ISH needs also ISD to be included
		
		extension = fs.getExt().toUpperCase();
		
		if (SINGLE_EXTENSIONS.contains(extension))
		{
			multi_file = false;
			return true;
		}
		if (MULTI_EXTENSIONS.containsKey(extension))
		{
			multi_file = true;
			return true;
		}
		return false;
		
	}
		
	@Override
	public void getDetailedInfo(Map<String, Object> list)
	{
		list.put("plugin", "VGMStream");
		list.put("format", extension);
		
		int freq = N_getIntInfo(songRef,DroidSoundPlugin.INFO_FREQUENCY);
		int channels = N_getIntInfo(songRef,DroidSoundPlugin.INFO_CHANNELS);
		
		list.put("frequency", Integer.toString(freq)+"Hz");

		if (channels == 1)
		{
			list.put("channels", "Mono");
		}
		else if (channels == 2)
		{
			list.put("channels", "Stereo");
		}
	}
	
	@Override
	public int getIntInfo(int what)
	{
	    return N_getIntInfo(songRef, what);
	} 
	
	@Override
	public int getSoundData(short[] dest, int size)
	{
		return N_getSoundData(songRef, dest, size);
	}
	
	@Override
	public String getStringInfo(int what)
	{
		return null;
	}

	@Override
	public boolean load(FileSource fs)
	{
		if (multi_file)
		{
			FileSource fs2 = null;
			
			String ext = fs.getExt();
			String ext2 = MULTI_EXTENSIONS.get(ext);
						
			String basename2 = getBaseName(fs.getReference()) + "." + ext2;
			 									
			fs2 = fs.getRelative(basename2); 
			fs2.getFile();
			fs2.close();
		}
		
		songRef = N_loadFile(fs.getFile().getPath());
		return ( songRef != 0);
	}

	@Override
	public void unload()
	{
		N_unload(songRef);
	}

	native public long N_loadFile(String fileName);	
	native public void N_unload(long song);
	native public int N_getSoundData(long song, short [] dest, int size);	
	native public int N_getIntInfo(long song, int what);
 
}
