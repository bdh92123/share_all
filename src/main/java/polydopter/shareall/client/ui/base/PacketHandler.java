package polydopter.shareall.client.ui.base;

import polydopter.shareall.common.packet.ErrorCode;
import polydopter.shareall.common.packet.ResponsePacket;

public interface PacketHandler {
	public void handlePacket(ResponsePacket packet);
	public void handleError(ErrorCode code);
}
