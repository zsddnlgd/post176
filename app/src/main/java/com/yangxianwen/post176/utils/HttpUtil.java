package com.yangxianwen.post176.utils;

import android.util.Log;

import com.yangxianwen.post176.bean.Balance;
import com.yangxianwen.post176.bean.Meal;
import com.yangxianwen.post176.bean.Nfc;
import com.yangxianwen.post176.bean.Recommend;
import com.yangxianwen.post176.bean.Result;
import com.yangxianwen.post176.bean.Student;
import com.yangxianwen.post176.bean.StudentSports;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

/**
 * 网络请求工具类
 */
public class HttpUtil {

    private static final String TAG = HttpUtil.class.getName();

    public interface NetService {

        @Streaming
        @GET("PicDownload")
        Observable<ResponseBody> downloadPicByFileName(@Query("fileName") String fileName);

        @GET("api/StudentInfo")
        Observable<ArrayList<Student>> getStudent();

        @GET("api/MealInfo")
        Observable<ArrayList<Meal>> getMeal();

        @GET("/api/VBalance/{studCode}")
        Observable<Balance> getBalanceByStudentCode(@Path("studCode") String studentCode);

        @GET("api/StudentInfo/codes-and-nfcs")
        Observable<ArrayList<Nfc>> getNfc();

        @Headers({"Content-Type:application/json;charset=UTF-8"})
        @POST("/api/OrderInfo/bulk-create")
        Observable<Result> createOrder(@Body RequestBody body);

        @Headers({"Content-Type:application/json;charset=UTF-8"})
        @POST("/api/StudentInfo/{studCode}/update-nfc-id/{nfcId}")
        Observable<Result> bindNfc(@Path("studCode") String studentCode, @Path("nfcId") String nfcId);

        @GET("/api/ReferenceValue/GetMaxIDByStudCode")
        Observable<ArrayList<StudentSports>> getStudentSports();

        @GET("/api/OrderInfo/get-recommendation/{cBillCode}")
        Observable<Recommend> getRecommendation(@Path("cBillCode") String billCode);
    }

    /**
     * 获取默认OkHttpClient
     */
    public static OkHttpClient getOkHttpClient() {
        return new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(netWorkInterceptor)
                .build();
    }

    /**
     * 网络拦截器
     */
    private static final Interceptor netWorkInterceptor = chain -> {
        Request request = chain.request();
        String url = request.url().toString();

        Log.i(TAG, "netWorkInterceptor url = " + url);

        String requestStr = readRequestStr(request.body());
        if (!url.contains("PicDownload")) {
            Log.i(TAG, "netWorkInterceptor requestStr = " + requestStr);
        }

        Response response = chain.proceed(request);

        String responseStr = readResponseStr(response.body());
        if (!url.contains("PicDownload")) {
            Log.i(TAG, "netWorkInterceptor responseStr = " + responseStr);
        }

        return response;
    };

    /**
     * 读取Response返回String内容
     */
    private static String readResponseStr(ResponseBody body) {
        if (body == null) return "response为空";

        MediaType contentType = body.contentType();
        if (contentType != null && Objects.equals("multipart", contentType.type())) {
            return "file";
        }

        BufferedSource source = body.source();
        try {
            source.request(Long.MAX_VALUE);
        } catch (Exception e) {
            return "responseBody写入失败";
        }
        Buffer buffer = source.getBuffer();

        return buffer.clone().readUtf8();
    }

    /**
     * 读取Request返回String内容
     */
    public static String readRequestStr(RequestBody body) {
        if (body == null) return "request为空";

        MediaType contentType = body.contentType();
        if (contentType != null && Objects.equals("multipart", contentType.type())) {
            return "file";
        }

        BufferedSink sink = new Buffer();
        try {
            body.writeTo(sink);
        } catch (Exception e) {
            return "requestBody写入失败";
        }
        Buffer buffer = sink.buffer();

        return buffer.clone().readUtf8();
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
        Retrofit retrofit = HttpUtil.getRetrofitAsync(SpUtil.getIpAddress());

        NetService netService = retrofit.create(NetService.class);

        Observable<ResponseBody> observable = netService.downloadPicByFileName(fileName);

        observable.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(observer);
    }

    public static void getStudent(Observer<ArrayList<Student>> observer) {
        Retrofit retrofit = getRetrofit(SpUtil.getIpAddress());

        NetService netService = retrofit.create(NetService.class);

        Observable<ArrayList<Student>> observable = netService.getStudent();

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void getMeal(Observer<ArrayList<Meal>> observer) {
        Retrofit retrofit = getRetrofit(SpUtil.getIpAddress());

        NetService netService = retrofit.create(NetService.class);

        Observable<ArrayList<Meal>> observable = netService.getMeal();

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void getBalance(String studentCode, Observer<Balance> observer) {
        Retrofit retrofit = getRetrofit(SpUtil.getIpAddress());

        NetService netService = retrofit.create(NetService.class);

        Observable<Balance> observable = netService.getBalanceByStudentCode(studentCode);

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void createOrder(RequestBody body, Observer<Result> observer) {
        Retrofit retrofit = getRetrofit(SpUtil.getIpAddress());

        NetService netService = retrofit.create(NetService.class);

        Observable<Result> observable = netService.createOrder(body);

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void bindNfc(String studentCode, String nfcId, Observer<Result> observer) {
        Retrofit retrofit = getRetrofit(SpUtil.getIpAddress());

        NetService netService = retrofit.create(NetService.class);

        Observable<Result> observable = netService.bindNfc(studentCode, nfcId);

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void getNfc(Observer<ArrayList<Nfc>> observer) {
        Retrofit retrofit = getRetrofit(SpUtil.getIpAddress());

        NetService netService = retrofit.create(NetService.class);

        Observable<ArrayList<Nfc>> observable = netService.getNfc();

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void getStudentSports(Observer<ArrayList<StudentSports>> observer) {
        Retrofit retrofit = getRetrofit(SpUtil.getIpAddress());

        NetService netService = retrofit.create(NetService.class);

        Observable<ArrayList<StudentSports>> observable = netService.getStudentSports();

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void getRecommendation(String billCode, Observer<Recommend> observer) {
        Retrofit retrofit = getRetrofit(SpUtil.getIpAddress());

        NetService netService = retrofit.create(NetService.class);

        Observable<Recommend> observable = netService.getRecommendation(billCode);

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}