package polydopter.shareall.server.broadcaster;

import java.util.List;

import polydopter.shareall.common.packet.ResponsePacket;
import polydopter.shareall.server.net.ClientSession;

public interface ShareAllBroadcaster {
	
	public List<ClientSession> getSessions();
	
	public void enter(ClientSession session);
	
	public void exit(ClientSession session);
	
	public void broadcast(ResponsePacket packet);
}
