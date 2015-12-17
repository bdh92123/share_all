package polydopter.shareall.common.action.media;

import java.io.Serializable;

import javafx.util.Duration;

public class SeekAction extends MusicAction implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public SeekAction(String name, Duration position) {
		super(name, position);
	}
}
