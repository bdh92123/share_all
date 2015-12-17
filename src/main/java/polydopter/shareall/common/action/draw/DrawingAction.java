package polydopter.shareall.common.action.draw;

import java.io.Serializable;

public class DrawingAction implements Serializable {
	private static final long serialVersionUID = 1L;
	public int color;
	public int border;
	public transient int index;
}
