package com.ssb.droidsound;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.ssb.droidsound.utils.Log;

import com.ssb.droidsound.file.FileSource;
import com.ssb.droidsound.plugins.DroidSoundPlugin;

public class FileIdentifier {
	private static final String TAG = FileIdentifier.class.getSimpleName();

	public static final Set<String> KNOWN_FORMATS = new HashSet<String>(Arrays.asList(
			"PSID", "RSID", "SID", "PRG",
            "PSF", "PSF2", "MINIPSF", "MINIPSF2",
            "DSF", "MINIDSF", "SSF", "MINISSF",
            "GSF", "MINIGSF",
            "QSF", "MINIQSF",
            "2SF", "MINI2SF",
            "SGC", "SFM", "SPC", "GYM", "NSF", "NSFE", "GBS", "AY", "SAP", "HES", "KSS", "VGM", "VGZ",
            "AHX", "HVL",
            "MP3",
            "MOD", "S3M", "XM", "IT", "MPTM", "STM", "NST", "M15", "STK", "WOW", "ULT", "669", "MTM", 
            "MED", "FAR", "MDL", "AMS", "DSM", "AMF", "OKT", "DMF", "PTM", "PSM", "MT2", "DBM", "DIGI", 
            "IMF", "J2B", "GDM", "UMX",
            "RSN", "RMU",
            "SNDH", "SC68", "SND",
            "USF", "MINIUSF",
            "CUS","CUST","CUSTOM","SMOD","TFX","SNG","RJP","JPN",
            "AST","AHX","THX","ADPCM","AMC","ABK","AAM","ALP","AON","AON4","AON8","ADSC","MOD_ADSC4",
            "BSS","BD","BDS","UDS","KRIS","CIN","CORE","CM","RK","RKB","DZ","MKIIO","DL","DL_DELI",
            "DLN","DH","DW","DWOLD","DLM2","DM","DLM1","DM1","DSR","DB","DIGI","DSC","DSS","DNS",
            "EMS","EMSV6","EX","FC13","FC3","FC14","FC4","FC","FRED","GRAY","BFC","BSI","FC-BSI",
            "FP","FW","GLUE","GM","EA","MG","HD","HIPC","EMOD","QC","IMS","DUM","IS","IS20","JAM",
            "JC","JMF","JCB","JCBO","JPN","JPND","JP","JT","MON_OLD","JO","HIP","HIP7","S7G","HST",
            "SOG","SOC","KH","POWT","PT","LME","MON","MFP","HN","MTP2","THN","MC","MCR","MCO","MK2",
            "MKII","AVP","MW","MAX","MCMD","MED","MMD0","MMD1","MMD2","MSO","MIDI","MD","MMDC","DMU",
            "MUG","DMU2","MUG2","MA","MM4","MM8","MMS","NTP","TWO","OCTAMED","OKT","ONE","DAT","PS",
            "SNK","PVP","PAP","PSA","MOD_DOC","MOD15","MOD15_MST","MOD_NTK","MOD_NTK1","MOD_NTK2",
            "MOD_NTKAMP","MOD_FLT4","MOD","MOD_COMP","!PM!","40A","40B","41A","50A","60A","61A",
            "AC1","AC1D","AVAL","CHAN","CP","CPLX","CRB","DI","EU","FC-M","FCM","FT","FUZ","FUZZ",
            "GMC","GV","HMC","HRT","HRT!","ICE","IT1","KEF","KEF7","KRS","KSM","LAX","MEXXMP","MPRO",
            "NP","NP1","NP2","NOISEPACKER2","NP3","NOISEPACKER3","NR","NRU","NTPK","P10","P21","P30",
            "P40A","P40B","P41A","P4X","P50A","P5A","P5X","P60","P60A","P61","P61A","P6X","PHA","PIN",
            "PM","PM0","PM01","PM1","PM10C","PM18A","PM2","PM20","PM4","PM40","PMZ","POLK","PP10",
            "PP20","PP21","PP30","PPK","PR1","PR2","PROM","PRU","PRU1","PRU2","PRUN","PRUN1","PRUN2",
            "PWR","PYG","PYGM","PYGMY","SKT","SKYT","SNT","SNT!","ST2","ST26","ST30","STAR","STPK",
            "TP","TP1","TP2","TP3","UN2","UNIC","UNIC2","WN","XAN","XANN","ZEN","PUMA","RJP","SNG",
            "RIFF","RH","RHO","SA-P","SCUMM","S-C","SCN","SCR","SID1","SMN","SID2","MOK","SA","SONIC",
            "SA_OLD","SMUS","SNX","TINY","SPL","SC","SCT","PSF","SFX","SFX13","TW","SM","SM1","SM2",
            "SM3","SMPRO","BP","SNDMON","BP3","SJS","JD","DODA","SAS","SS","SB","JPO","JPOLD","SUN",
            "SYN","SDR","OSP","ST","SYNMOD","TFMX1.5","TFHD1.5","TFMX7V","TFHD7V","TFMXPRO","TFHDPRO",
            "TFMX","MDST","MDAT","THM","TF","TME","SG","DP","TRC","TRO","TRONIC","MOD15_UST","VSS",
            "WB","ML","MOD15_ST-IV","AGI","TPU","QPA","SQT","QTS",
            "AAAP", "AAX", "ADX", "ADP", "AFC", "AGSC", "AHX", "AIX", "AMTS", "ASS", "ASF", "ASR",
			"AST",  "AUS", "BAF", "BG00", "BMDX", "BGW", "BNS", "BRSTM", "CAF", "CAPDSP", "CCC",
			"CFN", "CNK", "BCWAV", "DE2", "DSP", "DXH", "EAM", "ENTH", "EXST", "FAG", "FILP", "FSB",
			"GCA", "GCM", "GCSW", "GCW", "GENH", "GMS", "GSB", "HGC1", "HPS", "IDSP", "IKM",
			"ILD", "INT", "ISH", "IVB", "JOE", "KCES", "KCEY", "KHV", "LEG", "LOGG", "MATX", "MCG",
			"MIB", "MIHB", "MIC", "MPDSP", "MSA", "MSS", "MSVP", "MUSC", "MUSX", "NPSF", "PDT",
			"PNB", "PSH", "PSW", "RIFF", "RKV", "RND", "RRDS", "RSD", "RSF", "RSTM", "RWS", "RXW",
			"SAD", "SCD", "SEG", "SFS",	"SL3", "SPD", "SPM", "SS2", "STR", "STS", "STRM", "STER", "STX",
			"STS", "SVAG", "SVS", "SWAV", "TEC", "THP", "VAS", "VAG", "VIG", "VOI", "VPK", "VSF", "WAA",
			"WAM", "WAVM", "WP2", "WSI", "WVS", "XMU", "XA", "XA2", "XSS", "XWB", "WVAS", "WWAV",
			"YMF")); 

	
	private static Map<String, Integer> extensions;

