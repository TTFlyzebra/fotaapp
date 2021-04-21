package com.flyzebra.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/** 
* 功能说明：
* @author 作者：FlyZebra 
* @version 创建时间：2017年3月8日 下午5:07:03  
*/
public class HttpUtils {
	/**
	 * 检测downUrl是否支持断点续传，网络请求，尽量在线程中调用
	 * @param downUrl
	 * @return
	 */
	public static long getLength(String downUrl){
		URL url;
		HttpURLConnection con = null;
		long fileLength = 0;
		try {
			url = new URL(downUrl);
			con = (HttpURLConnection) url.openConnection();
			con.setRequestProperty("RANGE", "bytes=0-");
			fileLength = con.getContentLengthLong();
			if(con.getResponseCode()!=206){
				fileLength = -1;
			}
		} catch (MalformedURLException e) {
			FlyLog.d(e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			FlyLog.d(e.toString());
			e.printStackTrace();
		} finally {
			if(con!=null){
				con.disconnect();
			}
		}
		return fileLength;
	}
}
