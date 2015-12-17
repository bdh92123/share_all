package polydopter.shareall.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import polydopter.shareall.common.data.RoomMeta;
import polydopter.shareall.common.data.User;
import polydopter.shareall.server.data.Room;

public class LobbyService  extends ShareAllService {
	private Room key;
	private RoomMeta meta = new RoomMeta();
	private List<User> userList = new ArrayList<User>();
	
	private LobbyService(Room key) {
		this.key = key;
	}
	
	public Room getKey() {
		return key;
	}
	
	public static Set<Room> getRoomList() {
		return RoomService.getRoomList();
	}

	public RoomMeta getMeta() {
		return meta;
	}

	public List<User> getUserList() {
		return userList;
	}

	public void setUserList(List<User> userList) {
		this.userList = userList;
	}
}
