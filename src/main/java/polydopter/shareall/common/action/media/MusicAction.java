package polydopter.shareall.common.action.media;

import java.io.Serializable;

import javafx.util.Duration;
import polydopter.shareall.common.data.FileKey;

public class MusicAction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private FileKey fileKey;
    private Duration position;
    
    public MusicAction(String name, Duration position) {
    	this.fileKey = new FileKey(name);
    	this.position = position;
    }
    
    public FileKey getFileKey() {
        return fileKey;
    }
    public void setFileKey(FileKey fileKey) {
        this.fileKey = fileKey;
    }
	public Duration getPosition() {
		return position;
	}
	public void setPosition(Duration position) {
		this.position = position;
	}
    
}
