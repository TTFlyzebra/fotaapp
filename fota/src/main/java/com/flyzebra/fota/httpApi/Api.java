package com.flyzebra.fota.httpApi;


import com.flyzebra.fota.bean.RetAllVersion;
import com.flyzebra.fota.bean.RetVersion;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface Api {

    @POST("termapi/version")
    @FormUrlEncoded
    Observable<RetVersion> getUpVersion(@Field("model") String model, @Field("version") String ver, @Field("imei") String imei);

    @POST("termapi/version/all")
    @FormUrlEncoded
    Observable<RetAllVersion> getAllVersion(@Field("model") String model, @Field("version") String ver,@Field("imei") String imei);
}