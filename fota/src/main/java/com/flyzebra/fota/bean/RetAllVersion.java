package com.flyzebra.fota.bean;

import java.util.List;

public class RetAllVersion {
    public String msg;
    public int code;
    public List<OtaPackage> data;

    @Override
    public String toString() {
        return "{" +
                "\"msg\":\"" + msg + '\"' +
                ", \"code\":" + code +
                ", \"data\":\"" + (data == null ? "" : data.toString()) + '\"' +
                '}';
    }

}
