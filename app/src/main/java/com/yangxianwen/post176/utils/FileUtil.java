package com.yangxianwen.post176.utils;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtil {

    private static final String TAG = FileUtil.class.getName();

    /**
     * 将文件流转为本地图片
     */
    public static boolean savePhoto(String filePath, InputStream inputStream) {
        Log.i(TAG, "savePhoto: filePath = " + filePath);

        File file = new File(filePath);

        try {
            if (file.getParentFile() != null && !file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                Log.e(TAG, "savePhoto: file mkdirs fail");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "create file", e);
            return false;
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            int index;
            byte[] bytes = new byte[1024];
            while ((index = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, index);
                fileOutputStream.flush();
            }
        } catch (Exception e) {
            Log.e(TAG, "save file", e);
            return false;
        }
        return true;
    }
}
