package polydopter.shareall.server.processor;

import java.util.LinkedList;
import java.util.List;

import polydopter.shareall.common.data.Pair;
import polydopter.shareall.common.packet.RequestPacket;
import polydopter.shareall.server.handler.AuthHandler;
import polydopter.shareall.server.handler.LobbyHandler;
import polydopter.shareall.server.handler.RoomHandler;
import polydopter.shareall.server.handler.ShareAllHandler;
import polydopter.shareall.server.net.ClientSession;

public class PacketHandleProcessor extends ShareAllProcessor<Pair<ClientSession, RequestPacket>> {
	
	List<ShareAllHandler> handlerChain = new LinkedList<ShareAllHandler>();
	
	public PacketHandleProcessor(int processorCount) {
		super(processorCount);
		handlerChain.add(new AuthHandler());
		handlerChain.add(new LobbyHandler());
		handlerChain.add(new RoomHandler());
		
	}

	public void enque(ClientSession session, RequestPacket packet) throws Exception {
		if (!isStarted) {
			throw new Exception("Processor isn't started");
		}
		enque(new Pair<ClientSession, RequestPacket>(session, packet));
	}
	
	@Override
	public void process(Pair<ClientSession, RequestPacket> readJob) {
		for(ShareAllHandler handler : handlerChain) {
			if(handler.canHandle(readJob.value1, readJob.value2)) {
				boolean stopChain = handler.handle(readJob.value1, readJob.value2);
				if(stopChain) {
					break;
				}	
			}
		}
	}
	
}
