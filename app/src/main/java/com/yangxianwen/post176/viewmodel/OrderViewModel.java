package com.yangxianwen.post176.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.yangxianwen.post176.R;
import com.yangxianwen.post176.base.BaseViewModel;
import com.yangxianwen.post176.bean.Balance;
import com.yangxianwen.post176.bean.Meal;
import com.yangxianwen.post176.bean.Order;
import com.yangxianwen.post176.bean.Result;
import com.yangxianwen.post176.bean.Student;
import com.yangxianwen.post176.bean.StudentSports;
import com.yangxianwen.post176.enmu.OrderStatus;
import com.yangxianwen.post176.utils.GsonUtil;
import com.yangxianwen.post176.utils.HttpUtil;
import com.yangxianwen.post176.utils.SpUtil;
import com.yangxianwen.post176.utils.TimeUtil;
import com.yangxianwen.post176.values.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class OrderViewModel extends BaseViewModel {

    private final MutableLiveData<OrderStatus> orderStatus = new MutableLiveData<>(OrderStatus.identify);
    private final MutableLiveData<Object> orderResult = new MutableLiveData<>();
    private final MutableLiveData<String> finish = new MutableLiveData<>();
    private final MutableLiveData<String> name = new MutableLiveData<>();
    private final MutableLiveData<String> className = new MutableLiveData<>();
    private final MutableLiveData<String> balance = new MutableLiveData<>();
    private final MutableLiveData<String> stepNumber = new MutableLiveData<>();
    private final MutableLiveData<String> distance = new MutableLiveData<>();
    private final MutableLiveData<String> calorie = new MutableLiveData<>();
    private final MutableLiveData<String> nfcNumber = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Meal>> mealList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<ArrayList<Meal>> mealSelectList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<ArrayList<Meal>> mealEmptyList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> hasFailOrder = new MutableLiveData<>(false);
    private double studentBalance = 0;
    private final String nameStr;
    private final String classStr;
    private final String balanceStr;
    private final String nfcStr;
    private final String stepStr;
    private final String distanceStr;
    private final String calorieStr;

    public OrderViewModel(@NonNull Application application) {
        super(application);
        nameStr = application.getResources().getString(R.string.studName);
        classStr = application.getResources().getString(R.string.className);
        balanceStr = application.getResources().getString(R.string.balance_n);
        nfcStr = application.getResources().getString(R.string.nfc_id);
        stepStr = application.getResources().getString(R.string.step_number_i);
        distanceStr = application.getResources().getString(R.string.distance_i);
        calorieStr = application.getResources().getString(R.string.calorie_i);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        hasFailOrder.postValue(!SpUtil.getOrders().isEmpty());
    }

    public void setStatusInfo(Student student) {
        name.setValue(nameStr + (student != null && student.getCStudName() != null ? student.getCStudName() : ""));
        className.setValue(classStr + (student != null && student.getCClass() != null ? student.getCClass() : ""));
        balance.setValue(balanceStr + (student != null ? student.getNBalance() : "0.0"));
        nfcNumber.setValue(nfcStr + (student != null && student.getNfcId() != null ? student.getNfcId() : ""));
        StudentSports sports = SpUtil.getStudentSportsByCode(student != null ? student.getCStudCode() : "");
        stepNumber.setValue(stepStr + (sports != null ? sports.getIStepNumber() : "0"));
        distance.setValue(distanceStr + (sports != null ? sports.getIDistance() : "0"));
        calorie.setValue(calorieStr + (sports != null ? sports.getNCalorie() : "0.0"));
        orderStatus.setValue(OrderStatus.pick);
    }

    public void clearStatusInfo() {
        name.setValue(nameStr);
        className.setValue(classStr);
        balance.setValue(balanceStr);
        nfcNumber.setValue(nfcStr);
        stepNumber.setValue(stepStr);
        distance.setValue(distanceStr);
        calorie.setValue(calorieStr);
        orderStatus.setValue(OrderStatus.identify);
    }

    public void getMeal() {
        HttpUtil.getMeal(new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ArrayList<Meal> meals) {
                closeLoading.setValue(new Object());

                SpUtil.putMeal(meals);

                ArrayList<Meal> subList = new ArrayList<>();
                for (Meal meal : meals) {
                    String start = meal.getCDate() + meal.getCStartTime();
                    String end = meal.getCDate() + meal.getCEndTime();
                    if (TimeUtil.inTime(start, end, "yyyyMMddHH:mm")) {
                        subList.add(meal);
                    }
                }
                if (subList.isEmpty()) {
                    finish.postValue("当前不在开餐时间");
                    return;
                }
                mealList.postValue(subList);
            }

            @Override
            public void onError(Throwable e) {
                closeLoading.setValue(new Object());

                ArrayList<Meal> meals = SpUtil.getMeal();
                if (meals == null || meals.isEmpty()) {
                    finish.postValue("未获取到菜单数据，请联网后再试");
                    return;
                }

                ArrayList<Meal> subList = new ArrayList<>();
                for (Meal meal : meals) {
                    String start = meal.getCDate() + meal.getCStartTime();
                    String end = meal.getCDate() + meal.getCEndTime();
                    if (TimeUtil.inTime(start, end, "yyyyMMddHH:mm")) {
                        subList.add(meal);
                    }
                }
                if (subList.isEmpty()) {
                    finish.postValue("当前不在开餐时间");
                    return;
                }
                mealList.postValue(subList);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    public void getBalances(Student student) {
        HttpUtil.getBalance(student.getCStudCode(), new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Balance result) {
                if (result == null) {
                    return;
                }

                closeLoading.setValue(new Object());
                balance.postValue(balanceStr + result.getNBalance());

                student.setNBalance(result.getNBalance());
                SpUtil.setStudentBalance(student);
            }

            @Override
            public void onError(Throwable e) {
                closeLoading.setValue(new Object());
            }

            @Override
            public void onComplete() {

            }
        });
    }

    public void saveTurnover() {
        ArrayList<Meal> meals = getMealSelectList().getValue();
        if (meals == null) {
            return;
        }
        double allBalance = 0;
        for (Meal meal : meals) {
            allBalance += meal.getNSum();
        }
        SpUtil.setTurnover(allBalance);
    }

    public void createOrder(Student student) {
        ArrayList<Meal> meals = getMealSelectList().getValue();
        if (meals == null || meals.isEmpty()) {
            tips.postValue("您还未选择任何菜品！");
            closeLoading.setValue(new Object());
            return;
        }
        if (!canBuy()) {
            tips.postValue("您的余额不足！");
            closeLoading.setValue(new Object());
            return;
        }
        if (student == null) {
            tips.postValue("学生信息为空！");
            closeLoading.setValue(new Object());
            return;
        }

        ArrayList<Order> newOrders = getNewOrder(student, meals);
        SpUtil.putOrders(newOrders);

        HashMap<String, ArrayList<Order>> requestMap = new HashMap<>();
        requestMap.put("requests", SpUtil.getOrders());
        String jsonRequest = GsonUtil.objToJson(requestMap);

        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), jsonRequest);

        HttpUtil.createOrder(body, new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Result result) {
                if (result.getCode() == Constants.NFC_ID_UPDATE_SUCCESS) {
                    SpUtil.clearOrders();
                    hasFailOrder.postValue(false);
                } else {
                    hasFailOrder.postValue(true);
                }
                orderResult.postValue(new Object());

                balance.postValue(String.format(Locale.getDefault(), "%s%.1f", balanceStr, studentBalance));

                student.setNBalance(studentBalance);
                SpUtil.setStudentBalance(student);
            }

            @Override
            public void onError(Throwable e) {
                hasFailOrder.postValue(true);
                orderResult.postValue(new Object());

                balance.postValue(String.format(Locale.getDefault(), "%s%.1f", balanceStr, studentBalance));

                student.setNBalance(studentBalance);
                SpUtil.setStudentBalance(student);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private ArrayList<Order> getNewOrder(Student student, ArrayList<Meal> meals) {
        ArrayList<Order> orders = new ArrayList<>();
        String[] times = TimeUtil.getStringTime();
        String deviceNumber = String.format(Locale.getDefault(), "%02d", SpUtil.getDeviceNumber());
        String billCode = times[0] + deviceNumber;
        String create = times[1];
        for (Meal meal : meals) {
            Order order = new Order();
            order.setCBillCode(billCode);
            order.setCFoodCode(meal.getCFoodCode());
            order.setCStudCode(student.getCStudCode());
            order.setINumber(1);
            order.setNPrice(meal.getNSum());
            order.setNSum(meal.getNSum());
            order.setIState(1);
            order.setCDinnerTable(deviceNumber);
            order.setDCreate(create);
            orders.add(order);
        }
        return orders;
    }

    public void bindRfid(String rfid, Student student) {
        HttpUtil.bindNfc(student.getCStudCode(), rfid, new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Result result) {
                closeLoading.setValue(new Object());
                if (result.getCode() == Constants.NFC_ID_UPDATE_SUCCESS) {
                    nfcNumber.postValue(nfcStr + rfid);

                    student.setNfcId(rfid);
                    SpUtil.setStudentNfc(student);
                }
                tips.postValue(result.getMessage());
            }

            @Override
            public void onError(Throwable e) {
                closeLoading.setValue(new Object());
                tips.postValue("网络异常，绑定NFC失败：" + e.getMessage());
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

    public boolean canBuy() {
        ArrayList<Meal> meals = getMealSelectList().getValue();
        if (meals == null) {
            return false;
        }
        String balance = getBalance().getValue();
        if (balance == null) {
            return false;
        }
        try {
            double allBalance = 0;
            for (Meal meal : meals) {
                allBalance += meal.getNSum();
            }
            double balanceNum = Double.parseDouble(balance.replace(balanceStr, ""));
            studentBalance = balanceNum - allBalance;
            return studentBalance >= 0;
        } catch (Exception ignored) {

        }
        return false;
    }

    public boolean hasSportsData() {
        String calorie = getCalorie().getValue();
        if (calorie == null) {
            return false;
        }
        try {
            double calorieNum = Double.parseDouble(calorie.replace(calorieStr, ""));
            if (calorieNum > 0) {
                return true;
            }
        } catch (Exception ignored) {

        }
        return false;
    }

    public boolean suggestBuy(Meal meal) {
        ArrayList<Meal> meals = getMealSelectList().getValue();
        if (meals == null) {
            return false;
        }
        String calorie = getCalorie().getValue();
        if (calorie == null) {
            return false;
        }
        if (meal == null) {
            return false;
        }
        try {
            double allCalorie = 0;
            for (Meal tempMeal : meals) {
                allCalorie += tempMeal.getNReferenceValue();
            }
            double calorieNum = Double.parseDouble(calorie.replace(calorieStr, ""));
            if (calorieNum - allCalorie >= meal.getNReferenceValue()) {
                return true;
            }
        } catch (Exception ignored) {

        }
        return false;
    }

    public boolean cantPick() {
        return orderStatus.getValue() != OrderStatus.pick;
    }

    public MutableLiveData<Object> getOrderResult() {
        return orderResult;
    }

    public MutableLiveData<String> getFinish() {
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

    public MutableLiveData<ArrayList<Meal>> getMealList() {
        return mealList;
    }

    public MutableLiveData<ArrayList<Meal>> getMealSelectList() {
        return mealSelectList;
    }

    public MutableLiveData<ArrayList<Meal>> getMealEmptyList() {
        return mealEmptyList;
    }

    public MutableLiveData<Boolean> getHasFailOrder() {
        return hasFailOrder;
    }
}
