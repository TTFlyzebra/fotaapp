package com.flyzebra.flydown;

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
    public static String mCacheDir = "/sdcard/downtest/";
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

    public static void delAllDownFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(mCacheDir);
                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        delete(f);
                    }
                }
            }
        }).start();
    }
}
