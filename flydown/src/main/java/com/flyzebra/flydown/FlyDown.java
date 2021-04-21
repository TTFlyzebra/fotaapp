package com.flyzebra.flydown;

import java.util.HashMap;
import java.util.Hashtable;

import com.flyzebra.flydown.request.IFileReQuest;
import com.flyzebra.flydown.request.SimpleFileReQuest;

/**
 * 功能说明：用户操作接口封装
 * 
 * @author 作者：FlyZebra
 * @version 创建时间：2017年2月27日 上午11:57:27
 */
public class FlyDown {
	public static String mCacheDir = "/sdcard/downtest/";
	private static HashMap<String, IFileReQuest> downQuests = new HashMap<>();

	FlyDown setCacheDir(String path) {
		mCacheDir = path;
		return this;
	}

	public static IFileReQuest load(String url) {
		IFileReQuest fileQequest = new SimpleFileReQuest(url);
		downQuests.put(url, fileQequest);
		return fileQequest;
	}

	public static void cancel(String url) {
		downQuests.get(url).cancle();
		downQuests.remove(url);
	}
}
