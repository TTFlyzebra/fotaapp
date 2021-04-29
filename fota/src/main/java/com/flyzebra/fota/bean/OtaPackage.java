package com.flyzebra.fota.bean;

public class OtaPackage {
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
        public String version;
        public String downurl;
        public String md5sum;
        public int autoup;
        public String oldversion;

        @Override
        public String toString() {
            return "{" +
                    "\"version\":\"" + version + '\"' +
                    ", \"downurl\":\"" + downurl + '\"' +
                    ", \"autoup\":" + autoup +
                    ", \"md5sum\":\"" + md5sum + '\"' +
                    ", \"oldver\":\"" + oldversion + '\"' +
                    '}';
        }
    }

}