	private static HashSet<String> modMagic;

	private static List<DroidSoundPlugin> plugins;
	
	public static final int TYPE_MOD = 1;
	public static final int TYPE_SID = 2;
	public static final int TYPE_XM = 3;
	public static final int TYPE_S3M = 4;
	public static final int TYPE_IT = 5;
	public static final int TYPE_NSF = 6;
	public static final int TYPE_SPC = 7;
	public static final int TYPE_PRG = 8;
	//public static final int TYPE_VGM = 8;
	
	public static class MusicInfo {
		public String title;
		public String composer;
		public String copyright;
		//String game;
		public String format;

		public int channels;
		public int type;
		public int date;
	};


	static {
		extensions = new HashMap<String, Integer>();
		extensions.put("MOD", TYPE_MOD);
		extensions.put("SID", TYPE_SID);
		extensions.put("XM", TYPE_XM);
		extensions.put("S3M", TYPE_S3M);
		extensions.put("IT", TYPE_IT);
		extensions.put("NSF", TYPE_NSF);
		extensions.put("SPC", TYPE_SPC);
		extensions.put("PRG", TYPE_PRG);
		//extensions.put("VGM", TYPE_VGM);
			
		
		modMagic = new HashSet<String>();
		modMagic.add("M.K.");
		modMagic.add("M!K!");
		modMagic.add("M&K!");
		modMagic.add("N.T.");
		modMagic.add("M!K!");
		modMagic.add("4CHN");
		modMagic.add("6CHN");
		modMagic.add("8CHN");
		modMagic.add("FLT4");
		modMagic.add("FLT8");
	}
	
