package polydopter.shareall.server.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import polydopter.shareall.common.action.draw.DrawingAction;
import polydopter.shareall.common.action.image.ImageAction;
import polydopter.shareall.common.action.media.MusicAction;
import polydopter.shareall.common.data.FileKey;
import polydopter.shareall.common.data.FileType;
import polydopter.shareall.common.data.ParamMap;
import polydopter.shareall.common.data.RoomMeta;
import polydopter.shareall.common.data.User;
import polydopter.shareall.common.packet.ErrorCode;
import polydopter.shareall.common.packet.PacketCode;
import polydopter.shareall.common.packet.ResponsePacket;
import polydopter.shareall.common.sync.ImageSync;
import polydopter.shareall.common.sync.MusicSync;
import polydopter.shareall.server.broadcaster.ShareAllBroadcaster;
import polydopter.shareall.server.data.Room;
import polydopter.shareall.server.net.ClientManager;
import polydopter.shareall.server.net.ClientSession;
import polydopter.shareall.server.net.SessionListener;
import polydopter.shareall.server.storage.ServerStorageManager;

public class RoomService extends ShareAllService implements SessionListener, ShareAllBroadcaster {
	private Room room;
	private RoomMeta meta = new RoomMeta();
	private List<ClientSession> userSessions;
    private ClientSession master;
    private ReentrantLock lock = new ReentrantLock(true);
    
    private static Set<Integer> roomIdSet = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
	private static Map<Room, RoomService> instanceMap = new ConcurrentHashMap<>();
	
    private RoomService(Room key, ClientSession master) {
        this.room = key;
        this.setMaster(master);
        userSessions = new ArrayList<ClientSession>(key.getMaxPerson());
        ClientManager.addSessionListener(this);
    }

	
	public static RoomService get(Integer id) {
		Room temp = new Room();
		temp.setId(id);
		return instanceMap.get(temp);
	}
	
    public static RoomService makeRoom(String title, int maxPerson, String password, ClientSession master) {
    	int id=1;
    	for(;;id++) {
    		if(!roomIdSet.contains(id)) {
    			break;
    		}
    	}
    	roomIdSet.add(id);
    	
		Room room = new Room();
		room.setId(id);
		room.setTitle(title);
		room.setMaxPerson(maxPerson);
		room.setPassword(password);
		if(password != null && !password.isEmpty()) {
			room.setSecret(true);
		}
		room.setCreated(new Date());
		
		if(!instanceMap.containsKey(room)) {
            instanceMap.put(room, new RoomService(room, master));

		}
		return instanceMap.get(room);
	}
	
	public Room getRoom() {
		return room;
	}
	
	public static Set<Room> getRoomList() {
		return new HashSet<>(instanceMap.keySet());
	}
	
	public List<User> getUsers() {
		synchronized (userSessions) {
			return userSessions.stream().map((session) -> {
				return session.getUser();
			}).collect(Collectors.toList());	
		}
	}

	public RoomMeta getMeta() {
		return meta;
	}

	public void enter(ClientSession session) {
		synchronized (userSessions) {
			if(!userSessions.contains(session)) {
				if(userSessions.size() >= room.getMaxPerson()) {
					throw new IndexOutOfBoundsException();
				}
				userSessions.add(session);
				session.setRoom(room);	
				getMeta().getHearFrom().put(session.getUser(), true);
				broadcastExcept(ResponsePacket.fromCode(PacketCode.USER_ENTER).setData(session.getUser()), session);
				session.writePacketAsync(ResponsePacket.fromCode(PacketCode.ENTER_ROOM).setData(
						ParamMap.create("room", getRoom(), "users", getUsers(),
								"meta", getMeta(), "master", getMaster().getUser())));
				handleHearFrom(session, true);
			}
		}
	}
	
    public void exit(ClientSession session) {
    	synchronized (userSessions) {
    		if(userSessions.contains(session)) {
                userSessions.remove(session);
                session.setRoom(null);    
                broadcast(ResponsePacket.fromCode(PacketCode.EXIT_ROOM).setData(session.getUser()));
                
                // if master exit, set next user as master.
                if(session == getMaster()) {
                    if(userSessions.size() == 0) {
                        destroyRoom();
                        return;
                    }
                    
                    setMaster(userSessions.get(0));
                    broadcast(ResponsePacket.fromCode(PacketCode.CHANGE_MASTER).setData(getMaster().getUser()));
                }
            }	
    	}
    }


	public List<ClientSession> getSessions() {
		return userSessions;
	}
	
	public void broadcast(ResponsePacket packet) {
		synchronized (userSessions) {
			for (ClientSession clientSession : userSessions) {
				clientSession.writePacketAsync(packet);
			}
		}
	}

