package polydopter.shareall.client.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

public class ConsoleOutput extends OutputStream implements Initializable {

	private TextArea consoleArea;
	private PrintStream origOut;
	private PrintStream origErr;

	public ConsoleOutput(TextArea consoleArea) {
		this.consoleArea = consoleArea;
		origOut = System.out;
		origErr = System.err;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				ConsoleOutput.this.write(b);
			}
		};
		origOut = System.out;
		origErr = System.err;
		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}
	
	@Override
	public void write(int b) throws IOException {
		if(consoleArea.getScene().getWindow().isShowing()) {
			Platform.runLater(() -> consoleArea.appendText(String.valueOf((char) b)));	
		} else {
			origOut.write(b);
		}
	}
}
