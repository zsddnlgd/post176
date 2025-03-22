package com.yangxianwen.post176.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.reflect.TypeToken;
import com.yangxianwen.post176.App;
import com.yangxianwen.post176.bean.Meal;
import com.yangxianwen.post176.bean.Nfc;
import com.yangxianwen.post176.bean.Order;
import com.yangxianwen.post176.bean.Student;
import com.yangxianwen.post176.bean.StudentSports;
import com.yangxianwen.post176.bean.Turnover;
import com.yangxianwen.post176.values.Urls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class SpUtil {

    private static final String device = "DeviceInfo";
    private static final String deviceNumberKey = "deviceId";
    private static final String ipAddressKey = "ipAddress";
    private static final String livenessDetectKey = "livenessDetect";
    private static final String order = "OrderInfo";
    private static final String orderKey = "orderList";
    private static final String student = "StudentInfo";
    private static final String studentKey = "studentList";
    private static final String studentSportsKey = "studentSportsList";
    private static final String meal = "MealInfo";
    private static final String mealKey = "mealList";
    private static final String turnover = "TurnoverInfo";

    public static void setTurnover(double price) {
        SharedPreferences sp = App.getIns().getSharedPreferences(turnover, Context.MODE_PRIVATE);
        String key = TimeUtil.getDate();
        String json = sp.getString(key, null);
        Turnover turnover;
        if (json == null) {
            turnover = new Turnover();
        } else {
            turnover = GsonUtil.getGson().fromJson(json, new TypeToken<Turnover>() {
            }.getType());
        }
        turnover.setOrderNumber(turnover.getOrderNumber() + 1);
        turnover.setOrderPrice(turnover.getOrderPrice() + price);
        sp.edit().putString(key, GsonUtil.getGson().toJson(turnover)).apply();
    }

    public static Turnover getTurnover() {
        SharedPreferences sp = App.getIns().getSharedPreferences(turnover, Context.MODE_PRIVATE);
        String key = TimeUtil.getDate();
        String json = sp.getString(key, null);
        if (json == null) {
            return null;
        }
        return GsonUtil.getGson().fromJson(json, new TypeToken<Turnover>() {
        }.getType());
    }

    public static void putDeviceNumber(int value) {
        SharedPreferences sp = App.getIns().getSharedPreferences(device, Context.MODE_PRIVATE);
        sp.edit().putInt(deviceNumberKey, value).apply();
    }

    public static int getDeviceNumber() {
        SharedPreferences sp = App.getIns().getSharedPreferences(device, Context.MODE_PRIVATE);
        return sp.getInt(deviceNumberKey, 11);
    }

    public static void putIpAddress(String value) {
        SharedPreferences sp = App.getIns().getSharedPreferences(device, Context.MODE_PRIVATE);
        sp.edit().putString(ipAddressKey, value).apply();
    }

    public static String getIpAddress() {
        SharedPreferences sp = App.getIns().getSharedPreferences(device, Context.MODE_PRIVATE);
        return sp.getString(ipAddressKey, Urls.main);
    }

    public static void putLivenessDetect(boolean value) {
        SharedPreferences sp = App.getIns().getSharedPreferences(device, Context.MODE_PRIVATE);
        sp.edit().putBoolean(livenessDetectKey, value).apply();
    }

    public static boolean getLivenessDetect() {
        SharedPreferences sp = App.getIns().getSharedPreferences(device, Context.MODE_PRIVATE);
        return sp.getBoolean(livenessDetectKey, true);
    }

    public static void putStudentSports(ArrayList<StudentSports> sports) {
        SharedPreferences sp = App.getIns().getSharedPreferences(student, Context.MODE_PRIVATE);
        sp.edit().putString(studentSportsKey, GsonUtil.objToJson(sports)).apply();
    }

    public static StudentSports getStudentSportsByCode(String key) {
        SharedPreferences sp = App.getIns().getSharedPreferences(student, Context.MODE_PRIVATE);
        String json = sp.getString(studentSportsKey, null);
        if (json == null) {
            return null;
        }
        ArrayList<StudentSports> sports = GsonUtil.getGson().fromJson(json, new TypeToken<ArrayList<StudentSports>>() {
        }.getType());
        for (StudentSports sport : sports) {
            if (Objects.equals(sport.getCStudCode(), key)) {
                return sport;
            }
        }
        return null;
    }

    public static void putStudent(ArrayList<Student> students) {
        SharedPreferences sp = App.getIns().getSharedPreferences(student, Context.MODE_PRIVATE);
        sp.edit().putString(studentKey, GsonUtil.objToJson(students)).apply();
    }

    public static Student getStudentByPic(String key) {
        SharedPreferences sp = App.getIns().getSharedPreferences(student, Context.MODE_PRIVATE);
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

    public static void setStudentNfc(Student student) {
        if (student == null) {
            return;
        }
        SharedPreferences sp = App.getIns().getSharedPreferences("StudentInfo", Context.MODE_PRIVATE);
        String json = sp.getString(studentKey, null);
        if (json == null) {
            return;
        }
        ArrayList<Student> students = GsonUtil.getGson().fromJson(json, new TypeToken<ArrayList<Student>>() {
        }.getType());
        for (Student tempStudent : students) {
            if (Objects.equals(student.getCStudCode(), tempStudent.getCStudCode())) {
                tempStudent.setNfcId(student.getNfcId());
                break;
            }
        }
        putStudent(students);
    }

    public static void setStudentNfc(ArrayList<Nfc> nfcs) {
        if (nfcs == null) {
            return;
        }
        SharedPreferences sp = App.getIns().getSharedPreferences("StudentInfo", Context.MODE_PRIVATE);
        String json = sp.getString(studentKey, null);
        if (json == null) {
            return;
        }
        ArrayList<Student> students = GsonUtil.getGson().fromJson(json, new TypeToken<ArrayList<Student>>() {
        }.getType());
        for (Student tempStudent : students) {
            for (Nfc nfc : nfcs) {
                if (Objects.equals(nfc.getCStudCode(), tempStudent.getCStudCode())) {
                    tempStudent.setNfcId(nfc.getNfcId());
                    break;
                }
            }
        }
        putStudent(students);
    }

    public static void setStudentBalance(Student student) {
        if (student == null) {
            return;
        }
        SharedPreferences sp = App.getIns().getSharedPreferences("StudentInfo", Context.MODE_PRIVATE);
        String json = sp.getString(studentKey, null);
        if (json == null) {
            return;
        }
        ArrayList<Student> students = GsonUtil.getGson().fromJson(json, new TypeToken<ArrayList<Student>>() {
        }.getType());
        for (Student tempStudent : students) {
            if (Objects.equals(student.getCStudCode(), tempStudent.getCStudCode())) {
                tempStudent.setNBalance(student.getNBalance());
                break;
            }
        }
        putStudent(students);
    }

    public static void putMeal(ArrayList<Meal> meals) {
        SharedPreferences sp = App.getIns().getSharedPreferences(meal, Context.MODE_PRIVATE);
        sp.edit().putString(mealKey, GsonUtil.objToJson(meals)).apply();
    }

    public static ArrayList<Meal> getMeal() {
        SharedPreferences sp = App.getIns().getSharedPreferences(meal, Context.MODE_PRIVATE);
        String json = sp.getString(mealKey, null);
        if (json == null) {
            return null;
        }
        return GsonUtil.getGson().fromJson(json, new TypeToken<ArrayList<Meal>>() {
        }.getType());
    }

    public static void putOrders(ArrayList<Order> orders) {
        if (orders.isEmpty()) {
            return;
        }
        SharedPreferences sp = App.getIns().getSharedPreferences(order, Context.MODE_PRIVATE);
        sp.edit().putString(orderKey + "_" + orders.get(0).getCBillCode(), GsonUtil.getGson().toJson(orders)).apply();
    }

    public static ArrayList<Order> getOrders() {
        SharedPreferences sp = App.getIns().getSharedPreferences(order, Context.MODE_PRIVATE);
        ArrayList<Order> orders = new ArrayList<>();
        if (sp.getAll() == null) {
            return orders;
        }
        HashMap<String, ?> map = (HashMap<String, ?>) sp.getAll();
        for (String key : map.keySet()) {
            ArrayList<Order> list = GsonUtil.getGson().fromJson((String) map.get(key), new TypeToken<ArrayList<Order>>() {
            }.getType());
            if (list != null) {
                orders.addAll(list);
            }
        }
        return orders;
    }

    public static void clearOrders() {
        SharedPreferences sp = App.getIns().getSharedPreferences(order, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }
}