	public void broadcastExcept(ResponsePacket packet, ClientSession except) {
		synchronized (userSessions) {
			for (ClientSession clientSession : userSessions) {
				if(clientSession == except) {
					continue;
				}
				clientSession.writePacketAsync(packet);
			}
		}
	}

    private void destroyRoom() {
    	instanceMap.remove(room);
    	ServerStorageManager.clearRoom(room, meta);
    }
    
	@Override
	public void sessionCreated(ClientSession session) {
	}

    public void sessionClosed(ClientSession session) {
        exit(session);
    }

    public void onDrawingAction(ClientSession session, DrawingAction action) {
    	try {
    		lock.lock();
    		meta.getDrawingSync().applyAction(session.getUser(), action);
            broadcastExcept(ResponsePacket.fromCode(PacketCode.DRAWING_ACTION)
                    .setData(ParamMap.create("user", session.getUser(), "action", action)), session);	
		} finally {
			lock.unlock();
		}
    }

    public void onMediaAction(ClientSession session, MusicAction action) {
    	if(session != getMaster()) {
    		return;
    	}
    	
    	MusicSync mediaSync = meta.getMusicSync();
    	mediaSync.setFileKey(action.getFileKey());
    	mediaSync.applyMusicAction(action);

        broadcastExcept(
                ResponsePacket.fromCode(PacketCode.MEDIA_ACTION).setData(ParamMap.create("user", session.getUser(), "action", action)), session);
    }

    public void onImageAction(ClientSession session, ImageAction action) {
    	if(session != getMaster()) {
    		return;
    	}
    	
    	ImageSync imageSync = meta.getImageSync();
    	if(action instanceof ImageAction) {
    		FileKey fileKey = action.getImageKey();
    		imageSync.setFileKey(fileKey);
    	}
    	
    	broadcastExcept(
                ResponsePacket.fromCode(PacketCode.IMAGE_ACTION).setData(ParamMap.create("user", session.getUser(), "action", action)), session);
    }


	public void handleHearFrom(ClientSession session, Boolean hearFrom) {
		getMeta().getHearFrom().put(session.getUser(), hearFrom);
		broadcast(ResponsePacket.fromCode(PacketCode.HEAR_ACTION).setData(ParamMap.create("user", session.getUser(), "hearFrom", hearFrom)));		
	}
	
	public void changeMaster(ClientSession session, User masterUser) {
		if(session != getMaster() || masterUser.equals(session.getUser())) {
    		return;
    	}
    	
		for (ClientSession tempSession : userSessions) {
			if(tempSession.getUser().equals(masterUser)) {
				setMaster(tempSession);
				break;
			}
		}
		
		getMeta().getMusicSync().setFileKey(null);
		getMeta().getImageSync().setFileKey(null);
		broadcast(ResponsePacket.fromCode(PacketCode.CHANGE_MASTER).setData(masterUser));
	}


	public void onFileRemoved(ClientSession session, FileKey fileKey) {
		if(!getMeta().getFileList().contains(fileKey) && !getMeta().getMusicList().contains(fileKey) && !getMeta().getImageList().contains(fileKey)) {
			session.writePacket(ResponsePacket.fromCode(PacketCode.ERROR, ErrorCode.REMOVE_DENIED));
    		return;
		}
		for(FileKey key : getMeta().getFileList()) {
			if(key.equals(fileKey)) {
				fileKey = key;
			}
		}
		for(FileKey key : getMeta().getImageList()) {
			if(key.equals(fileKey)) {
				fileKey = key;
			}
		}
		for(FileKey key : getMeta().getMusicList()) {
			if(key.equals(fileKey)) {
				fileKey = key;
			}
		}
		if((session != getMaster() && !session.getUser().equals(fileKey.getUser()))) {
			session.writePacket(ResponsePacket.fromCode(PacketCode.ERROR, ErrorCode.REMOVE_DENIED));
    		return;
    	}
    	
    	File file = ServerStorageManager.getLocalFile(getRoom(), fileKey);
    	if(file.exists()) {
    		file.delete();
		}
    	if(fileKey.getType() == FileType.FILE) {
    		meta.removeFile(fileKey);	
    	} else if(fileKey.getType() == FileType.IMAGE) {
    		meta.removeImage(fileKey);
    	} else if(fileKey.getType() == FileType.MUSIC) {
    		meta.removeMusic(fileKey);
    	}
    	
    	broadcast(ResponsePacket.fromCode(PacketCode.FILE_REMOVED).setData(fileKey));		
	}


	public ClientSession getMaster() {
		return master;
	}


	public void setMaster(ClientSession master) {
		this.master = master;
	}
	
}

