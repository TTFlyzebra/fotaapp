package com.flyzebra.fota.httpApi;


import com.flyzebra.fota.bean.RetAllVersion;
import com.flyzebra.fota.bean.RetPhoneLog;
import com.flyzebra.fota.bean.RetVersion;

import io.reactivex.Observer;


public interface ApiAction {

    void getUpVersion(String sid, String ver,String imei, String uid, Observer<RetVersion> observer);

    void getAllVersion(String sid, String ver, String imei, String uid, Observer<RetAllVersion> observer);

    void upPhoneLog(String imei, int event, String emsg, Observer<RetPhoneLog> observer);

}