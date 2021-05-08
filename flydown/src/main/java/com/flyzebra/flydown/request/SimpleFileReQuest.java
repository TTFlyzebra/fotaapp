package com.flyzebra.flydown.request;

import android.text.TextUtils;

import com.flyzebra.flydown.FlyDown;
import com.flyzebra.flydown.file.FileBlock;
import com.flyzebra.flydown.file.FileIO;
import com.flyzebra.flydown.file.IFileIO;
import com.flyzebra.utils.FileUtils;
import com.flyzebra.utils.FlyLog;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 功能说明：单个文件下载请求
 * @author 作者：FlyZebra
 * @version 创建时间：2017年3月22日 上午10:03:57
 */
public class SimpleFileReQuest implements Runnable, IFileReQuest, IFileBlockReQuestListener {

    private String downUrl;
    private String fileName;
    private String saveName;
    private String tempFile;
    private int threadNum;
    private IFileReQuestListener iFileReQuestListener;
    private IFileBlockQueue iFileBlockQueue;

    private IFileIO saveFileIO;
    private IFileIO tempFileIO;
    private int fileSize;

    // 添加原子操作数判断下载是否完成
    public SimpleFileReQuest(String downUrl) {
        this.downUrl = downUrl;
    }

    private static final ExecutorService executor = Executors.newFixedThreadPool(8);

    @Override
    public SimpleFileReQuest setUrl(String downUrl) {
        this.downUrl = downUrl;
        return this;
    }

    @Override
    public SimpleFileReQuest setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    @Override
    public SimpleFileReQuest setThread(int threadNum) {
        this.threadNum = threadNum;
        return this;
    }

    @Override
    public IFileReQuest setFileSize(int fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    @Override
    public SimpleFileReQuest listener(IFileReQuestListener iFileReQuestListener) {
        this.iFileReQuestListener = iFileReQuestListener;
        return this;
    }

    @Override
    public void start() {
        // 在线程中执行下载操作
        executor.execute(this);
    }

    @Override
    public void cancle() {
        iFileBlockQueue.cancle();
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(downUrl)) {
            FlyLog.d("无效的下载地址!");
            return;
        }

        if (TextUtils.isEmpty(fileName)) {
            saveName = FlyDown.mCacheDir + "/" + downUrl.substring(downUrl.lastIndexOf('/') + 1, downUrl.length());
            tempFile = FlyDown.mCacheDir + "/" + "." + fileName.substring(fileName.lastIndexOf('/') + 1, fileName.length()) + ".tmp";
        } else {
            saveName = FlyDown.mCacheDir + "/" + fileName + ".zip";
            tempFile = FlyDown.mCacheDir + "/" + fileName + ".fly";
        }

        if (iFileBlockQueue == null) {
            iFileBlockQueue = new SimpleFileBlockQueue();
        }

        try {
            FlyLog.d("saveFile=" + saveName);
            FlyLog.d("tempFile=" + tempFile);
            saveFileIO = new FileIO(saveName);
            tempFileIO = new FileIO(tempFile);
            iFileBlockQueue.setUrl(downUrl).setSaveFileIO(saveFileIO).setTempFileIO(tempFileIO).listener(this).createQueue();
            iFileReQuestListener.progress((IFileBlockQueue.BLOCK_NUM - iFileBlockQueue.getBlockSum()) * 100 / IFileBlockQueue.BLOCK_NUM);
            for (int i = 0; i < threadNum; i++) {
                iFileBlockQueue.doNextQueue();
            }
        } catch (IOException e) {
            iFileReQuestListener.error(downUrl,1);
            FlyLog.e(e.toString());
        }
    }

    @Override
    public void error(FileBlock fileBlock, int ErrorCode) {
        iFileReQuestListener.error(downUrl, ErrorCode);
    }

    @Override
    public void finish(FileBlock fileBlock) {
        iFileReQuestListener.progress((IFileBlockQueue.BLOCK_NUM - iFileBlockQueue.getBlockSum()) * 100 / IFileBlockQueue.BLOCK_NUM);
        if (iFileBlockQueue.getBlockSum() == 0) {
            iFileReQuestListener.finish(saveName);
            try {
                saveFileIO.close();
                tempFileIO.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileUtils.delFileInTread(tempFile);
        } else {
            iFileBlockQueue.doNextQueue();
        }
    }

    @Override
    public void cancleTask(FileBlock fileBlock) {
        if (iFileBlockQueue.getBlockSum() == 0) {
            try {
                saveFileIO.close();
                tempFileIO.close();
            } catch (IOException e) {
                FlyLog.e(e.toString());
            }
        }
    }

}
