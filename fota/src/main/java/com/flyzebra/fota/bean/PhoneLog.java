package com.flyzebra.fota.bean;

public class PhoneLog {
    public String msg;
    public int code;
    public Data data;

    @Override
    public String toString() {
        return "{" +
                "\"msg\":\"" + msg + '\"' +
                ", \"code\":" + code +
                ", \"data\":\"" + (data == null ? "" : data.toString()) + '\"' +
                '}';
    }

    public class Data {
        public int phone_logId;

        @Override
        public String toString() {
            return "{" +
                    "\"phone_logId\":" + phone_logId +
                    '}';
        }
    }

}
