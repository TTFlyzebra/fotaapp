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
import com.flyzebra.fota.view.NotificationView;
import com.flyzebra.utils.FileUtils;
import com.flyzebra.utils.FlyLog;
import com.flyzebra.utils.IDUtils;
import com.flyzebra.utils.SystemPropTools;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

//adb shell升级OTA命令
//adb push ./update-xxxx.zip /data/update.zip
//uncrypt /data/update.zip /cache/recovery/block.map
//echo  "--update_package=@/cache/recovery/block.map"  > /cache/recovery/command
//reboot recovery

public class MainService extends Service implements Runnable {
    private static final HandlerThread mTaskThread = new HandlerThread("fota_service");

    static {
        mTaskThread.start();
    }

    private static final Handler tHandler = new Handler(mTaskThread.getLooper());
    private static final Handler mHandler = new Handler(Looper.getMainLooper());
    private String imei;
    private String aid;
    private String uid;
    private String ver;
    private String sid;
    private ApiAction apiAction = new ApiActionlmpl();

    private ProgressDialog dialog;
    private AtomicInteger vProcess = new AtomicInteger(0);
    private OtaPackage mOtaPackage;
    private AtomicBoolean isUpdaterRunning = new AtomicBoolean(false);

    private static final int CHECK_TIME = 60000 * 60 * 1;

    private NotificationView notificationView;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FlyDown.mCacheDir = getFilesDir().getAbsolutePath();

        notificationView = new NotificationView(this);

        sid = "OC_VLTE";
        ver = SystemPropTools.get("persist.vendor.display.id", "CM3003_V5.0.0_20210010100_USER");
        imei = IDUtils.getIMEI(this);
        uid = "ff.ff.ff.ff".toLowerCase();
        aid = IDUtils.getAndroidID(this);

        dialog = new ProgressDialog(this);
        dialog.setTitle("Android系统更新");
        dialog.setMessage("......");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMax(100);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        mHandler.post(this);

        notificationView.show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        notificationView.hide();

        tHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void updateUIMessage(final String msg, final int progress) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                dialog.show();
                dialog.setMessage(msg);
                dialog.setProgress(progress);
            }
        });
    }

    @Override
    public void run() {
        FlyLog.d("fota service running.....");
        dialog.hide();
        if (!isUpdaterRunning.get()) {
            mOtaPackage = null;
            isUpdaterRunning.set(true);
            apiAction.getUpVersion(sid, ver, imei, uid, aid, new Observer<OtaPackage>() {
                @Override
                public void onSubscribe(Disposable d) {
                    FlyLog.d("onSubscribe:" + d);
                }

                @Override
                public void onNext(OtaPackage otaPackage) {
                    if (otaPackage.code == 0) {
                        FlyLog.d("Get version ok, %s", otaPackage.data.version);
                        mOtaPackage = otaPackage;
                        dialog.show();
                        File saveFile = new File(FlyDown.mCacheDir + "/" + otaPackage.data.md5sum + ".zip");
                        File tempFile = new File(FlyDown.mCacheDir + "/" + otaPackage.data.md5sum + ".fly");
                        if (saveFile.exists() && !tempFile.exists()) {
                            tHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    verityOtaFile(mOtaPackage, FlyDown.mCacheDir + "/" + otaPackage.data.md5sum + ".zip");
                                }
                            });
                        } else {
                            downOtaFile(mOtaPackage);
                        }

                    } else if(otaPackage.code == 1) {
                        FlyLog.d("Get version ok, %s", otaPackage.msg);
                        dialog.dismiss();
                        isUpdaterRunning.set(false);
                        FlyDown.delAllDownFile();
                    }else{
                        FlyLog.e("Get version failed! %s", otaPackage.msg);
                        dialog.dismiss();
                        isUpdaterRunning.set(false);
                        FlyDown.delAllDownFile();
                    }
                }

                @Override
                public void onError(Throwable e) {
                    FlyLog.e("onError:" + e.toString());
                    isUpdaterRunning.set(false);
                }

                @Override
                public void onComplete() {
                    if (mOtaPackage == null) {
                        isUpdaterRunning.set(false);
                    }
                    FlyLog.d("onComplete");
                }
            });
        }
        mHandler.postDelayed(this, CHECK_TIME);
    }

    public void downOtaFile(final OtaPackage otaPackage) {
        FlyLog.d("-----start downFile-----\n");
        IFileReQuestListener listener = new IFileReQuestListener() {
            @Override
            public void error(String url, int ErrorCode) {
                FlyLog.e("--onError----%s,%d\n", url, ErrorCode);
                isUpdaterRunning.set(false);
                mHandler.removeCallbacksAndMessages(null);
                updateUIMessage("下载安装包失败, 1分钟后重试......", 100);
                mHandler.postDelayed(MainService.this, 60000);
            }

            @Override
            public void finish(String saveName) {
                FlyLog.e("--onFinish----%s\n", saveName);
                updateUIMessage("安装包下载完成，准备安装更新......", 100);
                tHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        verityOtaFile(otaPackage, saveName);
                    }
                });
            }

            @Override
            public void progress(final int progress) {
                updateUIMessage("正在下载安装包......", progress);
            }
        };
        FlyDown.load(otaPackage.data.downurl).setThread(5).setFileName(otaPackage.data.md5sum).listener(listener).start();
    }

    private void verityOtaFile(OtaPackage otaPackage, String saveName) {
        updateUIMessage(" 正在校验安装包MD5值......", 100);
        tHandler.post(new Runnable() {
            @Override
            public void run() {
                FlyLog.d("verityOtaFile %s", saveName);
                String md5sum = FileUtils.getFileMD5(saveName);
                if (md5sum.equals(otaPackage.data.md5sum)) {
                    updateUIMessage("安装包MD5值校验成功......", 100);
                    upOta(saveName);
                } else {
                    FlyLog.e("verityOtaFile failed! md5sum=%s, fileName=%s", md5sum, saveName);
                    isUpdaterRunning.set(false);
                    FlyDown.delAllDownFile();
                    mHandler.removeCallbacksAndMessages(null);
                    updateUIMessage("安装包MD5值校验失败, 1分钟后重试......", 100);
                    mHandler.postDelayed(MainService.this, 60000);
                }
            }
        });
    }

    public void upOta(String fileName) {
        try {
            updateUIMessage("正在检测系统安装包......", 100);
            final File file = new File(fileName);
            if (file != null && file.exists() && !file.isDirectory()) {
                vProcess.set(0);
                RecoverySystem.verifyPackage(file, new RecoverySystem.ProgressListener() {
                    @Override
                    public void onProgress(int i) {
                        vProcess.set(i);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                dialog.setProgress(vProcess.get());
                                if (vProcess.get() < 100) {
                                    if (!dialog.isShowing()) {
                                        dialog.show();
                                    }
                                } else {
                                    if (dialog.isShowing()) {
                                        dialog.dismiss();
                                    }
                                    vProcess.set(0);
                                    FlyLog.e("verifyPackage finish!");
                                    tHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                RecoverySystem.installPackage(MainService.this, file);
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
                FlyLog.e("update file path=%s", file.getAbsolutePath());
            } else {
                FlyLog.e("can not find update.zip!");
            }
        } catch (GeneralSecurityException | IOException e) {
            FlyLog.e(e.toString());
        }
    }
}
