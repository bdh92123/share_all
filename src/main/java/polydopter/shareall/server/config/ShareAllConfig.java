package polydopter.shareall.server.config;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShareAllConfig {
	
	private static Logger LOG = Logger.getLogger(ShareAllConfig.class.getName());
	private static Properties props = new Properties();
	
	static {
		try {
			props.load(ClassLoader.getSystemResourceAsStream("config.properties"));	
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Error to load config.properties", e);
		}
	}
	
	public static String get(String key) {
		return props.getProperty(key);
	}
	
	public static String get(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}
	
	public static Integer getInteger(String key) {
		return Integer.parseInt(props.getProperty(key));
	}
	
	public static Integer getInteger(String key, Integer defaultValue) {
		if(props.containsKey(key)) {
			return Integer.parseInt(props.getProperty(key));	
		} 
		
		return defaultValue;
	}
}
