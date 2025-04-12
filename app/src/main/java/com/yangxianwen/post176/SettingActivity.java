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

        mBinding.deviceNumber.setText(String.valueOf(SpUtil.getDeviceNumber()));
        mBinding.ipAddress.setText(SpUtil.getIpAddress());
        mBinding.adminPassword.setText(SpUtil.getAdminPassword());
        mBinding.livenessDetect.setChecked(SpUtil.getLivenessDetect());

        mBinding.cancelButton.setOnClickListener(v -> finish());
        mBinding.confirmButton.setOnClickListener(v -> {
            int number;
            try {
                number = Integer.parseInt(mBinding.deviceNumber.getText().toString());
            } catch (NumberFormatException ignored) {
                showToast("本机编号必须为数字格式！");
                return;
            }
            SpUtil.putDeviceNumber(number);
            SpUtil.putIpAddress(mBinding.ipAddress.getText().toString());
            SpUtil.putAdminPassword(mBinding.adminPassword.getText().toString());
            SpUtil.putLivenessDetect(mBinding.livenessDetect.isChecked());
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
