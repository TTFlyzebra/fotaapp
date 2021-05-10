package com.flyzebra.fota.model;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Flyup implements IFlyup{

    private static final HandlerThread mTaskThread = new HandlerThread("FlyUP_Task");

    static {
        mTaskThread.start();
    }

    private static final Handler tHandler = new Handler(mTaskThread.getLooper());
    private AtomicBoolean isRun = new AtomicBoolean(false);
    private AtomicInteger vProcess = new AtomicInteger(0);
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void startUpVersion(FlyupResult upResult) {

    }

    @Override
    public boolean isUPVeriosnRunning() {
        return false;
    }

    @Override
    public void cancelAllTasks() {

    }

    private static class FlyUpdateHolder {
        public static final Flyup sInstance = new Flyup();
    }

    public static Flyup getInstance() {
        return Flyup.FlyUpdateHolder.sInstance;
    }

}
