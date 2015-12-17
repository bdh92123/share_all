package polydopter.shareall.server.net;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import polydopter.shareall.common.Logger;
import polydopter.shareall.common.data.FileKey;
import polydopter.shareall.common.data.FileType;
import polydopter.shareall.common.packet.PacketCode;
import polydopter.shareall.common.packet.ResponsePacket;
import polydopter.shareall.server.config.ShareAllConfig;
import polydopter.shareall.server.data.Room;
import polydopter.shareall.server.service.RoomService;
import polydopter.shareall.server.storage.ServerStorageManager;

public class FileAcceptor implements Runnable {
	private static boolean isStarted = false;
	private static byte UPLOAD = 1;
	private static byte DOWNLOAD = 2;

	private static Thread _th;
	private Map<String, AtomicInteger> concurrentCount;

	private int port;
	private static ServerSocket serverSocket;

	private FileAcceptor(int port) {
		concurrentCount = new ConcurrentHashMap<>();
		this.port = port;
	}

	public static void start(int port) {
		if (!isStarted) {
			isStarted = true;
			_th = new Thread(new FileAcceptor(port), FileAcceptor.class.getSimpleName());
			_th.start();
		}
	}

	@Override
	public void run() {
		int concurrentFileCount = ShareAllConfig.getInteger("concurrent_file_count", 5);

		Logger.info("FileAcceptor is starting...");
		try {
			serverSocket = new ServerSocket(port);
			while (isStarted) {
				try {
					Socket client = serverSocket.accept();
					String address = client.getRemoteSocketAddress().toString();
					AtomicInteger count = concurrentCount.get(address);
					if (count == null || count.get() < concurrentFileCount) {
						if (count == null) {
							count = new AtomicInteger(0);
							concurrentCount.put(address, count);
						}
						int index = count.incrementAndGet();
						new Thread(new FileTransferTask(client),
								FileTransferTask.class.getName() + "-" + address + "-" + index).start();
					} else {
						client.close();
					}
				} catch (SocketException e) {
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	class FileTransferTask implements Runnable {
		private Socket socket;
		private DataInputStream inputStream;
		private DataOutputStream outputStream;
		private boolean error;

		FileTransferTask(Socket socket) {
			this.socket = socket;
			try {
				this.inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				this.outputStream = new DataOutputStream(socket.getOutputStream());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			FileOutputStream fileOutputStream = null;
			FileInputStream fileInputStream = null;

			byte requestType = -1;
			FileKey fileKey = null;
			Room room = null;
			try {
				String chatSessionKey = inputStream.readUTF();
				ClientSession session = ClientManager.getClient(chatSessionKey);
				if (session == null) {
					Logger.info("Session is invalid");
					return;
				}
				room = session.getRoom();
				if (room == null) {
					Logger.info("Room is invalid");
					return;
				}
				requestType = inputStream.readByte();

				byte dataType = inputStream.readByte();
				String name = inputStream.readUTF();
				fileKey = new FileKey(name);
				if (dataType == FileType.FILE.intValue()) {
					fileKey.setType(FileType.FILE);
				} else if (dataType == FileType.MUSIC.intValue()) {
					fileKey.setType(FileType.MUSIC);
				} else if (dataType == FileType.IMAGE.intValue()) {
					fileKey.setType(FileType.IMAGE);
				}

				RoomService roomService = RoomService.get(room.getId());
				if (requestType == UPLOAD) {
					byte buffer[] = new byte[8192];
					long size = inputStream.readLong();
					long totalRead = 0;
					int read;

					fileOutputStream = (FileOutputStream) ServerStorageManager.createOutputStream(room, fileKey);

					if (fileOutputStream != null) {
						while (totalRead < size && (read = inputStream.read(buffer)) > -1) {
							fileOutputStream.write(buffer, 0, read);
							totalRead += read;
							if(Thread.interrupted()) {
								throw new InterruptedException();
							}
						}

						fileOutputStream.flush();
						fileOutputStream.close();

						Logger.info("File uploaded " + name);
						fileKey.setSize(totalRead);
						fileKey.setUser(session.getUser());
						fileKey.setDate(new Date());

						if (dataType == FileType.FILE.intValue()) {
							roomService.getMeta().addFile(fileKey);
						} else if (dataType == FileType.MUSIC.intValue()) {
							roomService.getMeta().addMusic(fileKey);
						} else if (dataType == FileType.IMAGE.intValue()) {
							roomService.getMeta().addImage(fileKey);
						}

						roomService.broadcast(ResponsePacket.fromCode(PacketCode.FILE_ADDED).setData(fileKey));
					}
				} else if (requestType == DOWNLOAD) {
					byte buffer[] = new byte[8192];
					fileInputStream = (FileInputStream) ServerStorageManager.createInputStream(room, fileKey);
					if (fileInputStream != null) {
						long size = fileInputStream.getChannel().size();
						outputStream.writeLong(size);
						int bytesRead;
						while ((bytesRead = fileInputStream.read(buffer)) != -1) {
							outputStream.write(buffer, 0, bytesRead);
							if(Thread.interrupted()) {
								throw new InterruptedException();
							}
						}

						outputStream.flush();
					}
				}

			} catch (Exception e) {
				Logger.error(e);
				error = true;
			} finally {
				String remoteIp = socket.getRemoteSocketAddress().toString();
				if (concurrentCount.get(remoteIp).decrementAndGet() == 0) {
					concurrentCount.remove(remoteIp);
				}
				try {
					if (fileOutputStream != null) {
						fileOutputStream.close();
					}
					socket.close();
				} catch (Exception e) {
				}

				if (error && requestType == UPLOAD) {
					File broken = ServerStorageManager.getLocalFile(room, fileKey);
					broken.delete();
				}
			}
		}
	}

	public static void stop() {
		isStarted = false;
		if (_th != null && _th.isAlive()) {
			if (serverSocket != null && !serverSocket.isClosed()) {
				try {
					serverSocket.close();
				} catch (IOException e) {
				}
			}
			_th.interrupt();
		}
	}

}