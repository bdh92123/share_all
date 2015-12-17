package polydopter.shareall.client.util;

import java.util.Collections;
import java.util.Map;

import javafx.scene.image.Image;

public class ImageCache  {

	private static final int CACHE_SIZE = 50;
	private static Map<String, Image> cache = Collections.synchronizedMap(new LruCache<String, Image>(CACHE_SIZE));
	
	public static Image getImage(String path) {
		if(!cache.containsKey(path)) {
			 cache.put(path, new Image(ImageCache.class.getClassLoader().getResourceAsStream(path)));
		}
		return cache.get(path);
	}
	
	public static void clear() {
		cache.clear();
	}
}

