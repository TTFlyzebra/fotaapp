package com.flyzebra.fota;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.flyzebra.fota.fragment.AllotaFragment;
import com.flyzebra.fota.fragment.FileFragment;
import com.flyzebra.fota.fragment.MainFragment;
import com.flyzebra.fota.fragment.SettingsFragment;
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
    int passWordCount1 = 0;
    int passWordCount2 = 0;
    Rect[] rect;
    int setp = 200;
    int passWords1[] = new int[]{0, 1, 3, 2};
    int passWords2[] = new int[]{0, 2, 3, 1};

    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (String s : PERMISSIONS_STORAGE) {
            if (ActivityCompat.checkSelfPermission(this, s) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
                break;
            }
        }

        startService(new Intent(this, MainService.class));
        replaceFragMent(new MainFragment());
    }

    private void initRect() {
        int width = getWindow().getDecorView().getWidth();
        int height = getWindow().getDecorView().getHeight();;
        setp = width / 8;
        rect = new Rect[]{new Rect(0, 0, setp, height/2),
                new Rect(width - setp, 0, width, height/2),
                new Rect(0, height/2, setp, height),
                new Rect(width - setp, height/2, width, height)};
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void replaceFragMent(Fragment fragment) {
        mFragment = fragment;
        FragmentManager fm = getSupportFragmentManager();
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
            initRect();
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            if (rect[passWords1[passWordCount1]].contains(x, y)) {
                passWordCount1++;
            } else {
                passWordCount1 = 0;
            }
            if (passWordCount1 >= passWords1.length) {
                passWordCount1 = 0;
                replaceFragMent(new AllotaFragment());
            }

            if (rect[passWords2[passWordCount2]].contains(x, y)) {
                passWordCount2++;
            } else {
                passWordCount2 = 0;
            }
            if (passWordCount2 >= passWords2.length) {
                passWordCount2 = 0;
                replaceFragMent(new FileFragment());
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if (!(mFragment instanceof MainFragment)) {
            replaceFragMent(new MainFragment());
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
//            case R.id.action_main:
//                if (!(mFragment instanceof MainFragment))
//                    replaceFragMent(new MainFragment());
//                break;
            case R.id.action_settings:
                if (!(mFragment instanceof SettingsFragment))
                    replaceFragMent(new SettingsFragment());
                break;
//            case R.id.action_exit:
//                onBackPressed();
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, MainService.class));
        super.onDestroy();
    }

}