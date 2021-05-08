package com.flyzebra.flydown.network;

import com.flyzebra.flydown.file.FileBlock;
import com.flyzebra.flydown.file.IFileIO;
import com.flyzebra.flydown.request.IFileBlockReQuestListener;

/** 
* 功能说明：
* @author 作者：FlyZebra 
* @version 创建时间：2017年3月1日 上午9:46:27  
*/
public interface IDownTask {
	
	IDownTask setUrl(String downUrl);
	
	IDownTask setSaveFileIO(IFileIO saveFileIO);
	
	IDownTask setTempFileIO(IFileIO tempFileIO);
	
	IDownTask setFileBlock(FileBlock fileBlock);
	
	IDownTask setFileBlockReQuestListener(IFileBlockReQuestListener iFileBlockEnvent);

	void handle();
	
	void cancle();
}
