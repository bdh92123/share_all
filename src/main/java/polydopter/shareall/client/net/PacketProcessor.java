package polydopter.shareall.client.net;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.application.Platform;
import polydopter.shareall.client.storage.ClientStorageManager;
import polydopter.shareall.client.ui.base.PacketHandler;
import polydopter.shareall.common.Logger;
import polydopter.shareall.common.data.FileKey;
import polydopter.shareall.common.data.FileType;
import polydopter.shareall.common.packet.ErrorCode;
import polydopter.shareall.common.packet.PacketCode;
import polydopter.shareall.common.packet.RequestPacket;
import polydopter.shareall.common.packet.ResponsePacket;
import polydopter.shareall.server.data.Room;

public class PacketProcessor {
	private static Set<PacketHandler> handlerSet = new HashSet<>();
	private static PacketReadThread packetReadThread;
	private static PacketWriteThread packetWriteThread;
	private static Socket socket;
	private static boolean isStarted = false;
	private static String serverIp;
	private static int serverPort;
	
	public static boolean isStarted() {
		return isStarted;
	}
	
	public static void start(String serverIp, int serverPort) throws UnknownHostException, IOException {
		if(!isStarted) {
			socket = new Socket(serverIp, serverPort);
			PacketProcessor.serverIp = serverIp;
			PacketProcessor.serverPort = serverPort;
			packetReadThread = new PacketReadThread(socket);
			packetReadThread.start();
			packetWriteThread = new PacketWriteThread(socket);
			packetWriteThread.start();
			isStarted = true;	
		}
	}
	
