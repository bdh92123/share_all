package polydopter.shareall.client.util;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ResizeListener implements EventHandler<MouseEvent> {
    private Stage stage;
    private Cursor cursorEvent = Cursor.DEFAULT;
    private int border = 4;
    private double startX = 0;
    private double startY = 0;

    public ResizeListener(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        EventType<? extends MouseEvent> mouseEventType = mouseEvent.getEventType();
        Scene scene = stage.getScene();

        double mouseEventX = mouseEvent.getSceneX(), 
               mouseEventY = mouseEvent.getSceneY(),
               sceneWidth = scene.getWidth(),
               sceneHeight = scene.getHeight();

        if (MouseEvent.MOUSE_MOVED.equals(mouseEventType) == true) {
            if (mouseEventX < getBorder() && mouseEventY < getBorder()) {
                cursorEvent = Cursor.NW_RESIZE;
            } else if (mouseEventX < getBorder() && mouseEventY > sceneHeight - getBorder()) {
                cursorEvent = Cursor.SW_RESIZE;
            } else if (mouseEventX > sceneWidth - getBorder() && mouseEventY < getBorder()) {
                cursorEvent = Cursor.NE_RESIZE;
            } else if (mouseEventX > sceneWidth - getBorder() && mouseEventY > sceneHeight - getBorder()) {
                cursorEvent = Cursor.SE_RESIZE;
            } else if (mouseEventX < getBorder()) {
                cursorEvent = Cursor.W_RESIZE;
            } else if (mouseEventX > sceneWidth - getBorder()) {
                cursorEvent = Cursor.E_RESIZE;
            } else if (mouseEventY < getBorder()) {
                cursorEvent = Cursor.N_RESIZE;
            } else if (mouseEventY > sceneHeight - getBorder()) {
                cursorEvent = Cursor.S_RESIZE;
            } else {
                cursorEvent = Cursor.DEFAULT;
            }
            scene.setCursor(cursorEvent);
        } else if (MouseEvent.MOUSE_PRESSED.equals(mouseEventType) == true) {
            startX = stage.getWidth() - mouseEventX;
            startY = stage.getHeight() - mouseEventY;
        } else if (MouseEvent.MOUSE_DRAGGED.equals(mouseEventType) == true) {
            if (Cursor.DEFAULT.equals(cursorEvent) == false) {
                if (Cursor.W_RESIZE.equals(cursorEvent) == false && Cursor.E_RESIZE.equals(cursorEvent) == false) {
                    double minHeight = stage.getMinHeight() > (getBorder()*2) ? stage.getMinHeight() : (getBorder()*2);
                    if (Cursor.NW_RESIZE.equals(cursorEvent) == true || Cursor.N_RESIZE.equals(cursorEvent) == true || Cursor.NE_RESIZE.equals(cursorEvent) == true) {
                        if (stage.getHeight() > minHeight || mouseEventY < 0) {
                            stage.setHeight(stage.getY() - mouseEvent.getScreenY() + stage.getHeight());
                            stage.setY(mouseEvent.getScreenY());
                        }
                    } else {
                        if (mouseEventY + startY > minHeight || mouseEventY + startY - stage.getHeight() > 0) {
                            stage.setHeight(mouseEventY + startY);
                        } else if(mouseEventY + startY <= minHeight) {
                        	stage.setHeight(minHeight);
                        }
                    }
                }

                if (Cursor.N_RESIZE.equals(cursorEvent) == false && Cursor.S_RESIZE.equals(cursorEvent) == false) {
                    double minWidth = stage.getMinWidth() > (getBorder()*2) ? stage.getMinWidth() : (getBorder()*2);
                    if (Cursor.NW_RESIZE.equals(cursorEvent) == true || Cursor.W_RESIZE.equals(cursorEvent) == true || Cursor.SW_RESIZE.equals(cursorEvent) == true) {
                        if (stage.getWidth() > minWidth || mouseEventX < 0) {
                            stage.setWidth(stage.getX() - mouseEvent.getScreenX() + stage.getWidth());
                            stage.setX(mouseEvent.getScreenX());
                        }
                    } else {
                        if (mouseEventX + startX > minWidth || mouseEventX + startX - stage.getWidth() > 0) {
                            stage.setWidth(mouseEventX + startX);
                        } else if(mouseEventX + startX <= minWidth) {
                        	stage.setWidth(minWidth);
                        }
                    }
                }
                
            }

        }
    }

	public int getBorder() {
		return border;
	}

	public void setBorder(int border) {
		this.border = border;
	}
}