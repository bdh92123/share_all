package polydopter.shareall.common.action.draw;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PathAction extends DrawingAction {
	
	private int blur = 0;
	private List<PathElement> elements = new ArrayList<>();
	
	public void addElement(PathElement elem) {
		elements.add(elem);
	}
	
	public List<PathElement> getElements() {
		return elements;
	}
	
	public int getBlur() {
		return blur;
	}

	public void setBlur(int blur) {
		this.blur = blur;
	}

	public static class PathElement implements Serializable {
		
	}
	
	public static class MoveTo extends PathElement {
		public MoveTo(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public double x, y;
	}
	
	public static class LineTo extends PathElement  {
		public double x, y;
		
		public LineTo(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
	
}
