package polydopter.shareall.server.handler;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

import polydopter.shareall.common.data.ParamMap;
import polydopter.shareall.common.packet.ErrorCode;
import polydopter.shareall.common.packet.PacketCode;
import polydopter.shareall.common.packet.RequestPacket;
import polydopter.shareall.common.packet.ResponsePacket;
import polydopter.shareall.server.data.Room;
import polydopter.shareall.server.net.ClientSession;
import polydopter.shareall.server.service.RoomService;

public class LobbyHandler extends ShareAllHandler {

	
	@Override
	public boolean handle(ClientSession session, RequestPacket packet) {
		switch(packet.getCode()) {
			case ROOM_LIST: {
				session.writePacketAsync(ResponsePacket.fromCode(PacketCode.ROOM_LIST, (Set<Room>)RoomService.getRoomList()));
				break;
			}
			case MAKE_ROOM: {
				ParamMap param = (ParamMap) packet.getData();
				RoomService service = RoomService.makeRoom(param.getString("title"), param.getInt("maxPerson"), param.getString("password"), session);
				session.writePacketAsync(ResponsePacket.fromCode(PacketCode.MAKE_ROOM, param.put("roomId", service.getRoom().getId())));
				break; 
			}
			case ENTER_ROOM: {
				ParamMap param = (ParamMap) packet.getData();
				RoomService roomService = RoomService.get(param.getInt("roomId"));
				if(roomService != null) {
					if(!roomService.getRoom().getPassword().equals(param.getString("password"))) {
						session.writePacketAsync(ResponsePacket.fromCode(PacketCode.ERROR, ErrorCode.INVALID_PASSWORD));
						break;
					}
					
					if(session.getRoom() == null || (roomService.getRoom().getId() != session.getRoom().getId())) {
						try {
							roomService.enter(session);	
						} catch (IndexOutOfBoundsException e) {
							session.writePacketAsync(ResponsePacket.fromCode(PacketCode.ERROR, ErrorCode.EXCEED_PERSON));
							break;
						}
					}
				} else {
					session.writePacketAsync(ResponsePacket.fromCode(PacketCode.ERROR, ErrorCode.INVALID_ROOM));
				}
			}
		}
		
		return true;
	}

	@Override
	public boolean canHandle(ClientSession session, RequestPacket packet) {
		return (packet.getCode().intValue() & 0xff0000) == 0x20000;
	}
}
