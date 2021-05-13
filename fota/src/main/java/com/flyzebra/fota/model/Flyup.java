package com.flyzebra.fota.model;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.RecoverySystem;
import android.text.TextUtils;

import com.flyzebra.flydown.FlyDown;
import com.flyzebra.flydown.request.IFileReQuestListener;
import com.flyzebra.fota.bean.OtaPackage;
import com.flyzebra.fota.bean.PhoneLog;
import com.flyzebra.fota.httpApi.ApiAction;
import com.flyzebra.fota.httpApi.ApiActionlmpl;
import com.flyzebra.utils.FileUtils;
import com.flyzebra.utils.FlyLog;
import com.flyzebra.utils.IDUtils;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

//adb shell升级OTA命令
//adb push ./update-xxxx.zip /data/update.zip
//uncrypt /data/update.zip /cache/recovery/block.map
//echo  "--update_package=@/cache/recovery/block.map"  > /cache/recovery/command
//reboot recovery
public class Flyup implements IFlyup, IFlyCode {

    private static final HandlerThread mTaskThread = new HandlerThread("fota_thread");

    static {
        mTaskThread.start();
    }

    private static final Handler tHandler = new Handler(mTaskThread.getLooper());
    private static Handler mHandler = new Handler(Looper.getMainLooper());

    private Context mContext;
    private AtomicBoolean isUpdaterRunning = new AtomicBoolean(false);

    private ApiAction apiAction;

    private List<FlyupResult> flyupResults = new ArrayList<>();
    private OtaPackage mOtaPackage;
    private int lastCode = 0;
    private int lastProgress = 0;
    private String lastMessage = "";


    @Override
    public void init(Context context) {
        FlyDown.mCacheDir = "/data/cache/recovery";
        mContext = context;
    }

    @Override
    public boolean isUpVeriosnRunning() {
        return isUpdaterRunning.get();
    }

    @Override
    public OtaPackage getOtaPackage() {
        return mOtaPackage;
    }

    @Override
    public int getLastCode() {
        return lastCode;
    }

    @Override
    public int getLastProgress() {
        return lastProgress;
    }

    @Override
    public String getLastMessage() {
        return lastMessage;
    }

    @Override
    public void addListener(FlyupResult flyupResult) {
        flyupResults.add(flyupResult);
    }

    @Override
    public void removeListener(FlyupResult flyupResult) {
        flyupResults.remove(flyupResult);
    }

    @Override
    public void startUpVersion() {
        notifyListener(CODE_00, 0, "系统更新程序开始运行...");
        if (isUpdaterRunning.get()) {
            notifyListener(CODE_91, 0, "系统更新正在运行！");
            return;
        }
        isUpdaterRunning.set(true);
        if (apiAction == null) {
            apiAction = new ApiActionlmpl();
        }
        apiAction.getUpVersion(IDUtils.getModel(mContext), IDUtils.getVersion(mContext), IDUtils.getIMEI(mContext),
                IDUtils.getSnUid(mContext), IDUtils.getAndroidID(mContext), new Observer<OtaPackage>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(@NonNull OtaPackage otaPackage) {
                        FlyLog.d("getUpVersion OK [%s]", otaPackage.data.version);
                        mOtaPackage = otaPackage;
                        tHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (otaPackage.code == 0) {
                                    FlyDown.delOtherFile(otaPackage.data.md5sum);
                                    notifyListener(CODE_02, 0, "新版本" + otaPackage.data.version + "...");
                                    if (otaPackage.data.upType == 1) {
                                        if (FlyDown.isFileDownFinish(otaPackage.data.md5sum)) {
                                            verityOtaFile(otaPackage);
                                        } else {
                                            downloadOtaFile(otaPackage);
                                        }
                                    } else {
                                        isUpdaterRunning.set(false);
                                        notifyListener(CODE_92, 0, "需要手动更新版本！");
                                    }
                                } else if (otaPackage.code == 1) {
                                    isUpdaterRunning.set(false);
                                    FlyDown.delAllDownFile();
                                    notifyListener(CODE_01, 100, "已更新到最新版本！");
                                } else {
                                    isUpdaterRunning.set(false);
                                    notifyListener(CODE_03, 100, "获取最新版本失败！");
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        FlyLog.e(e.toString());
                        notifyListener(CODE_04, 100, "获取最新版本失败，网络错误！");
                        isUpdaterRunning.set(false);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    @Override
    public void startUpVersion(OtaPackage otaPackage) {
        if (otaPackage == null || otaPackage.data == null
                || TextUtils.isEmpty(otaPackage.data.downurl)
                || TextUtils.isEmpty(otaPackage.data.version)
                || TextUtils.isEmpty(otaPackage.data.md5sum)) {
            startUpVersion();
            return;
        }
        mOtaPackage = otaPackage;
        if (isUpdaterRunning.get()) {
            notifyListener(CODE_91, 0, "系统更新正在运行！");
            return;
        }
        isUpdaterRunning.set(true);
        notifyListener(CODE_02, 0, "手动更新版本" + otaPackage.data.version + "...");
        if (FlyDown.isFileDownFinish(otaPackage.data.md5sum)) {
            verityOtaFile(otaPackage);
        } else {
            downloadOtaFile(otaPackage);
        }
    }

    @Override
    public void stopUpVersion() {
        tHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
    }

    public void notifyListener(final int code, final int progress, final String msg) {
        lastCode = code;
        lastProgress = progress;
        lastMessage = msg;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (FlyupResult flyupResult : flyupResults) {
                    flyupResult.upVesionProgress(code, progress, msg);
                }
            }
        });
    }

    public void downloadOtaFile(final OtaPackage otaPackage) {
        notifyListener(CODE_05, 0, "开始下载升级包...");
        IFileReQuestListener listener = new IFileReQuestListener() {
            @Override
            public void error(String url, int ErrorCode) {
                isUpdaterRunning.set(false);
                notifyListener(CODE_06, 100, "下载升级包出错！");
            }

            @Override
            public void finish(String saveName) {
                notifyListener(CODE_05, 100, "升级包下载完成...");
                tHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        verityOtaFile(otaPackage);
                    }
                });
            }

