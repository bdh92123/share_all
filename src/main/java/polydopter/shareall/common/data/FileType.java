package polydopter.shareall.common.data;

public enum FileType {
	MUSIC(1), IMAGE(2), FILE(3);
	
	int value;
	FileType(int value) {
		this.value = value;
	}
	
	public int intValue() {
		return value;
	}
}
