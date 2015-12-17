package polydopter.shareall.server.handler;

import polydopter.shareall.common.packet.RequestPacket;
import polydopter.shareall.server.net.ClientSession;

public abstract class ShareAllHandler {
	
	public abstract boolean handle(ClientSession session, RequestPacket packet);

	public boolean canHandle(ClientSession session, RequestPacket packet) {
		// TODO Auto-generated method stub
		return false;
	}
}
