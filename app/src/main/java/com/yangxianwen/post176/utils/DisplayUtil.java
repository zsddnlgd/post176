package com.yangxianwen.post176.utils;

import android.graphics.Rect;

public class DisplayUtil {

    public static final float density = 2.0f;
    public static final float scaledDensity = 2.0f;
    public static final int densityDpi = 320;

    public static final float widthMargin = 0.2f;
    public static final float heightMargin = 0.2f;

    public static Rect getFaceIdentifyRect(int width, int height) {
        int left = (int) (width * DisplayUtil.widthMargin);
        int top = (int) (height * DisplayUtil.heightMargin);
        int right = width - left;
        int bottom = height - top;
        return new Rect(left, top, right, bottom);
    }

    /**
     * 将dp转换为px
     *
     * @param dpValue dp值
     * @return px值
     */
    public static int dpToPx(float dpValue) {
        return (int) (dpValue * density + 0.5f);
    }
}
