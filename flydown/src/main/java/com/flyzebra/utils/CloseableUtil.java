package com.flyzebra.utils;

import java.io.Closeable;
import java.io.IOException;

/** 
* 功能说明：
* @author 作者：FlyZebra 
* @version 创建时间：2017年2月27日 下午2:46:11  
*/
public class CloseableUtil {

	public static void Close(Closeable closeable){
		if(closeable!=null){
			try {
				closeable.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
