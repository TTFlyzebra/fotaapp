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
import com.flyzebra.utils.SystemPropTools;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observer;
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
    private AtomicBoolean isStop = new AtomicBoolean(false);

    private String imei;
    private String aid;
    private String uid;
    private String ver;
    private String sid;
    private ApiAction apiAction;

    private List<FlyupResult> flyupResults = new ArrayList<>();
    private OtaPackage mOtaPackage;
    private int lastCode = 0;
    private int lastProgress = 0;
    private String lastMessage = "";


    @Override
    public void init(Context context) {
//        FlyDown.mCacheDir = context.getFilesDir().getAbsolutePath();
        FlyDown.mCacheDir = "/data/cache/recovery";
        mContext = context;
        sid = "OC_VLTE";
        ver = SystemPropTools.get("persist.vendor.display.id", "CM3003_V1.0.0_20210010000_USER");
        imei = IDUtils.getIMEI(context);
        uid = "FF.FF.FF.FF";
        aid = IDUtils.getAndroidID(context);
        if (TextUtils.isEmpty(imei)) {
            imei = "0";
        } else {
            imei = imei.toUpperCase();
        }
        if (TextUtils.isEmpty(aid)) {
            aid = "0";
        } else {
            aid = aid.toUpperCase();
        }
    }

    @Override
    public void startUpVersion() {
        notifyListener(CODE_00, 0, "系统更新程序开始运行......");
        if (isUpdaterRunning.get()) {
            notifyListener(CODE_91, 0, "系统更新正在运行！");
            return;
        }
        isUpdaterRunning.set(true);
        isStop.set(false);
        if (apiAction == null) {
            apiAction = new ApiActionlmpl();
        }
        apiAction.getUpVersion(sid, ver, imei, uid, aid, new Observer<OtaPackage>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(OtaPackage otaPackage) {
                FlyLog.d("getUpVersion OK [%s]", otaPackage.data.version);
                mOtaPackage = otaPackage;
                if (otaPackage.code == 0) {
                    notifyListener(CODE_02, 0, "发现新版本" + otaPackage.data.version);
                    if (otaPackage.data.upType == 1) {
                        File saveFile = new File(FlyDown.mCacheDir + "/" + otaPackage.data.md5sum + ".zip");
                        File tempFile = new File(FlyDown.mCacheDir + "/" + otaPackage.data.md5sum + ".fly");
                        if (saveFile.exists() && !tempFile.exists()) {
                            tHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    verityOtaFile(otaPackage, FlyDown.mCacheDir + "/" + otaPackage.data.md5sum + ".zip");
                                }
                            });
                        } else {
                            tHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    downloadOtaFile(otaPackage);
                                }
                            });
                        }
                    }else {
                        isUpdaterRunning.set(false);
                        notifyListener(CODE_92, 0, "需要手动更新版本!");
                    }
                } else if (otaPackage.code == 1) {
                    isUpdaterRunning.set(false);
                    FlyDown.delAllDownFile();
                    notifyListener(CODE_01, 100, "已是最新版本！");
                } else {
                    isUpdaterRunning.set(false);
                    FlyDown.delAllDownFile();
                    notifyListener(CODE_03, 100, "获取最新版本失败！");
                }
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
        if(otaPackage==null||otaPackage.data==null
                ||TextUtils.isEmpty(otaPackage.data.downurl)
                ||TextUtils.isEmpty(otaPackage.data.version)
                ||TextUtils.isEmpty(otaPackage.data.md5sum)) {
            startUpVersion();
            return;
        }
        mOtaPackage = otaPackage;
        if (isUpdaterRunning.get()) {
            notifyListener(CODE_91, 0, "系统更新正在运行！");
            return;
        }
        isUpdaterRunning.set(true);
        isStop.set(false);
        notifyListener(CODE_02, 0, "更新到指定版本：" + otaPackage.data.version);
        File saveFile = new File(FlyDown.mCacheDir + "/" + otaPackage.data.md5sum + ".zip");
        File tempFile = new File(FlyDown.mCacheDir + "/" + otaPackage.data.md5sum + ".fly");
        if (saveFile.exists() && !tempFile.exists()) {
            tHandler.post(new Runnable() {
                @Override
                public void run() {
                    verityOtaFile(otaPackage, FlyDown.mCacheDir + "/" + otaPackage.data.md5sum + ".zip");
                }
            });
        } else {
            tHandler.post(new Runnable() {
                @Override
                public void run() {
                    downloadOtaFile(otaPackage);
                }
            });
        }
    }

    @Override
    public void stopUpVersion() {
        isStop.set(true);
        tHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
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
        notifyListener(CODE_05, 0, "正在下载升级包......");
        IFileReQuestListener listener = new IFileReQuestListener() {
            @Override
            public void error(String url, int ErrorCode) {
                isUpdaterRunning.set(false);
                notifyListener(CODE_06, 100, "下载升级包出错!");
            }

            @Override
            public void finish(String saveName) {
                notifyListener(CODE_05, 100, "升级包下载完成......");
                tHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        verityOtaFile(otaPackage, saveName);
                    }
                });
            }

            @Override
            public void progress(final int progress) {
                notifyListener(CODE_05, progress, "正在下载升级包......");
            }
        };
        FlyDown.load(otaPackage.data.downurl).setThread(1).setFileName(otaPackage.data.md5sum).listener(listener).start();
    }

    private void verityOtaFile(OtaPackage otaPackage, String saveName) {
        notifyListener(CODE_07, 0, "正在校验升级包MD5值......");
        tHandler.post(new Runnable() {
            @Override
            public void run() {
                String md5sum = FileUtils.getFileMD5(saveName);
                if (md5sum.equals(otaPackage.data.md5sum)) {
                    notifyListener(CODE_07, 100, "升级包MD5值校验成功!");
                    try {
                        final File file = new File(saveName);
                        notifyListener(CODE_09, 100, "升级包数据校验......");
                        RecoverySystem.verifyPackage(file, new RecoverySystem.ProgressListener() {
                            @Override
                            public void onProgress(int i) {
                                notifyListener(CODE_09, i, "升级包数据校验......");
                                if (i >= 100) {
                                    tHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                notifyListener(CODE_10, 100, "安装升级包......");
                                                RecoverySystem.installPackage(mContext, file);
                                            } catch (IOException e) {
                                                notifyListener(CODE_12, 100, "安装升级包错误!");
                                                FlyLog.e(e.toString());
                                            }
                                        }
                                    });
                                }
                            }
                        }, null);
                        FlyLog.e("update ota file =%s", file.getAbsolutePath());
                    } catch (GeneralSecurityException | IOException e) {
                        notifyListener(CODE_11, 100, "升级包数据校验错误!");
                        FlyLog.e(e.toString());
                    }
                } else {
                    FlyLog.e("verityOtaFile failed! md5sum=%s, fileName=%s", md5sum, saveName);
                    isUpdaterRunning.set(false);
                    FlyDown.delDownFile(saveName);
                    notifyListener(CODE_08, 100, "升级包MD5值校验错误!");
                }
            }
        });
    }

    public void upPhoneLog(int event, String emsg){
        if(mOtaPackage==null||mOtaPackage.data==null) {
            FlyLog.e("no phoneId for upPhoneLog!");
            return;
        }

        apiAction.upPhoneLog(mOtaPackage.data.phoneId,event,emsg, new Observer<PhoneLog>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(PhoneLog phoneLog) {
                FlyLog.e("onNext [%s]",phoneLog.toString());
            }

            @Override
            public void onError(Throwable e) {
                FlyLog.e("upPhoneLog onError!"+e);
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
