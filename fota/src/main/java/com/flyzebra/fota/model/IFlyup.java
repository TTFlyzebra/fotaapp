package com.flyzebra.fota.model;


import android.content.Context;

import com.flyzebra.fota.bean.OtaPackage;

/**
 *
 * Created by FlyZebra on 2016/6/21.
 */
public interface IFlyup {

    /**
     * 初始化
     * @param context
     */
    void init(Context context);

    /**
     * 启动更线线程
     *
     */
    void startUpVersion();

    /**
     * 启动更线线程
     *
     */
    void startUpVersion(OtaPackage otaPackage);

    /**
     * 取消所有网络请求线程
     */
    void stopUpVersion();

    /**
     * 返回更新状态
     * @return true:正在更新
     * false:不是处于更新状态
     */
    boolean isUpVeriosnRunning();


    /**
     * 注册状态监听
     * @param flyupResult
     */
    void addListener(FlyupResult flyupResult);


    /**
     * 取消状态监听
     * @param flyupResult
     */
    void removeListener(FlyupResult flyupResult);


    interface FlyupResult {

        /**
         * 更新进度
         * @param code
         * @param progress
         *@param msg
         */
        void upVesionProgress(int code, int progress,String msg);
    }

}
