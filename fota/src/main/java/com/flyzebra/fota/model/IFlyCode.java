package com.flyzebra.fota.model;

public interface IFlyCode {

    //已是最新版本
    int CODE_01 = 1;

    //获取到最新版本
    int CODE_02 = 2;

    //获取最新版本失败
    int CODE_03 = 3;

    //获取最新版本，网络错误！
    int CODE_04 = 4;

    //正在下载更新包......
    int CODE_05 = 5;

    //下载更新包出错!
    int CODE_06 = 6;

    //正在校验更新包MD5值......
    int CODE_07 = 7;

    //更新包MD5值校验错误!
    int CODE_08 = 8;

    //更新包数据完成性校验......
    int CODE_09 = 9;

    //准备安装更新包......
    int CODE_10 = 10;

    //更新包数据完成性校验错误!
    int CODE_11 = 11;

    //安装更新包错误!
    int CODE_12 = 12;

    //系统正在更新
    int CODE_91 = 91;
}
