package com.flyzebra.fota.httpApi;


import com.flyzebra.fota.bean.OtaPackage;
import com.flyzebra.fota.bean.PhoneLog;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface Api {

    @POST("fotaapi/version")
    @FormUrlEncoded
    Observable<OtaPackage> getUpVersion(@Field("sid") String sid, @Field("ver") String ver, @Field("imei") String imei, @Field("uid") String uid, @Field("aid") String aid);

    @POST("fotaapi/phonelog")
    @FormUrlEncoded
    Observable<PhoneLog> upPhoneLog(@Field("phoneId") int phoneId, @Field("event") int event, @Field("emsg") String emsg);
}