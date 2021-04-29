package com.flyzebra.fota;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.flyzebra.fota.httpApi.ApiAction;
import com.flyzebra.fota.httpApi.ApiActionlmpl;

public class MainService extends Service {
    private ApiAction apiAction = new ApiActionlmpl();
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
