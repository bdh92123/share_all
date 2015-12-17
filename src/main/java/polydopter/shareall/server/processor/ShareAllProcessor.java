package polydopter.shareall.server.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class ShareAllProcessor<T> implements Runnable {

	private Logger LOG = Logger.getLogger(getClass().getName());
	private LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<T>();

	protected boolean isStarted = false;
	private List<Thread> threads;
	private int processorCount = 1;

	public ShareAllProcessor(int processorCount) {
		this.processorCount = processorCount;
		this.threads = new ArrayList<Thread>();
	}

	public void enque(T obj) throws Exception {
		if (!isStarted) {
			throw new Exception("Processor isn't started");
		}
		queue.add(obj);
	}

	@Override
	public void run() {
		isStarted = true;
		while (isStarted) {
			try {
				T obj = queue.take();
				process(obj);
			} catch (InterruptedException e) {
				break;
			} catch (Exception e) {
				LOG.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	public abstract void process(T obj);

	public void start() {
		for (int i = 0; i < processorCount; i++) {
			Thread th = new Thread(this, getClass().getName()+(i+1));
			threads.add(th);
			th.start();
		}
	}

	public void stop() {
		for(Thread th : threads) {
			th.interrupt();
		}
	}
}
