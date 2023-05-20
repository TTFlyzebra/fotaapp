package com.flyzebra.fota.httpApi;

import com.flyzebra.fota.bean.RetAllVersion;
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
    public void getUpVersion(String model, String ver, String imei, Observer<RetVersion> observer) {
        mNetService.getUpVersion(model, ver, imei)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    @Override
    public void getAllVersion(String model, String ver, String imei, Observer<RetAllVersion> observer) {
        mNetService.getAllVersion(model, ver, imei)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

}
