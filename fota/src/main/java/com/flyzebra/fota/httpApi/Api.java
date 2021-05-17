package com.flyzebra.fota.httpApi;


import com.flyzebra.fota.bean.RetAllVersion;
import com.flyzebra.fota.bean.RetPhoneLog;
import com.flyzebra.fota.bean.RetVersion;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface Api {

    @POST("fotaapi/version")
    @FormUrlEncoded
    Observable<RetVersion> getUpVersion(@Field("sid") String sid, @Field("ver") String ver, @Field("imei") String imei, @Field("uid") String uid, @Field("aid") String aid);

    @POST("fotaapi/version/all")
    @FormUrlEncoded
    Observable<RetAllVersion> getAllVersion(@Field("sid") String sid, @Field("ver") String ver,@Field("imei") String imei, @Field("uid") String uid, @Field("aid") String aid);

    @POST("fotaapi/phonelog")
    @FormUrlEncoded
    Observable<RetPhoneLog> upPhoneLog(@Field("phoneId") int phoneId, @Field("event") int event, @Field("emsg") String emsg,  @Field("phonetime") int phonetime);

}