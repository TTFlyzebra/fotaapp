package com.flyzebra.fota;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.RecoverySystem;
import android.view.WindowManager;

import com.flyzebra.flydown.FlyDown;
import com.flyzebra.flydown.request.IFileReQuestListener;
import com.flyzebra.fota.bean.OtaPackage;
import com.flyzebra.fota.httpApi.ApiAction;
import com.flyzebra.fota.httpApi.ApiActionlmpl;
import com.flyzebra.utils.FlyLog;
import com.flyzebra.utils.IDUtils;
import com.flyzebra.utils.SystemPropTools;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

//adb shell升级OTA命令
//adb push ./update-xxxx.zip /data/update.zip
//uncrypt /data/update.zip /cache/recovery/block.map
//echo  "--update_package=@/cache/recovery/block.map"  > /cache/recovery/command
//reboot recovery

public class MainService extends Service {
    private static final HandlerThread mTaskThread = new HandlerThread("fota_service");

    static {
        mTaskThread.start();
    }

    private static final Handler tHandler = new Handler(mTaskThread.getLooper());
    private static final Handler mHandler = new Handler(Looper.getMainLooper());
    private String imei = "";
    private String aid = "";
    private ApiAction apiAction = new ApiActionlmpl();

    private ProgressDialog progressDialog;
    private AtomicInteger vProcess = new AtomicInteger(0);
    private OtaPackage mOtaPackage;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        imei = IDUtils.getIMEI(this);
        aid = IDUtils.getAndroidID(this);

        tHandler.post(new Runnable() {
            @Override
            public void run() {
                apiAction.getUpVersion(imei, aid, new Observer<OtaPackage>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(OtaPackage otaPackage) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
                upgrade("/data/update.zip");
                //tHandler.postDelayed(this, 60000);
            }
        });
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setTitle("正在校验安装包");
        progressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        apiAction.getUpVersion(SystemPropTools.get("persist.vendor.display.id", ""), aid, new Observer<OtaPackage>() {
            @Override
            public void onSubscribe(Disposable d) {
                FlyLog.e("onSubscribe:" + d);
            }

            @Override
            public void onNext(OtaPackage otaPackage) {
                FlyLog.e("noNext:" + otaPackage);
                mOtaPackage = otaPackage;
                downFile(mOtaPackage);
            }

            @Override
            public void onError(Throwable e) {
                FlyLog.e("onError:" + e);
            }

            @Override
            public void onComplete() {
                FlyLog.e("onComplete");
            }
        });

        FlyLog.d("IMEI=%s", IDUtils.getIMEI(this));
        FlyLog.d("IMSI=%s", IDUtils.getIMSI(this));
        FlyLog.d("AndroidID=%s", IDUtils.getAndroidID(this));
    }

    @Override
    public void onDestroy() {
        tHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    public void upgrade(String filePath) {
        FlyLog.d("upgrade %s", filePath);
        try {
            final File updateFile = new File(filePath);
            if (updateFile.exists()) {
                vProcess.set(0);
                RecoverySystem.verifyPackage(updateFile, new RecoverySystem.ProgressListener() {
                    @Override
                    public void onProgress(int i) {
                        vProcess.set(i);
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
                                    vProcess.set(0);
                                    FlyLog.e("verifyPackage finish!");
                                    tHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                RecoverySystem.installPackage(MainService.this, updateFile);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }, null);
                FlyLog.e("update file path=%s", updateFile.getAbsolutePath());
            } else {
                FlyLog.e("can not find update.zip!");
            }
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public void downFile(OtaPackage otaPackage) {
        FlyLog.d("-----start downFile-----\n");
        FlyDown.mCacheDir = getFilesDir().getAbsolutePath();
        String downUrl = otaPackage.data.downurl;
        IFileReQuestListener listener = new IFileReQuestListener() {
            @Override
            public void Error(String url, int ErrorCode) {
                FlyLog.e("--onError----%s,%d\n", url, ErrorCode);
            }

            @Override
            public void Finish(String url) {
                FlyLog.e("--onFinish----%s\n", url);
                upgrade(url);
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
}
