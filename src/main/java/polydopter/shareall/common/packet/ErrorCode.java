package polydopter.shareall.common.packet;

public enum ErrorCode {
    INVALID_ROOM(0x001),
    NICKNAME_DUPLICATED(0x002), INVALID_PASSWORD(0x003), EXCEED_PERSON(0x004), INVALID_AUTH(0x005), REMOVE_DENIED(0x006);

    private int code;
    
    private ErrorCode(int code) {
        this.code = code;
    }
    
    public int intValue() {
        return code;
    }
}
