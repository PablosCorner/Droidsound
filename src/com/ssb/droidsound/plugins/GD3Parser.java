package com.ssb.droidsound.plugins;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

public class GD3Parser {
	
	private static HashMap<String, String> infoMap = new HashMap<String, String>();
	
	private static String[] items = new String[] {"TrackNameE",  "TrackNameJ", 
												  "GameNameE",   "GameNameJ", 
												  "SystemNameE", "SystemNameJ",
												  "AuthorNameE", "AuthorNameJ",
											      "ReleaseDate", "Creator", "Notes"};    //keep the order, its static

	public static HashMap<String, String> getTags(String filename)
	{
		try
		{
            byte [] buf = new byte[1024];
            byte buffer[] = null;

			FileInputStream filereader = new FileInputStream(filename);

            int filesize = filereader.available();
            byte [] fbuffer = new byte[filesize];
            
            filereader.read(fbuffer);
			filereader.close(); 
			
			int i=0;
			
			if (fbuffer[i] == 0x1f && ((fbuffer[i+1] & 0xff) == 0x8b) && fbuffer[i+2] == 0x08)
			{
				ByteArrayInputStream bytein = new ByteArrayInputStream(fbuffer);
				GZIPInputStream gzin = new GZIPInputStream(bytein);
				ByteArrayOutputStream byteout = new ByteArrayOutputStream();
			
				int res = 0;
	
				while (res >= 0) {
				    res = gzin.read(buf, 0, buf.length);
				    if (res > 0) {
				        byteout.write(buf, 0, res);
				    }
				}
				buffer = byteout.toByteArray();
				byteout.close();
				gzin.close();
				bytein.close();
				gzin = null;
				byteout = null;
				bytein = null;
				buf = null;

			}
			else
			{
				buffer = fbuffer;
			}
			
			filereader = null;


            // find the "GD3\x20" tag
            while (i < buffer.length)
            {
                if (buffer[i] == 'G' && buffer[i+1] == 'd' && buffer[i+2] == '3' && buffer[i+3] == 0x20)
                {
                    i += 12;
                    break;
                }
                i++;
            }
            if (i == buffer.length)
            	return null;
            
            String val = "";
            int k=0;
            
            while (i < buffer.length)
            {
                String itemname = items[k].toString();

                if (!itemname.endsWith("J") && ((buffer[i] & 0xff) >= 0x20) && ((buffer[i] & 0xff) <= 0xff) && buffer[i+1] == 0)
                {
                    val = val.concat(String.valueOf((char)buffer[i]));
                }

                else if (buffer[i] == 0 && buffer[i+1] == 0)
                {
                    infoMap.put(items[k], val);
                    k+=1;
                    val = "";
                }
                
                i+=2;
                if (k == items.length)
                    break;
            }
       
            return infoMap;

        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            return null;
        }
	}
}
