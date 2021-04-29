package com.flyzebra.fota.httpApi;



import com.flyzebra.fota.bean.OtaPackage;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface Api {

    @GET("fotaapi/test")
    Observable<List<String>> doTheme(@Query("type") String type);

    @POST("fotaapi/version")
    Observable<OtaPackage> getUpVersion(@Query("version") String version, @Query("systemId") String systemId);

}