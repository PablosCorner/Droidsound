package com.ssb.droidsound.file;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.os.NetworkOnMainThreadException;

import java.net.URL;

import org.apache.commons.net.ftp.FTPClient;


import com.ssb.droidsound.utils.Log;

public class FTPStreamSource extends FileSource
{
	private static final String TAG = FTPStreamSource.class.getSimpleName();

	public FTPStreamSource(String ref)
	{
		super(ref);
	}
	
	protected static boolean pingFTP(String host) {
		
		InetAddress in = null;
		
		try {
            
			in = InetAddress.getByName(host);
            
        } 
		catch (UnknownHostException e) {

            e.printStackTrace();
            return false;
        }

        try {
            if (in.isReachable(2000))
            {
            	return true;
            } 
            else
            {
            	return false;
                
            }
        } 
        catch (IOException e) {
            return false;
        }
	}
	
	//
	// this func exists because URL library's getPath fails when there 
	// are special chars like '#'
	//
	protected String intgetPath() throws MalformedURLException
	{
		
		URL url = new URL(getReference());
			
		String full_url = getReference();
		String host = url.getHost();
		int port_string_sz = 0;
		if (url.getPort() != -1)
		{
			Integer port = url.getPort();
			String sPort = port.toString();
			port_string_sz = sPort.length() + 1;
		}
		String path = full_url.substring(full_url.indexOf(host) + host.length() + port_string_sz );
		return path;
	}
	protected URL intGetFTPURL() throws MalformedURLException
	{
		URL url = new URL(getReference());
		return url;
	}
	
	public static FTPClient intGetFTP(String path) {
				
		try {

			URL url = new URL(path);
			
			int reply;		
							
			FTPClient ftp = new FTPClient();
			String host = url.getHost();
			
			
			int port = url.getPort() > -1 ? url.getPort() : 21;

			String userinfo = url.getUserInfo();
			String username = "anonymous";
			String password = "";
			
			if (userinfo != null)
			{
				if (userinfo.contains(":"))
				{
					username = userinfo.indexOf(":") > 0 ? userinfo.substring(0, userinfo.indexOf(":")) : "anonymous";
					password = userinfo.substring(userinfo.indexOf(":")+1);	
				}
			}
			ftp.setConnectTimeout(3000);
			ftp.connect(host, port);
            ftp.login(username, password);
            reply = ftp.getReplyCode();
          
            if (reply == 530)
            {
            	return ftp;
            }
            
            if (reply == 230)
            {
            	
	            ftp.setFileType(FTPClient.BINARY_FILE_TYPE); // change this to ASCII when checking folder lists
	            ftp.enterLocalPassiveMode();
	            ftp.setControlKeepAliveTimeout(30);// 10 seconds
	            //ftp.setDataTimeout(30);
	            ftp.setKeepAlive(true);
	            		            
	            ftp.setReceiveBufferSize(1024 * 512 * 1);
	            ftp.setBufferSize(1024 * 512 * 1);	            	
            	Log.d(TAG, "FTP getting FileStream");
                
                return ftp;
            }
			
		} 
		
		catch (NetworkOnMainThreadException n)
		{
			n.printStackTrace();
		}
		catch (SocketException s)
		{
			s.printStackTrace();

		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();

		} 
		catch (IOException e) 
		{
			e.printStackTrace();

		}
		
		return null;
	}	
}
