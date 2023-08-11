package com.flyzebra.fota.model;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.PowerManager;
import android.os.UpdateEngine;
import android.os.UpdateEngineCallback;
import android.text.TextUtils;

import com.flyzebra.flydown.FlyDown;
import com.flyzebra.flydown.request.IFileReQuestListener;
import com.flyzebra.fota.Config;
import com.flyzebra.fota.bean.OtaPackage;
import com.flyzebra.fota.bean.RetVersion;
import com.flyzebra.fota.httpApi.ApiAction;
import com.flyzebra.fota.httpApi.ApiActionlmpl;
import com.flyzebra.utils.FileUtils;
import com.flyzebra.utils.FlyLog;
import com.flyzebra.utils.IDUtil;
import com.flyzebra.utils.SPUtil;

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

public class Flyup implements IFlyup, FlyEvent {

    private static final HandlerThread mTaskThread = new HandlerThread("fota_thread");

    static {
        mTaskThread.start();
    }

    private static final Handler tHandler = new Handler(mTaskThread.getLooper());
    private static final Handler mHandler = new Handler(Looper.getMainLooper());

    private Context mContext;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private PowerManager.WakeLock wakeLock;
    private final AtomicBoolean isFinish = new AtomicBoolean(false);

    private ApiAction apiAction = new ApiActionlmpl();

    private final List<FlyupResult> flyupResults = new ArrayList<>();
    private OtaPackage mOtaPackage;
    private int lastCode = 0;
    private int lastProgress = 0;
    private String lastMessage = "";
    private final UpdateEngine mUpdateEngine = new UpdateEngine();

    @Override
    public void init(Context context) {
        FlyDown.mCacheDir = "/data/cache/recovery";
        File file = new File(FlyDown.mCacheDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        mContext = context;
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, mContext.getPackageName());
    }

    @Override
    public boolean isRunning() {
        return isRunning.get();
    }

