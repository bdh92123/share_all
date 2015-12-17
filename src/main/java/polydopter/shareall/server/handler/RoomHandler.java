package polydopter.shareall.server.handler;

import polydopter.shareall.common.action.ChatAction;
import polydopter.shareall.common.action.draw.DrawingAction;
import polydopter.shareall.common.action.image.ImageAction;
import polydopter.shareall.common.action.media.MusicAction;
import polydopter.shareall.common.data.FileKey;
import polydopter.shareall.common.data.ParamMap;
import polydopter.shareall.common.data.User;
import polydopter.shareall.common.packet.ErrorCode;
import polydopter.shareall.common.packet.PacketCode;
import polydopter.shareall.common.packet.RequestPacket;
import polydopter.shareall.common.packet.ResponsePacket;
import polydopter.shareall.server.net.ClientSession;
import polydopter.shareall.server.service.RoomService;

public class RoomHandler extends ShareAllHandler {

    @Override
    public boolean handle(ClientSession session, RequestPacket packet) {
        if(session.getRoom() == null) {
        	return true;
        }
        
        RoomService roomService = RoomService.get(session.getRoom().getId());
        if(roomService.getRoom().getId() != session.getRoom().getId()) {
            session.writePacketAsync(ResponsePacket.fromCode(PacketCode.ERROR).setData(ErrorCode.INVALID_ROOM));
            return true;
        }
        switch (packet.getCode()) {
            case CHAT_ACTION: {
                ChatAction data = (ChatAction) packet.getData();
                roomService.broadcast(ResponsePacket.fromCode(PacketCode.CHAT_ACTION).setData(ParamMap.create("user", session.getUser(), "chat", data)));
                break;
            }
            case DRAWING_ACTION: {
                DrawingAction action = (DrawingAction) packet.getData();
                roomService.onDrawingAction(session, action);
                break;
            }
            case MEDIA_ACTION: {
                MusicAction action = (MusicAction) packet.getData();
                roomService.onMediaAction(session, action);
                break;
            }
            case IMAGE_ACTION: {
                ImageAction action = (ImageAction) packet.getData();
                roomService.onImageAction(session, action);
                break;
            }
            case EXIT_ROOM: {
                roomService.exit(session);
                break;
            }
            case FILE_REMOVED: {
            	FileKey fileKey = (FileKey) packet.getData();
            	roomService.onFileRemoved(session, fileKey);

            	break;
            }
            case HEAR_ACTION: {
            	Boolean hearFrom = (Boolean) packet.getData();
            	roomService.handleHearFrom(session, hearFrom);
            	break;
            }
            case CHANGE_MASTER: {
            	User masterUser = (User) packet.getData();
            	roomService.changeMaster(session, masterUser);
            }
        }
        

        return true;
    }

	@Override
	public boolean canHandle(ClientSession session, RequestPacket packet) {
		return (packet.getCode().intValue() & 0xff0000) == 0x30000;
	}
}
