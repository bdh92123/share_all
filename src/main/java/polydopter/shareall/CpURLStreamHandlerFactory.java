package polydopter.shareall;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import polydopter.shareall.client.util.CpURLStreamHandler;

public class CpURLStreamHandlerFactory implements URLStreamHandlerFactory {

	@Override
	public URLStreamHandler createURLStreamHandler(String protocol) {
		if ( protocol.equalsIgnoreCase("cp") ) 
            return new CpURLStreamHandler();
        else 
            return null; 
	}

}
