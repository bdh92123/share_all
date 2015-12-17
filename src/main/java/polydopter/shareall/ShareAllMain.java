package polydopter.shareall;
	
import java.io.PrintStream;
import java.net.URL;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import polydopter.shareall.client.net.PacketProcessor;
import polydopter.shareall.client.net.UserHolder;
import polydopter.shareall.client.ui.SplashController;
import polydopter.shareall.client.ui.base.BaseController;
import polydopter.shareall.client.ui.base.PacketHandler;
import polydopter.shareall.client.ui.dialog.Dialogs;
import polydopter.shareall.client.util.ConsoleOutput;
import polydopter.shareall.common.Logger;
import polydopter.shareall.common.data.User;
import polydopter.shareall.common.packet.ErrorCode;
import polydopter.shareall.common.packet.PacketCode;
import polydopter.shareall.common.packet.RequestPacket;
import polydopter.shareall.common.packet.ResponsePacket;
import polydopter.shareall.server.ServerMain;


public class ShareAllMain extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			Font.loadFont(ShareAllMain.class.getResourceAsStream("/font/NanumBarunGothicBold.ttf"), 12);
			Font.loadFont(ShareAllMain.class.getResourceAsStream("/font/NanumBarunGothicLight.ttf"), 12);
			
			BootController controller = new BootController(null);
//			RoomController controller = new RoomController(null);
			controller.showWindow();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			URL.setURLStreamHandlerFactory(new CpURLStreamHandlerFactory());	
		} catch (Error e) {
		}
		launch(args);
	}
	
	class BootController extends BaseController implements PacketHandler {
		
		@FXML
		TextArea logMessage;
		
		@FXML
		TextField listenPort, serverIpText, serverPortText, nicknameText;
		
		@FXML
		Button serverStartButton, serverStopButton, connectButton, logClearButton;

		private boolean serverStart = false;
		
		public BootController(BaseController parent) {
			super(parent, "boot.fxml", true, true);
		}

		@Override
		protected void initController(boolean initialize) {
			ConsoleOutput out = new ConsoleOutput(logMessage);
			serverStopButton.setDisable(true);
			nicknameText.setText("Guest" + (int)(Math.random() * 1000));
//			serverStartButton.fireEvent(new ActionEvent(serverStartButton, null));
//			connectButton.fireEvent(new ActionEvent(connectButton, null));
			System.setErr(new PrintStream(out));
			System.setOut(new PrintStream(out));
		}
		
		@Override
		public void onCloseRequest(WindowEvent event, boolean softClose) {
			if(serverStart) {
				ServerMain.stop();	
			}
		}

		@FXML
		public void onClick(ActionEvent event) {
			Object src = event.getSource();
			if(src == serverStartButton) {
				serverStart = true;
				ServerMain.start(Integer.parseInt(listenPort.getText()), Integer.parseInt(listenPort.getText()) + 1);
				serverStopButton.setDisable(false);
				serverStartButton.setDisable(true);
			} else if(src == serverStopButton) {
				ServerMain.stop();
				serverStopButton.setDisable(true);
				serverStartButton.setDisable(false);
			} else if(src == connectButton) {
				try {
					if(!PacketProcessor.isStarted()) {
						PacketProcessor.start(serverIpText.getText(), Integer.parseInt(serverPortText.getText()));
						PacketProcessor.addHandler((PacketHandler) this);
					}
					
					PacketProcessor.writePacket(RequestPacket.fromCode(PacketCode.AUTH, nicknameText.getText()));
					Logger.info(nicknameText.getText() + " try..");
				} catch (Exception e) {
					Dialogs.showAlert("서버연결에 실패했습니다");
					Logger.error(e);
				}
			} else if(src == logClearButton) {
				logMessage.clear();
			}
		}

		@Override
		public void handlePacket(ResponsePacket packet) {
			if(packet.getCode() == PacketCode.AUTH) {
				User user = (User) packet.getData();
				UserHolder.setUser(user);
				SplashController controller = new SplashController(this);
				controller.showWindow();
			}
		}

		@Override
		public void handleError(ErrorCode code) {
			switch(code) {
			case NICKNAME_DUPLICATED: {
				Dialogs.showAlert("닉네임이 중복됩니다. 다른닉네임을 사용해주세요.");
				break;
			}
			}
		}
	}
}
