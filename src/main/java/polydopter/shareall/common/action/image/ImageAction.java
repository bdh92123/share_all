package polydopter.shareall.common.action.image;

import java.io.Serializable;

import polydopter.shareall.common.data.FileKey;

public class ImageAction implements Serializable {
    private static final long serialVersionUID = 1L;
    private FileKey imageKey;
    
    public ImageAction(FileKey imageKey) {
    	this.imageKey = imageKey;
    }
    
    public FileKey getImageKey() {
        return imageKey;
    }
    public void setImageKey(FileKey imageKey) {
        this.imageKey = imageKey;
    }
}
