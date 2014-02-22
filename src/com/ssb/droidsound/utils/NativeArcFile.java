package com.ssb.droidsound.utils;
 
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.ZipEntry;

import com.ssb.droidsound.file.ArcFileSource;
import com.ssb.droidsound.file.FileSource;

public class NativeArcFile implements Archive {
	private static final String TAG = NativeArcFile.class.getSimpleName();

	public static class ArcEntry extends ZipEntry implements Archive.Entry {

		public ArcEntry(String name) {
			super(name);
		}
		
		public ArcEntry(ZipEntry ze) {
			super(ze);
		}
		
		private int index;
		private long entry_size;
		private long position;

		protected void setIndex(int i) {
            index = i;
        }

		protected int getIndex() {
            return index;
        }

		protected void setPosition(long pos) {
            position = pos;
        }

		public long getPosition() {
            return position;
        }

		@Override
		public String getPath() {
			return this.getName();
		}

        @Override
        public long getSize() {
            return entry_size;
        }
     
        public void setSize(long size) {
        	entry_size = size;
        }
    };
	
	private static long lastArcRef = 0;
	private static String lastArcName = null;
	
	private static long arcRef;
	private String arcName;
	

	public static void closeCached() {
		try {
			new NativeArcFile((String)null);
		} catch (IOException e) {
		}
	}
	
	public NativeArcFile(String fileName) throws IOException {
		init(fileName);
	}
	
	public NativeArcFile(File file) throws IOException {
		init(file.getPath());
	}
	
	private void init(String fileName) throws IOException {
		
		arcName = fileName;

		if(lastArcName != null)
		{
			if(arcName != null && arcName.equals(lastArcName))
			{
				arcRef = lastArcRef;
				return;
			} 
			else 
			{
				arcRef = lastArcRef;
				closeFile(arcRef);
				arcRef = 0;
				lastArcName = null;
			}
		}
		
		if(arcName == null)
			return;

		arcRef = openFile(arcName);
		if(arcRef == 0) 
		{
			throw new IOException();
		}
	}
	
	
	
	public String getArcName() {
		return arcName;
	}
	
	public Archive.Entry getEntry(String entryName) {
		
		int e = findEntry(arcRef, entryName);
		if(e >= 0) {
			//String name = getEntry(arcRef, e);
			String name = getcurrentName(arcRef);
			ArcEntry entry = new ArcEntry(name);
			entry.setIndex(e);
			entry.setSize(getcurrentSize(arcRef));
			return entry;
		}
		return null;
	}

	static class NZInputStream extends InputStream {

		private NativeArcFile arcFile;
		private int total;

		
		private NZInputStream(int idx, long arcRef, NativeArcFile af, int len)
		{
			arcFile = af;
			total = len;
		}

	
		@Override
		public int read() throws IOException {
			byte [] b = new byte [1];
			int rc = read(b, 0, 1);
			if(rc > 0)
			{
				return b[0];
			} 
			else
			{
				return -1;
			}
		}

		@Override
		public int available() throws IOException {
			return total;
		}
		
		@Override
		public int read(byte[] buffer, int offset, int length) throws IOException
		{
			
			int avail = available();
			if (avail == 0)
				return -1;
			if (avail < length)
				length = avail;
			
			int bytesread = arcFile.readfd(arcRef, buffer, length);
			if(bytesread > 0)
			{
				total -= bytesread;
			}
			return bytesread;
		}
		
		@Override
		public int read(byte[] b) throws IOException {
			return read(b, 0, b.length);
		}
	}
	
	
	public InputStream getInputStream(ArcEntry entry) {
		
		int index = entry.getIndex();
		int cursize = (int)entry.getSize();
		long position = entry.getPosition();

		if (position > 0)
			setPosition(arcRef, position);
		else
			getPositionByIndex(arcRef, index);
		
		if (arcRef != 0)
		{
			return new NZInputStream(index, arcRef, this, cursize);
		}
		else
		{
			return null;
		}
	}
	
	public int size() {
		return getTotalCount(arcRef);
	}
	
	public void close() {
		lastArcRef = arcRef;
		lastArcName = arcName;
		return;
	}
	
	private static class MyIterator implements Iterator<Archive.Entry> {
		
		private NativeArcFile arcFile;
		private int currentIndex;
		private int total;
		private int position;

		public MyIterator(NativeArcFile af){
			arcFile = af;
			total = af.getTotalCount(arcRef);
			position = -1;
			currentIndex = 0;
			af.rewind(arcRef);
		}
		
		@Override
		public boolean hasNext() {
			return (currentIndex < total);
		}

		@Override
		public Archive.Entry next()
		{
			position = (int)arcFile.getPositionByIndex(arcRef, currentIndex);			
			
			String name = arcFile.getcurrentName(arcRef);
			ArcEntry entry = new ArcEntry(name);
			entry.setIndex(currentIndex);
			entry.setPosition(position);
			entry.setSize(arcFile.getcurrentSize(arcRef));
			currentIndex++;			
			return entry;
		}

		@Override
		public void remove() {
		}
	}
	
	@Override
	public Iterator<Archive.Entry> getIterator() {
		return new MyIterator(this);
	}

	@Override
	public FileSource getFileSource(Archive.Entry entry) {
		FileSource fs = new ArcFileSource(this, (ArcEntry) entry);
		return fs;
	}

	@Override
	public int getFileCount() {
		return getTotalCount(arcRef);
	}
		
	private native void setPosition(long arcRef, long pos);
	private native long getPositionByIndex(long arcRef, int i);
	private native void rewind(long arcRef);
    private native long openFile(String fileName);
    private native void closeFile(long arcRef);
    private native int getTotalCount(long arcRef);
    private native String getentryName(long arcRef, int i);
    private native int getcurrentSize(long arcRef);
    private native String getcurrentName(long arcRef);    
    private native int findEntry(long arcRef, String name);
    private native int readfd(long arcRef, byte [] buffer, int size);


}
