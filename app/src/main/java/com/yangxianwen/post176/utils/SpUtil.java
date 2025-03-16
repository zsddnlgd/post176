package com.yangxianwen.post176.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.reflect.TypeToken;
import com.yangxianwen.post176.App;
import com.yangxianwen.post176.bean.Student;

import java.util.ArrayList;
import java.util.Objects;

public class SpUtil {

    private static final String studentKey = "studentList";

    public static void putStudent(ArrayList<Student> students) {
        SharedPreferences sp = App.getIns().getSharedPreferences("StudentInfo", Context.MODE_PRIVATE);
        sp.edit().putString(studentKey, GsonUtil.objToJson(students)).apply();
    }

    public static Student getStudentByPic(String key) {
        SharedPreferences sp = App.getIns().getSharedPreferences("StudentInfo", Context.MODE_PRIVATE);
        String json = sp.getString(studentKey, null);
        if (json == null) {
            return null;
        }
        ArrayList<Student> students = GsonUtil.getGson().fromJson(json, new TypeToken<ArrayList<Student>>() {
        }.getType());
        for (Student student : students) {
            if (Objects.equals(student.getCPic(), key)) {
                return student;
            }
        }
        return null;
    }

    public static Student getStudentByNfc(String key) {
        SharedPreferences sp = App.getIns().getSharedPreferences("StudentInfo", Context.MODE_PRIVATE);
        String json = sp.getString(studentKey, null);
        if (json == null) {
            return null;
        }
        ArrayList<Student> students = GsonUtil.getGson().fromJson(json, new TypeToken<ArrayList<Student>>() {
        }.getType());
        for (Student student : students) {
            if (Objects.equals(student.getNfcId(), key)) {
                return student;
            }
        }
        return null;
    }
}
