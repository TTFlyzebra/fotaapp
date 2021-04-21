package com.flyzebra.flydown.request;
/** 
* 功能说明：
* @author 作者：FlyZebra 
* @version 创建时间：2017年2月27日 下午1:55:43  
*/
public interface IFileReQuestListener {

	public void Error(String url,int ErrorCode);
	
	public void Finish(String url);
	
	public void Pause(String url);
	
	public void Progress(String url, long downBytes, long sumBytes);
	
}