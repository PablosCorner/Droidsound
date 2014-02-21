package com.ssb.droidsound.database;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;

import com.ssb.droidsound.FileIdentifier;
import com.ssb.droidsound.file.FTPStreamSource;

import com.ssb.droidsound.utils.Log;

public class HttpSongSource {
	private static final String TAG = HttpSongSource.class.getSimpleName();
	
	private static class CacheEntry
	{
		public MatrixCursor cursor;
		public int status;		
		public CacheEntry(MatrixCursor cr, int st)
		{
			status = st;
			cursor = cr;
		}
	}
 
	public static FTPClient cur_ftp;
	private static String cur_host = "";
		
	private static Map<String, CacheEntry> dirMap = new HashMap<String, CacheEntry>();
	
	private static Map<String, CacheEntry> allfilesdirMap = new HashMap<String, CacheEntry>();

	private static List<String> failed_ftps = new ArrayList<String>();
	
	private static Map<String, Character> htmlMap = new HashMap<String, Character>();

	private static Thread httpThread = null;
	
	private static HTTPWorker httpWorker = null;
	static {
		htmlMap.put("amp", '&');
		htmlMap.put("lt", '<');
		htmlMap.put("gt", '>');
		htmlMap.put("quot", '"');
		
	}
	
	
	private static void enableHttpResponseCache() {
	    try {
	        long httpCacheSize = 16 * 1024 * 1024; // 16 MiB
	        
	        File droidDir = new File(Environment.getExternalStorageDirectory(), "droidsound");
			File tempDir = new File(droidDir, "httpCache");
	        
	        File httpCacheDir = new File(tempDir, "http");
	        Class.forName("android.net.http.HttpResponseCache")
	            .getMethod("install", File.class, long.class)
	            .invoke(null, httpCacheDir, httpCacheSize);
	        	
	    } 
	    
	    catch (Exception httpResponseCacheNotAvailable) {
	    }
	}
	
	
	
	private static String htmlFix(String s) {

		if(s == null) return s;
		
		int a = 0;
		int start = 0;
		StringBuilder sb = new StringBuilder();
		
		while(a >= 0) {
			a = s.indexOf('&', start);
			if(a >= 0) {
				int e = s.indexOf(';', a+1);
				if(e >= 0) {
					sb.append(s, start, a);
					String code = s.substring(a+1, e);
					if(code.charAt(0) == '#') {
						try {
							sb.append((char)Integer.parseInt(code.substring(1)));
						} catch (NumberFormatException excp) {
						}
					} else {
						Character c = htmlMap.get(code);
						if(c != null)
							sb.append(c);
					}
					a = e;
				}
				start = a+1;
			}
		}
		
		sb.append(s.substring(start));
		
		return sb.toString();
		
	}
	// *********************************************************************
	
	private static class HTTPWorker implements Runnable
	{

		private List<String> dirList = new ArrayList<String>();
		private Context context;
		private boolean doQuit = false;
		private boolean failed = false;
		
		public HTTPWorker(Context ctx)
		{
			context = ctx;
		}

		@Override
		public void run()
		{
				
			while(!doQuit)
			{
				try
				{
					synchronized (this)
					{
						wait();						
					}
				} 
				catch (InterruptedException e)
				{
					return;
				}
				Log.d(TAG, "HTTP THREAD WOKE UP");				
				
				while(true)
				{
					String path = null;
					
					synchronized (dirList)
					{
						if(dirList.size() > 0)
						{
							Log.d(TAG, "List has %d entries ", dirList.size());
							path = dirList.get(0);

							Log.d(TAG, "Found " + path);
						}
					}
					if(path != null)
					{
						boolean result = false;
						Log.d(TAG, "Trying to get directory from FTP/HTTP");
						
						result = getDirFromHTTP(path);
						if (result == true)
						{
							Log.d(TAG, "SUCCESS, got directory from FTP/HTTP");
						}
						if (result == false)
						{
							
							//failed_ftps.add(path);
							Log.d(TAG, "FAILURE, connection failed to FTP/HTTP");
							
							//Intent intent = new Intent("com.sddb.droidsound.FAILED_SERVER_CONN");
							//context.sendBroadcast(intent);
						}
						
						if (dirList.size() > 0)
							dirList.remove(path);
					}
					
					else
						break;
				}
			}
			Log.d(TAG, "FTP/HTTP WORKER EXITING");
			return;
			
		}