    @Override
    public boolean isFinish() {
        return isFinish.get();
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
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        apiAction.getUpVersion(
                IDUtil.getModel(mContext),
                IDUtil.getVersion(mContext),
                IDUtil.getIMEI(mContext),
                new Observer<RetVersion>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull RetVersion resultVersion) {
                        isRunning.set(false);
                        wakeLock.release();
                        mOtaPackage = resultVersion.data;
                        FlyLog.d("getUpVersion OK [%s]", mOtaPackage.version);
                        if (resultVersion.code == 0) {
                            FlyDown.delOtherFile(mOtaPackage.filemd5);
                            notifyListener(CODE_02, 0, "新版本" + mOtaPackage.version + "……");
                            String upck_model = (String) SPUtil.get(mContext, Config.UPCK_MODEL, Config.UPCK_HAND);
                            boolean auto = upck_model.equals(Config.UPCK_AUTO);
                            if (mOtaPackage.upType == 1 && auto) {
                                updaterOtaPackage(mOtaPackage);
                            } else {
                                notifyListener(CODE_92, 0, "需要手动更新……");
                            }
                        } else if (resultVersion.code == 1) {
                            FlyDown.delAllDownFile();
                            notifyListener(CODE_01, 100, "已是最新版本！");
                        } else {
                            notifyListener(CODE_03, 100, "获取版本失败……");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        wakeLock.release();
                        isRunning.set(false);
                        notifyListener(CODE_04, 1, "正在连接服务器……");
                        FlyLog.e(e.toString());
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
                || TextUtils.isEmpty(mOtaPackage.filemd5)) {
            updateNewVersion();
        } else {
            wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
            isRunning.set(true);
            if (FlyDown.isFileDownFinish(otaPackage.filemd5)) {
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
        mHandler.post(() -> {
            for (FlyupResult flyupResult : flyupResults) {
                flyupResult.upVesionProgress(code, progress, msg);
            }
        });
    }

    @Override
    public void downloadFile(final OtaPackage otaPackage) {
        notifyListener(CODE_05, 0, "开始下载升级包……");
        IFileReQuestListener listener = new IFileReQuestListener() {
            @Override
            public void error(String url, int ErrorCode) {
                wakeLock.release();
                isRunning.set(false);
                notifyListener(CODE_06, 100, "下载升级包失败……");
            }

            @Override
            public void finish(String saveName) {
                notifyListener(CODE_05, 100, "升级包下载完成……");
                verityFileMd5(otaPackage);
            }

            @Override
            public void progress(final int progress) {
                notifyListener(CODE_05, Math.max(progress, 1), "正在下载升级包……");
            }
        };
        FlyDown.load(otaPackage.downurl).setThread(1).setFileName(otaPackage.filemd5).listener(listener).start();
    }

    @Override
    public void verityFileMd5(OtaPackage otaPackage) {
        notifyListener(CODE_07, 0, "开始校验升级包……");
        tHandler.post(() -> {
            String md5sum = FileUtils.getFileMD5(FlyDown.getFilePath(otaPackage.filemd5));
            if (md5sum.equals(otaPackage.filemd5)) {
                notifyListener(CODE_07, 100, "升级包校验成功……");
                final File file = new File(FlyDown.getFilePath(otaPackage.filemd5));
                updaterFile(file);
            } else {
                wakeLock.release();
                isRunning.set(false);
                FlyDown.delDownFile(otaPackage.filemd5);
                notifyListener(CODE_08, 100, "升级包校验失败……");
                FlyLog.e("verityOtaFile failed! md5sum=%s, fileName=%s", md5sum, FlyDown.getFilePath(md5sum));
            }
        });
    }

    UpdateEngineCallback callback = new UpdateEngineCallback() {
        @Override
        public void onStatusUpdate(int i, float v) {
            switch (i) {
                case UpdateEngine.UpdateStatusConstants.IDLE:
                    //notifyListener(CODE_11, 100, "IDLE……");
                    FlyLog.d("升级状态：[IDLE]");
                    break;
                case UpdateEngine.UpdateStatusConstants.CHECKING_FOR_UPDATE:
                    //notifyListener(CODE_11, Math.min((int) (v * 100), 100), "CHECKING_FOR_UPDATE……");
                    FlyLog.d("升级状态：[CHECKING_FOR_UPDATE]");
                    break;
                case UpdateEngine.UpdateStatusConstants.UPDATE_AVAILABLE:
                    //notifyListener(CODE_11, 100, "UPDATE_AVAILABLE……");
                    FlyLog.d("升级状态：[UPDATE_AVAILABLE]");
                    break;
                case UpdateEngine.UpdateStatusConstants.DOWNLOADING:
                    notifyListener(CODE_11, Math.min((int) (v * 100), 100), "正在写入更新文件……");
                    break;
                case UpdateEngine.UpdateStatusConstants.VERIFYING:
                    notifyListener(CODE_11, Math.min((int) (v * 100), 100), "正在校验更新文件……");
                    break;
                case UpdateEngine.UpdateStatusConstants.FINALIZING:
                    notifyListener(CODE_11, Math.min((int) (v * 100), 100), "正在进行最后的校验……");
                    break;
                case UpdateEngine.UpdateStatusConstants.UPDATED_NEED_REBOOT:
                    //notifyListener(CODE_11, 100, "UPDATED_NEED_REBOOT……");
                    FlyLog.d("升级状态：[UPDATED_NEED_REBOOT]");
                    break;
                case UpdateEngine.UpdateStatusConstants.REPORTING_ERROR_EVENT:
                    //notifyListener(CODE_11, 100, "REPORTING_ERROR_EVENT……");
                    FlyLog.d("升级状态：[REPORTING_ERROR_EVENT]");
                    break;
                case UpdateEngine.UpdateStatusConstants.ATTEMPTING_ROLLBACK:
                    //notifyListener(CODE_11, Math.min((int) (v * 100), 100), "ATTEMPTING_ROLLBACK……");
                    FlyLog.d("升级状态：[ATTEMPTING_ROLLBACK]");
                    break;
                case UpdateEngine.UpdateStatusConstants.DISABLED:
                    //notifyListener(CODE_11, 100, "DISABLED……");
                    FlyLog.d("升级状态：[DISABLED]");
                    break;
                default:
                    notifyListener(CODE_10, 100, "升级状态：[Code:" + i + "]");
                    FlyLog.e("升级状态：[Code:" + i + "]");
                    break;
            }

        }

        @Override
        public void onPayloadApplicationComplete(int i) {
            switch (i) {
                case UpdateEngine.ErrorCodeConstants.SUCCESS:
                    wakeLock.release();
                    isRunning.set(false);
                    isFinish.set(true);
                    notifyListener(CODE_12, 100, "系统升级完成，需要重启系统！");
                    break;
                case UpdateEngine.ErrorCodeConstants.ERROR:
                case UpdateEngine.ErrorCodeConstants.FILESYSTEM_COPIER_ERROR:
                case UpdateEngine.ErrorCodeConstants.POST_INSTALL_RUNNER_ERROR:
                case UpdateEngine.ErrorCodeConstants.PAYLOAD_MISMATCHED_TYPE_ERROR:
                case UpdateEngine.ErrorCodeConstants.INSTALL_DEVICE_OPEN_ERROR:
                case UpdateEngine.ErrorCodeConstants.KERNEL_DEVICE_OPEN_ERROR:
                case UpdateEngine.ErrorCodeConstants.DOWNLOAD_TRANSFER_ERROR:
                case UpdateEngine.ErrorCodeConstants.PAYLOAD_HASH_MISMATCH_ERROR:
                case UpdateEngine.ErrorCodeConstants.PAYLOAD_SIZE_MISMATCH_ERROR:
                case UpdateEngine.ErrorCodeConstants.DOWNLOAD_PAYLOAD_VERIFICATION_ERROR:
                case UpdateEngine.ErrorCodeConstants.UPDATED_BUT_NOT_ACTIVE:
                default:
                    wakeLock.release();
                    isRunning.set(false);
                    notifyListener(CODE_10, 100, "系统升级失败，错误码[" + i + "]");
                    FlyLog.e("系统升级失败，错误码[" + i + "]");
                    break;
            }
        }
    };

    @Override
    public void updaterFile(File file) {
        tHandler.post(() -> {
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
                        } catch (Exception e) {
                            wakeLock.release();
                            isRunning.set(false);
                            notifyListener(CODE_09, 100, "获取升级参数失败！");
                            FlyLog.e(e.toString());
                            return;
                        }
                    }
                    if (!payloadFound) {
                        payloadOffset += fileSize;
                    }
                }
            } catch (Exception e) {
                wakeLock.release();
                isRunning.set(false);
                notifyListener(CODE_10, 100, "升级文件校验失败！");
                FlyLog.e(e.toString());
                return;
            }
            try {
                mUpdateEngine.unbind();
                mUpdateEngine.bind(callback);
                mUpdateEngine.applyPayload(
                        FILE_URL_PREFIX + file.getAbsolutePath(),
                        payloadOffset,
                        payloadSize,
                        props);
            } catch (Exception e) {
                wakeLock.release();
                isRunning.set(false);
                notifyListener(CODE_10, 100, "升级异常[%s" + e.toString() + "]");
                FlyLog.e(e.toString());
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
