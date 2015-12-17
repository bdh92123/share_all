package polydopter.shareall.server.handler;

import polydopter.shareall.common.Logger;
import polydopter.shareall.common.data.User;
import polydopter.shareall.common.packet.ErrorCode;
import polydopter.shareall.common.packet.PacketCode;
import polydopter.shareall.common.packet.RequestPacket;
import polydopter.shareall.common.packet.ResponsePacket;
import polydopter.shareall.server.net.ClientManager;
import polydopter.shareall.server.net.ClientSession;

public class AuthHandler extends ShareAllHandler {

	
	@Override
	public boolean handle(ClientSession session, RequestPacket packet) {
		if(session.isAuth()) {
			return true;
		}
		
		switch(packet.getCode()) {
			case AUTH: {
				String nickname = (String) packet.getData();
				Logger.info(session.hashCode() + ", " + nickname +" login");
				User user = ClientManager.getUserFromNickname(nickname);
				if(user != null) {
					session.writePacketAsync(ResponsePacket.fromCode(PacketCode.ERROR).setData(ErrorCode.NICKNAME_DUPLICATED));
				} else {
					session.setAuth(true);
					session.getUser().setNickname(nickname);
					session.writePacketAsync(ResponsePacket.fromCode(PacketCode.AUTH).setData(session.getUser()));
					
				}
				
				break;
			}
		}
		
		return true;
	}

	@Override
	public boolean canHandle(ClientSession session, RequestPacket packet) {
		return (packet.getCode().intValue() & 0xff0000) == 0x10000;
	}
}
