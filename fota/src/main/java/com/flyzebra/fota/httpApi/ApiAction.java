package com.flyzebra.fota.httpApi;



import com.flyzebra.fota.bean.OtaPackage;

import java.util.List;

import io.reactivex.Observer;


public interface ApiAction {
    void doTheme(String type, Observer<List<String>> observer);

    void getUpVersion(String version, String systemId, Observer<OtaPackage> observer);
}