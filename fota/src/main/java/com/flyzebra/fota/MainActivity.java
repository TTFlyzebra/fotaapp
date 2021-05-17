package com.flyzebra.fota;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.flyzebra.fota.fragment.AllotaFragment;
import com.flyzebra.fota.fragment.MainFragment;
import com.flyzebra.utils.FlyLog;

public class MainActivity extends AppCompatActivity {
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static int REQUEST_PERMISSION_CODE = 101;

    /**
     * 添加进入AllApps后门
     */
    int passWordCount = 0;
    int setp = 200;
    Rect rect[] = new Rect[]{new Rect(0, 240, setp, 240 + setp),
            new Rect(1080 - setp, 240, 1080, 240 + setp),
            new Rect(0, 1920 - setp, setp, 1920),
            new Rect(1080 - setp, 1920 - setp, 1080, 1920)};
    int passWords[] = new int[]{0, 1, 3, 2};

    private Fragment mFragment;

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

        rePlaceFragMent(new MainFragment());
    }

    public void rePlaceFragMent(Fragment fragment) {
        mFragment = fragment;
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.ac_fm01, fragment);
        ft.commit();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            FlyLog.d("onRequestPermissionsResult");
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            if (rect[passWords[passWordCount]].contains(x, y)) {
                passWordCount++;
            } else {
                passWordCount = 0;
            }
            if (passWordCount >= passWords.length) {
                passWordCount = 0;
                rePlaceFragMent(new AllotaFragment());
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if(mFragment instanceof AllotaFragment){
            rePlaceFragMent(new MainFragment());
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_exit:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}