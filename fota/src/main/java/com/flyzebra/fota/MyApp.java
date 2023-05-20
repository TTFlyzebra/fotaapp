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

        Intent mainintent = new Intent();
        mainintent.setClass(getApplicationContext(), MainService.class);
        mainintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(mainintent);

        Flyup.getInstance().init(getApplicationContext());
    }
}
