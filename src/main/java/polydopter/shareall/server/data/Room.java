package polydopter.shareall.server.data;

import java.io.Serializable;
import java.util.Date;

public class Room implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String title;
	private int maxPerson;
	private boolean isSecret;
	private transient String password;
	private Date created;
	
	public Room() {
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		
		return ((Room) obj).getId() == getId();
	}
	
	@Override
	public int hashCode() {
		return getId();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getMaxPerson() {
		return maxPerson;
	}

	public void setMaxPerson(int maxPerson) {
		this.maxPerson = maxPerson;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public boolean isSecret() {
		return isSecret;
	}

	public void setSecret(boolean isSecret) {
		this.isSecret = isSecret;
	}
	
}
