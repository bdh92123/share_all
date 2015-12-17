package polydopter.shareall.server.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.UUID;

import polydopter.shareall.common.Logger;
import polydopter.shareall.common.data.User;
import polydopter.shareall.common.packet.PacketCode;
import polydopter.shareall.common.packet.RequestPacket;
import polydopter.shareall.common.packet.ResponsePacket;
import polydopter.shareall.server.data.Room;
import polydopter.shareall.server.pool.FlexibleBuffer;
import polydopter.shareall.server.processor.Processors;

public class ClientSession {
	private boolean isAuth;
	private User user;
	private Room room;
	private SocketChannel socketChannel;
	private boolean readLength = true;
	private ByteBuffer lengthBuffer = ByteBuffer.allocateDirect(4);
	private ByteBuffer dataByteBuffer;
	private Object readLock = new Object();
	private Object writeLock = new Object();
	private RequestPacket packet;
	private ByteArrayOutputStream baos;
    private ObjectOutputStream oos;
	
	public ClientSession(SocketChannel socketChannel) {
		try {
			this.setAuth(false);
			this.socketChannel = socketChannel;
			this.setUser(new User());
			this.getUser().setSessionKey(UUID.randomUUID().toString());
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public int tryReadPacket() {
		synchronized(readLock) {
			try {
				int read;
				if (readLength) {
					read = socketChannel.read(lengthBuffer);
					if (lengthBuffer.remaining() == 0) {
						readLength = false;
						dataByteBuffer = FlexibleBuffer.get(hashCode(), lengthBuffer.getInt(0));
						lengthBuffer.clear();
					}
				} else {
					read = socketChannel.read(dataByteBuffer);
					if (dataByteBuffer.remaining() == 0) {
						ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(dataByteBuffer.array()));
						final RequestPacket ret = (RequestPacket) ois.readObject();
						this.packet = ret;
						dataByteBuffer = null;
						readLength = true;
						return 1;
					}
				}
				
				if(read == -1) {
					return -1;
				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				return -1;
			}
			
			return 0;
		}
	}
	
	public RequestPacket takePacket() {
		RequestPacket readPacket = packet;
		packet = null;
		return readPacket;
	}

	public void writePacket(ResponsePacket packet) {
		synchronized (writeLock) {
			try {
			    oos.writeObject(packet);
			    oos.reset();
			    final ByteBuffer wrap = ByteBuffer.wrap(baos.toByteArray());
			    socketChannel.write(wrap);
			    baos.reset();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void writePacketAsync(ResponsePacket packet) {
		try {
			Processors.packetWriter.enque(this, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public boolean isAuth() {
		return isAuth;
	}

	public void setAuth(boolean isAuth) {
		this.isAuth = isAuth;
	}
}
