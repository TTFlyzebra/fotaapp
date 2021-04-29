package com.flyzebra.fota.httpApi;

import com.flyzebra.utils.FlyLog;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpService {
    public static String API_BASE_URL = "http://192.168.8.140/flyota/";
    private static final int DEFAULT_TIMEOUT = 10;
    private Api mNetService;
    private static boolean isWork = false;

    public HttpService() {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .client(httpClientBuilder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl(API_BASE_URL)
                    .build();
            mNetService = retrofit.create(Api.class);
            isWork = true;
        } catch (Exception e) {
            FlyLog.d(e.toString());
            isWork = false;
        }
    }

    public Api getInspectionService() {
        return mNetService;
    }

    public static boolean getConfigStatus() {
        return isWork;
    }
}