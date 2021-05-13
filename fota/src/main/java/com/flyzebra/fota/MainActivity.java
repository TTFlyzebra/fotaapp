package com.flyzebra.fota;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.flyzebra.fota.bean.OtaPackage;
import com.flyzebra.fota.model.Flyup;
import com.flyzebra.fota.model.IFlyCode;
import com.flyzebra.fota.model.IFlyup;
import com.flyzebra.utils.FlyLog;
import com.flyzebra.utils.IDUtils;

public class MainActivity extends AppCompatActivity implements IFlyup.FlyupResult, IFlyCode {
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static int REQUEST_PERMISSION_CODE = 101;

    private TextView tv_version, tv_verinfo, tv_upinfo;
    private ProgressBar progressBar;
    private Button bt_updater;

    private StringBuffer verinfo = new StringBuffer();


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

        tv_version = findViewById(R.id.tv_version);
        tv_verinfo = findViewById(R.id.tv_verinfo);
        tv_upinfo = findViewById(R.id.tv_upinfo);
        progressBar = findViewById(R.id.ac_pbar);
        bt_updater = findViewById(R.id.bt_updater);
        progressBar.setMax(100);
        tv_version.setText("当前版本：\n" + IDUtils.getVersion(this) + "\n");

        Flyup.getInstance().addListener(this);
        upVesionProgress(Flyup.getInstance().getLastCode(),Flyup.getInstance().getLastProgress(),Flyup.getInstance().getLastMessage());
    }

    private void upVersionInfo() {
        OtaPackage otaPackage = Flyup.getInstance().getOtaPackage();
        if (otaPackage != null && otaPackage.data.version != null) {
            verinfo.delete(0, verinfo.length());
            verinfo.append("最新版本：\n")
                    .append(otaPackage.data.version).append("\n");
            if (otaPackage.code == 0) {
                verinfo.append("文件大小").append(otaPackage.data.filesize / 1024 / 1024).append("M --- ")
                        .append(otaPackage.data.otaType == 0 ? "全量升级包" : "增量升级包").append("\n\n")
                        .append("发布说明：\n")
                        .append("").append(otaPackage.data.releaseNote);
            }
        }
        tv_verinfo.setText(verinfo.toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            FlyLog.d("onRequestPermissionsResult");
        }
    }

    public void upgrade(View view) {
        Flyup.getInstance().startUpVersion(Flyup.getInstance().getOtaPackage());
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
                upVersionInfo();
                break;
            //获取到最新版本
            case CODE_02:
                upVersionInfo();
                break;
            //获取最新版本失败
            case CODE_03:
                upVersionInfo();
                break;
            //获取最新版本失败，网络错误！
            case CODE_04:
                break;
            //正在下载升级包...
            case CODE_05:
                break;
            //下载升级包出错!
            case CODE_06:
                break;
            //正在校验升级包MD5值...
            case CODE_07:
                break;
            //升级包MD5值校验错误!
            case CODE_08:
                break;
            //升级包数据校验...
            case CODE_09:
                break;
            //准备安装升级包...
            case CODE_10:
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
    }
}