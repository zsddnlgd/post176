package com.yangxianwen.post176.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class GsonUtil {

    private static class GsonHolder {
        private static final Gson INSTANCE = new Gson();
    }

    /**
     * 获取Gson实例，由于Gson是线程安全的，这里共同使用同一个Gson实例
     */
    public static Gson getGson() {
        return GsonHolder.INSTANCE;
    }

    public static <T> T jsonToObj(String json) {
        if (json == null) return null;
        return getGson().fromJson(json, new TypeToken<T>() {
        }.getType());
    }

    public static String objToJson(Object obj) {
        if (obj == null) return null;
        return getGson().toJson(obj);
    }
}
