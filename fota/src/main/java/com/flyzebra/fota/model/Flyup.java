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
import com.flyzebra.fota.bean.RetPhoneLog;
import com.flyzebra.fota.bean.RetVersion;
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
    private static final Handler mHandler = new Handler(Looper.getMainLooper());

    private Context mContext;
    private AtomicBoolean isRunning = new AtomicBoolean(false);

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
    public boolean isRunning() {
        return isRunning.get();
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
    public void checkNewVersion() {
        notifyListener(CODE_00, 0, "系统更新程序开始运行...");
        if (isRunning.get()) {
            notifyListener(CODE_91, 0, "系统更新正在运行！");
            return;
        }
        isRunning.set(true);
        if (apiAction == null) {
            apiAction = new ApiActionlmpl();
        }
        apiAction.getUpVersion(IDUtils.getModel(mContext), IDUtils.getVersion(mContext), IDUtils.getIMEI(mContext),
                IDUtils.getSnUid(mContext), IDUtils.getAndroidID(mContext), new Observer<RetVersion>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(@NonNull RetVersion resultVersion) {
                        mOtaPackage = resultVersion.data;
                        FlyLog.d("getUpVersion OK [%s]", mOtaPackage.version);
                        if (resultVersion.code == 0) {
                            FlyDown.delOtherFile(mOtaPackage.md5sum);
                            notifyListener(CODE_02, 0, "新版本" + mOtaPackage.version + "...");
                            isRunning.set(false);
                            if (mOtaPackage.upType == 1) {
                                updaterOtaPackage(mOtaPackage);
                            } else {
                                notifyListener(CODE_92, 0, "需要手动更新版本！");
                            }
                        } else if (resultVersion.code == 1) {
                            isRunning.set(false);
                            FlyDown.delAllDownFile();
                            notifyListener(CODE_01, 100, "已更新到最新版本！");
                        } else {
                            isRunning.set(false);
                            notifyListener(CODE_03, 100, "获取最新版本失败！");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        FlyLog.e(e.toString());
                        notifyListener(CODE_04, 100, "获取最新版本失败，网络错误！");
                        isRunning.set(false);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    @Override
    public void updaterOtaPackage(OtaPackage otaPackage) {
        if (isRunning.get()) {
            notifyListener(CODE_91, 0, "系统更新正在运行！");
            return;
        }
        isRunning.set(true);
        mOtaPackage = otaPackage;
        if (mOtaPackage == null
                || TextUtils.isEmpty(mOtaPackage.downurl)
                || TextUtils.isEmpty(mOtaPackage.version)
                || TextUtils.isEmpty(mOtaPackage.md5sum)) {
            isRunning.set(false);
            return;
        }
        if (FlyDown.isFileDownFinish(otaPackage.md5sum)) {
            verityOtaFile(mOtaPackage);
        } else {
            downloadOtaPackage(mOtaPackage);
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

    @Override
    public void downloadOtaPackage(final OtaPackage otaPackage) {
        notifyListener(CODE_05, 0, "开始下载升级包...");
        IFileReQuestListener listener = new IFileReQuestListener() {
            @Override
            public void error(String url, int ErrorCode) {
                isRunning.set(false);
                notifyListener(CODE_06, 100, "下载升级包出错！");
            }

            @Override
            public void finish(String saveName) {
                notifyListener(CODE_05, 100, "升级包下载完成...");
                verityOtaFile(otaPackage);
            }

            @Override
            public void progress(final int progress) {
                notifyListener(CODE_05, Math.max(progress, 1), "正在下载升级包...");
            }
        };
        FlyDown.load(otaPackage.downurl).setThread(1).setFileName(otaPackage.md5sum).listener(listener).start();
    }

    @Override
    public void verityOtaFile(OtaPackage otaPackage) {
        notifyListener(CODE_07, 0, "开始校验升级包MD5值...");
        tHandler.post(new Runnable() {
            @Override
            public void run() {
                String md5sum = FileUtils.getFileMD5(FlyDown.getFilePath(otaPackage.md5sum));
                if (md5sum.equals(otaPackage.md5sum)) {
                    notifyListener(CODE_07, 100, "升级包MD5值校验成功...");
                    try {
                        final File file = new File(FlyDown.getFilePath(otaPackage.md5sum));
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
                    isRunning.set(false);
                    FlyDown.delDownFile(otaPackage.md5sum);
                    notifyListener(CODE_08, 100, "升级包MD5值校验错误！");
                }
            }
        });
    }

    public void upPhoneLog(int event, String emsg) {
        if (mOtaPackage == null) {
            FlyLog.e("no phoneId for upPhoneLog!");
            return;
        }

        apiAction.upPhoneLog(mOtaPackage.phoneId, event, emsg, new Observer<RetPhoneLog>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(RetPhoneLog phoneLog) {
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
