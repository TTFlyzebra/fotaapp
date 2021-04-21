package com.flyzebra.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 字符串转码实现类，已实现MD5
 * Created by FlyZebra on 2016/6/22.
 */
public class EncodeHelper {

	/**
	 * 求字符串的MD5值
	 * @param str 求MD5的字符串
	 * @return 返回字符串的MD5值
	 */
    public static String md5(String str) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(str.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(str.hashCode());
        } catch (NullPointerException e){
            cacheKey = "null";
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
