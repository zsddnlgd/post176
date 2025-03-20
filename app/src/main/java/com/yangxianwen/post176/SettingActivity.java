package com.yangxianwen.post176;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.yangxianwen.post176.base.BaseMvvmActivity;
import com.yangxianwen.post176.databinding.ActivitySettingBinding;
import com.yangxianwen.post176.utils.SpUtil;
import com.yangxianwen.post176.viewmodel.SettingViewModel;

public class SettingActivity extends BaseMvvmActivity<SettingViewModel, ActivitySettingBinding> {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding.deviceNumber.setText(SpUtil.getDeviceNumber());
        mBinding.ipAddress.setText(SpUtil.getIpAddress());

        mBinding.cancelButton.setOnClickListener(v -> finish());
        mBinding.confirmButton.setOnClickListener(v -> {
            SpUtil.putDeviceNumber(mBinding.deviceNumber.getText().toString());
            SpUtil.putIpAddress(mBinding.ipAddress.getText().toString());
            finish();
        });

    }

    @Override
    public void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }

    @Override
    public boolean needHideNavigationBar() {
        return false;
    }
}
