package polydopter.shareall.server.processor;

import polydopter.shareall.common.data.Pair;
import polydopter.shareall.common.packet.ResponsePacket;
import polydopter.shareall.server.net.ClientSession;

public class PacketWriterProcessor extends ShareAllProcessor<Pair<ClientSession, ResponsePacket>> {

	public PacketWriterProcessor(int processorCount) {
		super(processorCount);
		
	}

	public void enque(ClientSession session, ResponsePacket packet) throws Exception {
		if (!isStarted) {
			throw new Exception("Processor isn't started");
		}
		enque(new Pair<ClientSession, ResponsePacket>(session, packet));
	}
	
	@Override
	public void process(Pair<ClientSession, ResponsePacket> writeJob) {
		writeJob.value1.writePacket(writeJob.value2);
	}

}
