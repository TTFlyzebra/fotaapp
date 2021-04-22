package com.flyzebra.fota;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.RecoverySystem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.flyzebra.flydown.FlyDown;
import com.flyzebra.flydown.request.IFileReQuestListener;
import com.flyzebra.fota.httpApi.ApiAction;
import com.flyzebra.fota.httpApi.ApiActionlmpl;
import com.flyzebra.utils.FlyLog;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements RecoverySystem.ProgressListener {

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static int REQUEST_PERMISSION_CODE = 101;

    private static final HandlerThread mTaskThread = new HandlerThread("HeartBeat_Task");

    static {
        mTaskThread.start();
    }

    private static final Handler tHandler = new Handler(mTaskThread.getLooper());
    private AtomicBoolean isRun = new AtomicBoolean(false);
    private AtomicInteger vProcess = new AtomicInteger(0);
    private static final Handler mHandler = new Handler(Looper.getMainLooper());

    private ProgressDialog progressDialog;
    
    private ApiAction apiAction = new ApiActionlmpl();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            for (String s : PERMISSIONS_STORAGE) {
                if (ActivityCompat.checkSelfPermission(this, s) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
                    break;
                }
            }
        }

        Intent mainintent = new Intent();
        mainintent.setClass(this, MainService.class);
        mainintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(mainintent);

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setTitle("正在校验安装包");

        apiAction.doTheme("1", new Observer<List<String>>() {
            @Override
            public void onSubscribe(Disposable d) {
                FlyLog.e("onSubscribe:"+d);
            }

            @Override
            public void onNext(List<String> strings) {
                FlyLog.e("noNext:"+strings);
            }

            @Override
            public void onError(Throwable e) {
                FlyLog.e("onError:"+e);
            }

            @Override
            public void onComplete() {
                FlyLog.e("onComplete");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            FlyLog.d("onRequestPermissionsResult");
        }
    }

    public void upgrade(View view) {
        final File updateFile = new File(getFilesDir(), "update.zip");
        if (updateFile.exists()) {
            vProcess.set(0);
            tHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        RecoverySystem.verifyPackage(updateFile, MainActivity.this, null);
                        if (vProcess.get() == 100) {
                            vProcess.set(0);
                            RecoverySystem.installPackage(MainActivity.this, updateFile);
                        } else {
                            FlyLog.e("verifyPackage failed!");
                        }
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            FlyLog.e("update file path=%s", updateFile.getAbsolutePath());
        } else {
            FlyLog.e("can not find update.zip!");
        }
    }

    @Override
    public void onProgress(int i) {
        vProcess.set(i);
        upVProcess();
    }

    private void upVProcess() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.setProgress(vProcess.get());
                if (vProcess.get() < 100) {
                    if (!progressDialog.isShowing()) {
                        progressDialog.show();
                    }
                } else {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            }
        });
    }

    public void downFile() {
        FlyLog.d("-----start downFile-----\n");
        FlyDown.mCacheDir = getFilesDir().getAbsolutePath();
        String downUrl = "http://192.168.8.140/version/update.zip";
        IFileReQuestListener listener = new IFileReQuestListener() {
            @Override
            public void Error(String url, int ErrorCode) {
                FlyLog.e("--onError----%s,%d\n", url, ErrorCode);
            }

            @Override
            public void Finish(String url) {
                FlyLog.e("--onFinish----%s\n", url);
            }

            @Override
            public void Pause(String url) {
                FlyLog.e("--onPause----%s\n", url);
            }

            @Override
            public void Progress(String url, long downBytes, long sumBytes) {
                FlyLog.e("url=%s, downBytes=%d, sumBytes=%d", url, downBytes, sumBytes);
            }
        };
        FlyDown.load(downUrl).setThread(10).listener(listener).goStart();
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        tHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}