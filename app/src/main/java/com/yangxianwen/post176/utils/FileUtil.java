package com.yangxianwen.post176.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {

    private static final String TAG = FileUtil.class.getName();

    public static final String ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "postPic";
    /**
     * 存放等待注册图的目录
     */
    public static final String REGISTER_DIR = ROOT_DIR + File.separator + "register";
    /**
     * 存放注册失败图的目录
     */
    public static final String REGISTER_FAILED_DIR = ROOT_DIR + File.separator + "failed";
    /**
     * 存放已经注册图的目录
     */
    public static final String SAVE_IMG_DIR = ROOT_DIR + File.separator + "face" + File.separator + "register" + File.separator + "imgs";
    /**
     * 存放特征的目录
     */
    public static final String SAVE_FEATURE_DIR = ROOT_DIR + File.separator + "face" + File.separator + "register" + File.separator + "features";


    public FileUtil() {
    }

    public static FileUtil getInstance() {
        return FileUtilHolder.instance;
    }

    private static class FileUtilHolder {
        private static final FileUtil instance = new FileUtil();
    }

    /**
     * 将文件流转为本地图片
     */
    public void savePhoto(String filePath, InputStream inputStream) {
        synchronized (this) {
            if (filePath == null || inputStream == null) {
                return;
            }

            Log.i(TAG, "savePhoto: filePath = " + filePath);

            File file = new File(filePath);

            try {
                if (file.getParentFile() != null && !file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    Log.e(TAG, "savePhoto: file mkdirs fail");
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "create file", e);
                return;
            }

            try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file))) {
                int bytesRead;
                byte[] buffer = new byte[1024];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    bufferedOutputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                Log.e(TAG, "save file", e);
            }
        }
    }
}
