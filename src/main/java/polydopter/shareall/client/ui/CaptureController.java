package polydopter.shareall.client.ui;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import polydopter.shareall.client.storage.ClientStorageManager;
import polydopter.shareall.client.ui.base.BaseController;
import polydopter.shareall.client.util.FXUtil;
import polydopter.shareall.client.util.ResizeListener;
import polydopter.shareall.common.Logger;
import polydopter.shareall.server.data.Room;

public class CaptureController extends BaseController {
	private Room room;
	private File saved;
	@FXML
	Pane captureArea;
	
	@FXML
	Button captureButton, cancelButton;

	public CaptureController(BaseController parent, Room room) {
		super(parent, "capture.fxml", false);
		this.room = room;
	}

	@Override
	protected void initController(boolean initialize) {
		if (initialize) {
			getStage().initModality(Modality.APPLICATION_MODAL);
			getStage().initStyle(StageStyle.TRANSPARENT);
			getStage().setOpacity(1);
			scene.setFill(Color.TRANSPARENT);
			getStage().setScene(scene);
			ResizeListener l = new ResizeListener(getStage());
			l.setBorder(6);
			
			captureButton.addEventHandler(MouseEvent.MOUSE_MOVED, l);
			cancelButton.addEventHandler(MouseEvent.MOUSE_MOVED, l);
			getStage().getScene().addEventHandler(MouseEvent.MOUSE_MOVED, l);
			getStage().getScene().addEventHandler(MouseEvent.MOUSE_PRESSED, l);
			getStage().getScene().addEventHandler(MouseEvent.MOUSE_DRAGGED, l);
			FXUtil.setMoverForWindow(captureArea, getStage());
		}
	}

	public void onClick(Event event) {
		if(event.getSource() == captureButton) {
			saved = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
			String captureName = sdf.format(new Date()) + ".jpg";

			int x = (int) (getStage().getX() + getStage().getScene().getX() + captureArea.getLayoutX());
			int y = (int) (getStage().getY() + getStage().getScene().getY() + captureArea.getLayoutY());
			;
			int width = (int) (captureArea.getWidth());
			int height = (int) (captureArea.getHeight());

			BufferedImage capture;
			try {
				capture = new Robot().createScreenCapture(new Rectangle(x, y, width, height));
				ClientStorageManager.getLocalFile(room, null).mkdirs();
				File file = ClientStorageManager.getLocalFile(room, captureName);
				ImageIO.write(capture, "jpg", file);
				saved = file;
				closeWindow();
//				Desktop.getDesktop().open(ClientStorageManager.getLocalFile(room, captureName));
			} catch (Exception e) {
				Logger.error(e);
			}	
		} else if(event.getSource() == cancelButton){
			saved = null;
			closeWindow();
		}
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public File getSaved() {
		return saved;
	}

}
