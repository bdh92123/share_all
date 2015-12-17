package polydopter.shareall.common.packet;

import java.io.Serializable;

public class ResponsePacket implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private PacketCode code;
	private Object data;
	
	private ResponsePacket(){
	}

	public static ResponsePacket fromCode(PacketCode code) {
		ResponsePacket packet = new ResponsePacket();
		packet.setCode(code);
		return packet;
	}
	
	public static ResponsePacket fromCode(PacketCode code, Object data) {
		ResponsePacket packet = new ResponsePacket();
		packet.setCode(code);
		packet.setData(data);
		return packet;
	}
	
	public PacketCode getCode() {
		return code;
	}

	public void setCode(PacketCode code) {
		this.code = code;
	}

	public Object getData() {
		return data;
	}

	public ResponsePacket setData(Object data) {
		this.data = data;
		return this;
	}
	
}