	/**
	 * NAME RULES:
	 * 
	 * If composer could not be found, try to parse title & composer from filename.
	 * 
	 * If game was set but not title, use game as title
	 *
	 * If title still not found, use filename (without extension) as title 
	 * 
	 */
	private static void fixName(String basename, MusicInfo info) {

		
		if(info.composer == null || info.composer.length() == 0) {
			int sep = basename.indexOf(" - ");
			if(sep > 0) {
				info.composer = basename.substring(0, sep);
				info.title = basename.substring(sep+3);
			}
		}

		if(info.composer != null && info.composer.length() == 0) {
			info.composer = null;
		}



		if(info.title == null || info.title.length() == 0) {
			info.title = basename;
		}
		
		if(info.date < 0 && info.copyright != null && info.copyright.length() >= 4) {
			int year = -1;
			try {
				year = Integer.parseInt(info.copyright.substring(0,4));
			} catch (NumberFormatException e) {
			}
			if(year > 1000 && year < 2100) {
				info.date = year * 10000;
			}
		}		
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
	
	private static MusicInfo tryLoad(DroidSoundPlugin plugin, FileSource fs) {

		if(plugin.loadInfo(fs)) {
			MusicInfo info = new MusicInfo();
			info.title = plugin.getStringInfo(DroidSoundPlugin.INFO_TITLE);
			info.composer = plugin.getStringInfo(DroidSoundPlugin.INFO_AUTHOR);
			info.copyright = plugin.getStringInfo(DroidSoundPlugin.INFO_COPYRIGHT);
			//info.game = plugin.getStringInfo(songRef, DroidSoundPlugin.INFO_GAME);
			info.format = plugin.getStringInfo(DroidSoundPlugin.INFO_TYPE);
			//info. = plugin.getIntInfo(songRef, DroidSoundPlugin.INFO_LENGTH);
			info.date = -1;
			plugin.unload();
			
			Log.d(TAG, "TITLE: %s -- COMPOSER: %s", info.title, info.composer);
			
			return info;
		}		
		return null;
	}

	private static String getBaseName(String fname) {
		int slash = fname.lastIndexOf('/');
		if(slash >= 0) {
			fname = fname.substring(slash+1);
		}
		int dot = fname.lastIndexOf('.');
		if(dot > 0) {
			fname = fname.substring(0, dot);
		}
		return fname;
	}
	
	
	public static String canHandle(String name) {
		if(plugins == null) {
			plugins = DroidSoundPlugin.createPluginList();
		}
		for(DroidSoundPlugin plugin : plugins) {
			if(plugin.canHandle(FileSource.fromData(name, new byte [0]))) {
				return plugin.getClass().getSimpleName();
			}
		}
		return null;
	}

	
	public static MusicInfo identify(FileSource fs) {
		return identify(fs.getName(), fs);
	}

	public static MusicInfo identify(String name, FileSource fs) {

		byte data [];
		String magic;
		
		if(plugins == null) {
			plugins = DroidSoundPlugin.createPluginList();
		}
		

		int dot = name.lastIndexOf('.');
		
		String ext = name.substring(dot+1).toUpperCase(Locale.ENGLISH);
		//Log.d(TAG, "hash %s %d", extensions.toString(), extensions.size());
		Integer i = extensions.get(ext);
		if(i == null) {
			
			List<DroidSoundPlugin> list = new ArrayList<DroidSoundPlugin>();
			for(DroidSoundPlugin plugin : plugins) {
				if(plugin.canHandle(fs)) {
					list.add(plugin);
					Log.d(TAG, "%s handled by %s", fs.getName(), plugin.getClass().getSimpleName());
				}
			}

			//Log.d(TAG, "MarkSupported " + is.markSupported());
			
			for(DroidSoundPlugin plugin : list) {
				Log.d(TAG, "Trying " + plugin.getClass().getName());
				MusicInfo info = null;
				info = tryLoad(plugin, fs);
				if(info != null) {
					Log.d(TAG, "Got info");
					name = plugin.getBaseName(name);
					fixName(name, info);
					return info;
				}
			}
			return null;
		}
		int extno = i;		
		//Log.d(TAG, "Ext %s -> Format %02x", ext, extno);

		MusicInfo info = new MusicInfo();
		info.type = extno;
		info.date = -1;
		
		try {
			switch(extno) {
			case TYPE_PRG:
				name = getBaseName(name);
				info.format = "SID";
				fixName(name, info);
				return info;
			case TYPE_SID:
				data = new byte [0x80];
				fs.read(data);
				magic = new String(data, 1, 3);
				if(magic.equals("SID")) {
					info.title = fromData(data, 0x16, 32); //new String(data, 0x16, o-0x15, "ISO-8859-1");
					info.composer = fromData(data, 0x36, 32); //new String(data, 0x36, o-0x35, "ISO-8859-1");
					info.copyright = fromData(data, 0x56, 32); //new String(data, 0x56, o-0x55, "ISO-8859-1");
					info.format = "SID";
					int year = -1;
					if(info.copyright.length() >= 4) {
						try {
							year = Integer.parseInt(info.copyright.substring(0,4));
						} catch (NumberFormatException e) {
						}
						if(year > 1000 && year < 2100) {
							info.date = year * 10000;
						}
					}
					return info;
					
				} else {
					return null;
				}

			case TYPE_MOD:
				data = new byte [0x480];
				fs.read(data);
				//magic = new String(data, 0x438,4);
				//Log.d(TAG, "MOD MAGIC %s", magic);
				//if(!modMagic.contains(magic)) {
				//	return null;
				//}
				info.title = null; //fromData(data, 0, 20); //new String(data, 0, 22, "ISO-8859-1");
				info.format = "MOD";
				break;
			case TYPE_S3M:
				data = new byte [50];
				fs.read(data);
				magic = new String(data, 44, 4);
				if(!magic.equals("SCRM")) {
					return null;
				}
				info.title = fromData(data, 0, 28); //new String(data, 0, 28, "ISO-8859-1");
				info.format = "S3M";
				break;
			case TYPE_XM:
				data = new byte [0x70];
				fs.read(data);
				magic = new String(data, 0, 15);
				if(!magic.equals("Extended Module")) {
					return null;
				}
				info.title = fromData(data, 17, 20); //new String(data, 17, 20, "ISO-8859-1");
				info.channels = data[0x68];
				info.format = "XM";
				break;
			case TYPE_IT:
				data = new byte [32];
				fs.read(data);
				magic = new String(data, 0, 4);
				if(!magic.equals("IMPM")) {
					return null;
				}
				info.title = fromData(data, 4, 26); //new String(data, 4, 26, "ISO-8859-1");
				info.format = "IT";
				break;
			case TYPE_NSF:
				data = new byte [128];
				fs.read(data);
				magic = new String(data, 0, 4);
				if(!magic.equals("NESM")) {
					return null;
				}
				info.title = fromData(data, 0xe, 32); //new String(data, 0xe, 32, "ISO-8859-1");
				info.composer = fromData(data, 0x2e, 32); //new String(data, 0x2e, 32, "ISO-8859-1");
				info.copyright = fromData(data, 0x4e, 32); //new String(data, 0x4e, 32, "ISO-8859-1");
				info.format = "NES";
				break;
			case TYPE_SPC:				
				data = new byte [0xd8];
				fs.read(data);
				info.format = "SNES";
				magic = new String(data, 0, 27);
				if(!magic.equals("SNES-SPC700 Sound File Data")) {
					return null;
				}
				
				if(data[0x23] == 0x1a) {
					info.title = fromData(data, 0x2e, 32);
					String game = fromData(data, 0x4e, 32);
					
					if(game.length() > 0) {
						info.title = game + " - " + info.title;
					}
					
					info.composer = fromData(data, 0xb1, 32);					
				}
				break;
		
			default:
				return null;
			}
		} catch (IOException e) {
			return null;
		}
		
		fs.close();
		
		if(dot > 0) {
			name = name.substring(0, dot);
		}
		int slash = name.lastIndexOf('/');
		if(slash >= 0) {
			name = name.substring(slash+1);
		}

		fixName(name, info);
		return info;
	}
	
	static int getInt(byte [] data, int o) {		
		return (data[o] & 0xff) | ((data[o+1] & 0xff)<<8) | ((data[o+2] & 0xff)<<16) | ((data[o+3] & 0xff)<<24);		
	}

}
