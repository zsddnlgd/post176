package com.yangxianwen.post176;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.yangxianwen.post176.base.BaseMvvmActivity;
import com.yangxianwen.post176.databinding.DisplayMainBinding;
import com.yangxianwen.post176.face.FaceManageActivity;
import com.yangxianwen.post176.utils.ActiveUtil;
import com.yangxianwen.post176.viewmodel.MainViewModel;
import com.yangxianwen.post176.widget.ProgressDialog;

public class MainActivity extends BaseMvvmActivity<MainViewModel, DisplayMainBinding> {

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    // 在线激活所需的权限
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE
    };

    private boolean activeEngineSuccess = false;

    private ProgressDialog progressDialog;

    @Override
    protected int getLayoutId() {
        return R.layout.display_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLiveDataManager.observeForever(mViewModel.getStudentSize(), integer -> {
            if (progressDialog != null) {
                progressDialog.setMaxProgress(integer);
                progressDialog.refreshProgress(0);
            }
        });

        mLiveDataManager.observeForever(mViewModel.getStudentProgress(), integer -> {
            if (progressDialog != null) {
                progressDialog.refreshProgress(integer);
            }
        });

        mLiveDataManager.observeForever(mViewModel.getStudentFinish(), aBoolean -> {
            if (aBoolean) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            }
        });

        if (checkPermissions(NEEDED_PERMISSIONS)) {
            ActiveUtil.activeEngine(this, success -> {
                if (success) {
                    activeEngineSuccess = true;
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void afterRequestPermission(int requestCode, boolean isAllGranted) {
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            if (isAllGranted) {
                ActiveUtil.activeEngine(this, success -> {
                    if (success) {
                        activeEngineSuccess = true;
                    }
                });
            } else {
                showToast(getString(R.string.permission_denied));
            }
        }
    }

    public void onStart(View view) {
        if (!activeEngineSuccess) {
            showToast("正在激活系统，请稍候...");
            return;
        }
        startActivity(new Intent(this, OrderActivity.class));
    }

    public void onUpdate(View view) {
        progressDialog = new ProgressDialog(this, ProgressDialog.update);
        progressDialog.setTitleText("更新中，请稍候...");
        progressDialog.setContentText("正在获取进度...");
        progressDialog.show();

        mViewModel.registerStatusPic();
    }

    public void onRegister(View view) {
        startActivity(new Intent(this, FaceManageActivity.class));
    }

    public void onExit(View view) {
        finish();
    }

    private String getSavedDeviceId() {
        SharedPreferences settings = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        return "本机编号：" + settings.getString("device_id", "未设置");
    }
}