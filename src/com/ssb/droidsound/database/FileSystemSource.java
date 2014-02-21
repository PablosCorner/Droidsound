package com.ssb.droidsound.database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;


import com.ssb.droidsound.FileIdentifier;
import com.ssb.droidsound.PlayerActivity;

public class FileSystemSource implements DataSource {
	
	public static final String NAME = ".fs_source";
	
	public static String BasePath = null;
	
	private String dirPath = null;
	private String fs_root_path = null;
	private String fs_title = null;
	
	@Override
	public boolean parseDump(InputStream is, int size, SQLiteDatabase scanDb, ScanCallback scanCallback) {
		return true;
	}

	@Override
	public String getTitle()
	{
		return fs_title;
	}
	
	public void setFilesystemPath(String path, String filename) {
		
		fs_title = filename; 
		
		String modsDir = PlayerActivity.prefs.getString("modsDir", null);
		
		File file = new File(modsDir, filename+".fs_source");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter( new FileWriter (file));
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			writer.write(path);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
	}
	public static String getFilesystemPath(File file) {
		
		String buffer = "";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader( new FileReader (file));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			buffer = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return buffer;
	}
	
	@Override
	public Cursor getCursorFromPath(File file, SQLiteDatabase db, int sorting)
	{
		
		String fs = file.getPath();
		
		if (fs_root_path == null)
			fs_root_path = getFilesystemPath(file);
		
		int pos = fs.indexOf(NAME);
		
		//String dirPath = Environment.getExternalStorageDirectory() + "/droidsound/fileCache/" + fs.substring(pos + NAME.length());
		dirPath = fs_root_path + fs.substring(pos + NAME.length());
		file = new File(dirPath);
		if(!file.exists())
			return null;

		MatrixCursor mc = new MatrixCursor(new String[] { "TITLE", "TYPE", "PATH", "FILENAME" });
		
		for(File f : file.listFiles()) {
			
			String name = f.getName();
			//String path = f.getParent();
			name = name.substring(name.lastIndexOf('/') + 1);
			
			if(f.isDirectory()) 
			{
				mc.addRow(new Object [] { name, SongDatabase.TYPE_DIR, null, name } );
			} 
			else 
			{
				if(FileIdentifier.canHandle(name) != null)
					mc.addRow(new Object [] { name, SongDatabase.TYPE_FILE, f.getParent(), f.getName() } );
			}
			
		}		
		return mc;

	}

	@Override
	public String getPathTitle(File file) {		
		//if(displayTitle != null) return displayTitle;
		//String n = file.getName();
		//if(n.contains(NAME)) 
		//	return "Filesystem";
		String name = file.getName().replace(NAME, "");
		return name;
	}

	@Override
	public void createIndex(int mode, SQLiteDatabase db) {
	}

	@Override
	public Cursor search(String query, String fromPath, SQLiteDatabase db) {
		return null;
	}

}
