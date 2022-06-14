package com.flyzebra.fota.httpApi;


import com.flyzebra.fota.bean.RetAllVersion;
import com.flyzebra.fota.bean.RetPhoneLog;
import com.flyzebra.fota.bean.RetVersion;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface Api {

    @POST("termapi/version")
    @FormUrlEncoded
    Observable<RetVersion> getUpVersion(@Field("sid") String sid, @Field("ver") String ver, @Field("imei") String imei, @Field("uid") String uid);

    @POST("termapi/version/all")
    @FormUrlEncoded
    Observable<RetAllVersion> getAllVersion(@Field("sid") String sid, @Field("ver") String ver,@Field("imei") String imei, @Field("uid") String uid);

    @POST("termapi/phonelog")
    @FormUrlEncoded
    Observable<RetPhoneLog> upPhoneLog(@Field("imei") String imei, @Field("event") int event, @Field("emsg") String emsg);

}