	public static void stop() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();	
			}
			
			if(packetReadThread != null && packetReadThread.isAlive()) {
				packetReadThread.interrupt();	
			}
			
			if(packetWriteThread != null && packetWriteThread.isAlive()) {
				packetWriteThread.interrupt();	
			}
		} catch (Exception e) {
			Logger.error(e);
		} finally {
			isStarted = false;
		}
	}	
	
	public static void addHandler(PacketHandler handler) {
		handlerSet.add(handler);
		System.out.println(handler + " handler add");
	}
	
	public static void removeHandler(PacketHandler handler) {
		handlerSet.remove(handler);
		System.out.println(handler + " handler removed");
	}
	
	public static void writePacket(RequestPacket packet) {
		if(isStarted) {
			enque(packet);	
		}
	}
	
	public static FileTransferTask uploadFile(FileType type, Room room, File file, ProgressHandler progressHandler) {
		Socket socket = null;
		try {
			socket = new Socket(serverIp, serverPort + 1);
			FileTransferTask task = new FileTransferTask(socket, room, FileTransferTask.UPLOAD, type, file, progressHandler);
			task.start();	
			return task;
		} catch (Exception e) {
			progressHandler.onError(e);
			return null;
		}
	}
	
	public static FileTransferTask downloadFile(Room room, FileType type, File file, ProgressHandler progressHandler) {
		Socket socket = null;
		try {
			socket = new Socket(serverIp, serverPort + 1);
			FileTransferTask task = new FileTransferTask(socket, room, FileTransferTask.DOWNLOAD, type, file, progressHandler);
			task.start();	
			return task;
		} catch (Exception e) {
			progressHandler.onError(e);
			return null;
		} 
	}
	
	private static void enque(RequestPacket packet) {
		packetWriteThread.enque(packet);
	}
	
	static class PacketReadThread extends Thread {
		private static int instanceCount;
		private Socket socket;
		private ObjectInputStream inputStream;
		
		public PacketReadThread(Socket socket) {
			try {
				instanceCount++;
				setName(PacketReadThread.class.getSimpleName() + "-" + instanceCount);
				this.socket = socket;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				this.inputStream = new ObjectInputStream(this.socket.getInputStream());
				ResponsePacket packet = (ResponsePacket) inputStream.readObject();
				while(packet != null) {
					final ResponsePacket _packet = packet;
					for (Object packetHandler : handlerSet.toArray()) {
						Platform.runLater(new Runnable(){
							@Override
							public void run() {
								if(_packet.getCode() == PacketCode.ERROR) {
									ErrorCode code = (ErrorCode) _packet.getData();
									((PacketHandler)packetHandler).handleError(code);
								} else {
									((PacketHandler)packetHandler).handlePacket(_packet);	
								}
							}
						});
					}
					packet = (ResponsePacket) inputStream.readObject();
				}
			} catch (SocketException e) {
				packetWriteThread.interrupt();
			} catch (Exception e) {
				Logger.error(e);
			}
			
		}
	}
	
	static class PacketWriteThread extends Thread {
		private static int instanceCount;
		private Socket socket;
		private OutputStream outputStream;
		private BlockingQueue<RequestPacket> packetQueue;
		
		public PacketWriteThread(Socket socket) {
			instanceCount++;
			this.socket = socket;
			this.packetQueue = new LinkedBlockingQueue<>();
			setName(PacketWriteThread.class.getSimpleName() + "-" + instanceCount);
		}

		public void enque(RequestPacket packet) {
			this.packetQueue.add(packet);
		}
		
		@Override
		public void run() {
			try {
				this.outputStream = socket.getOutputStream();
				while(true) {
					RequestPacket packet = packetQueue.take();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
				    for(int i=0;i<4;i++) baos.write(0);
				    ObjectOutputStream oos = new ObjectOutputStream(baos);
				    oos.writeObject(packet);
				    oos.flush();
				    oos.close();
				    final ByteBuffer wrap = ByteBuffer.wrap(baos.toByteArray());
				    wrap.putInt(0, baos.size()-4);
					this.outputStream.write(wrap.array());
					this.outputStream.flush();
				}
			} catch (InterruptedException e) {
			} catch (IOException e) {
				Logger.error(e);
			}

		}
	}
	
	public static class FileTransferTask extends Thread {
		private Socket socket;
		private Room room;
		private DataInputStream inputStream;
		private DataOutputStream outputStream;
		private File file;
		private FileType type;
		public static final byte UPLOAD = 1;
		public static final byte DOWNLOAD = 2;
		private byte requestType;
		ProgressHandler progressHandler;
		private boolean error;

		FileTransferTask(Socket socket, Room room, byte requestType, FileType type, File file, ProgressHandler progressHandler) {
			this.socket = socket;
			this.room = room;
			this.file = file;
			this.type = type;
			this.requestType = requestType;
			this.progressHandler = progressHandler;
			try {
				this.inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				this.outputStream = new DataOutputStream(socket.getOutputStream());
			} catch (Exception e) {
				progressHandler.onError(e);
			}
		}

		@Override
		public void run() {
			FileOutputStream fileOutputStream = null;
			FileInputStream fileInputStream = null;

			try {
				outputStream.writeUTF(UserHolder.getUser().getSessionKey());
				outputStream.writeByte(requestType);
				outputStream.writeByte(type.intValue());
				outputStream.writeUTF(file.getName());
				
				if (requestType == DOWNLOAD) {
					byte buffer[] = new byte[8192];
					long totalSize = inputStream.readLong();
					long totalRead = 0;
					int readSize;
					int readCount=0;
					
					fileOutputStream = (FileOutputStream) ClientStorageManager.createOutputStream(room, file);

					if (fileOutputStream != null) {
						while ((readSize = inputStream.read(buffer)) > -1 && totalRead <= totalSize) {
							fileOutputStream.write(buffer, 0, readSize);
							totalRead += readSize;
							readCount++;
							if(readCount % 10 == 0) {
								progressHandler.inProgress(totalSize, totalRead);
							}
							if(Thread.interrupted()) {
								throw new InterruptedException();
							}
						}
						
						progressHandler.inProgress(totalSize, totalRead);
						fileOutputStream.flush();
						fileOutputStream.close();
					}
				} else if (requestType == UPLOAD) {
					fileInputStream = new FileInputStream(file);
					long totalSize = fileInputStream.getChannel().size();
					byte buffer[] = new byte[8192];
					long totalWrite = 0;
					int readSize;
					int writeCount=0;
					
					outputStream.writeLong(totalSize);
					while ((readSize = fileInputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, readSize);
						totalWrite += readSize;
						writeCount++;
						if(writeCount % 10 == 0) {
							progressHandler.inProgress(totalSize, totalWrite);
						}
						if(Thread.interrupted()) {
							throw new InterruptedException();
						}
					}
					progressHandler.inProgress(totalSize, totalWrite);
					outputStream.flush();
				}

			} catch (Exception e) {
				progressHandler.onError(e);
				error = true;
			} finally {
				try {
					if (fileOutputStream != null && requestType == DOWNLOAD) {
						fileOutputStream.close();
						if(error) {
							File broken = ClientStorageManager.getLocalFile(room, file.getName());
							broken.delete();	
						}
					} else if(fileInputStream != null && requestType == UPLOAD) {
						fileInputStream.close();
					}
				} catch (Exception e) {
				} finally {
					try {
						socket.shutdownOutput();
						socket.close();
					} catch (Exception e2) {
					}
				}
			}
		}

		public boolean isFor(FileKey fileKey) {
			if(fileKey.getName().equals(file.getName()) && fileKey.getType().equals(type)) {
				return true;
			}
			
			return false;
		}
	}
}


