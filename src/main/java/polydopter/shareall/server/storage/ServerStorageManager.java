package polydopter.shareall.server.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import polydopter.shareall.common.data.FileKey;
import polydopter.shareall.common.data.RoomMeta;
import polydopter.shareall.server.config.ShareAllConfig;
import polydopter.shareall.server.data.Room;

public class ServerStorageManager {
    
    private static final String fileDir = ShareAllConfig.get("fileDir", System.getProperty("user.dir") + File.separator + "ShareAllUploaded");
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
    
    public static OutputStream createOutputStream(Room room, FileKey fileKey) {
        try {
        	File file = new File(fileDir + "/" + sdf.format(room.getCreated()) + "_" + room.getId() + "/" + fileKey.getName());
        	file.getParentFile().mkdirs();
            return new FileOutputStream(file);   
        } catch (Exception e) {
        	e.printStackTrace();
            return null;
        }
    }
    
    public static InputStream createInputStream(Room room, FileKey fileKey) {
        try {
        	File file = getLocalFile(room, fileKey);
        	return new FileInputStream(file);  
        } catch (Exception e) {
        	e.printStackTrace();
            return null;
        }
    }
    
    public static File getLocalFile(Room room, FileKey fileKey) {
    	File file = new File(fileDir + "/" + sdf.format(room.getCreated()) + "_" + room.getId() + "/" + (fileKey == null ? "" : fileKey.getName()));
    	return file;
    }

    public static void clearRoom(Room room, RoomMeta meta) {
        for(FileKey fileKey : meta.getFileList()) {
            File file = getLocalFile(room, fileKey);
            if(file.exists()) {
                file.delete();
            }
        }
        
        for(FileKey fileKey : meta.getImageList()) {
            File file = getLocalFile(room, fileKey);
            if(file.exists()) {
                file.delete();
            }
        }
        
        for(FileKey fileKey : meta.getMusicList()) {
            File file = getLocalFile(room, fileKey);
            if(file.exists()) {
                file.delete();
            }
        }
        
        getLocalFile(room, null).delete();
    }
}
