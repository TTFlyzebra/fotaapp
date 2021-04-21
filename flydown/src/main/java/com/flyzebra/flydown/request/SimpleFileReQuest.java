package com.flyzebra.flydown.request;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.flyzebra.flydown.FlyDown;
import com.flyzebra.flydown.file.FileBlock;
import com.flyzebra.flydown.file.FileIO;
import com.flyzebra.flydown.file.IFileIO;
import com.flyzebra.utils.FileUtils;
import com.flyzebra.utils.FlyLog;

/**
 * 功能说明：单个文件下载请求
 * 
 * @author 作者：FlyZebra
 * @version 创建时间：2017年3月22日 上午10:03:57
 */
public class SimpleFileReQuest implements Runnable, IFileReQuest, IFileBlockReQuestListener {

	private String downUrl;
	private String saveFile;
	private String tempFile;
	private int threadNum;
	private IFileReQuestListener iFileReQuestListener;
	private IFileBlockQueue iFileBlockQueue;

	private IFileIO saveFileIO;
	private IFileIO tempFileIO;
	
	// 添加原子操作数判断下载是否完成

	public SimpleFileReQuest(String downUrl) {
		this.downUrl = downUrl;
	}

	private static  final ExecutorService executor = Executors.newFixedThreadPool(8);

	@Override
	public SimpleFileReQuest setUrl(String downUrl) {
		this.downUrl = downUrl;
		return this;
	}

	@Override
	public SimpleFileReQuest setSaveFile(String saveFile) {
		this.saveFile = saveFile;
		return this;
	}

	@Override
	public SimpleFileReQuest setThread(int threadNum) {
		this.threadNum = threadNum;
		return this;
	}

	@Override
	public SimpleFileReQuest listener(IFileReQuestListener iFileReQuestListener) {
		this.iFileReQuestListener = iFileReQuestListener;
		return this;
	}

	@Override
	public void goStart() {
		// 在线程中执行下载操作
		executor.execute(this);
	}

	@Override
	public void cancle() {
		iFileBlockQueue.cancle();
	}

	@Override
	public void run() {
		FlyLog.e("run start!");
		if (downUrl == null) {
			FlyLog.d("无效的下载地址!");
			return;
		}

		if (saveFile == null) {
			saveFile = FlyDown.mCacheDir + downUrl.substring(downUrl.lastIndexOf('/') + 1, downUrl.length());
		}

		if (tempFile == null) {
			tempFile = FlyDown.mCacheDir + "." +saveFile.substring(saveFile.lastIndexOf('/') + 1, saveFile.length()) + ".log";
		}

		if (iFileBlockQueue == null) {
			iFileBlockQueue = new SimpleFileBlockQueue();
		}

		try {
			FlyLog.d("saveFile="+saveFile);
			FlyLog.d("tempFile="+tempFile);
			saveFileIO = new FileIO(saveFile);
			tempFileIO = new FileIO(tempFile);
		} catch (IOException e) {
			FlyLog.e(e.toString());
		}

		iFileBlockQueue.setUrl(downUrl).setSaveFileIO(saveFileIO).setTempFileIO(tempFileIO).listener(this).createQueue();

		for (int i = 0; i < threadNum; i++) {
			iFileBlockQueue.doNextQueue();
		}

	}

	@Override
	public void error(FileBlock fileBlock, int ErrorCode) {
		iFileReQuestListener.Error(downUrl, ErrorCode);
	}

	@Override
	public void finish(FileBlock fileBlock) {
		if (iFileBlockQueue.getBlockSum() == 0) {
			iFileReQuestListener.Finish(downUrl);
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
	public void progress(FileBlock fileBlock) {
		FlyLog.d("download = %d\n",fileBlock.getStaPos());
	}

	@Override
	public void cancleTask(FileBlock fileBlock) {
		if (iFileBlockQueue.getBlockSum() == 0) {
			try {
				saveFileIO.close();
				tempFileIO.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
	}

}
