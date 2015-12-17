package polydopter.shareall.client.ui.base;

import polydopter.shareall.common.packet.ErrorCode;
import polydopter.shareall.common.packet.ResponsePacket;

public class GeneralPacketHandler implements PacketHandler {

	private static GeneralPacketHandler instance = null;
	
	private GeneralPacketHandler() {
		
	}
	
	public static GeneralPacketHandler getInstance()
	{
		if(instance == null)
		{
			instance = new GeneralPacketHandler();
		}
		
		return instance;
	}

	public void handlePacket(ResponsePacket packet) {
		switch (packet.getCode()) {
		case ERROR:
			handleError((ErrorCode) packet.getData());
			break;
		}
	}

	public void handleError(ErrorCode data) {
		switch(data) {
		case INVALID_AUTH:
			System.out.println("Invalid Auth!");
			break;
		}
	}

}
