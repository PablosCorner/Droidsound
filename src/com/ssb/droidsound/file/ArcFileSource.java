package com.ssb.droidsound.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ssb.droidsound.utils.Archive.Entry;
import com.ssb.droidsound.utils.NativeArcFile;
import com.ssb.droidsound.utils.NativeArcFile.ArcEntry;

public class ArcFileSource extends FileSource {	
	
	private static final Set<String> ARCHIVE_EXTENSIONS = new HashSet<String>(Arrays.asList(
			".ZIP", ".7Z", ".RAR", ".GZ")); 

	private NativeArcFile arcFile;
	long size;
	boolean isValid = false;

	private ArcEntry arcEntry;
	private String arcPath;
	private String arcExt;
	
	public ArcFileSource (String arcPath, String entryName) {
		super(arcPath + "/" + entryName);

		this.arcPath = arcPath;			
		try {
			arcFile = new NativeArcFile(arcPath);
			arcEntry = (ArcEntry) arcFile.getEntry(entryName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		size = arcEntry.getSize();		
	}
	
	public ArcFileSource (NativeArcFile arc, String entryName) {
		super(arc.getArcName() + "/" + entryName);
        isValid = false;
		this.arcFile = arc;
		this.arcPath = null;
		arcEntry = (ArcEntry) arcFile.getEntry(entryName);
		if(arcEntry != null) {
			size = arcEntry.getSize();
			isValid = true;
		}
        else {

		}
	}

	public ArcFileSource (NativeArcFile arc, ArcEntry ae) {
		super(arc.getArcName() + "/" + ae.getName());
		this.arcFile = arc;
		this.arcEntry = ae;
		this.arcPath = null;
		size = arcEntry.getSize();
	}

    public ArcFileSource (String fileRef) {
        super(fileRef);
        isValid = false;

        int ext = -1;
        for (String extension : ARCHIVE_EXTENSIONS)
        {
        	ext = fileRef.toUpperCase(Locale.ENGLISH).indexOf( extension + "/");
        	if (ext > 0)
        	{
        		arcExt = extension;
        		break;
        	}
        }
        if(ext < 0) 
        	return;
       	
        
        arcPath = fileRef.substring(0, ext+arcExt.length());
        String entryName = fileRef.substring(ext+arcExt.length() + 1);
        try {
            arcFile = new NativeArcFile(arcPath);
            arcEntry = (ArcEntry) arcFile.getEntry(entryName);
            if(arcEntry == null)
                return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        isValid = true;
        size = arcEntry.getSize();
    }
    
    protected ArcEntry intGetMatchingEntry(NativeArcFile arc, String name)
	{
		
		name = Pattern.quote(name);
		
		Pattern p = Pattern.compile("^"+name, Pattern.CASE_INSENSITIVE);
		
        Iterator<Entry> ae = arc.getIterator();
		while(ae.hasNext())
        {
            ArcEntry entry = (ArcEntry) ae.next();
            String entryname = entry.getName();
            Matcher m = p.matcher(entryname);
            if (m.find())
            {
                return entry;
            }
        }
		return null;
	}
    
	@Override
	protected FileSource intGetRelative(String name) {
		String entryName = arcEntry.getName();
		int slash = entryName.lastIndexOf('/');
		String arcDir = "";
		if(slash > 0)
			arcDir = entryName.substring(0, slash+1);		
		ArcFileSource afs = new ArcFileSource(arcFile, arcDir + name);
		if(afs != null && afs.isValid)
			return afs;
		return null;
	}

	@Override
	public InputStream intGetStream() {
		return arcFile.getInputStream(arcEntry);
	}
	
	@Override
	public long getLength() {
		return size;
	}
	
	@Override
	public void close() {
		super.close();
		if(arcFile != null)
			arcFile.close();
		arcFile = null;
	}
	
}
