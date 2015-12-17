package polydopter.shareall.server.net;

public interface SessionListener {
	public void sessionCreated(ClientSession session);
	public void sessionClosed(ClientSession session);
}
