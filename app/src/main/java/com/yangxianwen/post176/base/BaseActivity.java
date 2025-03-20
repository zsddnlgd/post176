package com.yangxianwen.post176.base;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.yangxianwen.post176.utils.NavigationBarUtil;
import com.yangxianwen.post176.widget.ProgressDialog;


public abstract class BaseActivity extends AppCompatActivity {

    protected final String TAG = getClass().getName();

    private ProgressDialog mProgressDialog;
    private ProgressDialog mProgressUpdateDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //修改主屏密度与副屏一致
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        displayMetrics.density = 2.0f;
        displayMetrics.scaledDensity = 2.0f;
        displayMetrics.densityDpi = 320;
        //保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //锁定为启动时的方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //隐藏导航栏
        if (needHideNavigationBar()) {
            NavigationBarUtil.hideNavigationBar(getWindow());
        }
    }

    /**
     * 权限检查
     *
     * @param neededPermissions 需要的权限
     * @return 是否全部被允许
     */
    protected boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isAllGranted = true;
        for (int grantResult : grantResults) {
            isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
        }
        afterRequestPermission(requestCode, isAllGranted);
    }

    /**
     * 请求权限的回调
     *
     * @param requestCode  请求码
     * @param isAllGranted 是否全部被同意
     */
    public abstract void afterRequestPermission(int requestCode, boolean isAllGranted);

    public abstract boolean needHideNavigationBar();

    protected void showToast(String s) {
        if (s == null) {
            s = "null";
        }
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    protected void showLongToast(String s) {
        if (s == null) {
            s = "null";
        }
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    protected void showLoading(String text) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity(), ProgressDialog.loading);
        }
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog.setContentText(text);
        mProgressDialog.show();
    }

    protected void dismissLoading() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    protected void showUpdateLoading(String title, String content, DialogInterface.OnDismissListener listener) {
        if (mProgressUpdateDialog == null) {
            mProgressUpdateDialog = new ProgressDialog(this, ProgressDialog.update);
        }
        if (mProgressUpdateDialog.isShowing()) {
            mProgressUpdateDialog.dismiss();
        }
        mProgressUpdateDialog.setProgress(0);
        mProgressUpdateDialog.setTitleText(title);
        mProgressUpdateDialog.setContentText(content);
        mProgressUpdateDialog.setOnDismissListener(listener);
        mProgressUpdateDialog.show();
    }

    protected void updateLoading(int max, int progress) {
        if (mProgressUpdateDialog != null && mProgressUpdateDialog.isShowing()) {
            if (max > 0) {
                mProgressUpdateDialog.setMaxProgress(max);
            }
            if (progress >= 0) {
                mProgressUpdateDialog.refreshProgress(progress);
            }
        }
    }

    protected void dismissUpdateLoading() {
        if (mProgressUpdateDialog != null && mProgressUpdateDialog.isShowing()) {
            mProgressUpdateDialog.dismiss();
        }
    }

    protected AppCompatActivity getActivity() {
        return this;
    }

    public Display getPresentationDisplays() {
        DisplayManager mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = mDisplayManager.getDisplays();
        for (Display display : displays) {
            if ((display.getFlags() & Display.FLAG_SECURE) != 0
                    && (display.getFlags() & Display.FLAG_SUPPORTS_PROTECTED_BUFFERS) != 0
                    && (display.getFlags() & Display.FLAG_PRESENTATION) != 0) {
                return display;
            }
        }
        return null;
    }
}
