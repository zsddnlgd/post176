package com.yangxianwen.post176.utils;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.Display;

import androidx.annotation.NonNull;

public class DisplayUtil {

    //基础dp
    private static final float baseDp = 1336f;

    private static float sNonCompatDensity;
    private static float sNonCompatScaledDensity;

    /**
     * 修改设备密度
     */
    public static void setCustomDensity(DisplayMetrics displayMetrics, final Application application) {
        if (displayMetrics == null || application == null) return;

        DisplayMetrics appDisplayMetrics = application.getResources().getDisplayMetrics();
        if (sNonCompatDensity == 0) {
            sNonCompatDensity = appDisplayMetrics.density;
            sNonCompatScaledDensity = appDisplayMetrics.scaledDensity;
            // 防止系统切换后不起作用
            application.registerComponentCallbacks(new ComponentCallbacks() {
                @Override
                public void onConfigurationChanged(@NonNull Configuration newConfig) {
                    if (newConfig.fontScale > 0) {
                        sNonCompatScaledDensity = application.getResources().getDisplayMetrics().scaledDensity;
                    }
                }

                @Override
                public void onLowMemory() {

                }
            });
        }
        float targetDensity = appDisplayMetrics.widthPixels / baseDp;
        // 防止字体变小
        float targetScaleDensity = targetDensity * (sNonCompatScaledDensity / sNonCompatDensity);
        int targetDensityDpi = (int) (160 * targetDensity);

        appDisplayMetrics.density = targetDensity;
        appDisplayMetrics.scaledDensity = targetScaleDensity;
        appDisplayMetrics.densityDpi = targetDensityDpi;

        displayMetrics.density = targetDensity;
        displayMetrics.scaledDensity = targetScaleDensity;
        displayMetrics.densityDpi = targetDensityDpi;
    }
}
