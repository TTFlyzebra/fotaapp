package com.flyzebra.flydown.request;
/** 
* 功能说明：
* @author 作者：FlyZebra 
* @version 创建时间：2017年3月22日 上午10:00:42  
*/
public interface IFileReQuest {

	/**
	 * 设置下载地址
	 * @param downUrl
	 * @return
	 */
	IFileReQuest setUrl(String downUrl);
	
	/**
	 * 设置保存的文件名称
	 * @param fileName
	 * @return
	 */
	IFileReQuest setFileName(String fileName);
	
	IFileReQuest setThread(int threadNum);

	IFileReQuest setFileSize(int fileSize);
	
	/**
	 * 设置文件下载状态监听
	 * @param iFileReQuestListener
	 * @return
	 */
	IFileReQuest listener(IFileReQuestListener iFileReQuestListener);
	
	/**
	 * 开始执行文件下载任务
	 */
	void start();
	
	/**
	 * 取消下载任务
	 */
	void cancle();
}
