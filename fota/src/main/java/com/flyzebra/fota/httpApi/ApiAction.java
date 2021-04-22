package com.flyzebra.fota.httpApi;



import java.util.List;

import io.reactivex.Observer;


public interface ApiAction {
    void doTheme(String type, Observer<List<String>> observer);
}