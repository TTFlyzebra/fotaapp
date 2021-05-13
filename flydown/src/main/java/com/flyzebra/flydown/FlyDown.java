package com.flyzebra.flydown;

import android.text.TextUtils;

import com.flyzebra.flydown.request.IFileReQuest;
import com.flyzebra.flydown.request.SimpleFileReQuest;

import java.io.File;
import java.util.HashMap;

/**
 * 功能说明：用户操作接口封装
 *
 * @author 作者：FlyZebra
 * @version 创建时间：2017年2月27日 上午11:57:27
 */
public class FlyDown {
    public static String mCacheDir = "/data/cache/recovery";
    private static HashMap<String, IFileReQuest> downQuests = new HashMap<>();

    FlyDown setCacheDir(String path) {
        mCacheDir = path;
        return this;
    }

    public static IFileReQuest load(String url) {
        IFileReQuest fileQequest = new SimpleFileReQuest(url);
        downQuests.put(url, fileQequest);
        return fileQequest;
    }

    public static void cancel(String url) {
        downQuests.get(url).cancle();
        downQuests.remove(url);
    }

    private static void delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    delete(f);
                }
            }
            file.delete();
        } else {
            file.delete();
        }
    }

    public static void delDownFile(String fileMd5sum) {
        String saveName = FlyDown.mCacheDir + "/" + fileMd5sum + ".zip";
        File f1 = new File(saveName);
        if (f1.exists()) {
            f1.delete();
        }
        String tempFile = FlyDown.mCacheDir + "/" + fileMd5sum + ".fly";
        File f2 = new File(tempFile);
        if (f2.exists()) {
            f2.delete();
        }
    }

    public static void delOtherFile(String fileMd5sum) {
        File file = new File(mCacheDir);
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                String filaName = f.getName();
                if (!TextUtils.isEmpty(filaName) && !filaName.startsWith(fileMd5sum)) {
                    delete(f);
                }
            }
        }
    }

    public static boolean isFileDownFinish(String fileMd5sum) {
        File saveFile = new File(FlyDown.mCacheDir + "/" + fileMd5sum + ".zip");
        File tempFile = new File(FlyDown.mCacheDir + "/" + fileMd5sum + ".fly");
        return saveFile.exists() && !tempFile.exists();
    }

    public static void delAllDownFile() {
        File file = new File(mCacheDir);
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                delete(f);
            }
        }
    }

    public static String getFilePath(String md5sum) {
        return FlyDown.mCacheDir + "/" + md5sum + ".zip";
    }

}
