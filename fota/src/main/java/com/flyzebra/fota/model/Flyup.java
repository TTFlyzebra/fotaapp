package com.flyzebra.fota.model;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.os.UpdateEngine;
import android.os.UpdateEngineCallback;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import com.flyzebra.flydown.FlyDown;
import com.flyzebra.flydown.request.IFileReQuestListener;
import com.flyzebra.fota.bean.OtaPackage;
import com.flyzebra.fota.bean.RetPhoneLog;
import com.flyzebra.fota.bean.RetVersion;
import com.flyzebra.fota.config.OsEvent;
import com.flyzebra.fota.httpApi.ApiAction;
import com.flyzebra.fota.httpApi.ApiActionlmpl;
import com.flyzebra.utils.FileUtils;
import com.flyzebra.utils.FlyLog;
import com.flyzebra.utils.IDUtils;
import com.flyzebra.utils.SPUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

//adb shell升级OTA命令
//adb push ./update-xxxx.zip /data/update.zip
//uncrypt /data/update.zip /cache/recovery/block.map
//echo  "--update_package=@/cache/recovery/block.map"  > /cache/recovery/command
//reboot recovery
public class Flyup implements IFlyup, OsEvent {

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
    private AtomicBoolean isFirst = new AtomicBoolean(true);

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
    public void updateNewVersion() {
        if (isRunning.get()) {
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
                        if (isFirst.get()) {
                            if (resultVersion.code == 0 || resultVersion.code == 1) {
                                SPUtils.set(mContext, "PHONE_ID", mOtaPackage.phoneId);
                                apiAction.upPhoneLog((int) SPUtils.get(mContext, "PHONE_ID", -1), CODE_00,
                                        "系统首次启动!", (int) (SystemClock.elapsedRealtime() / 1000), new Observer<RetPhoneLog>() {
                                            @Override
                                            public void onSubscribe(Disposable d) {
                                            }

                                            @Override
                                            public void onNext(RetPhoneLog phoneLog) {
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                FlyLog.e("upPhoneLog onError!" + e);
                                            }

                                            @Override
                                            public void onComplete() {
                                                isFirst.set(false);
                                            }
                                        });
                            }
                        }
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
        if (isRunning.get()) return;
        mOtaPackage = otaPackage;
        if (mOtaPackage == null
                || TextUtils.isEmpty(mOtaPackage.downurl)
                || TextUtils.isEmpty(mOtaPackage.version)
                || TextUtils.isEmpty(mOtaPackage.md5sum)) {
            updateNewVersion();
        } else {
            isRunning.set(true);
            if (FlyDown.isFileDownFinish(otaPackage.md5sum)) {
                verityFileMd5(mOtaPackage);
            } else {
                downloadFile(mOtaPackage);
            }
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
    public void downloadFile(final OtaPackage otaPackage) {
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
                verityFileMd5(otaPackage);
            }

            @Override
            public void progress(final int progress) {
                notifyListener(CODE_05, Math.max(progress, 1), "正在下载升级包...");
            }
        };
        FlyDown.load(otaPackage.downurl).setThread(1).setFileName(otaPackage.md5sum).listener(listener).start();
    }

    @Override
    public void verityFileMd5(OtaPackage otaPackage) {
        notifyListener(CODE_07, 0, "开始校验升级包MD5值...");
        tHandler.post(new Runnable() {
            @Override
            public void run() {
                String md5sum = FileUtils.getFileMD5(FlyDown.getFilePath(otaPackage.md5sum));
                if (md5sum.equals(otaPackage.md5sum)) {
                    notifyListener(CODE_07, 100, "升级包MD5值校验成功...");
                    final File file = new File(FlyDown.getFilePath(otaPackage.md5sum));
                    updaterFile(file);
                } else {
                    FlyLog.e("verityOtaFile failed! md5sum=%s, fileName=%s", md5sum, FlyDown.getFilePath(md5sum));
                    isRunning.set(false);
                    FlyDown.delDownFile(otaPackage.md5sum);
                    notifyListener(CODE_08, 100, "升级包MD5值校验错误！");
                }
            }
        });
    }


    UpdateEngineCallback callback = new UpdateEngineCallback() {
        @Override
        public void onStatusUpdate(int i, float v) {
            notifyListener(CODE_11, Math.min((int)v * 100, 100), "正在升级系统, 步骤(" + i + "/5).");
        }

        @Override
        public void onPayloadApplicationComplete(int i) {
            isRunning.set(false);
            notifyListener(CODE_12, 100, "系统升级完成，需要重启系统才能生效！");
        }
    };

    @Override
    public void updaterFile(File file) {
        isRunning.set(true);
        tHandler.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                final String PAYLOAD_BIN_FILE = "payload.bin";
                final String PAYLOAD_PROPERTIES = "payload_properties.txt";
                final String FILE_URL_PREFIX = "file://";
                boolean payloadFound = false;
                long payloadOffset = 0;
                long payloadSize = 0;
                String[] props = null;
                try {
                    ZipFile zipFile = new ZipFile(file);
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        long fileSize = entry.getCompressedSize();
                        if (!payloadFound) {
                            payloadOffset += 30 + entry.getName().length();
                            if (entry.getExtra() != null) {
                                payloadOffset += entry.getExtra().length;
                            }
                        }

                        if (entry.isDirectory()) {
                            continue;
                        } else if (entry.getName().equals(PAYLOAD_BIN_FILE)) {
                            payloadSize = fileSize;
                            payloadFound = true;
                        } else if (entry.getName().equals(PAYLOAD_PROPERTIES)) {
                            try (BufferedReader buffer = new BufferedReader(
                                    new InputStreamReader(zipFile.getInputStream(entry)))) {
                                props = buffer.lines().toArray(String[]::new);
                            }catch (Exception e){
                                FlyLog.e(e.toString());
                                isRunning.set(false);
                                notifyListener(CODE_09, 100, "获取升级参数失败！");
                                return;
                            }
                        }
                        if (!payloadFound) {
                            payloadOffset += fileSize;
                        }
                    }
                }catch (Exception e){
                    FlyLog.e(e.toString());
                    isRunning.set(false);
                    notifyListener(CODE_10, 100, "升级文件校验失败！");
                }
                UpdateEngine mUpdateEngine = new UpdateEngine();
                mUpdateEngine.bind(callback);
                mUpdateEngine.applyPayload(
                        FILE_URL_PREFIX+file.getAbsolutePath(),
                        payloadOffset,
                        payloadSize,
                        props);
            }
        });
    }

    public void upPhoneLog(int event, String emsg) {
        if (mOtaPackage == null) {
//            FlyLog.e("no phoneId for upPhoneLog!");
            return;
        }

        apiAction.upPhoneLog((int) SPUtils.get(mContext, "PHONE_ID", -1),
                event, emsg, (int) (SystemClock.elapsedRealtime() / 1000), new Observer<RetPhoneLog>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(RetPhoneLog phoneLog) {
//                        FlyLog.d("onNext [%s]", phoneLog.toString());
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
