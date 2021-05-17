package com.flyzebra.fota.httpApi;


import com.flyzebra.fota.bean.RetAllVersion;
import com.flyzebra.fota.bean.RetPhoneLog;
import com.flyzebra.fota.bean.RetVersion;

import io.reactivex.Observer;


public interface ApiAction {

    void getUpVersion(String sid, String ver,String imei, String uid,  String aid, Observer<RetVersion> observer);

    void getAllVersion(String sid, String ver, String imei, String uid,  String aid, Observer<RetAllVersion> observer);

    void upPhoneLog(int phoneId, int event, String emsg, int phonetime, Observer<RetPhoneLog> observer);

}