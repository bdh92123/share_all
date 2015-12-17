package polydopter.shareall.client;
	
import javafx.application.Application;
import javafx.stage.Stage;
import polydopter.shareall.client.ui.LobbyController;


public class ClientMain extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			LobbyController controller = new LobbyController(null);
			controller.showWindow();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
