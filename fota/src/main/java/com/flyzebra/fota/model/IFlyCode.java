package com.flyzebra.fota.model;

public interface IFlyCode {

    //系统更新程序开始运行......
    int CODE_00 = 0;

    //已是最新版本
    int CODE_01 = 1;

    //获取到最新版本
    int CODE_02 = 2;

    //获取最新版本失败
    int CODE_03 = 3;

    //获取最新版本失败，网络错误！
    int CODE_04 = 4;

    //正在下载升级包......
    int CODE_05 = 5;

    //下载升级包出错!
    int CODE_06 = 6;

    //正在校验升级包MD5值......
    int CODE_07 = 7;

    //升级包MD5值校验错误!
    int CODE_08 = 8;

    //升级包数据校验......
    int CODE_09 = 9;

    //安装升级包......
    int CODE_10 = 10;

    //升级包数据校验错误!
    int CODE_11 = 11;

    //安装升级包错误!
    int CODE_12 = 12;

    //系统正在更新
    int CODE_91 = 91;

    //需要手动更新版本
    int CODE_92 = 92;
}
