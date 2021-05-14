package com.flyzebra.fota.bean;

public class RetVersion {
    public String msg;
    public int code;
    public OtaPackage data;

    @Override
    public String toString() {
        return "{" +
                "\"msg\":\"" + msg + '\"' +
                ", \"code\":" + code +
                ", \"data\":\"" + (data == null ? "" : data.toString()) + '\"' +
                '}';
    }

}
