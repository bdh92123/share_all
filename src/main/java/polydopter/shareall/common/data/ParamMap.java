package polydopter.shareall.common.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ParamMap implements Serializable {
	private static final long serialVersionUID = 1L;
	Map<Object, Object> map = new HashMap<>();
	
	public static ParamMap create(Object... paramValuePair) {
		ParamMap map = new ParamMap();
		for(int i=0; i<paramValuePair.length; i+=2) {
			map.put(paramValuePair[i], paramValuePair[i+1]);
		}
		return map;
	}
	
	public ParamMap put(Object key, Object value) {
		map.put(key, value);
		return this;
	}

	public Object get(String key) {
		return map.get(key);
	}
	
	public String getString(String key) {
		return (String) map.get(key);
	}
	
	public Integer getInt(String key) {
		return (Integer) map.get(key);
	}
	
	public Double getDouble(String key) {
		return (Double) map.get(key);
	}
}
