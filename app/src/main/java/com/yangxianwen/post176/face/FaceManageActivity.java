package com.yangxianwen.post176.face;

import android.Manifest;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.yangxianwen.post176.R;
import com.yangxianwen.post176.base.BaseActivity;
import com.yangxianwen.post176.face.faceserver.FaceServer;
import com.yangxianwen.post176.utils.FileUtil;
import com.yangxianwen.post176.utils.NavigationBarUtil;
import com.yangxianwen.post176.utils.SpUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 批量注册页面
 */
public class FaceManageActivity extends BaseActivity {

    private final int ACTION_REQUEST_PERMISSIONS = 0x001;
    private final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private ExecutorService executorService;

    private TextView tvNotificationRegisterResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_manage);
        //本地人脸库初始化
        FaceServer.getInstance().init(this);
        executorService = Executors.newSingleThreadExecutor();
        tvNotificationRegisterResult = findViewById(R.id.notification_register_result);
    }

    @Override
    protected void onDestroy() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        FaceServer.getInstance().unInit();
        super.onDestroy();
    }

    public void batchRegister(View view) {
        if (checkPermissions(NEEDED_PERMISSIONS)) {
            doRegister();
        } else {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        }
    }

    private void doRegister() {
        File dir = new File(FileUtil.REGISTER_DIR);
        if (!dir.exists()) {
            showToast(getString(R.string.batch_process_path_is_not_exists, FileUtil.REGISTER_DIR));
            return;
        }
        if (!dir.isDirectory()) {
            showToast(getString(R.string.batch_process_path_is_not_dir, FileUtil.REGISTER_DIR));
            return;
        }
        String[] imgFileDir = new File(FileUtil.SAVE_IMG_DIR).list();
        List<String> imgFiles = imgFileDir == null ? new ArrayList<>() : Arrays.asList(imgFileDir);
        final File[] jpgFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !imgFiles.contains(name) && name.endsWith(FaceServer.IMG_SUFFIX);
            }
        });
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final int totalCount = jpgFiles.length;

                int successCount = 0;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showUpdateLoading("正在注册", "获取进度", totalCount);
                        tvNotificationRegisterResult.setText("");
                        tvNotificationRegisterResult.append(getString(R.string.batch_process_processing_please_wait));
                    }
                });
                for (int i = 0; i < totalCount; i++) {
                    final int finalI = i;
                    runOnUiThread(() -> {
                        updateLoading(-1, finalI);
                    });
                    final File jpgFile = jpgFiles[i];
                    Bitmap bitmap = BitmapFactory.decodeFile(jpgFile.getAbsolutePath());
                    if (bitmap == null) {
                        File failedFile = new File(FileUtil.REGISTER_FAILED_DIR + File.separator + jpgFile.getName());
                        if (!failedFile.getParentFile().exists()) {
                            failedFile.getParentFile().mkdirs();
                        }
                        jpgFile.renameTo(failedFile);
                        continue;
                    }
                    bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
                    if (bitmap == null) {
                        File failedFile = new File(FileUtil.REGISTER_FAILED_DIR + File.separator + jpgFile.getName());
                        if (!failedFile.getParentFile().exists()) {
                            failedFile.getParentFile().mkdirs();
                        }
                        jpgFile.renameTo(failedFile);
                        continue;
                    }
                    byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
                    int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
                    if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissUpdateLoading();
                                tvNotificationRegisterResult.append("");
                            }
                        });
                        return;
                    }
                    boolean success = FaceServer.getInstance().registerBgr24(FaceManageActivity.this, bgr24, bitmap.getWidth(), bitmap.getHeight(),
                            jpgFile.getName().substring(0, jpgFile.getName().lastIndexOf(".")));
                    if (!success) {
                        File failedFile = new File(FileUtil.REGISTER_FAILED_DIR + File.separator + jpgFile.getName());
                        if (!failedFile.getParentFile().exists()) {
                            failedFile.getParentFile().mkdirs();
                        }
                        jpgFile.renameTo(failedFile);
                    } else {
                        successCount++;
                    }
                }
                final int finalSuccessCount = successCount;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissUpdateLoading();
                        tvNotificationRegisterResult.append(getString(R.string.batch_process_finished_info, totalCount, finalSuccessCount, totalCount - finalSuccessCount, FileUtil.REGISTER_FAILED_DIR));
                    }
                });
                Log.i(FaceManageActivity.class.getSimpleName(), "run: " + executorService.isShutdown());
            }
        });
    }

    @Override
    public void afterRequestPermission(int requestCode, boolean isAllGranted) {
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            if (isAllGranted) {
                doRegister();
            } else {
                showToast(getString(R.string.permission_denied));
            }
        }
    }

    @Override
    public boolean needHideNavigationBar() {
        return true;
    }

    public void clearFaces(View view) {
        EditText editText = (EditText) LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit, new FrameLayout(getActivity()), false);
        AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("请输入管理员密码")
                .setView(editText)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    if (Objects.equals(SpUtil.getAdminPassword(), editText.getText().toString())) {
                        //清空人脸
                        clearFaces();
                    } else {
                        showToast("密码错误");
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(false)
                .create();
        NavigationBarUtil.hideNavigationBar(alertDialog.getWindow());
        alertDialog.show();
    }

    private void clearFaces() {
        int faceNum = FaceServer.getInstance().getFaceNumber();
        if (faceNum == 0) {
            showToast(getString(R.string.batch_process_no_face_need_to_delete));
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                    .setTitle(R.string.batch_process_notification)
                    .setMessage(getString(R.string.batch_process_confirm_delete, faceNum))
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        int deleteCount = FaceServer.getInstance().clearAllFaces(FaceManageActivity.this);
                        showToast(deleteCount + "张人脸被清除");
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .setCancelable(false)
                    .create();
            NavigationBarUtil.hideNavigationBar(alertDialog.getWindow());
            alertDialog.show();
        }
    }

    public void exit(View view) {
        finish();
    }
}
