package polydopter.shareall.client.net;

import polydopter.shareall.common.data.User;

public class UserHolder {
	private static User user = null;
	
	public static void setUser(User user) {
		UserHolder.user = user;
	}
	public static User getUser() {
		return UserHolder.user;
	}
}
