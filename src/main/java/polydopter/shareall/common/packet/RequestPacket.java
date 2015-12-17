package polydopter.shareall.common.packet;

import java.io.Serializable;

public class RequestPacket implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private PacketCode code;
	private Object data;
	
	private RequestPacket() {
		
	}

	public static RequestPacket fromCode(PacketCode code) {
		RequestPacket packet = new RequestPacket();
		packet.setCode(code);
		return packet;
	}
	
	public static RequestPacket fromCode(PacketCode code, Object data) {
		RequestPacket packet = new RequestPacket();
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

	public void setData(Object data) {
		this.data = data;
	}

	
}
