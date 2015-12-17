package polydopter.shareall.common.data;

import java.io.Serializable;
import java.util.Date;

public class FileKey implements Serializable {
	private static final long serialVersionUID = 1L;
	private FileType type = FileType.FILE;
	private String name;
	private User user;
	private Date date;
	private long size;

	public FileKey(String name) {
		this.setName(name);
	}
	
	public FileKey(String name, FileType type) {
		this.setName(name);
		this.setType(type);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public FileType getType() {
		return type;
	}

	public void setType(FileType type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return type + ": " +name;
	}
	
	@Override
	public boolean equals(Object obj) {
		FileKey fileKey = (FileKey) obj;
		return obj != null && 
				fileKey.name != null && fileKey.name.equals(name) 
				&& fileKey.type != null && fileKey.type.equals(type);
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
