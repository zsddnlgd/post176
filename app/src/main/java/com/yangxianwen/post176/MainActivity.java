package com.yangxianwen.post176;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.yangxianwen.post176.base.BaseMvvmActivity;
import com.yangxianwen.post176.bean.Turnover;
import com.yangxianwen.post176.databinding.ActivityMainBinding;
import com.yangxianwen.post176.face.FaceManageActivity;
import com.yangxianwen.post176.service.UploadFailOrderService;
import com.yangxianwen.post176.utils.ActiveUtil;
import com.yangxianwen.post176.utils.SpUtil;
import com.yangxianwen.post176.viewmodel.MainViewModel;

public class MainActivity extends BaseMvvmActivity<MainViewModel, ActivityMainBinding> {

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    // 在线激活所需的权限
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    //是否激活人脸识别引擎
    private boolean activeEngineSuccess = false;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
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
    protected void onDestroy() {
        super.onDestroy();
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

    private void startService() {
        startService(new Intent(this, UploadFailOrderService.class));
    }

    private void stopService() {
        stopService(new Intent(this, UploadFailOrderService.class));
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
        showUpdateLoading("正在更新", "获取进度...", dialog -> {
            //取消下载
            mViewModel.cancelDownload();
        });
        //注册学生图片
        mViewModel.registerStatusPic();
    }

    public void onTurnover(View view) {
        //营业额
        Turnover turnover = SpUtil.getTurnover();
        if (turnover == null) {
            showLongToast("订单数量：0个，营业额：0元");
        } else {
            showLongToast("订单数量：" + turnover.getOrderNumber() + "个，营业额：" + turnover.getOrderPrice() + "元");
        }
    }

    public void onUploadOrder(View view) {
        //上传订单
        showLoading("正在上传订单...");
        mViewModel.uploadOrder();
    }

    public void onSetting(View view) {
        //设置
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    public void onRegister(View view) {
        //注册人脸
        startActivity(new Intent(this, FaceManageActivity.class));
    }

    public void onExit(View view) {
        finish();
    }
}