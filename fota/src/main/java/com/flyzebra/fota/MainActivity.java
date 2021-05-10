package com.flyzebra.fota;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.flyzebra.fota.model.Flyup;
import com.flyzebra.fota.model.IFlyCode;
import com.flyzebra.fota.model.IFlyup;
import com.flyzebra.utils.FlyLog;

public class MainActivity extends AppCompatActivity implements IFlyup.FlyupResult, IFlyCode {
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static int REQUEST_PERMISSION_CODE = 101;

    private TextView tv_verinfo,tv_upinfo;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_verinfo = findViewById(R.id.tv_verinfo);
        tv_upinfo = findViewById(R.id.tv_upinfo);
        progressBar = findViewById(R.id.ac_pbar);

        progressBar.setMax(100);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            for (String s : PERMISSIONS_STORAGE) {
                if (ActivityCompat.checkSelfPermission(this, s) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
                    break;
                }
            }
        }

        Flyup.getInstance().addListener(this);

        Intent mainintent = new Intent();
        mainintent.setClass(this, MainService.class);
        mainintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(mainintent);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            FlyLog.d("onRequestPermissionsResult");
        }
    }

    public void upgrade(View view) {
        Flyup.getInstance().startUpVersion();
    }


    @Override
    protected void onDestroy() {
        Flyup.getInstance().removeListener(this);
        super.onDestroy();
    }

    @Override
    public void upVesionProgress(int code, int progress, String msg) {
        tv_upinfo.setText(msg);
        progressBar.setProgress(progress);
        switch (code) {
            //已是最新版本
            case CODE_01:
                break;
            //获取到最新版本
            case CODE_02:
                break;
            //获取最新版本失败
            case CODE_03:
                break;
            //获取最新版本，网络错误！
            case CODE_04:
                break;
            //正在下载更新包......
            case CODE_05:
                break;
            //下载更新包出错!
            case CODE_06:
                break;
            //正在校验更新包MD5值......
            case CODE_07:
                break;
            //更新包MD5值校验错误!
            case CODE_08:
                break;
            //更新包数据完成性校验......
            case CODE_09:
                break;
            //准备安装更新包......
            case CODE_10:
                break;
            //更新包数据完成性校验错误!
            case CODE_11:
                break;
            //安装更新包错误!
            case CODE_12:
                break;
            //系统正在更新
            case CODE_91:
                break;
        }
    }
}