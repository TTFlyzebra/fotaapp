package com.flyzebra.fota;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RecoverySystem;
import android.view.View;

import com.flyzebra.flydown.FlyDown;
import com.flyzebra.flydown.request.IFileReQuestListener;
import com.flyzebra.utils.FlyLog;
import com.flyzebra.utils.HttpUtils;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements RecoverySystem.ProgressListener{

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static int REQUEST_PERMISSION_CODE = 101;

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

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            FlyLog.d("onRequestPermissionsResult");
        }
    }

    public void upgrade(View view) {
        try {
            RecoverySystem.verifyPackage(new File("/sdcard/updatetest.zip"),this, null);
            RecoverySystem.installPackage(this,new File("/sdcard/updatetest.zip"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgress(int i) {
        FlyLog.e("progress %d",i);
    }

    public void runTask(){
        FlyLog.d("-----start main-----\n");
        String downUrl = "http://192.168.8.140/video/testlong.mp4";
        IFileReQuestListener listener = new IFileReQuestListener() {
            @Override
            public void Error(String url, int ErrorCode) {
                FlyLog.e("--onError----%s,%d\n",url,ErrorCode);
            }

            @Override
            public void Finish(String url) {
                FlyLog.e("--onFinish----%s\n",url);
            }

            @Override
            public void Pause(String url) {
                FlyLog.e("--onPause----%s\n",url);
            }

            @Override
            public void Progress(String url, long downBytes, long sumBytes) {
                FlyLog.e("url=%s, downBytes=%d, sumBytes=%d", url,downBytes,sumBytes);
            }
        };
        FlyDown.load(downUrl).setThread(10).listener(listener).goStart();
        //FlyLog.d("file l = %d \n", HttpUtils.getLength("http://192.168.8.140/video/testlong.mp4"));
        //FlyLog.d("-----end main-----\n");
    }
}