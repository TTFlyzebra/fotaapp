package com.flyzebra.fota.model;

public interface FlyEvent {

    //系统首次启动...
    int CODE_00 = 0;

    //已是最新版本
    int CODE_01 = 1;

    //获取到最新版本
    int CODE_02 = 2;

    //获取最新版本失败
    int CODE_03 = 3;

    //获取最新版本失败，网络错误！
    int CODE_04 = 4;

    //正在下载升级包...
    int CODE_05 = 5;

    //下载升级包出错!
    int CODE_06 = 6;

    //正在校验升级包MD5值...
    int CODE_07 = 7;

    //升级包MD5值校验错误!
    int CODE_08 = 8;

    //获取升级参数失败！
    int CODE_09 = 9;

    //升级失败！
    int CODE_10 = 10;

    //正在升级系统
    int CODE_11 = 11;

    //系统升级完成，需要重启系统才能生效！
    int CODE_12 = 12;

    //系统正在更新
    int CODE_91 = 91;

    //需要手动更新版本
    int CODE_92 = 92;
}
