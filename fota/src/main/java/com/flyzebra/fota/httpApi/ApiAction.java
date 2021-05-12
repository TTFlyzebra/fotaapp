package com.flyzebra.fota.httpApi;


import com.flyzebra.fota.bean.OtaPackage;
import com.flyzebra.fota.bean.PhoneLog;

import io.reactivex.Observer;


public interface ApiAction {

    void getUpVersion(String sid, String ver,String imei, String uid,  String aid, Observer<OtaPackage> observer);

    void upPhoneLog(int phoneId, int event, String emsg, Observer<PhoneLog> observer);

}