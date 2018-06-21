package com.arcsoft.refocus.utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.arcsoft.refocus.UIGlobalDef;

public class FileUtil {
	public static byte[] LoadFromFile(String fileName) {
		
        File file = new File(fileName);
        byte[] fileData = new byte[(int) file.length()];

        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(fileData);
            dis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileData;
    }
	
	public static boolean saveImage(String fullpath, byte[] data) {
			if(null == fullpath || null == data)
				return false;
			
			File tempPath = new File( UIGlobalDef.LOCAL_PATH);
			if (!tempPath.exists())
				tempPath.mkdir();
		
			boolean success = false;
			String name = fullpath;
			int count = 0;
			File file = new File(name);
			while(file.exists()) {
				count ++;
				name = fullpath + count;
				file = new File(name);
			}
			try {
				FileOutputStream fs = new FileOutputStream(fullpath);
				fs.write(data);
				fs.close();
				success = true;
			} catch (FileNotFoundException e) {
					
			} catch (IOException e) {

			} finally {
					
			}
			return success;
	}   


}