		public void getDir(String pathName) {
			
			synchronized (dirList) {
				
				if(dirList.contains(pathName)) {
					Log.d(TAG, "Already working on " + pathName);
				} else {				
					dirList.add(pathName);
					Log.d(TAG, "Added " + pathName);
				}
			}
			synchronized (this)
			{
				notify();				
			}
		}
		// *****************************************************
		
		@SuppressWarnings("resource") // The matrix cursor does not need to be closed
		private boolean getDirFromHTTP(String pathName)
		{
			
			MatrixCursor cursor = new MatrixCursor(new String [] { "TITLE", "TYPE", "PATH", "FILENAME"} );
			//MatrixCursor cursor2 = new MatrixCursor(new String [] { "PATH", "FILENAME"} );
			
			String msg = null;
			int status = 0;
			
			try 
			{
				URL url = new URL(pathName);
				
				if (pathName.contains("ftp://")) 
				{
					if (!cur_host.equals(url.getHost()))
					{
						cur_host = url.getHost();
						if (cur_ftp != null)
						{
							cur_ftp.logout();
							cur_ftp.disconnect();
							cur_ftp = null;
						}
						cur_ftp = FTPStreamSource.intGetFTP(pathName);
						
					}
					if (cur_ftp == null)
					{
						cur_ftp = FTPStreamSource.intGetFTP(pathName);
					}

					// make a test here to see we are really connected or timed-out
					
					if (cur_ftp == null)
					{
						Intent intent = new Intent("com.sddb.droidsound.FAILED_SERVER_CONN");
						context.sendBroadcast(intent);
						return false;
					}
						
					if (cur_ftp != null)
		            {
						int replycode = cur_ftp.getReplyCode();
						if (replycode < 200 || replycode > 299)
						{
							cur_ftp = FTPStreamSource.intGetFTP(pathName);
							replycode = cur_ftp.getReplyCode();
							if (replycode < 200 || replycode > 299)
							{
								return false;
							}
						}
						
						Log.d(TAG, "FTP connected");
		            	
		            	cur_ftp.changeWorkingDirectory(url.getPath());
		            	
		                Log.d(TAG, "FTP changed working dir");
		                
		                FTPFile[] ftp_files = cur_ftp.listFiles();
		                Log.d(TAG, "FTP got dirlist");
		                
		                for(int i=0; i<ftp_files.length; i++)
		                {
		           
		                	String path = pathName;
		                    int type = ftp_files[i].getType();
		                    
		                    String filename = ftp_files[i].getName();
		                    String title = ftp_files[i].getName();
		                    //long filesize = ftp_files[i].getSize();
		                    
		                    if (type == 0)
		                    {
		                    	if(FileIdentifier.canHandle(filename) != null)
		                    	{            		
		                    		int entry_type = SongDatabase.TYPE_FILE;
		                    		cursor.addRow(new Object [] { title, entry_type, path, filename } );
		                    	}
		                    	//cursor2.addRow(new Object [] { path, filename } );
		                    	
		                    }
		                    
		                    else if (type == 1)
		                    {
	                    	    int entry_type = SongDatabase.TYPE_DIR;
		                    	cursor.addRow(new Object [] { title, entry_type, null, filename } );
		                    }
                    
		                }
		    			
		            }
		            Log.d(TAG, "FTP done processing files");
		            synchronized (dirMap)
	    			{
	    				 dirMap.put(pathName, new CacheEntry(cursor, status));							
	    				 //allfilesdirMap.put(pathName, new CacheEntry(cursor2, status));
	    			}
	    			 
	    			Intent intent = new Intent("com.sddb.droidsound.REQUERY");
	    			context.sendBroadcast(intent);
	    			
	    			Log.d(TAG, "FTP all done");
		            
		            return true;
		            }
									
// -----------------------------------------------------------------------					
		
				URLConnection conn = url.openConnection();
				HttpURLConnection httpConn = (HttpURLConnection) conn;
				httpConn.setAllowUserInteraction(false);
				httpConn.setInstanceFollowRedirects(true);
				httpConn.setRequestMethod("GET");
		
				Log.d(TAG, "Connecting to " + pathName);
		
				httpConn.connect();
		
				int response = httpConn.getResponseCode();
				if (response == HttpURLConnection.HTTP_OK) {
					Log.d(TAG, "HTTP connected");

					HtmlCleaner cleaner = new HtmlCleaner();
					CleanerProperties props = cleaner.getProperties();
					props.setOmitComments(true);

					 TagNode node = cleaner.clean(new InputStreamReader(conn.getInputStream()));
					 try {
						 Object[] links = node.evaluateXPath("//a");
						 						 
						 for(int i=0; i<links.length; i++) {
							 TagNode atag = (TagNode) links[i];
							 String href = atag.getAttributeByName("href");
							 String text = atag.getText().toString();
							 
							 if(href == null || text == null)
								 continue;
							 
							 Log.d(TAG, "Found link to '%s' named '%s'", href, text);
							 
							 String title = htmlFix(text);
							 String fileName = htmlFix(href);

							 String path = pathName;						 
							 
							 int type = SongDatabase.TYPE_FILE;
							 if(!fileName.startsWith("/") && !fileName.startsWith("?")) {
								 if(fileName.endsWith("/")) {								 
									 if(title.endsWith("/"))
										 title = title.substring(0, title.length()-1);
									 type = SongDatabase.TYPE_DIR;
									 fileName = fileName.substring(0, fileName.length()-1);
									 cursor.addRow(new Object [] { title, type, null, fileName } );
								 } 
								 else
								 {
									 if(FileIdentifier.canHandle(fileName) != null)
										 cursor.addRow(new Object [] { title, type, path, fileName } );
								 }
							 }
						 }						

					} catch (XPatherException e)
					{
						
						msg = "<HTML parsing failed>";
						status = -1;
						e.printStackTrace();
					}

				} else {
					msg = "<Connection failed>";
					status = -2;
					Log.d(TAG, "Connection failed: %d", response);
				}
			} catch (MalformedURLException me) {
				msg = "<Illegal URL>";
				status = -3;
				dirList.remove(pathName);
				return false;
			} catch (IOException e) {
				
				e.printStackTrace();
				msg = "<IO Error>";
				status = -4;
				dirList.remove(pathName);
				return false;
			}
			
			if(msg != null) {
				cursor.addRow(new Object [] { msg, SongDatabase.TYPE_FILE, null, "" } );
				msg = null;
			}
			
			 synchronized (dirMap)
			 {
				 dirMap.put(pathName, new CacheEntry(cursor, status));
			 }
			 
			Intent intent = new Intent("com.sddb.droidsound.REQUERY");
			context.sendBroadcast(intent);
			return true;
		}
		
		
	}

