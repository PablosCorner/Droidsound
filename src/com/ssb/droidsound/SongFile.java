package com.ssb.droidsound;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class SongFile {

	//private static final String TAG = SongFile.class.getSimpleName();
	private int subtune;
	private int playtime;
	private String fileName;
	
	private File file;
	private String path;
	private String prefix;
	private String suffix;
	private String midfix;
	private String arcPath;
	private String arcName;
	private long position;

	
	//private String tuneString;
	private String title;
	private String composer;
	
	private String protocol;
	
	//private static Locale locale = Locale.getDefault();

	private static final Set<String> ARCHIVE_EXTENSIONS = new HashSet<String>(Arrays.asList(
			".ZIP", ".7Z", ".RAR", ".GZ")); 

	public SongFile(SongFile s) {
		subtune = s.subtune;
		playtime = s.playtime;
		fileName = s.fileName;
		file = s.file;
		path = s.path;
		prefix = s.prefix;
		suffix = s.suffix;
		midfix = s.midfix;
		arcPath = s.arcPath;
		arcName = s.arcName;
		title = s.title;
		composer = s.composer;
		protocol = s.protocol;
		position = s.position;
	}
	
	public SongFile(File f) {
		init(f.getPath());
	}
	
	public SongFile(String fname) {
		init(fname);
	}
	
	private void init(String fname) {
		subtune = -1;
		playtime = -1;
		protocol = "";

		if (fname.contains("/MLDB/"))
		{
			String modland_path_prefix = "/pub/modules";
			String modland_ftp = PlayerActivity.prefs.getString("Modland_server", "modland.ziphoid.com");
			String songPath = fname.substring(fname.indexOf("/MLDB") + 5);
			fname = "ftp://" + modland_ftp + modland_path_prefix + songPath;
		}
		
		if (fname.contains(".db_source"))
		{
			String modland_path_prefix = "/pub/modules";
			String modland_ftp = PlayerActivity.prefs.getString("Modland_server", "modland.ziphoid.com");
			String songPath = fname.substring(fname.indexOf(".db_source") + 10);
			fname = "ftp://" + modland_ftp + modland_path_prefix + songPath;
		}
			

		if (fname.contains(".fs_source"))
			fname = PlayerActivity.translate_fss_sourcePath(fname);
	
		if(fname.startsWith("file://"))
		{
			try {
				fname = URLDecoder.decode(fname.substring(7), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		} 
		else if(fname.startsWith("http://"))
		{
			fname = fname.substring(7);
			protocol = "http://";
		}

		else if(fname.startsWith("ftp://"))
		{
			fname = fname.substring(6);
			protocol = "ftp://";
		}
		
		title = composer = null;
		
		if(fname.indexOf('\t') >= 0) {
			String t[] = fname.split("\t");
			fname = t[0];
			title = t[1];
			if(t.length > 2) {
				composer = t[2];
			}
		}
		
		String s[] = fname.split(";");
		
		fileName = s[0];
		
		//int sc = fileName.lastIndexOf(';');
		if(s.length > 1) {
			if(s.length > 2) {
				try {
					playtime = Integer.parseInt(s[2]);
				} catch (NumberFormatException e) {}					
			}
			try {
				subtune = Integer.parseInt(s[1]);
			} catch (NumberFormatException e) {}					
			fileName = s[0];
		}

		
		int slash = fileName.lastIndexOf('/');
		if(slash > 0) {
			path = fileName.substring(0, slash);
			fileName = fileName.substring(slash+1);
		} else {
			path = "";
		}

		midfix = fileName;
		prefix = suffix = "";
		int firstDot = fileName.indexOf('.');
		int lastDot = fileName.lastIndexOf('.');
		if(firstDot > 0) {
			prefix = fileName.substring(0, firstDot);
			suffix = fileName.substring(lastDot+1);
			midfix = fileName.substring(firstDot, lastDot+1);
		}

		for (String ext : ARCHIVE_EXTENSIONS)
		{
			String ext_ = ext + "/";
			int archive = s[0].toUpperCase(Locale.ENGLISH).indexOf(ext_);
			if(archive > 0) 
			{
				arcPath = s[0].substring(0, archive + ext.length());
				arcName = s[0].substring(archive + ext.length() + 1);
				break;
			}
		}
		
		file = new File(path, fileName);
		
		//Log.d(TAG, "SONGFILE -%s-%s-%s-%s- %d,%d", path, prefix, midfix, suffix, subtune, playtime);
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getComposer() {
		return composer;
	}

	public String getPrefix() {
		return prefix;
	}
	
	public String getSuffix() {
		return suffix;
	}
	
	public File getFile() {
		return file;
	}
	
	public int getSubtune() {
		return subtune;
	}
	
	public int getPlaytime() {
		return playtime;
	}
	
	public void changePrefix(String p) {
		prefix = p;
		fileName = prefix + midfix + "suffix"; 
	}
	
	public void changeSuffix(String s) {
		suffix = s;
		fileName = prefix + midfix + "suffix"; 
	}

	public SongFile(File f, int t, String title) {
		init(f.getPath());
		if(subtune < 0) {
			subtune = t;
		}
		this.title = title;
	}
	
	public SongFile(String song, int t) {
		init(song);
		if(t < 0) {
			subtune = t;
		}
	}

	public String getArcPath() {
		return arcPath;
	}

	public String getArcName() {
		return arcName;
	}

	public String getPath() {
		if(subtune >= 0) {
			if(playtime >= 0) {
				return protocol + path + "/" + fileName + ";" + subtune + ";" + playtime;
			}
			return protocol + path + "/" + fileName + ";" + subtune;
		} else {
			return protocol + path + "/" + fileName;
		}
	}
	
	public void setSubTune(int t) {
		subtune = t;
	}

	public void setPlayTime(int t) {
		playtime = t;
	}

	public void setTitle(String t) {
		title = t;
	}

	public String getName() {
		//return file.getName();
		return fileName;
	}

	public boolean exists() {
		return file.exists();
	}
	
	public String getParent() {
		return protocol + file.getParent();
	}

	public boolean isDirectory() {
		return file.isDirectory();
	}

	public File[] listFiles() {
		return file.listFiles();
	}

	public SongFile[] listSongFiles() {
		File [] files = file.listFiles();
		SongFile [] sfiles = new SongFile [files.length];
		for(int i=0; i<files.length; i++) {
			sfiles[i] = new SongFile(files[i]);
		}
		return sfiles;
	}

	public boolean delete() {
		return file.delete();
	}

	public String getFullTitle() {
		if(title != null) {
			if(composer != null)
				return composer + " - " + title;
			return title;
		}
		return fileName;
	}

}
