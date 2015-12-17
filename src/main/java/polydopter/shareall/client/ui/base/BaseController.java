package polydopter.shareall.client.ui.base;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.Optional;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import polydopter.shareall.client.net.PacketProcessor;
import polydopter.shareall.client.util.FXUtil;
import polydopter.shareall.server.processor.PacketWriterProcessor;
import polydopter.shareall.server.processor.Processors;

public abstract class BaseController {
	protected static PacketWriterProcessor packetWriter = Processors.packetWriter;
	protected Stage ownerStage, stage;
	private boolean sizeToScene;
	private Parent root;
	private String fxml;
	protected Scene scene;
	private static int screenWidth;
	private static int screenHeight;
	private boolean useOwnerStage;
	private BaseController parentController;
	private boolean softClose;

	private EventHandler<WindowEvent> closeRequestHandler = new EventHandler<WindowEvent>() {
		@Override
		public void handle(WindowEvent event) {
			removeHandler();
			BaseController.this.onCloseRequest(event, softClose);
		}
	};
	

	static {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		screenWidth = gd.getDisplayMode().getWidth();
		screenHeight = gd.getDisplayMode().getHeight();

		PacketProcessor.addHandler(GeneralPacketHandler.getInstance());
	}

	public BaseController(BaseController parent, String fxml) {
		this(parent, fxml, true, true);
	}

	public BaseController(BaseController parent, String fxml, boolean useOwnerStage) {
		this(parent, fxml, useOwnerStage, true);
	}

	public BaseController(BaseController parent, String fxml, boolean useOwnerStage, boolean sizeToScene) {
		this.fxml = fxml;
		this.parentController = parent;
		if (parent != null) {
			this.ownerStage = parent.getStage();
		}
		this.useOwnerStage = useOwnerStage;
		this.sizeToScene = sizeToScene;
	}

	private Parent getTitlePane() {
		try {
			Pane titlePane = (Pane) FXUtil.loadFxml("titleFrame.fxml");
			titlePane.prefWidthProperty().bind(stage.widthProperty());
			Button minButton = (Button) titlePane.lookup("#minButton");
			Button quitButton = (Button) titlePane.lookup("#quitButton");

			minButton.onActionProperty().set(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					stage.setIconified(true);
				}

			});

			quitButton.onActionProperty().set(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("확인");
					alert.setHeaderText("");
					alert.setContentText("정말로 종료하시겠습니까 ?");

					Optional<ButtonType> result = alert.showAndWait();
					if (result.get() == ButtonType.OK) {
						stage.close();
						System.exit(0);
					}
				}
			});
			return titlePane;
		} catch (Exception e) {
			return null;
		}
	}

	protected abstract void initController(boolean initialize);

	public Parent getRoot() {
		if (root == null) {
			makeRoot();
			assert root != null;
		}

		return root;
	}

	public Scene getScene() {
		assert Platform.isFxApplicationThread();

		if (scene == null) {
			scene = new Scene(getRoot());
			scene.setFill(Color.TRANSPARENT);
		}

		return scene;
	}

	private Stage prepareStage() {
		assert Platform.isFxApplicationThread();

		if (stage == null) {
			if (!useOwnerStage || ownerStage == null) {
				stage = new Stage();
			} else {
				stage = ownerStage;
			}
			if (stage != ownerStage) {
				// stage.initOwner(ownerStage);
			}

			stage.setScene(getScene());
			initController(true);
			stage.setOnCloseRequest(closeRequestHandler);
		} else {
			stage.setScene(getScene());
			initController(false);
			stage.setOnCloseRequest(closeRequestHandler);
		}

		if (sizeToScene) {
			stage.sizeToScene();
		}

		stage.getIcons().clear();
		stage.getIcons().add(new Image(BaseController.class.getResourceAsStream("/shareall.png")));

		Platform.runLater(() -> {
			stage.setMinWidth(stage.getWidth());
			stage.setMinHeight(stage.getHeight());
		});
		return stage;
	}

	public Stage getStage() {
		return stage;
	}

	public void showWindow() {
		showWindow(false);
	}

	public void showWindow(boolean wait) {
		assert Platform.isFxApplicationThread();
		if (useOwnerStage && FXUtil.getController(stage) != null) {
			FXUtil.getController(stage).removeHandler();
		}
		addHandler();
		prepareStage();
		getStage().toFront();
		if (wait) {
			if (!getStage().getModality().equals(Modality.APPLICATION_MODAL)) {
				getStage().initModality(Modality.APPLICATION_MODAL);
			}
			getStage().showAndWait();
		} else {
			getStage().show();
		}
	}

	public void closeWindow() {
        assert Platform.isFxApplicationThread();
    	getStage().close();
    	softClose = true;
    	stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    	softClose = false;
    }

	public void prevWindow() {
		if (parentController != null) {
			parentController.showWindow();
		}
	}

	public void onCloseRequest(WindowEvent event, boolean softClose) {
	}

	protected final void setRoot(Parent root) {
		assert root != null;
		this.root = root;
	}

	private void addHandler() {
		if (this instanceof PacketHandler && PacketProcessor.isStarted()) {
			PacketProcessor.addHandler((PacketHandler) this);
		}
	}

	private void removeHandler() {
		if (this instanceof PacketHandler && PacketProcessor.isStarted()) {
			PacketProcessor.removeHandler((PacketHandler) this);
		}
	}

	protected Node makeRoot() {
		try {
			if (fxml != null) {
				// Parent node = (Parent) FXUtil.loadFxml(fxml, this);
				// Pane pane = new AnchorPane();
				// pane.setStyle("-fx-background-color: transparent");
				// pane.getChildren().add(node);
				// pane.minWidthProperty().bind(((Region)
				// node).minWidthProperty());
				// pane.minHeightProperty().bind(((Region)
				// node).minHeightProperty());
				// pane.maxWidthProperty().bind(((Region)
				// node).maxWidthProperty());
				// pane.maxHeightProperty().bind(((Region)
				// node).maxHeightProperty());
				// AnchorPane.setBottomAnchor(node, 0d);
				// AnchorPane.setTopAnchor(node, 0d);
				// AnchorPane.setLeftAnchor(node, 0d);
				// AnchorPane.setRightAnchor(node, 0d);
				// pane.setUserData(this);
				// pane.getStylesheets().add("/css/base.css");
				// this.root = pane;
				// return pane;

				Parent node = (Parent) FXUtil.loadFxml(fxml, this);
				this.root = node;
				this.root.setUserData(this);
				return node;
			} else {
				this.root = new Pane();
				return this.root;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static int getScreenWidth() {
		return screenWidth;
	}

	public static int getScreenHeight() {
		return screenHeight;
	}

	public BaseController getParentController() {
		return parentController;
	}
	
	public boolean isShowing() {
		return stage != null && stage.isShowing();
	}
}
