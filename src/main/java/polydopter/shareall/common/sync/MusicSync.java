package polydopter.shareall.common.sync;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import polydopter.shareall.common.Logger;
import polydopter.shareall.common.action.media.MusicAction;
import polydopter.shareall.common.action.media.PauseAction;
import polydopter.shareall.common.action.media.PlayAction;
import polydopter.shareall.common.action.media.SeekAction;
import polydopter.shareall.common.action.media.StopAction;
import polydopter.shareall.common.data.FileKey;

public class MusicSync  implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FileKey fileKey;
    private AtomicLong lastPosition = new AtomicLong();
    private AtomicBoolean playing = new AtomicBoolean();
    private AtomicLong lastModified = new AtomicLong();
    
    public FileKey getFileKey() {
        return fileKey;
    }
    public void setFileKey(FileKey fileKey) {
        this.fileKey = fileKey;
    }
	
	public boolean isPlaying() {
		return playing.get();
	}
	public void setPlaying(boolean playing) {
		this.playing.set(playing);
	}
	public void applyMusicAction(MusicAction musicAction) {
		if(musicAction instanceof PlayAction) {
        	setPlaying(true);
        	if(musicAction.getPosition() == null) {
        		lastPosition.set(0l);;	
        	} else {
        		lastPosition.set((long) musicAction.getPosition().toMillis());	
        	}
        	fileKey = musicAction.getFileKey();
        	
        	getLastModified().set(System.currentTimeMillis());
        } else if(musicAction instanceof PauseAction && isPlaying()) {
        	setPlaying(false);
        	lastPosition.set((long) musicAction.getPosition().toMillis());
        	fileKey = musicAction.getFileKey();
        	getLastModified().set(System.currentTimeMillis());
        } else if(musicAction instanceof SeekAction) {
        	lastPosition.set((long) musicAction.getPosition().toMillis());
        	fileKey = musicAction.getFileKey();
        	getLastModified().set(System.currentTimeMillis());
        } else if(musicAction instanceof StopAction) {
        	setPlaying(false);
        	getLastPosition().set(0);
        	fileKey = musicAction.getFileKey();
        	getLastModified().set(System.currentTimeMillis());
        }
		
		Logger.info(musicAction.getClass().getSimpleName());
	}
	public AtomicLong getLastPosition() {
		return lastPosition;
	}
	public void setLastPosition(AtomicLong position) {
		this.lastPosition = position;
	}
	public AtomicLong getLastModified() {
		return lastModified;
	}
	public void setLastModified(AtomicLong lastModified) {
		this.lastModified = lastModified;
	}
}
