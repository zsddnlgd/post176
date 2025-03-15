package com.yangxianwen.post176.utils;

import android.provider.Settings;

import com.yangxianwen.post176.App;

public class GlobalUtil {

    public static void putFloat(String name, float value) {
        Settings.Global.putFloat(App.getIns().getContentResolver(), name, value);
    }

    public static void putInt(String name, int value) {
        Settings.Global.putInt(App.getIns().getContentResolver(), name, value);
    }

    public static void putLong(String name, long value) {
        Settings.Global.putLong(App.getIns().getContentResolver(), name, value);
    }

    public static void putString(String name, String value) {
        Settings.Global.putString(App.getIns().getContentResolver(), name, value);
    }

    public static float getFloat(String name) {
        return Settings.Global.getFloat(App.getIns().getContentResolver(), name, 0);
    }

    public static int getInt(String name) {
        return Settings.Global.getInt(App.getIns().getContentResolver(), name, 0);
    }

    public static float getLong(String name) {
        return Settings.Global.getLong(App.getIns().getContentResolver(), name, 0);
    }

    public static String getString(String name) {
        return Settings.Global.getString(App.getIns().getContentResolver(), name);
    }
}
