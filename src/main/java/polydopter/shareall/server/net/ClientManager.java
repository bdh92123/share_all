package polydopter.shareall.server.net;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import polydopter.shareall.common.data.User;

public class ClientManager {
	private static Map<SocketChannel, ClientSession> socketClientMap;
	private static Map<String, ClientSession> sessionClientMap;
	private static List<SessionListener> sessionListeners;
	
	static {
		socketClientMap = new ConcurrentHashMap<>();
		sessionClientMap = new ConcurrentHashMap<>();
		sessionListeners = new ArrayList<SessionListener>();
	}
	
	private ClientManager() {
		
	}
	
	public static void addSessionListener(SessionListener listener) {
		sessionListeners.add(listener);
	}
	
	public static void removeSessionListener(SessionListener listener) {
		sessionListeners.remove(listener);
	}
	
	public static ClientSession registClient(SocketChannel socketChannel) {
		ClientSession session = new ClientSession(socketChannel);
		socketClientMap.put(socketChannel, session);
		sessionClientMap.put(session.getUser().getSessionKey(), session);
		
		for (SessionListener listener : sessionListeners) {
			listener.sessionCreated(session);
		}
		
		return session;
	}

	public static void removeClient(SocketChannel socketChannel) {
		ClientSession session = socketClientMap.get(socketChannel);
		if(session != null) {
			socketClientMap.remove(socketChannel);
			sessionClientMap.remove(session.getUser().getSessionKey());
			
			for (SessionListener listener : sessionListeners) {
				listener.sessionClosed(session);
			}
		}
	}
	
    public static User getUserFromNickname(String nickname) {
        for(ClientSession session : sessionClientMap.values()) {
            if(session.getUser().getNickname().equals(nickname)) {
                return session.getUser();
            }
        }
        return null;
    }
    


	public static ClientSession getClient(SocketChannel socketChannel) {
		return socketClientMap.get(socketChannel);
	}
	
	public static ClientSession getClient(String sessionKey) {
		return sessionClientMap.get(sessionKey);
	}
}
