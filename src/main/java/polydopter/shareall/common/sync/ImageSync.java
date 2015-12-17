package polydopter.shareall.common.sync;

import java.io.Serializable;

import polydopter.shareall.common.action.image.ImageAction;
import polydopter.shareall.common.data.FileKey;

public class ImageSync implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FileKey fileKey;

    public FileKey getFileKey() {
        return fileKey;
    }

    public void setFileKey(FileKey fileKey) {
        this.fileKey = fileKey;
    }

	public void applyImageAction(ImageAction imageAction) {
		fileKey = imageAction.getImageKey();
	}
    
}
