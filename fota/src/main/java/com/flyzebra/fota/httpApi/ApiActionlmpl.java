package com.flyzebra.fota.httpApi;

import com.flyzebra.fota.bean.RetPhoneLog;
import com.flyzebra.fota.bean.RetVersion;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ApiActionlmpl implements ApiAction {
    private HttpService mHttpService;
    private Api mNetService;

    public ApiActionlmpl() {
        mHttpService = new HttpService();
        mNetService = mHttpService.getInspectionService();
    }

    @Override
    public void getUpVersion(String sid, String ver, String imei, String uid, String aid, Observer<RetVersion> observer) {
        mNetService.getUpVersion(sid, ver, imei, uid, aid)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    @Override
    public void upPhoneLog(int phoneId, int event, String emsg, Observer<RetPhoneLog> observer) {
        mNetService.upPhoneLog(phoneId, event, emsg)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
