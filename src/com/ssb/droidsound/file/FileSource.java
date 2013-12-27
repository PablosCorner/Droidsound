package com.ssb.droidsound.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.net.ftp.FTPClient;
import com.ssb.droidsound.utils.DataFileSource;
import com.ssb.droidsound.utils.Log;


public abstract class FileSource
{
	private static final String TAG = FileSource.class.getSimpleName();
	
	private File file;
	private InputStream inputStream;
	private byte[] buffer;
	int bufferPos;
	long size;


	private boolean isFile;
	private String baseName;
	private String reference;
	private CharSequence refPath;
	private FTPClient my_ftp = null;
	
	public FileSource(String ref)
	{
		reference = ref;
		int slash = ref.lastIndexOf('/');
		baseName = ref.substring(slash + 1);
		refPath = slash > 0 ? ref.subSequence(0, slash+1) : "";
	}

	public static FileSource create(String ref)
	{

		FileSource fs;
		
		if(ref.toLowerCase().startsWith("http"))
		{
			fs = new StreamSource(ref);
		} 
		
		else if(ref.toLowerCase().startsWith("ftp"))
		{
			fs = new FTPStreamSource(ref);
		} 
		
		else if(ref.toLowerCase().contains(".zip/"))
		{
			fs = new ZipFileSource(ref);
		}
		
		else
		{
			fs = new RealFileSource(new File(ref));
		}

		return fs;		
	}
	
	public static FileSource fromFile(File f) {
		return new RealFileSource(f);
	}
	
	public static FileSource fromData(String name, byte[] bs) {
		return new DataFileSource(name, bs);
	}
	
	public static FileSource fromStream(String name, InputStream is) {
		return new StreamFileSource(name, is);
	}
	
	protected File intGetFile() { return null; }
	protected byte [] intGetContents() { return null; }
	protected InputStream intGetStream() throws IOException { return null; }

	protected URL intGetFTPURL() throws IOException { return null; }
	protected String intgetPath() throws MalformedURLException { return null; }
	protected FileSource intGetRelative(String name) { return null; }


	public FileSource(String name, byte[] bs)
	{
		buffer = bs;
		this.baseName = name;
		this.file = null;
		this.size = bs.length;
		this.isFile = false;		
	}

