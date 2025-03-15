package com.yangxianwen.post176.utils;

import com.yangxianwen.post176.bean.Meal;
import com.yangxianwen.post176.bean.Student;
import com.yangxianwen.post176.values.Urls;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

/**
 * 网络请求工具类
 */
public class HttpUtil {

    public interface NetService {

        @Streaming
        @GET("PicDownload")
        Observable<ResponseBody> download(@Query("fileName") String fileName);

        @GET("api/StudentInfo")
        Observable<ArrayList<Student>> getStudent();
        @GET("api/MealInfo")
        Observable<ArrayList<Meal>> getMeal();
    }

    /**
     * 获取默认OkHttpClient
     */
    public static OkHttpClient getOkHttpClient() {
        return new OkHttpClient().newBuilder()
                .connectTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .build();
    }


    /**
     * 获取默认OkHttp的Retrofit
     */
    public static Retrofit getRetrofit(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(GsonUtil.getGson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getOkHttpClient())
                .build();
    }

    /**
     * 获取异步RxJava2CallAdapterFactory的Retrofit，避免创建多个线程
     */
    public static Retrofit getRetrofitAsync(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(GsonUtil.getGson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .client(getOkHttpClient())
                .build();
    }

    public static void downloadStudentPhoto(String fileName, Observer<ResponseBody> observer) {
        Retrofit retrofit = HttpUtil.getRetrofitAsync(Urls.main);

        NetService netService = retrofit.create(NetService.class);

        Observable<ResponseBody> observable = netService.download(fileName);

        observable.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(observer);
    }

    public static void getStudent(Observer<ArrayList<Student>> observer) {
        Retrofit retrofit = getRetrofit(Urls.main);

        NetService netService = retrofit.create(NetService.class);

        Observable<ArrayList<Student>> observable = netService.getStudent();

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void getMeal(Observer<ArrayList<Meal>> observer) {
        Retrofit retrofit = getRetrofit(Urls.main);

        NetService netService = retrofit.create(NetService.class);

        Observable<ArrayList<Meal>> observable = netService.getMeal();

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}