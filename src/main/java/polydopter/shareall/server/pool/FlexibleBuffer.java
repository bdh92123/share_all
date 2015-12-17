package polydopter.shareall.server.pool;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class FlexibleBuffer {
	
	private static ThreadLocal<Map<Integer, ByteBuffer>> local = new ThreadLocal<>();
	
	public static ByteBuffer get(int key, int capacity)  {
		Map<Integer, ByteBuffer> map = local.get();
		if(map == null) {
			local.set(new HashMap<>());
			map = local.get();
		}
		ByteBuffer buffer = map.get(key);
		if(buffer == null || buffer.capacity() < capacity) {
			buffer = ByteBuffer.allocate(capacity);
			map.put(key, buffer);
		} else {
			buffer.limit(capacity);
			buffer.position(0);
		}
		return buffer;
	}
	
	public static ByteBuffer getDirect(int key, int capacity)  {
		Map<Integer, ByteBuffer> map = local.get();
		ByteBuffer buffer = map.get(key);
		if(buffer == null || buffer.capacity() < capacity) {
			buffer = ByteBuffer.allocate(capacity);
			map.put(key, buffer);
		} else {
			buffer.limit(capacity);
			buffer.position(0);
		}
		return buffer;
	}
}
