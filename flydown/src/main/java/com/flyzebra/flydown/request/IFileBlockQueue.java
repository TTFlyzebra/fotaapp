package com.flyzebra.flydown.request;

import com.flyzebra.flydown.file.IFileIO;

/**
 * 功能说明：
 * 
 * @author 作者：FlyZebra
 * @version 创建时间：2017年3月22日 下午2:59:37
 */
public interface IFileBlockQueue {
	IFileBlockQueue setUrl(String downUrl);

	IFileBlockQueue setSaveFileIO(IFileIO saveFileIO);

	IFileBlockQueue setTempFileIO(IFileIO tempFileIO);

	IFileBlockQueue listener(IFileBlockReQuestListener iFileBlockReQuestListener);

	void createQueue();

	boolean isEmpty();
	
	int getBlockSum();

	void doNextQueue();

	void cancle(); 

}
