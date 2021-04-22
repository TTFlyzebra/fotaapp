package com.flyzebra.fota.httpApi;



import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Api {

    @GET("fotaapi/test")
    Observable<List<String>> doTheme(@Query("type") String type);

}