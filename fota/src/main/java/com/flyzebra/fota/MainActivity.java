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

import com.flyzebra.utils.FlyLog;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity implements RecoverySystem.ProgressListener {

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static int REQUEST_PERMISSION_CODE = 101;

    private static final HandlerThread mTaskThread = new HandlerThread("fota_Task");

    static {
        mTaskThread.start();
    }

    private static final Handler tHandler = new Handler(mTaskThread.getLooper());
    private AtomicInteger vProcess = new AtomicInteger(0);
    private static final Handler mHandler = new Handler(Looper.getMainLooper());
    private ProgressDialog progressDialog;

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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            FlyLog.d("onRequestPermissionsResult");
        }
    }

    public void upgrade(View view) {
        final File updateFile = new File("/data/update.zip");
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
                    } catch (GeneralSecurityException | IOException e) {
                        FlyLog.e(e.toString());
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

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        tHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}