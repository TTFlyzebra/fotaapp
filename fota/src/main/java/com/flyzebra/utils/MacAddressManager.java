package com.flyzebra.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MacAddressManager {
    /**
     * 需要权限
     * <uses-permission android:name="android.permission.INTERNET"/>
     * <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
     * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
     * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
     * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
     * <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
     */

    private static String TAG = "GetMac";

    public static String getAdresseMAC(Context context) {
        String strMac = "02:00:00:00:00:00";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            strMac = getLocalMacAddressFromWifiInfo(context);
            Log.e(TAG, "6.0以下 MAC = " + strMac);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            strMac = getMacfromMarshmallow();
            Log.e(TAG, "6.0以上7.0以下:MAC = " + strMac);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            strMac = getMacFromHardware();
            Log.e("MAC", "7.0以上:MAC = " + strMac);
        }
        return strMac;
    }

    /**
     * 6.0以下 ,根据wifi信息获取本地mac
     *
     * @param context
     * @return
     */
    public static String getLocalMacAddressFromWifiInfo(Context context) {
//		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//		WifiInfo winfo = wifi.getConnectionInfo();
//		String mac = winfo.getMacAddress();
//		return mac;
        String mac = "02:00:00:00:00:00";
        if (context == null) {
            return mac;
        }


        WifiManager wifi = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return mac;
        }
        WifiInfo info = null;
        try {
            info = wifi.getConnectionInfo();
        } catch (Exception e) {
        }
        if (info == null) {
            return null;
        }
        mac = info.getMacAddress();
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.toUpperCase(Locale.ENGLISH);
        }
        return mac;
    }

    /**
     * android 6.0及以上、7.0以下 获取mac地址
     * 如果是6.0以下，直接通过wifimanager获取
     *
     * @return
     */
    public static String getMacfromMarshmallow() {

        String WifiAddress = "02:00:00:00:00:00";
        try {
            WifiAddress = new BufferedReader(new FileReader(new File("/sys/class/net/wlan0/address"))).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return WifiAddress;
    }


    /**
     * 7.0 以上 遍历循环所有的网络接口，找到接口是 wlan0
     * 必须的权限 <uses-permission android:name="android.permission.INTERNET" />
     *
     * @return
     */
    private static String getMacFromHardware() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }
}
