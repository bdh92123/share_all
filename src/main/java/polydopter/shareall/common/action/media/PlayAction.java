package polydopter.shareall.common.action.media;

import java.io.Serializable;

import javafx.util.Duration;

public class PlayAction extends MusicAction implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public PlayAction(String name, Duration position) {
		super(name, position);
	}


}
