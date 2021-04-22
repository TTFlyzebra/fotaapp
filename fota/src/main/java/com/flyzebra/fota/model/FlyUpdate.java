package com.flyzebra.fota.model;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FlyUpdate {

    private static final HandlerThread mTaskThread = new HandlerThread("HeartBeat_Task");

    static {
        mTaskThread.start();
    }

    private static final Handler tHandler = new Handler(mTaskThread.getLooper());
    private AtomicBoolean isRun = new AtomicBoolean(false);
    private AtomicInteger vProcess = new AtomicInteger(0);
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private IFlyUpdateListener iFlyUpdateListener;


}
