package com.flyzebra.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 功能说明：
 *
 * @author 作者：FlyZebra
 * @version 创建时间：2017年3月21日 下午4:26:29
 */
public class FileUtils {

    /**
     * 以字符串方式一次读取文件所有内容
     *
     * @param file 要读取的文件的文件路径
     * @return 以字符串方式返回文件内容
     */
    public static String readFile(File file) {
        String readStr = null;
        if (file.exists()) {
            InputStream ins = null;
            InputStreamReader streamReader = null;
            BufferedReader reader = null;
            try {
                ins = new FileInputStream(file);
                streamReader = new InputStreamReader(ins);
                reader = new BufferedReader(streamReader);
                String line = null;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                readStr = stringBuilder.toString();
            } catch (Exception e) {
                FlyLog.d("Reading File error! %s", e.toString());
                e.printStackTrace();
            } finally {
                CloseableUtil.Close(reader);
                CloseableUtil.Close(streamReader);
                CloseableUtil.Close(ins);
            }
        }
        return readStr;
    }

    /**
     * 以字符串方式一次读取文件所有内容
     *
     * @param fileName 要读取的文件的文件路径
     * @return 以字符串方式返回文件内容
     */
    public static String readFile(String fileName) {
        if (fileName != null && fileName.equals("")) {
            File file = new File(fileName);
            return readFile(file);
        } else {
            return null;
        }
    }

    /**
     * 在线程中删除指定的文件
     *
     * @param fileName
     */
    public static synchronized void delFileInTread(final String fileName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(fileName);
                if (file.exists()) {
                    file.delete();
                }
            }
        }).start();
    }

    /**
     * 获取文件的MD5值
     */
    public static String getFileMD5(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return "";
        }
        String md5sum = "";
        FileInputStream ins = null;
        try {
            ins = new FileInputStream(file);
            int len = 0;
            byte[] buffer = new byte[8192];
            MessageDigest md = MessageDigest.getInstance("MD5");
            while ((len = ins.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            byte[] md5Bytes = md.digest();
            StringBuilder hexValue = new StringBuilder();
            for (byte md5Byte : md5Bytes) {
                int val = ((int) md5Byte) & 0xff;
                if (val < 16) {
                    hexValue.append("0");
                }
                hexValue.append(Integer.toHexString(val));
            }
            md5sum = hexValue.toString().toUpperCase();
        } catch (NoSuchAlgorithmException | IOException e) {
            FlyLog.e(e.toString());
        } finally {
            try {
                if (ins != null) {
                    ins.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return md5sum;
    }

    /**
     * 获取文件的MD5值
     */
    public static String getFileMD5(String path) {
        return getFileMD5(new File(path));
    }
}
