package com.flyzebra.flydown.request;

import android.text.TextUtils;

import com.flyzebra.flydown.file.FileBlock;
import com.flyzebra.flydown.file.IFileIO;
import com.flyzebra.flydown.network.IDownTask;
import com.flyzebra.flydown.network.TaskFactory;
import com.flyzebra.utils.FlyLog;
import com.flyzebra.utils.HttpUtils;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 功能说明：
 *
 * @author 作者：FlyZebra
 * @version 创建时间：2017年3月22日 下午3:11:27
 */
public class SimpleFileBlockQueue implements IFileBlockQueue, IFileBlockReQuestListener {
    private BlockingQueue<FileBlock> fileBlockQueue = new LinkedBlockingQueue<>();
    private String downUrl;
    private IFileIO saveFileIO;
    private IFileIO tempFileIO;
    private IFileBlockReQuestListener iFileBlockReQuestListener;
    private AtomicInteger blockSum = new AtomicInteger(0);
    private Hashtable<Integer, IDownTask> tasks = new Hashtable<>();

    @Override
    public boolean isEmpty() {
        return fileBlockQueue.isEmpty();
    }

    @Override
    public void doNextQueue() {
        if (!fileBlockQueue.isEmpty()) {
            IDownTask iHandleTask = TaskFactory.creat(downUrl);
            FileBlock fileBlock = fileBlockQueue.poll();
            tasks.put(fileBlock.getOrder(), iHandleTask);
            iHandleTask.setUrl(downUrl).setSaveFileIO(saveFileIO).setTempFileIO(tempFileIO)
                    .setFileBlockReQuestListener(this).setFileBlock(fileBlock).handle();
        }
    }

    @Override
    public SimpleFileBlockQueue setUrl(String downUrl) {
        this.downUrl = downUrl;
        return this;
    }

    @Override
    public SimpleFileBlockQueue setSaveFileIO(IFileIO saveFileIO) {
        this.saveFileIO = saveFileIO;
        return this;
    }

    @Override
    public SimpleFileBlockQueue setTempFileIO(IFileIO tempFileIO) {
        this.tempFileIO = tempFileIO;
        return this;
    }

    @Override
    public SimpleFileBlockQueue listener(IFileBlockReQuestListener iFileBlockReQuestListener) {
        this.iFileBlockReQuestListener = iFileBlockReQuestListener;
        return this;
    }

    @Override
    public void createQueue() {
        String str = tempFileIO.readAll();
        if (!TextUtils.isEmpty(str)) {
            for (int i = 0; i < str.length(); i = i + tempFileIO.blockSize()) {
                int end = i + tempFileIO.blockSize();
                FileBlock fileBlockData = FileBlock.create(str.substring(i, end));
                if (fileBlockData.getState() != 0xff) {
                    fileBlockQueue.add(fileBlockData);
                    blockSum.incrementAndGet();
                }
            }
        } else {
            long length = HttpUtils.getLength(downUrl);
            if (length == -1) {
                // 不支持断点续传
                FlyLog.d("Does not support resumable transmission!\n");
                FileBlock fileBlockData = new FileBlock();
                fileBlockData.setStaPos(0);
                fileBlockData.setEndPos(Long.MAX_VALUE);
                fileBlockData.setOrder(0);
                try {
                    tempFileIO.save(fileBlockData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fileBlockQueue.add(fileBlockData);
                blockSum.incrementAndGet();
            } else {
                // 简单划分成块(BLOCK_NUM)
                for (int i = 0; i < BLOCK_NUM; i++) {
                    FileBlock fileBlockData = new FileBlock();
                    fileBlockData.setStaPos(length * i / BLOCK_NUM);
                    fileBlockData.setEndPos(length * (i + 1) / BLOCK_NUM);
                    fileBlockData.setOrder(i);
                    byte b[] = fileBlockData.toString().getBytes();
                    try {
                        tempFileIO.save(b, i * b.length, b.length);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    fileBlockQueue.add(fileBlockData);
                    blockSum.incrementAndGet();
                }
            }
        }

    }

    @Override
    public int getBlockSum() {
        return blockSum.get();
    }

    @Override
    public void error(FileBlock fileBlock, int ErrorCode) {
        if (iFileBlockReQuestListener != null) {
            iFileBlockReQuestListener.error(fileBlock, ErrorCode);
        }
    }

    @Override
    public void finish(FileBlock fileBlock) {
        tasks.remove(fileBlock.getOrder());
        blockSum.decrementAndGet();
        if (iFileBlockReQuestListener != null) {
            iFileBlockReQuestListener.finish(fileBlock);
        }
    }

    @Override
    public void cancle() {
        Set<Integer> keys = tasks.keySet();
        for (Integer key : keys) {
            tasks.get(key).cancle();
        }
    }

    @Override
    public void cancleTask(FileBlock fileBlock) {
        tasks.remove(fileBlock.getOrder());
        blockSum.decrementAndGet();
        if (iFileBlockReQuestListener != null) {
            iFileBlockReQuestListener.cancleTask(fileBlock);
        }

    }

}
