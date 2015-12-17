package polydopter.shareall.client.ui;

import java.util.List;
import java.util.Set;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import polydopter.shareall.client.net.PacketProcessor;
import polydopter.shareall.client.ui.base.BaseController;
import polydopter.shareall.client.ui.base.PacketHandler;
import polydopter.shareall.client.ui.dialog.Dialogs;
import polydopter.shareall.client.util.CustomListCell;
import polydopter.shareall.client.util.FXUtil;
import polydopter.shareall.common.data.ParamMap;
import polydopter.shareall.common.data.RoomMeta;
import polydopter.shareall.common.data.User;
import polydopter.shareall.common.packet.ErrorCode;
import polydopter.shareall.common.packet.PacketCode;
import polydopter.shareall.common.packet.RequestPacket;
import polydopter.shareall.common.packet.ResponsePacket;
import polydopter.shareall.server.data.Room;

public class LobbyController extends BaseController implements PacketHandler {
	
	@FXML
	Button enterButton;
	@FXML
	ImageView createRoomButton;
	@FXML
	ImageView refreshButton;
	
	@FXML
	ListView<Room> roomListView;
	
	CreateRoomController createRoomDialog;
	
	RoomController roomController;
	
	public LobbyController(BaseController parent) {
		super(parent, "lobby.fxml", false);
	}

	@Override
	protected void initController(boolean initialize) {
		if(initialize) {
			FXUtil.setCellFactory(roomListView, RoomCell.class, this);
			createRoomDialog = new CreateRoomController();
			roomController = new RoomController(this);	
		}
		PacketProcessor.writePacket(RequestPacket.fromCode(PacketCode.ROOM_LIST));
	}
	
	@Override
	public void onCloseRequest(WindowEvent event, boolean softClose) {
		if(!softClose && Dialogs.showConfirm("종료하시겠습니까?")) {
			PacketProcessor.stop();
			if(roomController.isShowing()) {
				roomController.closeWindow();	
			}
		} else if(!softClose){
			event.consume();
		}
	}
	
	private void enterRoom(Room room) {
		if(room != null) {
			ParamMap param = ParamMap.create("roomId", room.getId(), "password", "");
			if(room.isSecret()) {
				String password = Dialogs.showInput("비밀번호를 입력하세요");
				param.put("password", password);
			}
			
			PacketProcessor.writePacket(RequestPacket.fromCode(PacketCode.ENTER_ROOM, param));
		}
	}

	public void onClick(Event event) {
		Object src = event.getSource();
		
		if(src == enterButton) {
			Room room = roomListView.getSelectionModel().getSelectedItem();
			enterRoom(room);
				
		} else if(src == createRoomButton) {
			createRoomDialog.showWindow(true);
		} else if(src == refreshButton) {
			PacketProcessor.writePacket(RequestPacket.fromCode(PacketCode.ROOM_LIST));	
		} else if(src == roomListView) {
			if(((MouseEvent) event).getClickCount() == 2) {
				Room room = roomListView.getSelectionModel().getSelectedItem();
				enterRoom(room);
			}
		}
	}

	@Override
	public void handlePacket(ResponsePacket packet) {
		switch(packet.getCode()) {
		case ROOM_LIST: {
			Set<Room> roomList = (Set<Room>) packet.getData();
			roomListView.setItems(FXCollections.observableArrayList(roomList));
			break;
		}
		case MAKE_ROOM: {
			ParamMap param = (ParamMap) packet.getData();
			PacketProcessor.writePacket(RequestPacket.fromCode(PacketCode.ENTER_ROOM, param));
			break;
		}
		case ENTER_ROOM: {
			ParamMap param = (ParamMap) packet.getData();
			if(createRoomDialog.getStage() != null) {
				createRoomDialog.closeWindow();	
			}
			
			roomController.showWindow();
			roomController.initRoom((Room) param.get("room"), (List<User>) param.get("users"), (RoomMeta) param.get("meta"), (User) param.get("master"));
			closeWindow();
			break;
		}
		default:
			break;
		}
	}
	
	public class RoomCell extends CustomListCell<Room>{
		@FXML 
		Label roomTitle;
		@FXML 
		Label roomNumber;
		@FXML
		ImageView enterButton;
		
		public RoomCell() {
			super("roomCell.fxml");
		}
		
		public void onClick(Event event) {
			if(event.getSource() == enterButton) {
				enterRoom((Room) enterButton.getUserData());
			}
		}

		@Override
		protected void drawCell(Room item, Node cell) {
			enterButton.setUserData(item);
			roomTitle.setText(item.getTitle());
			roomNumber.setText(String.valueOf(item.getId()));
		}
		
		
	}
	
	public class CreateRoomController extends BaseController {

		@FXML
		TextField titleText;
		
		@FXML
		PasswordField passwordText;
		
		@FXML
		RadioButton maxPersonTwo, maxPersonFive, maxPersonEight;
		
		@FXML
		CheckBox passwordCheck;
		
		@FXML
		Button createButton;
		
		@FXML
		ImageView closeButton;
		
		public CreateRoomController() {
			super(LobbyController.this, "createRoomDialog.fxml", false);
		}

		@Override
		protected void initController(boolean initialize) {
			if(initialize) {
				getStage().initStyle(StageStyle.UNDECORATED);
				FXUtil.setMoverForWindow(getRoot(), stage);
			}
			titleText.setText("");
			maxPersonTwo.setSelected(true);
			passwordCheck.setSelected(false);
			passwordText.setText("");
			passwordCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
				public void changed(javafx.beans.value.ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					if(!newValue) {
						passwordText.setText("");
					}
					passwordText.setDisable(!newValue);
				};
			});
		}
		
		public void onClick(Event event) {
			if(event.getSource() == createButton) {
				int person = maxPersonTwo.isSelected() ? 2 : 
					maxPersonFive.isSelected() ? 5 : 
					maxPersonEight.isSelected() ? 8 : 2;
				if(titleText.getText().isEmpty()) {
					Dialogs.showAlert("방 제목을 입력하세요");
					return;
				}
				LobbyController.this.makeRoom(titleText.getText(), person, passwordText.getText());
			} else if(event.getSource() == closeButton) {
				closeWindow();
			}
		}
	}
	
	public void makeRoom(String roomTitle, Integer maxPerson, String password) {
		PacketProcessor.writePacket(RequestPacket.fromCode(PacketCode.MAKE_ROOM, 
				ParamMap.create(
						"title", roomTitle, 
						"maxPerson", maxPerson, 
						"password", password)
				));
	}

	@Override
	public void handleError(ErrorCode code) {
		switch(code) {
		case EXCEED_PERSON:
			Dialogs.showAlert("인원이 초과하였습니다");
			break;
		case INVALID_PASSWORD:
			Dialogs.showAlert("비밀번호가 틀렸습니다");
			break;
		case INVALID_ROOM:
			Dialogs.showAlert("유효하지 않은 방입니다");
			PacketProcessor.writePacket(RequestPacket.fromCode(PacketCode.ROOM_LIST));
		}
	}
}
