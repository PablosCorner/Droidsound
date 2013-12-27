package com.ssb.droidsound.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.ssb.droidsound.PlayerActivity;
import com.ssb.droidsound.SettingsActivity;
import com.ssb.droidsound.plugins.DroidSoundPlugin;
import com.ssb.droidsound.utils.Log;


public class FileCache
{
	private static final String TAG = FileCache.class.getSimpleName();	
	private static FileCache _instance = null;
	
	private static class CacheEntry
	{
		public CacheEntry(File f)
		{
			file = f;
			File dotFile = FileCache.getDotFile(f);
			if(dotFile.exists())
			{
				time = dotFile.lastModified() / 1000;
			} 
			else
			{
				time = f.lastModified() / 1000;
			}
			if(time == 0) 
			{
				throw new RuntimeException("LASTMODIFIED IS ZERO");
			}
				
		}
		public File file;
		public long time;
	}
	
	private ArrayList<String> fileNameList;
	private LinkedHashSet<CacheEntry> fileList;
	private List<File> newFiles;

	private File cacheDir;

	private String exDir;
	private int totalSize;
	private static long limitSize;
	
	public static synchronized FileCache getInstance()
	{
		if(_instance == null) 
		{
			_instance = new FileCache();
		}
		return _instance;
	}
	
	public FileCache()
	{
		fileNameList = new ArrayList<String>();
		fileList = new LinkedHashSet<CacheEntry>();
				
		newFiles = new ArrayList<File>();
		exDir = Environment.getExternalStorageDirectory().getPath();
		//long freeSpace = Environment.getExternalStorageDirectory().getFreeSpace();
		cacheDir = new File(exDir + "/droidsound/fileCache");
		Log.d(TAG, "Created dir '%s'", cacheDir.getPath());
		cacheDir.mkdirs();
		
		//read the limitsize from SETTINGS!!!
		
		String cacheSize = PlayerActivity.prefs.getString("FileCache.fcsize", "64");
		Integer cacheSz = Integer.parseInt((String) cacheSize);
			
		limitSize = cacheSz * 1024 * 1024; // 16MB default

		totalSize = 0;
		indexFiles(cacheDir);
	}
	public static long getLimitSize(){
		return limitSize;
	}
	
	public void setLimitSize(long ls)
	{
		limitSize = ls;
		limit(limitSize);		
	}

	private void indexFiles(File dir)
	{
		
		File [] files = dir.listFiles();
		if(files != null)
		{
			for(File f : files)
			{
				if(f.isFile() && f.length() > 0 && f.canRead())
				{
					if(f.getName().charAt(0) != '.')
					{
						if (!fileNameList.contains(f.getPath()))
						{
							fileNameList.add(f.getPath());
							fileList.add(new CacheEntry(f));
							totalSize += f.length();
						}
					}
				} 
				else
				{
					indexFiles(f);
				}
			}
		}
		dir.delete();
	}

// *********************************************************
	// adds new files to filelist after they have been downloaded
	private void addNewFiles()
	{
		Iterator<File> i = newFiles.iterator();
		Log.d(TAG, "Trying to add file to filecache");
		while(i.hasNext())
		{
			File f = i.next();
			if(f != null && f.exists())
			{
				if (!fileNameList.contains(f.getPath()))
				{
					Log.d(TAG, "Adding FILE: %s", f.getPath());
					fileNameList.add(f.getPath());
					fileList.add(new CacheEntry(f));
					totalSize += f.length();

				}
				i.remove();
			}
		}
	}
	
	private void limit(long limitSize)
	{
	
		Log.d(TAG, "TOTAL SIZE IS %d", totalSize);
		if (totalSize <= limitSize)
		{
			return;
		}
									
		Iterator<CacheEntry> iter = fileList.iterator();
		while (iter.hasNext())
		{
			CacheEntry f = iter.next();
			totalSize -= f.file.length();
			Log.d(TAG, "Removing FILE: %s", f.file.getPath());

			f.file.delete();
			getDotFile(f.file).delete();
			
			iter.remove();
			fileNameList.remove(f.file.getPath());
			fileList.remove(f);
			
			if(totalSize <= (limitSize - limitSize/8))
				break;
		}

		indexFiles(cacheDir);
	}
	
	
	private static File getDotFile(File f)
	{
		return new File(f.getParentFile(), "." + f.getName());
	}
	
	public File getFile(String reference)
	{
		Log.d(TAG, "entered getFile function");
		addNewFiles();
		
		if(limitSize >= 0)
			limit(limitSize);
		
		String path = reference;
		
		if(path.indexOf("http://") == 0)
		{
			path = "http/" + path.substring(7);
		}
		
		if(path.indexOf("ftp://") == 0)
		{
			// we need to clip username:password@host:port -> host
			
			path = path.replaceFirst("[a-zA-Z0-9:_]+@",""); // remove username:password@
	        path = path.replaceFirst(":\\d+",""); // remove the port
	        
			path = "ftp/" + path.substring(6);
			
		}
		
		if(path.indexOf(exDir) == 0)
		{
			path = path.substring(exDir.length());
		}
		if(path.indexOf("/") == 0)
		{
			path = path.substring(1);
		}
				
		File file = new File(cacheDir, path);
		
		Log.d(TAG, "Creating file '%s'", file.getPath());
		
		file.getParentFile().mkdirs();
		
		if(file.exists() && file.length() > 0 && file.canRead()) {
			Log.d(TAG, "Exists, move to end", file.getPath());
			
			File dotFile = getDotFile(file);
			dotFile.delete();
			try {
				dotFile.createNewFile();
			} catch (IOException e) {
			}
			Log.d(TAG, "Touched file, %d %d %d", System.currentTimeMillis(), dotFile.lastModified(), file.lastModified());
			fileList.remove(file);
			fileList.add(new CacheEntry(file));
		} 
		else {
			newFiles.add(file);			
		}
		
		return file;
	}

	public void purge() {
		limit(-1);
	}

	
	
}
