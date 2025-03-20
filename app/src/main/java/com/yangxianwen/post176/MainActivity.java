package com.yangxianwen.post176;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.yangxianwen.post176.base.BaseMvvmActivity;
import com.yangxianwen.post176.databinding.DisplayMainBinding;
import com.yangxianwen.post176.face.FaceManageActivity;
import com.yangxianwen.post176.utils.ActiveUtil;
import com.yangxianwen.post176.utils.SpUtil;
import com.yangxianwen.post176.viewmodel.MainViewModel;

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

    @Override
    protected int getLayoutId() {
        return R.layout.display_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initObserve();

        activeEngine();

        syncStudentInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBinding.deviceNumber.setText(String.format("本机编号：%s", SpUtil.getDeviceNumber()));
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

    @Override
    public boolean needHideNavigationBar() {
        return true;
    }

    private void initObserve() {
        mLiveDataManager.observeForever(mViewModel.getStudentSize(), integer -> {
            if (integer == null) {
                return;
            }
            updateLoading(integer, 0);
        });

        mLiveDataManager.observeForever(mViewModel.getStudentProgress(), integer -> {
            if (integer == null) {
                return;
            }
            updateLoading(-1, integer);
        });

        mLiveDataManager.observeForever(mViewModel.getSyncFinish(), o -> {
            if (o == null) {
                return;
            }
            startActivity(new Intent(MainActivity.this, OrderActivity.class));
        });
    }

    private void activeEngine() {
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

    private void syncStudentInfo() {
        showLoading("正在同步学生信息...");
        mViewModel.syncStudentInfo();
    }

    public void onStart(View view) {
        if (!activeEngineSuccess) {
            showToast("正在激活系统，请稍候...");
            return;
        }
        //同步学生信息
        showLoading("正在同步运动数据...");
        mViewModel.syncStudentNfc();
    }

    public void onUpdate(View view) {
        showUpdateLoading("更新中，请稍候...", "正在获取进度...", dialog -> {
            //取消下载
            mViewModel.cancelDownload();
        });
        //注册学生图片
        mViewModel.registerStatusPic();
    }

    public void onSetting(View view) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    public void onRegister(View view) {
        startActivity(new Intent(this, FaceManageActivity.class));
    }

    public void onExit(View view) {
        finish();
    }
}