	public boolean savedata_to_file(byte[] buffer)
	{
		try
		{
			FileOutputStream fos;
			file = FileCache.getInstance().getFile(reference);
			
			fos = new FileOutputStream(file);
			fos.write(buffer);
			fos.flush();
			fos.close();
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return true;
		
		
	}
	
	public byte[] download_data(InputStream is)
	{
		
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		byte [] data = new byte [65536 * 2];
		int n;
		try
		{
			while ((n = is.read(data, 0, data.length)) != -1)
			{
				bs.write(data, 0, n);
			}
			bs.flush();
			bs.close();
			
			is.close();
			buffer = bs.toByteArray();
			size = buffer.length;
		}
		
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		
		return buffer;		
		
	}
	
	
	//
	// returns buffer containing the song data
	//
	public byte [] getContents() {

		if(buffer == null)
		{
			
			buffer = intGetContents();
			size = getLength();
			if(buffer == null)
			{
				InputStream is = null;
				try
				{
					// see if its in fileCache
					if (reference.contains("tp://"))
					{
						file = FileCache.getInstance().getFile(reference);
						if(file.exists())
						{
							buffer = new byte[(int) file.length()];
							FileInputStream fis = new FileInputStream(file);
							fis.read(buffer);
							fis.close();
							size = buffer.length;
							return buffer;
						}
						
					}
					if (reference.contains("ftp://"))
					{
						while (true)
						{
							my_ftp = FTPStreamSource.intGetFTP(reference);
							if (my_ftp != null)
							{
								break;
							}
						}
						String host_path = intgetPath();
						is = my_ftp.retrieveFileStream(host_path);
					}
					else
					{
						is = intGetStream();
					}
					
				} 
				catch (IOException e1)
				{
				}
				if(is != null)
				{
					download_data(is);
					savedata_to_file(buffer);
					if (my_ftp != null)
					{
						try
						{
							my_ftp.logout();
							my_ftp.disconnect();
							my_ftp = null;
						} 
						catch (IOException e) 
						{
							e.printStackTrace();
						}
					}
				} 
				else
				{
					getFile();
					if(buffer == null)
					{
						buffer = new byte[(int) size];
						try
						{
							FileInputStream fis = new FileInputStream(file);
							fis.read(buffer);
							fis.close();
						} 
						catch (IOException e)
						{
							return null;
							//e.printStackTrace();
						}
					}
				}
			}	

		}
		return buffer;
		
	}
	
	public long getLength() {
		return size;
	}
	
	//
	// this function returns filelist from wanted directory (http/ftp)
	//
	/*
	public String scanRemoteFiles(String path, String filename)
	{
		MatrixCursor ce = HttpSongSource.getFilesInCurPath(path);
		ce.moveToFirst();
		while (ce.isAfterLast() == false)
		{
			String str = ce.getString(ce.getColumnIndex("FILENAME"));
			ce.moveToNext();
		}
		
	
		return "joo";			
		
	}
	*/
	
	//
	// this function does 2 things, download the file and return the file name
	//
	public File getFile()
	{
		
		if(file == null)
		{
			// First see if we can get the file directly
			file = intGetFile();
			if(file != null)
			{
				size = file.length();
				return file;
			}

			file = FileCache.getInstance().getFile(reference);			
			
			if(file.exists())
			{
				Log.d(TAG, "LUCKY! File '%s' exists already\n", file.getPath());
				size = file.length();
				return file;
			}
			
			if(buffer == null)
				buffer = intGetContents();
			
			if(buffer == null)
				getContents();
		}
		if (file.exists())
		{
			return file;
		}
		return null;
	}
	

	public String getName() {
		return baseName;
	}
	
	public Boolean renameFile(String newName)
	{
					
		File file = new File(this.file.getPath());
		File newfile = new File(newName);
				
		boolean success = file.renameTo(newfile);
		
		return success;
	}
	public FileSource getRelative(String name)
	{
		FileSource fs = null;

		fs = intGetRelative(name);
				
		if(fs == null)
		{
			if(file == null)
			{
				fs = FileSource.create(refPath + name);
				return fs;
			} 
			
			File f = new File(file.getParent() + '/' + name);
			if (!f.exists())
			{
				fs = FileSource.create(refPath + name);
				//scanRemoteFiles(refPath.toString(), name);
				return fs;
			}
			
			if(file != null)
			{
				fs = fromFile(new File(file.getParentFile(), name));
			}
		}
		
		return fs;
	}


	public String getExt()
	{
		String ext = "";
		int dot = baseName.lastIndexOf('.');
		if(dot > 0) {
			ext = baseName.substring(dot+1);

			char c = 'X';
			int e = 0;
			while(e < ext.length() && Character.isLetterOrDigit(c)) {
				e++;
				if(e == ext.length())
						break;
				c = ext.charAt(e);
			}
			ext = ext.substring(0,e);

		}
		return ext.toUpperCase();
	}

	public boolean isFile()
	{
		return isFile;
	}

	public int read(byte[] data) throws IOException
	{
		if(buffer != null) {
			System.arraycopy(buffer, bufferPos, data, 0, data.length);
			bufferPos += data.length;
			return data.length;
		} 
		if (reference.contains("ftp://"))
		{
			
			
			
		}
		else
		{
			inputStream = intGetStream();		
			if(inputStream != null) {
				return inputStream.read(data);
			}
			
		}
			
			
		file = intGetFile();
		if(file !=null) {
			inputStream = new FileInputStream(file);
			return inputStream.read(data);
		}
		return -1;
	}

	public void close()
	{
		if(inputStream != null)
			try {
				inputStream.close();
			} catch (IOException e) {
			}
		inputStream = null;
	}

	public String getReference()
	{
		return reference;
	}
	
	public String getLocalPath()
	{
		return this.file.getPath();
	}
	
	public String getrefPath()
	{
		String refPath;
		int slash = reference.lastIndexOf('/');
		refPath = (String) (slash > 0 ? reference.subSequence(0, slash+1) : "");
		return refPath;
	}
}
