package polydopter.shareall.common.packet;

public enum PacketCode {
	    
    ERROR(0x0000),
    
    // Auth
    AUTH(0x10001), 
    SESSION(0x10002),
    
    // Lobby
    ROOM_LIST(0x20001), 
    ROOM_ENTER(0x20002), 
    MAKE_ROOM(0x20003), 
    ENTER_ROOM(0x20004), 
    
    // Room
    ROOM_META(0x30001),
    EXIT_ROOM(0x30002),
    CHANGE_MASTER(0x30003),
    USER_ENTER(0x30004),
    
    // Room Meta 
    FILE_REMOVED(0x31002),
    FILE_ADDED(0x31004),
    
    // Room Action
    CHAT_ACTION(0x32001),
    DRAWING_ACTION(0x32002), 
    MEDIA_ACTION(0x32003), 
    IMAGE_ACTION(0x32004),
    
    HEAR_ACTION(0x33001),
    ; 

	
	private int code;
	
	private PacketCode(int code) {
		this.code = code;
	}
	
    public int intValue() {
        return code;
    }

}

