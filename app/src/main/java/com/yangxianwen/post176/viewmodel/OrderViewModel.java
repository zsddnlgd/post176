package com.yangxianwen.post176.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.yangxianwen.post176.base.BaseViewModel;
import com.yangxianwen.post176.bean.Meal;
import com.yangxianwen.post176.bean.Student;
import com.yangxianwen.post176.enmu.OrderStatus;
import com.yangxianwen.post176.utils.HttpUtil;
import com.yangxianwen.post176.utils.SpUtil;
import com.yangxianwen.post176.utils.TimeUtil;

import java.util.ArrayList;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class OrderViewModel extends BaseViewModel {

    private final MutableLiveData<OrderStatus> orderStatus = new MutableLiveData<>(OrderStatus.identify);
    private final MutableLiveData<Object> finish = new MutableLiveData<>();
    private final MutableLiveData<String> name = new MutableLiveData<>();
    private final MutableLiveData<String> className = new MutableLiveData<>();
    private final MutableLiveData<String> balance = new MutableLiveData<>();
    private final MutableLiveData<String> stepNumber = new MutableLiveData<>();
    private final MutableLiveData<String> distance = new MutableLiveData<>();
    private final MutableLiveData<String> calorie = new MutableLiveData<>();
    private final MutableLiveData<String> nfcNumber = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Meal>> mealList = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Meal>> mealSelectList = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Meal>> mealEmptyList = new MutableLiveData<>();

    public OrderViewModel(@NonNull Application application) {
        super(application);
    }

    public void setStatusInfo(String key) {
        Student student = SpUtil.getStudentByPic("/Pic/StuImg/" + key + ".jpg");
        setStatusInfo(student);
    }

    public void setStatusInfo(Student student) {
        name.postValue("姓名：" + (student != null ? student.getCStudName() : ""));
        className.postValue("班级：" + (student != null ? student.getCClass() : ""));
        balance.postValue("余额：" + (student != null ? student.getNBalance() : ""));
        nfcNumber.postValue("卡号：" + (student != null ? student.getNfcId() : ""));
    }

    public void clearStatusInfo() {
        name.postValue("姓名：");
        className.postValue("班级：");
        balance.postValue("余额：");
        nfcNumber.postValue("卡号：");
    }

    public void getMeal() {
        HttpUtil.getMeal(new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ArrayList<Meal> meals) {
                loading.setValue(false);

                ArrayList<Meal> subList = new ArrayList<>();
                for (Meal meal : meals) {
                    if (TimeUtil.inTime(meal.getCStartTime(), meal.getCEndTime())) {
                        subList.add(meal);
                    }
                }
                if (subList.isEmpty()) {
                    tips.postValue("当前不在开餐时间");
                    finish.postValue(new Object());
                    return;
                }
                mealList.postValue(subList);
            }

            @Override
            public void onError(Throwable e) {
                loading.setValue(false);
                tips.setValue("网络异常：" + e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
    }

    public void addMealSelect(Meal meal) {
        ArrayList<Meal> meals = mealSelectList.getValue();
        if (meals == null) {
            meals = new ArrayList<>();
        }
        meals.add(meal);
        mealSelectList.setValue(meals);
    }

    public void removeMealSelect(Meal meal) {
        ArrayList<Meal> meals = mealSelectList.getValue();
        if (meals == null) {
            meals = new ArrayList<>();
        }
        meals.remove(meal);
        mealSelectList.setValue(meals);
    }

    public boolean containsMealSelect(Meal meal) {
        ArrayList<Meal> meals = mealSelectList.getValue();
        if (meals == null) {
            return false;
        }
        return meals.contains(meal);
    }

    public void addMealEmpty(Meal meal) {
        ArrayList<Meal> meals = mealEmptyList.getValue();
        if (meals == null) {
            meals = new ArrayList<>();
        }
        meals.add(meal);
        mealEmptyList.setValue(meals);
    }

    public void removeMealEmpty(Meal meal) {
        ArrayList<Meal> meals = mealEmptyList.getValue();
        if (meals == null) {
            meals = new ArrayList<>();
        }
        meals.remove(meal);
        mealEmptyList.setValue(meals);
    }

    public boolean containsMealEmpty(Meal meal) {
        ArrayList<Meal> meals = mealEmptyList.getValue();
        if (meals == null) {
            return false;
        }
        return meals.contains(meal);
    }

    public MutableLiveData<OrderStatus> getOrderStatus() {
        return orderStatus;
    }

    public MutableLiveData<Object> getFinish() {
        return finish;
    }

    public MutableLiveData<String> getName() {
        return name;
    }

    public MutableLiveData<String> getClassName() {
        return className;
    }

    public MutableLiveData<String> getBalance() {
        return balance;
    }

    public MutableLiveData<String> getStepNumber() {
        return stepNumber;
    }

    public MutableLiveData<String> getDistance() {
        return distance;
    }

    public MutableLiveData<String> getCalorie() {
        return calorie;
    }

    public MutableLiveData<String> getNfcNumber() {
        return nfcNumber;
    }

    public MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    public MutableLiveData<ArrayList<Meal>> getMealList() {
        return mealList;
    }

    public MutableLiveData<ArrayList<Meal>> getMealSelectList() {
        return mealSelectList;
    }

    public MutableLiveData<ArrayList<Meal>> getMealEmptyList() {
        return mealEmptyList;
    }
}
