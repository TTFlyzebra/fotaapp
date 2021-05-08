package com.flyzebra.fota.httpApi;



import com.flyzebra.fota.bean.OtaPackage;

import java.util.List;

import io.reactivex.Observer;


public interface ApiAction {
    void doTheme(String type, Observer<List<String>> observer);

    void getUpVersion(String sid, String ver,String imei, String uid,  String aid, Observer<OtaPackage> observer);
}