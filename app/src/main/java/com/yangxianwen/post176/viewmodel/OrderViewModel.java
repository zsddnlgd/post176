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
    private final MutableLiveData<Object> finish = new MutableLiveData<>();
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
        //每次进入判断是否有未上传订单
        createOrder(SpUtil.getFailOrders());
    }

    public void setStatusInfo(Student student) {
        name.setValue(nameStr + (student != null && student.getCStudName() != null ? student.getCStudName() : ""));
        className.setValue(classStr + (student != null && student.getCClass() != null ? student.getCClass() : ""));
        balance.setValue(balanceStr + (student != null ? student.getNBalance() : "0.0"));
        nfcNumber.setValue(nfcStr + (student != null && student.getNfcId() != null ? student.getNfcId() : ""));
        StudentSports sports = SpUtil.getStudentSportsByCode(student != null ? student.getCStudCode() : "");
        stepNumber.setValue(stepStr + (sports != null ? sports.getIStepNumber() : "0"));
        distance.setValue(distanceStr + (sports != null ? sports.getIDistance() : "0"));
        calorie.postValue(calorieStr + (sports != null ? sports.getNCalorie() : "0.0"));
    }

    public void clearStatusInfo() {
        name.setValue(nameStr);
        className.setValue(classStr);
        balance.setValue(balanceStr);
        nfcNumber.setValue(nfcStr);
        stepNumber.setValue(stepStr);
        distance.setValue(distanceStr);
        calorie.setValue(calorieStr);
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
                    tips.postValue("当前不在开餐时间");
                    finish.postValue(new Object());
                    return;
                }
                mealList.postValue(subList);
            }

            @Override
            public void onError(Throwable e) {
                closeLoading.setValue(new Object());

                ArrayList<Meal> meals = SpUtil.getMeal();
                if (meals == null || meals.isEmpty()) {
                    tips.postValue("未获取到菜单数据，请联网后再试");
                    return;
                }

                ArrayList<Meal> subList = new ArrayList<>();
                for (Meal meal : meals) {
                    String start = meal.getCDate() + meal.getCStartTime();
                    String end = meal.getCDate() + meal.getCEndTime();
                    if (TimeUtil.inTime(start, end, "yyyy-MM-ddHH:mm")) {
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
        if (student == null) {
            tips.postValue("学生信息为空");
            closeLoading.setValue(new Object());
            return;
        }
        ArrayList<Meal> meals = getMealSelectList().getValue();
        if (meals == null) {
            tips.postValue("菜单信息为空");
            closeLoading.setValue(new Object());
            return;
        }

        ArrayList<Order> orders = getOrderInfo(student, meals);
        HashMap<String, ArrayList<Order>> requestMap = new HashMap<>();
        requestMap.put("requests", orders);
        String jsonRequest = GsonUtil.objToJson(requestMap);

        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), jsonRequest);

        HttpUtil.createOrder(body, new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Result result) {
                closeLoading.setValue(new Object());
                if (result.getCode() == Constants.NFC_ID_UPDATE_SUCCESS) {
                    SpUtil.clearFailOrders();
                    hasFailOrder.postValue(false);

                } else {
                    SpUtil.putFailOrders(orders);
                    hasFailOrder.postValue(true);
                }
                orderResult.postValue(new Object());
                balance.postValue(String.format(Locale.getDefault(), "%s%.1f", balanceStr, studentBalance));
                student.setNBalance(studentBalance);
                SpUtil.setStudentBalance(student);
            }

            @Override
            public void onError(Throwable e) {
                closeLoading.setValue(new Object());
                SpUtil.putFailOrders(orders);
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

    public void createOrder(ArrayList<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }

        hasFailOrder.postValue(true);

        HashMap<String, ArrayList<Order>> requestMap = new HashMap<>();
        requestMap.put("requests", orders);
        String jsonRequest = GsonUtil.objToJson(requestMap);

        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), jsonRequest);

        HttpUtil.createOrder(body, new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Result result) {
                SpUtil.clearFailOrders();
                hasFailOrder.postValue(false);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @NonNull
    private ArrayList<Order> getOrderInfo(Student student, ArrayList<Meal> meals) {
        ArrayList<Order> orders;
        ArrayList<Order> failOrders = SpUtil.getFailOrders();
        if (failOrders != null && !failOrders.isEmpty()) {
            orders = new ArrayList<>(failOrders);
            hasFailOrder.postValue(true);
        } else {
            orders = new ArrayList<>();
        }
        String[] times = TimeUtil.getStringTime();
        String billCode = times[0] + SpUtil.getDeviceNumber();
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
            order.setCDinnerTable("");
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
            if (balanceNum >= allBalance) {
                studentBalance = balanceNum - allBalance;
                return true;
            }
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

    private int getFoodType() {
        ArrayList<Meal> meals = getMealList().getValue();
        if (meals == null || meals.isEmpty()) {
            return -1;
        }
        Meal meal = meals.get(0);
        if ("午餐".equals(meal.getCName())) {
            return 0;
        } else if ("晚餐".equals(meal.getCName())) {
            return 1;
        } else if ("夜餐".equals(meal.getCName())) {
            return 2;
        }
        return -1;
    }

    public MutableLiveData<OrderStatus> getOrderStatus() {
        return orderStatus;
    }

    public MutableLiveData<Object> getOrderResult() {
        return orderResult;
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
