package com.yangxianwen.post176.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.yangxianwen.post176.bean.Order;
import com.yangxianwen.post176.bean.Result;
import com.yangxianwen.post176.utils.GsonUtil;
import com.yangxianwen.post176.utils.HttpUtil;
import com.yangxianwen.post176.utils.SpUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class UploadFailOrderService extends Service {

    private Disposable mDisposable;

    @Override
    public void onCreate() {
        super.onCreate();
        mDisposable = Observable.interval(30, TimeUnit.SECONDS)
                .subscribe(aLong -> uploadFailOrder());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void uploadFailOrder() {
        ArrayList<Order> orders = SpUtil.getOrders();

        if (orders.isEmpty()) {
            return;
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
                SpUtil.clearOrders();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }
}
