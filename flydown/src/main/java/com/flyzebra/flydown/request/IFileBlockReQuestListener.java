package com.flyzebra.flydown.request;

import com.flyzebra.flydown.file.FileBlock;

/**
 * 功能说明：
 * 
 * @author 作者：FlyZebra
 * @version 创建时间：2017年3月21日 下午5:40:46
 */
public interface IFileBlockReQuestListener {

	public void error(FileBlock fileBlock, int ErrorCode);

	public void finish(FileBlock fileBlock);
	
	public void progress(FileBlock fileBlock);
	
	public void cancleTask(FileBlock fileBlock);
}
