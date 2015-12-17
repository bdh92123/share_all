package polydopter.shareall.client.ui.dialog;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import polydopter.shareall.client.util.FXUtil;

public class Dialogs {

	// public static void showAlert(String message) {
	// showDialog(AlertType.WARNING, "알림", "알림", message);
	// }
	//
	// public static String showInput(String message, String initialText) {
	// TextInputDialog dialog = new TextInputDialog(initialText);
	// dialog.setTitle("입력");
	// dialog.setHeaderText(message);
	// dialog.setContentText("");
	//
	// Optional<String> result = dialog.showAndWait();
	// if (result.isPresent()){
	// return result.get();
	// }
	//
	// return null;
	// }
	//
	// public static boolean showConfirm(String message) {
	// return showDialog(AlertType.CONFIRMATION, "확인", "확인", message) ==
	// ButtonType.OK;
	// }
	//
	// public static ButtonType showDialog(AlertType alertType, String title,
	// String headerText, String contentText) {
	// Alert alert = new Alert(alertType);
	// alert.setTitle(title);
	// alert.setHeaderText(headerText);
	// alert.setContentText(contentText);
	//
	// Optional<ButtonType> result = alert.showAndWait();
	// return result.get();
	// }

	private static final int ALERT = 1, CONFIRM = 2, INPUT = 3;

	public static class Controller {
		Stage stage;
		int type;
		String messageString;
		StringBuffer result;

		@FXML
		Button okButton, cancelButton;
		@FXML
		ImageView closeButton;
		@FXML
		TextField inputField;
		@FXML
		Label message;

		public Controller(Stage stage, final int type, String messageString, StringBuffer result) {
			this.stage = stage;
			this.type = type;
			this.messageString = messageString;
			this.result = result;
		}

		public void initialize() {

			if (type != CONFIRM) {
				((Pane) cancelButton.getParent()).getChildren().remove(cancelButton);
			}

			if (type != INPUT) {
				((Pane) inputField.getParent()).getChildren().remove(inputField);
			}

			this.message.setText(messageString);
		}

		public void onKeyPressed(Event event) {
			if (((KeyEvent) event).getCode().equals(KeyCode.ENTER)) {
				okButton.fireEvent(new ActionEvent(okButton, ActionEvent.NULL_SOURCE_TARGET));
			}
		}

		public void onClick(Event event) {
			if (event.getSource() == okButton) {
				if (type == INPUT) {
					result.append(inputField.getText());
				} else {
					result.append("OK");
				}
				stage.close();
			} else if (event.getSource() == cancelButton) {
				if (type != INPUT) {
					result.append("CANCEL");
				}

				stage.close();
			} else if (event.getSource() == closeButton) {
				if (type != INPUT) {
					result.append("CANCEL");
				}

				stage.close();
			}
		}
	}

	public static void showAlert(String message) {
		Stage stage = new Stage(StageStyle.UNDECORATED);
		
		StringBuffer result = new StringBuffer();
		Pane pane = (Pane) FXUtil.loadFxml("dialog.fxml", new Controller(stage, ALERT, message, result));
		FXUtil.setMoverForWindow(pane, stage);
		stage.setScene(new Scene(pane));
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.showAndWait();
	}

	public static boolean showConfirm(String message) {
		Stage stage = new Stage(StageStyle.UNDECORATED);
		StringBuffer result = new StringBuffer();
		Pane pane = (Pane) FXUtil.loadFxml("dialog.fxml", new Controller(stage, CONFIRM, message, result));
		FXUtil.setMoverForWindow(pane, stage);
		stage.setScene(new Scene(pane));
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.showAndWait();
		return result.toString().equals("OK");
	}

	public static String showInput(String message) {
		Stage stage = new Stage(StageStyle.UNDECORATED);
		StringBuffer result = new StringBuffer();
		Pane pane = (Pane) FXUtil.loadFxml("dialog.fxml", new Controller(stage, INPUT, message, result));
		FXUtil.setMoverForWindow(pane, stage);
		stage.setScene(new Scene(pane));
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.showAndWait();
		return result.toString();
	}
}
