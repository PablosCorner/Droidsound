package com.ssb.droidsound.utils;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

public class Unpacker {
	@SuppressWarnings("unused")
	private static final String TAG = Unpacker.class.getSimpleName();
	
	public static ZipFile openArchive(File path) throws IOException {		
		return new ZipFile(path);
	}
	

}
