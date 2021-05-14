package com.flyzebra.fota.bean;

public class RetPhoneLog {
    public String msg;
    public int code;
    public PhoneLog data;

    @Override
    public String toString() {
        return "{" +
                "\"msg\":\"" + msg + '\"' +
                ", \"code\":" + code +
                ", \"data\":\"" + (data == null ? "" : data.toString()) + '\"' +
                '}';
    }
}