	public static void resetdirMap(Context ctx)
	{
		Toast.makeText(ctx, "Directory cache cleaned!", Toast.LENGTH_LONG).show();
		dirMap.clear();
		return;
	
	}
	
	public static MatrixCursor getFilesInCurPath(String pathName)
	{
		CacheEntry ce = null;
		ce = allfilesdirMap.get(pathName);
		return ce.cursor;
	
	}
	public static Cursor getFilesInPath(Context ctx, String pathName, int sorting)
	{
			
		Log.d(TAG, "PATH '%s'", pathName);
		
		if(!pathName.endsWith("/"))
			pathName = pathName + "/";
		
		
		CacheEntry ce = null;
		synchronized (dirMap)
		{
			ce = dirMap.get(pathName);
			if(ce != null && ce.status < 0)
			{
				dirMap.remove(ce);
			}
		}
		if (httpWorker != null)
		{
			if (httpWorker.failed == true)
			{
				
				httpWorker.failed = false;
				httpWorker.doQuit = true;
				
				
				Toast toast = Toast.makeText(ctx, "Failed connecting to the site, check the URL", Toast.LENGTH_SHORT);
				TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
				v.setTextColor(Color.RED);
				toast.show();

				return null;
			}
		}

		if(ce != null)
		{
			Log.d(TAG, "IN CACHE!");
			return ce.cursor;
		}
		
		if (pathName.startsWith("http://"))
			enableHttpResponseCache();
		
		if(httpThread == null || !httpThread.isAlive())
		{
			httpWorker = new HTTPWorker(ctx);
			httpThread = new Thread(httpWorker);
			httpThread.start();
			try
			{
				Thread.sleep(100);
			} 
			
			catch (InterruptedException e) {
			}
			httpWorker.doQuit = false;
		}
		
		MatrixCursor cursor = new MatrixCursor(new String [] { "TITLE", "TYPE", "PATH", "FILENAME"} );

		//if (failed_ftps.contains(pathName))
		//	return cursor;

		httpWorker.getDir(pathName);
		
		//MatrixCursor cursor = new MatrixCursor(new String [] { "TITLE", "TYPE", "PATH", "FILENAME"} );
		//Toast.makeText(ctx, "...working...", Toast.LENGTH_SHORT).show();
		cursor.addRow(new Object [] { "...working...", SongDatabase.TYPE_FILE, null, "" } );
		return cursor;
	}
}
