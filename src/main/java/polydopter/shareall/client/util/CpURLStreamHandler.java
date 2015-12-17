package polydopter.shareall.client.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Stack;

public class CpURLStreamHandler extends URLStreamHandler {

	@Override
	protected URLConnection openConnection(URL url) throws IOException {
		url = new URL(null, url.getProtocol() + ":" + url.getPath(), this);
		return new CpURLConnection(url);
	}

	static class CpURLConnection extends URLConnection {
	    InputStream in;
	    CpURLConnection (URL url)
	        throws IOException {
	        super( url );
	    }
	    synchronized public void connect() throws IOException { 
	    	String path = url.getPath();
	    	String filePath  = reducePath(path);
			in = ClassLoader.getSystemResourceAsStream(filePath);
	    	connected = true;
	    }
	    
	    public static String reducePath(String path)
	    {
	    	String[] paths = path.split("/");
	    	Stack<String> stk = new Stack<>();
	    	for(int i=0;i<paths.length;i++)
	    	{
	    		stk.push(paths[i]);
	    		if(paths[i].equals("..")){ stk.pop(); if(stk.size() > 0) stk.pop();}
	    	}
	    	StringBuffer buffer = new StringBuffer();
	    	for(int i=0;i<stk.size();i++)
	    		buffer.append(stk.get(i)).append("/");
	    	buffer.deleteCharAt(buffer.length()-1);
	    	return buffer.toString();
	    }
	    
	    synchronized public InputStream getInputStream()
	       throws IOException {
	        if (!connected)
	            connect();
	        return ( in );
	    }  
	}
}

