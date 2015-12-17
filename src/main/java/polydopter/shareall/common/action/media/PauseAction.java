package polydopter.shareall.common.action.media;

import java.io.Serializable;

import javafx.util.Duration;

public class PauseAction extends MusicAction implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public PauseAction(String name, Duration position) {
		super(name, position);
	}

	
}
