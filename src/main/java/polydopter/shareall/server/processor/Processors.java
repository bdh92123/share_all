package polydopter.shareall.server.processor;

public class Processors {
	public static PacketHandleProcessor packetHandle = new PacketHandleProcessor(5);
	public static PacketWriterProcessor packetWriter = new PacketWriterProcessor(5);
	
	private static boolean isStarted = false;
	
	public static void start() {
		if(isStarted) {
			return;
		}
		
		packetHandle.start();
		packetWriter.start();
		
		isStarted = true;
	}
	
	public static void stop() {
		if(isStarted) {
			packetHandle.stop();
			packetWriter.stop();
			isStarted = false;
		}
	}
}
