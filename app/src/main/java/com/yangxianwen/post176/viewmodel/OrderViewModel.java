package com.yangxianwen.post176.viewmodel;

import android.app.Application;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.yangxianwen.post176.R;
import com.yangxianwen.post176.base.BaseViewModel;
import com.yangxianwen.post176.bean.Balance;
import com.yangxianwen.post176.bean.Meal;
import com.yangxianwen.post176.bean.Order;
import com.yangxianwen.post176.bean.Recommend;
import com.yangxianwen.post176.bean.Result;
import com.yangxianwen.post176.bean.Student;
import com.yangxianwen.post176.bean.StudentSports;
import com.yangxianwen.post176.enmu.OrderStatus;
import com.yangxianwen.post176.utils.FileUtil;
import com.yangxianwen.post176.utils.GsonUtil;
import com.yangxianwen.post176.utils.HttpUtil;
import com.yangxianwen.post176.utils.SpUtil;
import com.yangxianwen.post176.utils.TimeUtil;
import com.yangxianwen.post176.values.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class OrderViewModel extends BaseViewModel {

    private final MutableLiveData<OrderStatus> orderStatus = new MutableLiveData<>(OrderStatus.identify);
    private final MutableLiveData<Object> orderFinish = new MutableLiveData<>();
    private final MutableLiveData<String> finish = new MutableLiveData<>();
    private final MutableLiveData<String> name = new MutableLiveData<>();
    private final MutableLiveData<String> className = new MutableLiveData<>();
    private final MutableLiveData<String> balance = new MutableLiveData<>();
    private final MutableLiveData<String> stepNumber = new MutableLiveData<>();
    private final MutableLiveData<String> distance = new MutableLiveData<>();
    private final MutableLiveData<String> calorie = new MutableLiveData<>();
    private final MutableLiveData<String> nfcNumber = new MutableLiveData<>();
    private final MutableLiveData<String> headImage = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Meal>> mealList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<ArrayList<Meal>> mealSelectList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<ArrayList<Meal>> mealEmptyList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> hasFailOrder = new MutableLiveData<>(false);
    private final MutableLiveData<String> recommendText = new MutableLiveData<>();
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

    public void showStatusInfo(Student student) {
        headImage.setValue(student.getCPic().replace("/Pic/StuImg", FileUtil.REGISTER_DIR));
        name.setValue(nameStr + student.getCStudName());
        className.setValue(classStr + student.getCClass());
        balance.setValue(balanceStr + student.getNBalance());
        nfcNumber.setValue(nfcStr + (student.getNfcId() != null ? student.getNfcId() : ""));
        StudentSports sports = SpUtil.getStudentSportsByCode(student.getCStudCode());
        stepNumber.setValue(stepStr + (sports != null ? sports.getIStepNumber() : "0"));
        distance.setValue(distanceStr + (sports != null ? sports.getIDistance() : "0"));
        calorie.setValue(calorieStr + (sports != null ? sports.getNCalorie() : "0.0"));
    }

    public void clearStatusInfo() {
        headImage.setValue("transparent");
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
            public void onNext(Balance balance) {
                closeLoading.setValue(new Object());

                student.setNBalance(balance.getNBalance());
                SpUtil.setStudent(student);
                showStatusInfo(student);
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

    public double getAllBalance(ArrayList<Meal> meals) {
        if (meals == null) {
            return 0;
        }
        double allBalance = 0;
        for (Meal meal : meals) {
            allBalance += meal.getNSum();
        }
        return allBalance;
    }

    public void createLocalOrder(Student student, double studentBalance, ArrayList<Meal> meals) {
        Pair<String, ArrayList<Order>> newOrder = getNewOrder(student, meals);
        SpUtil.putOrders(newOrder.first, newOrder.second);

        double allBalance = getAllBalance(meals);
        SpUtil.setTurnover(allBalance);

        student.setNBalance(studentBalance);
        SpUtil.setStudent(student);
        showStatusInfo(student);

        uploadOrders(newOrder.first);
    }

    private Pair<String, ArrayList<Order>> getNewOrder(Student student, ArrayList<Meal> meals) {
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
        return new Pair<>(billCode, orders);
    }

    private void uploadOrders(String billCode) {
        HashMap<String, ArrayList<Order>> orderMap = SpUtil.getUploadOrders();
        Set<String> keys = orderMap.keySet();
        ArrayList<Order> orders = new ArrayList<>();
        for (String key : keys) {
            ArrayList<Order> order = orderMap.get(key);
            if (order == null) {
                continue;
            }
            orders.addAll(order);
        }

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
                if (result.getCode() == Constants.NFC_ID_UPDATE_SUCCESS) {
                    SpUtil.removeOrders(keys);
                    getRecommendation(billCode);
                }
            }

            @Override
            public void onError(Throwable e) {
                hasFailOrder.postValue(true);
            }

            @Override
            public void onComplete() {
                hasFailOrder.postValue(!SpUtil.getOrders().isEmpty());
            }
        });
    }

    private void getRecommendation(String billCode) {
        HttpUtil.getRecommendation(billCode, new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Recommend recommend) {
                if (orderStatus.getValue() == OrderStatus.createOrder) {
                    recommendText.postValue(recommend.getRecommendation());
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
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
                    student.setNfcId(rfid);
                    SpUtil.setStudent(student);
                    showStatusInfo(student);
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

    public double getStudentBalance() {
        ArrayList<Meal> meals = getMealSelectList().getValue();
        if (meals == null) {
            return -1;
        }
        String balance = getBalance().getValue();
        if (balance == null) {
            return -1;
        }
        try {
            double allBalance = 0;
            for (Meal meal : meals) {
                allBalance += meal.getNSum();
            }
            double balanceNum = Double.parseDouble(balance.replace(balanceStr, ""));
            return balanceNum - allBalance;
        } catch (Exception ignored) {

        }
        return -1;
    }

    public boolean hasSportsData() {
        String calorie = getCalorie().getValue();
        if (calorie == null) {
            Log.i(TAG, "hasSportsData: 1");
            return false;
        }
        try {
            double calorieNum = Double.parseDouble(calorie.replace(calorieStr, ""));
            Log.i(TAG, "hasSportsData: calorieNum " + calorieNum);
            if (calorieNum > 0) {
                return true;
            }
        } catch (Exception ignored) {
            Log.i(TAG, "hasSportsData: 2");
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

    public MutableLiveData<OrderStatus> getOrderStatus() {
        return orderStatus;
    }

    public MutableLiveData<Object> getOrderFinish() {
        return orderFinish;
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

    public MutableLiveData<String> getHeadImage() {
        return headImage;
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

    public MutableLiveData<String> getRecommendText() {
        return recommendText;
    }
}
