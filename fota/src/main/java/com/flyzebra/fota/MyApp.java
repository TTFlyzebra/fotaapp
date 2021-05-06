package com.flyzebra.fota;

import android.app.Application;
import android.content.Intent;

import com.flyzebra.utils.FlyLog;
import com.tencent.bugly.crashreport.CrashReport;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FlyLog.setTAG("ZEBRA-FOTA-APP");

        Intent mainintent = new Intent();
        mainintent.setClass(getApplicationContext(), MainService.class);
        mainintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(mainintent);

        //腾讯BUGLY
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setAppChannel("myChannel");  //设置渠道
        strategy.setAppVersion("1.01");      //App的版本
        strategy.setAppPackageName("com.flyzebra.fota");  //App的包名
        CrashReport.initCrashReport(getApplicationContext(), "22951229ad", false, strategy);
    }
}
