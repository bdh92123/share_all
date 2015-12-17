package polydopter.shareall.client.ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import polydopter.shareall.client.ui.base.BaseController;

public class SplashController extends BaseController {
	
	public SplashController(BaseController parent) {
		super(parent, "splash.fxml", false);
	}

	@Override
	protected void initController(boolean initialize) {
		stage.initStyle(StageStyle.TRANSPARENT);
		FadeTransition ft = new FadeTransition(Duration.millis(500), getStage().getScene().getRoot());
		ft.setFromValue(0.0);
		ft.setToValue(1.0);
		PauseTransition pt = new PauseTransition(Duration.millis(2000));

		FadeTransition ft2 = new FadeTransition(Duration.millis(1000),
				getRoot());
		ft2.setFromValue(1.0);
		ft2.setToValue(0.0);

		SequentialTransition st = new SequentialTransition(ft, pt, ft2);
		st.onFinishedProperty().set(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				closeWindow();
				new LobbyController(SplashController.this).showWindow();
			}
		});
		st.playFromStart();
	}

}
