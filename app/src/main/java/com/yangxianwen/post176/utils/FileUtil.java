package com.yangxianwen.post176.utils;

import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
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

    public static byte[] cropNV21(byte[] input, int inputWidth, int inputHeight, int outputWidth, int outputHeight, int cropX, int cropY) {
        int width = outputWidth;
        int height = outputHeight;
        byte[] output = new byte[width * height * 3 / 2];

        int frameSize = width * height;

        int maxX = cropX + width;
        int maxY = cropY + height;

        for (int y = cropY; y < maxY; y++) {
            int poutputY = (y - cropY) * width;
            int pinputY = y * inputWidth;
            for (int x = cropX; x < maxX; x++) {
                if (x < inputWidth && y < inputHeight) {
                    output[poutputY + (x - cropX)] = input[pinputY + x];
                } else {
                    output[poutputY + (x - cropX)] = 0; // 或者其他边界填充值
                }
            }
        }

        int chromaHeight = height / 2;
        int chromaWidth = width;
        int chromaSize = chromaWidth * chromaHeight;
        int chromaStartY = frameSize; // VU平面开始位置
        int chromaStartU = frameSize + chromaSize; // U平面开始位置
        int chromaStartV = frameSize; // V平面开始位置，因为UV交错存储，此处仅为简化说明，实际上应为UV交错裁剪

        for (int y = 0; y < chromaHeight; y++) {
            int poutputY = (y * chromaWidth); // 由于UV是交错存储，此处应为简化示意，实际应为交错裁剪逻辑
            int pinputY = (y + cropY / 2) * inputWidth / 2; // 注意此处应为UV的输入计算方式，简化示意，实际应为交错裁剪逻辑
            for (int x = 0; x < chromaWidth; x++) {
                if (x < inputWidth / 2 && y < inputHeight / 2) {
                    if (chromaStartV + poutputY + x < output.length - 1 && chromaStartV + pinputY + x < input.length - 1)
                        output[chromaStartV + poutputY + x] = input[chromaStartV + pinputY + x]; // V平面裁剪
                    if (chromaStartU + poutputY + x < output.length - 1 && chromaStartU + pinputY + x < input.length - 1)
                        output[chromaStartU + poutputY + x] = input[chromaStartU + pinputY + x]; // U平面裁剪
                } else {
                    output[chromaStartV + poutputY + x] = (byte) 128; // 或者其他边界填充值，通常使用128作为默认色度值
                    output[chromaStartU + poutputY + x] = (byte) 128; // 同上
                }
            }
        }
        return output;
    }

    public static byte[] cropNV21(byte[] nv21, int x, int y, int cropWidth, int cropHeight) {
        int frameWidth = nv21.length / (cropHeight * 3 / 2); // 原始图像的宽度
        int frameHeight = nv21.length / frameWidth; // 原始图像的高度

        // 检查裁剪区域是否有效
        if (x < 0 || y < 0 || x + cropWidth > frameWidth || y + cropHeight > frameHeight) {
            throw new IllegalArgumentException("Crop rectangle out of bounds");
        }

        // 计算裁剪后NV21数据的大小
        int croppedSize = cropWidth * cropHeight * 3 / 2;
        byte[] croppedNV21 = new byte[croppedSize];

        // 复制Y分量
        System.arraycopy(nv21, y * frameWidth + x, croppedNV21, 0, cropWidth * cropHeight);

        // 计算UV分量的起始位置
        int uvOffset = frameWidth * frameHeight;
        int uvWidth = frameWidth / 2;
        int uvHeight = frameHeight / 2;

        // 复制U和V分量
        for (int j = 0, yp = cropWidth * cropHeight; j < cropHeight / 2; j++) {
            for (int i = 0; i < cropWidth / 2; i++) {
                int uvIndex = uvOffset + (j * uvWidth + i) * 2;
                if (yp > croppedNV21.length - 3 || uvIndex > nv21.length - 2) {
                    continue;
                }
                croppedNV21[yp++] = nv21[uvIndex]; // U
                croppedNV21[yp++] = nv21[uvIndex + 1]; // V
            }
        }

        return croppedNV21;
    }

    /**
     * 裁剪NV21数据
     */
    public static byte[] cropNV21(byte[] nv21Data, int originalWidth, int originalHeight, Rect cropRect) {
        if (nv21Data == null || cropRect == null) {
            throw new IllegalArgumentException("Input data and crop rectangle cannot be null");
        }

        int cropWidth = cropRect.width();
        int cropHeight = cropRect.height();

        if (cropWidth <= 0 || cropHeight <= 0) {
            throw new IllegalArgumentException("Invalid crop dimensions");
        }

        if (cropRect.left < 0 || cropRect.top < 0 || cropRect.right > originalWidth || cropRect.bottom > originalHeight) {
            throw new IllegalArgumentException("Crop rectangle exceeds original image bounds");
        }

        byte[] croppedData = new byte[cropWidth * cropHeight * 3 / 2];

        // Copy Y component
        for (int y = cropRect.top; y < cropRect.bottom; y++) {
            System.arraycopy(nv21Data, y * originalWidth + cropRect.left,
                    croppedData, (y - cropRect.top) * cropWidth,
                    cropWidth);
        }

        // Copy UV component
        int uvRowStride = originalWidth / 2;
        int uvStartIndex = originalWidth * originalHeight;
        int uvOffsetTop = cropRect.top / 2;
        int uvOffsetLeft = cropRect.left / 2;
        int uvCropWidth = cropWidth / 2;
        int uvCropHeight = cropHeight / 2;

        for (int v = uvOffsetTop; v < uvOffsetTop + uvCropHeight; v++) {
            System.arraycopy(nv21Data, uvStartIndex + v * uvRowStride + uvOffsetLeft,
                    croppedData, cropWidth * cropHeight + (v - uvOffsetTop) * uvCropWidth,
                    uvCropWidth * 2);
        }

        return croppedData;
    }

}
