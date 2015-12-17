package polydopter.shareall.client.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import polydopter.shareall.server.config.ShareAllConfig;
import polydopter.shareall.server.data.Room;

public class ClientStorageManager {
    
//	private static final String fileDir = FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + File.separator + "ShareAllDownload";
	private static final String fileDir = ShareAllConfig.get("fileDir", System.getProperty("user.dir") + File.separator + "ShareAllDownload");
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
    
    public static OutputStream createOutputStream(Room room, File file) throws FileNotFoundException {
    	file = getLocalFile(room, file.getName());
    	file.getParentFile().mkdirs();
        return new FileOutputStream(file);    
    }
    
    public static File getLocalFile(Room room, String name) {
        try {
        	return new File(fileDir + "/" + sdf.format(room.getCreated()) + "_" + room.getId() + "/" + (name == null ? "" : name));    
        } catch (Exception e) {
            return null;
        }
    }
    
    public static InputStream createInputStream(Room room, String name) {
        try {
        	return new FileInputStream(getLocalFile(room, name));
        } catch (Exception e) {
            return null;
        }
    }
    
}
