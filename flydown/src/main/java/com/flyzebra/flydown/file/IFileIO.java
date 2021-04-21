package com.flyzebra.flydown.file;

import java.io.IOException;

/** 
* 功能说明：
* @author 作者：FlyZebra 
* @version 创建时间：2017年3月21日 上午10:06:10  
*/
public interface IFileIO {

	/**
	 * 
	 * @param b 写入数据
	 * @param start 写入文件的起始位置
	 * @param len 写入文件的长度
	 */
	void save(byte b[],long start,int len) throws IOException;
	
	
	void close() throws IOException;
	
	String readAll();


	void save(FileBlock fileBlock) throws IOException;
}
