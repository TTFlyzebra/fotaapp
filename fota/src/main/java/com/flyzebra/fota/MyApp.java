package com.flyzebra.fota;

import android.app.Application;
import android.content.Intent;

import com.flyzebra.fota.model.Flyup;
import com.flyzebra.utils.FlyLog;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FlyLog.setTAG("ZEBRA-FOTA");
        Flyup.getInstance().init(getApplicationContext());
    }
}
