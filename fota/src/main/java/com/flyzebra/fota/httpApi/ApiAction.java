package com.flyzebra.fota.httpApi;


import com.flyzebra.fota.bean.RetAllVersion;
import com.flyzebra.fota.bean.RetVersion;

import io.reactivex.Observer;


public interface ApiAction {

    void getUpVersion(String model, String ver,String imei, Observer<RetVersion> observer);

    void getAllVersion(String model, String ver, String imei, Observer<RetAllVersion> observer);
}