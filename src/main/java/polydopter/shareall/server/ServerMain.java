package polydopter.shareall.server;

import polydopter.shareall.server.net.FileAcceptor;
import polydopter.shareall.server.net.MainAcceptor;
import polydopter.shareall.server.processor.Processors;

public class ServerMain {

	private ServerMain() {
	}
	
	public static void start(int mainPort, int filePort) {
		MainAcceptor.start(mainPort);
		FileAcceptor.start(filePort);
		Processors.start();
	}
	
	public static void stop() {
		MainAcceptor.stop();
		FileAcceptor.stop();
		Processors.stop();
	}

}
