package polydopter.shareall.client.net;

public interface ProgressHandler {
	public void inProgress(long total, long current);
	public void onError(Throwable e);
}
