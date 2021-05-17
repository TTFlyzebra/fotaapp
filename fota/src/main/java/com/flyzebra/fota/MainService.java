package com.flyzebra.fota;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import com.flyzebra.fota.config.OsEvent;
import com.flyzebra.fota.model.Flyup;
import com.flyzebra.fota.model.IFlyup;
import com.flyzebra.fota.view.NotificationView;
import com.flyzebra.utils.FlyLog;


public class MainService extends Service implements Runnable, IFlyup.FlyupResult, OsEvent {
    private static final HandlerThread mTaskThread = new HandlerThread("fota_service");

    static {
        mTaskThread.start();
    }

    private static final Handler tHandler = new Handler(mTaskThread.getLooper());
    private static final Handler mHandler = new Handler(Looper.getMainLooper());

    private static final int CHECK_TIME = 60 * 60 * 1000;
    private static final int MIN_TIME = 2 * 60 * 1000;
    private static final int FIRST_TIME = 10 * 60 * 1000;
    private NotificationView notificationView;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FlyLog.e("++++++++++++++++++++++++++++++++++++");
        FlyLog.e("++++++OcFota 1.02---2021.05.13++++++");
        FlyLog.e("++++++++++++++++++++++++++++++++++++");
        FlyLog.e("+++video decoder sevice is start!+++");
        Flyup.getInstance().addListener(this);
        notificationView = new NotificationView(this);
        startCheckUpVersion(0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void startCheckUpVersion(long time) {
        tHandler.removeCallbacksAndMessages(this);
        tHandler.postDelayed(this, time);
    }

    @Override
    public void onDestroy() {
        Flyup.getInstance().stopUpVersion();
        Flyup.getInstance().removeListener(this);
        tHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void run() {
        FlyLog.d("fota service running.....");
        Flyup.getInstance().checkNewVersion();
    }

    @Override
    public void upVesionProgress(int code, int progress, String msg) {
        FlyLog.d("upVesionProgress: %d, %d, %s", code, progress, msg);
        switch (code) {
            //已是最新版本
            case CODE_01:
                startCheckUpVersion(CHECK_TIME);
                break;
            //获取到最新版本
            case CODE_02:
                notificationView.show(code, progress, msg);
                break;
            //获取最新版本失败
            case CODE_03:
                startCheckUpVersion(CHECK_TIME);
                break;
            //获取最新版本失败，网络错误！
            case CODE_04:
                startCheckUpVersion(MIN_TIME);
                break;
            //正在下载升级包...
            case CODE_05:
                notificationView.show(code, progress, msg);
                break;
            //下载升级包出错!
            case CODE_06:
                startCheckUpVersion(10000);
                break;
            //正在校验升级包MD5值...
            case CODE_07:
                notificationView.show(code, progress, msg);
                break;
            //升级包MD5值校验错误!
            case CODE_08:
                startCheckUpVersion(10000);
                break;
            //升级包数据校验...
            case CODE_09:
                notificationView.show(code, progress, msg);
                break;
            //安装升级包...
            case CODE_10:
                notificationView.show(code, progress, msg);
                break;
            //升级包数据校验错误!
            case CODE_11:
                break;
            //安装升级包错误!
            case CODE_12:
                break;
            //系统正在更新
            case CODE_91:
                break;
            //需要手动更新版本
            case CODE_92:
                break;
        }
        if (progress == 0 || progress == 100 || (code != CODE_00 && code!= CODE_91)) {
            Flyup.getInstance().upPhoneLog(code, msg);
        }
    }
}
