package com.yangxianwen.post176.utils;

import android.util.Log;

import com.yangxianwen.post176.face.faceserver.FaceServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtil {

    private static final String TAG = FileUtil.class.getName();

    /**
     * 将文件流转为本地图片
     */
    public static void savePhoto(String filePath, InputStream inputStream) {
        Log.i(TAG, "savePhoto: filePath = " + filePath);

        File file = new File(filePath);

        try {
            if (file.getParentFile() != null && !file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                Log.e(TAG, "savePhoto: file mkdirs fail");
            }

            if (file.exists() && !file.delete()) {
                Log.e(TAG, "savePhoto: file delete fail");
            }

            if (!file.createNewFile()) {
                Log.e(TAG, "savePhoto: file createNewFile fail");
            }
        } catch (Exception e) {
            Log.e(TAG, "create file", e);
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
        }
    }

    public static void createPath(String filePath) {
        File file = new File(filePath);

        try {
            if (file.getParentFile() != null && !file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                Log.e(TAG, "createPath: parentFile mkdirs fail");
            }

            if (!file.isDirectory()) {
                if (!file.mkdir()) {
                    Log.e(TAG, "createPath: file mkdir fail");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "createPath file", e);
        }
    }

    public static File getFileByName(String fileName) {
        return new File(FaceServer.ROOT_PATH + File.separator + FaceServer.SAVE_IMG_DIR + File.separator + fileName + FaceServer.IMG_SUFFIX);
    }

}
