package com.yangxianwen.post176.http;


import com.yangxianwen.post176.utils.HttpUtil;
import com.yangxianwen.post176.values.Urls;

import java.io.IOException;
import java.util.ArrayList;

import kotlin.Pair;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

public class HttpConnect {

    /**
     * 请求服务器接口
     */
    public static String httpGetData(String urlName) {
        Request request = new Request.Builder()
                .url(Urls.main + urlName)
                .get()
                .build();

        try (Response response = HttpUtil.getOkHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return response.message();
            }
            return response.body().string();
        } catch (IOException ignored) {

        }

        return null;
    }

    /**
     * 请求服务器接口
     */
    public static String httpGetData(String urlName, ArrayList<Pair<String, String>> params) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Pair<String, String> nameValuePair : params) {
            builder.add(nameValuePair.getFirst(), nameValuePair.getSecond());
        }

        Request request = new Request.Builder()
                .url(Urls.main + urlName)
                .get()
                .put(builder.build())
                .build();

        try (Response response = HttpUtil.getOkHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return response.message();
            }
            return response.body().string();
        } catch (IOException ignored) {

        }

        return null;
    }

    /**
     * 请求服务器接口
     */
    public static void httpDownloadData(String fileName, Callback callback) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("fileName", fileName);

        Request request = new Request.Builder()
                .url(Urls.main)
                .get()
                .put(builder.build())
                .build();
        HttpUtil.getOkHttpClient().newCall(request).enqueue(callback);
    }
}
