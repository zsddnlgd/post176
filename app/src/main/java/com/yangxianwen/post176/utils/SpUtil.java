package com.yangxianwen.post176.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.reflect.TypeToken;
import com.yangxianwen.post176.App;
import com.yangxianwen.post176.bean.Student;

public class SpUtil {

    public static void putStudent(String key, Student student) {
        SharedPreferences sp = App.getIns().getSharedPreferences("StudentInfo", Context.MODE_PRIVATE);
        sp.edit().putString(key, GsonUtil.objToJson(student)).apply();
    }

    public static Student getStudent(String key) {
        SharedPreferences sp = App.getIns().getSharedPreferences("StudentInfo", Context.MODE_PRIVATE);
        String json = sp.getString(key, null);
        if (json == null) {
            return null;
        }
        return GsonUtil.getGson().fromJson(json, new TypeToken<Student>() {
        }.getType());
    }
}
