package polydopter.shareall.common.data;

import java.io.Serializable;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	private String nickname;
	private String sessionKey;

	public User() {
		nickname = "";
	}
	
	public User(String nickname) {
		this.nickname = nickname;
	}
	
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	@Override
	public int hashCode() {
		return nickname == null ? -1 : nickname.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		return nickname != null && nickname.equals(((User) obj).getNickname());
	}
	
	@Override
	public String toString() {
		return nickname;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}
}
