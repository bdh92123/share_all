package polydopter.shareall.server.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import polydopter.shareall.common.Logger;
import polydopter.shareall.common.packet.ErrorCode;
import polydopter.shareall.common.packet.PacketCode;
import polydopter.shareall.common.packet.RequestPacket;
import polydopter.shareall.common.packet.ResponsePacket;
import polydopter.shareall.server.processor.Processors;

public class MainAcceptor implements Runnable {

	private static ServerSocket serverSocket = null;
	private static boolean isStarted = false;
	private int port;
	private static Selector selector;
	private static Thread _th;

	private MainAcceptor(int port) {
		this.port = port;
	}

	public static void start(int port) {
		if (!isStarted) {
			isStarted = true;
			_th = new Thread(new MainAcceptor(port), MainAcceptor.class.getSimpleName());
			_th.start();
		}
	}

	public static void stop() {
		isStarted = false;
		if (_th != null && _th.isAlive()) {
			_th.interrupt();
		}

		try {
			serverSocket.close();
		} catch (Exception e) {
		}

		try {
			selector.close();
		} catch (Exception e) {
		}
	}

	@Override
	public void run() {

		Logger.info("MainAcceptor is starting...");

		try {
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);

			serverSocket = serverSocketChannel.socket();
			InetSocketAddress inetSocketAddress = new InetSocketAddress(this.port);
			serverSocket.bind(inetSocketAddress);

			selector = Selector.open();
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (Exception e) {
			Logger.error(e);
			return;
		}

		try {
			while (true) {
				int count = selector.select();

				// nothing to process
				if (count == 0) {
					continue;
				}

				Set<SelectionKey> keySet = selector.selectedKeys();
				Iterator<SelectionKey> itor = keySet.iterator();

				while (itor.hasNext()) {
					SelectionKey selectionKey = (SelectionKey) itor.next();
					itor.remove();

					Socket socket = null;
					SocketChannel channel = null;

					if (selectionKey.isAcceptable()) {
						Logger.info("Got acceptable key");
						try {
							socket = serverSocket.accept();
							Logger.info("Connection from: " + socket);
							channel = socket.getChannel();
						} catch (IOException e) {
							Logger.info("Unable to accept channel");
							e.printStackTrace();
							selectionKey.cancel();
						}
						if (channel != null) {
							try {
								Logger.info("Watch for something to read");
								channel.configureBlocking(false);
								channel.register(selector, SelectionKey.OP_READ);
								ClientSession session = ClientManager.registClient(channel);
								session.writePacket(
										ResponsePacket.fromCode(PacketCode.SESSION, session.getUser().getSessionKey()));
							} catch (IOException e) {
								Logger.error("Unable to use channel", e);
								e.printStackTrace();
								selectionKey.cancel();
							}
						}
					}
					if (selectionKey.isConnectable() && channel.isConnectionPending()) {
						channel.finishConnect();
					} else if (selectionKey.isReadable()) {
						SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
						try {
							ClientSession session = ClientManager.getClient(socketChannel);
							if (session == null) {
								Logger.error("null session.");
								selectionKey.cancel();
								continue;
							}
							int result = session.tryReadPacket();
							if (result == -1) {
								ClientManager.removeClient(socketChannel);
								socketChannel.close();
							} else if (result == 1) {
								RequestPacket packet = session.takePacket();
								if(packet.getCode().equals(PacketCode.AUTH) || session.isAuth()) {
									Processors.packetHandle.enque(session, packet);	
								} else {
									Processors.packetWriter.enque(session, ResponsePacket.fromCode(PacketCode.ERROR, ErrorCode.INVALID_AUTH));
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							selectionKey.cancel();
						}
					}
				}
			}
		} catch (ClosedSelectorException e) {
		} catch (Exception e) {
			System.err.println("[" + Thread.currentThread().getName() + "] Error during select");
			e.printStackTrace();
		}
	}
}
