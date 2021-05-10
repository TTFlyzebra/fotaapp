package com.flyzebra.fota.model;



/**
 *
 * Created by FlyZebra on 2016/6/21.
 */
public interface IFlyup {


    /**
     * 启动更线线程
     *
     * @param upResult 更新结果的回调通知
     */
    void startUpVersion(FlyupResult upResult);

    /**
     * 返回更新状态
     *
     * @return true:正在更新
     * false:不是处于更新状态
     */
    boolean isUPVeriosnRunning();

    /**
     * 取消所有网络请求线程
     */
    void cancelAllTasks();


    interface FlyupResult {

        /**
         * 更新进度
         * @param msg
         * @param sum
         * @param progress
         */
        void upVesionProgress(String msg, int sum, int progress);
    }

}
