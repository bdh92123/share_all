package polydopter.shareall.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Test {
	static List<String> copy = new CopyOnWriteArrayList<>();
	static List<String> coll = Collections.synchronizedList(new ArrayList<String>());

	public static void main(String[] args) throws Exception {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					for (int i = 0; i < 100; i++) {
						copy.add(i + "");
						Thread.sleep(1);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		Thread.sleep(2);
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					for (int i = 0; i < 100; i++) {
						copy.remove(0);
						Thread.sleep(2);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					for (int i = 0; i < 100; i++) {
						coll.add(i + "");
						Thread.sleep(1);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		Thread.sleep(2);
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					for (int i = 0; i < 100; i++) {
						coll.remove(0);
						Thread.sleep(2);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

}
