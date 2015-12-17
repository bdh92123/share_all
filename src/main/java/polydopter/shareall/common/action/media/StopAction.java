package polydopter.shareall.common.action.media;

import java.io.Serializable;

public class StopAction  extends MusicAction implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public StopAction(String name) {
		super(name, null);
	}
}
