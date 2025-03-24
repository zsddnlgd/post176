package com.yangxianwen.post176.utils;


public class DoubleClickUtil {

    private DoubleClickUtil() {
    }

    private static long lastClick = 0;

    public static boolean isDoubleClick() {
        long nowTime = System.currentTimeMillis();
        long diffClick = nowTime - lastClick;
        boolean result = Math.abs(diffClick) < 200;
        if (!result) {
            lastClick = nowTime;
        }
        return result;
    }
}
