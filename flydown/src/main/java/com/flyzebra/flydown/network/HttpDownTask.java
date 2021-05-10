package com.flyzebra.flydown.network;

import com.flyzebra.flydown.file.FileBlock;
import com.flyzebra.flydown.file.IFileIO;
import com.flyzebra.flydown.request.IFileBlockReQuestListener;
import com.flyzebra.utils.CloseableUtil;
import com.flyzebra.utils.FlyLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 功能说明：
 * 
 * @author 作者：FlyZebra
 * @version 创建时间：2017年3月1日 上午9:53:41
 */
public class HttpDownTask implements IDownTask, Runnable {
	private String downUrl;
	private IFileIO saveFileIO;
	private IFileIO tempFileIO;
	private FileBlock fileBlock;
	private IFileBlockReQuestListener iFileBlockReQuestListener;
	private boolean isCancel = false;
	private static final ExecutorService executor = Executors.newFixedThreadPool(8);

	@Override
	public void handle() {
		executor.execute(this);
	}

	@Override
	public HttpDownTask setFileBlockReQuestListener(IFileBlockReQuestListener iFileBlockReQuestListener) {
		this.iFileBlockReQuestListener = iFileBlockReQuestListener;
		return this;
	}

	@Override
	public HttpDownTask setFileBlock(FileBlock fileBlock) {
		this.fileBlock = fileBlock;
		return this;
	}

	@Override
	public HttpDownTask setUrl(String downUrl) {
		this.downUrl = downUrl;
		return this;
	}

	@Override
	public HttpDownTask setSaveFileIO(IFileIO saveFileIO) {
		this.saveFileIO = saveFileIO;
		return this;
	}

	@Override
	public void run() {
		HttpURLConnection con = null;
		InputStream ins = null;
		try {
			final URL url = new URL(downUrl);
			con = (HttpURLConnection) url.openConnection();
			// con.setConnectTimeout(CONNECT_TIME);
			// con.setReadTimeout(CONNECT_TIME);
			if (fileBlock.getEndPos() != Long.MAX_VALUE) {
				con.setRequestProperty("RANGE", "bytes=" + fileBlock.getStaPos() + "-" + fileBlock.getEndPos());
			}
			// int fileLength = con.getContentLength();
			// urlConnection.setDoInput(true);
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK||con.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
				ins = con.getInputStream();
				byte[] b = new byte[4096];
				int nRead = 0;
				while (!isCancel&& fileBlock.getStaPos() <= fileBlock.getEndPos()&&(nRead = ins.read(b, 0, 4096)) > 0) {
					saveFileIO.save(b, fileBlock.getStaPos(), nRead);
					fileBlock.setStaPos(fileBlock.getStaPos() + nRead);
//					if (iFileBlockReQuestListener != null) {
//						iFileBlockReQuestListener.progress(fileBlock);
//					}
					if (fileBlock.getEndPos() != Long.MAX_VALUE) {
						tempFileIO.save(fileBlock);
					}
				}
				if (fileBlock.getEndPos() != Long.MAX_VALUE) {
					fileBlock.setState(0xff);
					tempFileIO.save(fileBlock);
				}
				if (iFileBlockReQuestListener != null) {
					iFileBlockReQuestListener.finish(fileBlock);
				}
				
				if(isCancel){
					iFileBlockReQuestListener.cancleTask(fileBlock);
				}
			} else {
				FlyLog.d("ResponseCode = \n" + con.getResponseCode());
			}
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.disconnect();
			}
			CloseableUtil.Close(ins);
		}
	}

	@Override
	public IDownTask setTempFileIO(IFileIO tempFileIO) {
		this.tempFileIO = tempFileIO;
		return this;
	}

	@Override
	public void cancle() {
		isCancel = true;
	}

}
