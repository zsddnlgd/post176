package com.yangxianwen.post176.base;

import android.content.Context;
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


public abstract class BaseActivity extends AppCompatActivity {

    protected final String TAG = getClass().getName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        displayMetrics.density = 2.0f;
        displayMetrics.scaledDensity = 2.0f;
        displayMetrics.densityDpi = 320;
        //保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //锁定为启动时的方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        //隐藏状态栏
        NavigationBarUtil.hideNavigationBar(getWindow());
    }

    @Override
    protected void onResume() {
        super.onResume();
        //隐藏导航栏
        NavigationBarUtil.hideNavigationBar(getWindow());
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

    protected void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    protected void showLongToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
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
