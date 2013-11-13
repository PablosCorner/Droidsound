package com.ssb.droidsound.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import java.util.regex.*;

public class ZipFileSource extends FileSource {	
	@SuppressWarnings("unused") private static final String TAG = ZipFileSource.class.getSimpleName();
	
	private ZipFile zipFile;
	int bufferPos;
	long size;
	private ZipEntry zipEntry;
	private String zipPath;

	
	
	public ZipFileSource(String zipPath, String entryName)
	{		
		super(zipPath + "/" + entryName);
		this.zipPath = zipPath;			
		try 
		{
			zipFile = new ZipFile(zipPath);
			zipEntry = (ZipEntry) zipFile.getEntry(entryName);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		size = zipEntry.getSize();		
	}
	


	public ZipFileSource(ZipFile zip, String entryName)
	{
		super(zip.getName() + "/" + entryName);
		this.zipFile = zip;
		this.zipPath = null;
		zipEntry = intGetMatchingEntry(zip, entryName);
		//zipEntry = (ZipEntry) zip.getEntry(entryName);
		if (zipEntry != null)
		{
			size = zipEntry.getSize();
		}
		
	}
	

	public ZipFileSource(ZipFile zip, ZipEntry ze) {
		super(zip.getName() + "/" + ze.getName());
		this.zipFile = zip;
		this.zipEntry = ze;
		this.zipPath = null;
		size = zipEntry.getSize();
	}
	
	@Override
	protected FileSource intGetRelative(String name) {
		
		String entryName = zipEntry.getName();
		int slash = entryName.lastIndexOf('/');
		String zipDir = "";
		if(slash > 0)
			zipDir = entryName.substring(0, slash+1);		
		return new ZipFileSource(zipFile, zipDir + name);
		
	}
	
	protected ZipEntry intGetMatchingEntry(ZipFile zip, String name)
	{
		
		name = Pattern.quote(name);
		
		Pattern p = Pattern.compile("^"+name, Pattern.CASE_INSENSITIVE);
		
		Enumeration<? extends ZipEntry> entries = zip.entries();
        
		while(entries.hasMoreElements())
        {
            ZipEntry entry = entries.nextElement();
            String entryname = entry.getName();
            Matcher m = p.matcher(entryname);
            if (m.find())
            {
                return entry;
            }
        }
		return null;
	}
	
	
	public ZipFileSource(String fileRef)
	{
		super(fileRef);
		
		int ext = fileRef.toLowerCase().indexOf(".zip/");
		if(ext < 0) return;
		zipPath = fileRef.substring(0, ext+4);
		String entryName = fileRef.substring(ext+5);
		try {
			zipFile = new ZipFile(zipPath);
			zipEntry = (ZipEntry) zipFile.getEntry(entryName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		size = zipEntry.getSize();
	}

	@Override
	public InputStream intGetStream() throws IOException {
		return zipFile.getInputStream(zipEntry);
	}
	
	@Override
	public long getLength()
	{
		return size;
	}
	
	@Override
	public void close()
	{
		super.close();
		if(zipFile != null)
			try {
				zipFile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		zipFile = null;
	}
	
}