            @Override
            public void progress(final int progress) {
                notifyListener(CODE_05, Math.max(progress, 1), "正在下载升级包...");
            }
        };
        FlyDown.load(otaPackage.data.downurl).setThread(1).setFileName(otaPackage.data.md5sum).listener(listener).start();
    }

    private void verityOtaFile(OtaPackage otaPackage) {
        notifyListener(CODE_07, 0, "开始校验升级包MD5值...");
        tHandler.post(new Runnable() {
            @Override
            public void run() {
                String md5sum = FileUtils.getFileMD5(FlyDown.getFilePath(otaPackage.data.md5sum));
                if (md5sum.equals(otaPackage.data.md5sum)) {
                    notifyListener(CODE_07, 100, "升级包MD5值校验成功...");
                    try {
                        final File file = new File(FlyDown.getFilePath(otaPackage.data.md5sum));
                        notifyListener(CODE_09, 0, "开始升级包数据校验...");
                        RecoverySystem.verifyPackage(file, new RecoverySystem.ProgressListener() {
                            @Override
                            public void onProgress(int i) {
                                if (i >= 100) {
                                    notifyListener(CODE_09, 100, "升级包数据校验完成...");
                                    tHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                notifyListener(CODE_10, 100, "开始安装升级包...");
                                                RecoverySystem.installPackage(mContext, file);
                                            } catch (IOException e) {
                                                notifyListener(CODE_12, 100, "安装升级包错误！");
                                                FlyLog.e(e.toString());
                                            }
                                        }
                                    });
                                }
                            }
                        }, null);
                        FlyLog.e("update ota file =%s", file.getAbsolutePath());
                    } catch (GeneralSecurityException | IOException e) {
                        notifyListener(CODE_11, 100, "升级包数据校验错误！");
                        FlyLog.e(e.toString());
                    }
                } else {
                    FlyLog.e("verityOtaFile failed! md5sum=%s, fileName=%s", md5sum, FlyDown.getFilePath(md5sum));
                    isUpdaterRunning.set(false);
                    FlyDown.delDownFile(otaPackage.data.md5sum);
                    notifyListener(CODE_08, 100, "升级包MD5值校验错误！");
                }
            }
        });
    }

    public void upPhoneLog(int event, String emsg) {
        if (mOtaPackage == null || mOtaPackage.data == null ) {
            FlyLog.e("no phoneId for upPhoneLog!");
            return;
        }

        apiAction.upPhoneLog(mOtaPackage.data.phoneId, event, emsg, new Observer<PhoneLog>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(PhoneLog phoneLog) {
                FlyLog.d("onNext [%s]", phoneLog.toString());
            }

            @Override
            public void onError(Throwable e) {
                FlyLog.e("upPhoneLog onError!" + e);
            }

            @Override
            public void onComplete() {
            }
        });
    }

    private static class FlyUpdateHolder {
        public static final Flyup sInstance = new Flyup();
    }

    public static Flyup getInstance() {
        return Flyup.FlyUpdateHolder.sInstance;
    }

}
