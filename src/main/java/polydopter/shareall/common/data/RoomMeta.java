package polydopter.shareall.common.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import polydopter.shareall.common.action.draw.DrawingAction;
import polydopter.shareall.common.sync.DrawingSync;
import polydopter.shareall.common.sync.ImageSync;
import polydopter.shareall.common.sync.MusicSync;

public class RoomMeta implements Serializable {

    private static final long serialVersionUID = 1L;

    private int roomId;
    private Set<FileKey> imageList = new HashSet<FileKey>();
    private Set<FileKey> musicList = new HashSet<FileKey>();
    private Set<FileKey> fileList = new HashSet<FileKey>();
    private DrawingSync drawingSync = new DrawingSync();
    private ImageSync imageSync = new ImageSync();
    private MusicSync musicSync = new MusicSync();
    private Map<User, Boolean> hearFrom = new HashMap<>();
    
    public Set<FileKey> getImageList() {
        return imageList;
    }
    public Set<FileKey> getMusicList() {
        return musicList;
    }
    public Set<FileKey> getFileList() {
        return fileList;
    }
    public void addFile(FileKey fileKey) {
        fileList.add(fileKey);
    }
    public void addMusic(FileKey musicKey) {
        musicList.add(musicKey);
    }
    public void addImage(FileKey imageKey) {
        imageList.add(imageKey);
    }
	public DrawingSync getDrawingSync() {
		return drawingSync;
	}
	public void setDrawingSync(DrawingSync drawingSync) {
		this.drawingSync = drawingSync;
	}
	public int getRoomId() {
		return roomId;
	}
	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}
	
	public Map<User, Boolean> getHearFrom() {
		return hearFrom;
	}
	public void setHearFrom(Map<User, Boolean> hearFrom) {
		this.hearFrom = hearFrom;
	}
	public ImageSync getImageSync() {
		return imageSync;
	}
	public void setImageSync(ImageSync imageSync) {
		this.imageSync = imageSync;
	}
	public MusicSync getMusicSync() {
		return musicSync;
	}
	public void setMusicSync(MusicSync musicSync) {
		this.musicSync = musicSync;
	}
	
	public void removeFile(FileKey fileKey) {
		fileList.remove(fileKey);
	}
	public void removeMusic(FileKey fileKey) {
		musicList.remove(fileKey);
	}
	public void removeImage(FileKey fileKey) {
		imageList.remove(fileKey);
	}
    
}
