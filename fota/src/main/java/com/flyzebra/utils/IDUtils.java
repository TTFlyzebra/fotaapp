package com.flyzebra.utils; /**
 * FileName: IDUtils
 * Author: FlyZebra
 * Email:flycnzebra@gmail.com
 * Date: 2021/2/28 19:37
 * Description:
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
 * 获取手机信息工具类
 * @author HLQ
 * @createtime 2016-12-7下午2:06:03
 * @remarks
 */
public class IDUtils {
    /**
     * 获取手机型号,兼容云手机
     * @param context
     * @return
     */
    public static String getModel(Context context) {
        String model = SystemPropUtils.get("ro.product.model", "CM3003").toUpperCase();
        switch (model) {
            case "C10":
            case "C8":
            case "CPE02":
                return model;
            default:
                return "CM3003";
        }
    }

    /**
     * 获取版本号，兼容云手机
     * @param context
     * @return
     */
    public static String getVersion(Context context) {
        String version = SystemPropUtils.get("persist.vendor.display.id", "");
        if(TextUtils.isEmpty(version)){
            version = SystemPropUtils.get("ro.build.display.id", "");
        }
        return version.toUpperCase();
    }

    public static String getSnUid(Context context) {
        String snuid = SystemPropUtils.get("persist.radio.mcwill.pid", "").replace(".", "").trim();
        if(TextUtils.isEmpty(snuid)){
            snuid = SystemPropUtils.get("persist.sys.nv.sn", "");
        }
        return snuid.toUpperCase();
    }

    /**
     * 获取手机IMEI
     * @param context
     * @return
     */
    @SuppressLint("HardwareIds")
    public static String getIMEI(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = tm.getDeviceId();
            if (TextUtils.isEmpty(imei)) {
                return  "";
            }else{
                return imei.toUpperCase();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取手机IMSI
     * @param context
     */
    public static String getIMSI(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //获取IMSI号
            String imsi = telephonyManager.getSubscriberId();
            if (TextUtils.isEmpty(imsi)) {
                return  "";
            }else{
                return imsi.toUpperCase();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取设备系统版本号
     * @return 设备系统版本号
     */
    public static int getSDKVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    /**
     * 获取设备AndroidID
     * @param context 上下文
     * @return AndroidID
     */
    @SuppressLint("HardwareIds")
    public static String getAndroidID(Context context) {
        String aid =  Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if(TextUtils.isEmpty(aid)){
            return "";
        }else{
            return aid.toUpperCase();
        }
    }


}
