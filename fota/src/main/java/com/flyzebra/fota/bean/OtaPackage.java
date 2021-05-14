package com.flyzebra.fota.bean;

public class OtaPackage {
    public int phoneId;
    public String version;
    public String downurl;
    public int filesize;
    public String md5sum;
    public String oldver;
    public int upType;
    public int otaType;
    public String releaseNote;

    @Override
    public String toString() {
        return "{" +
                "\"phoneId\":" + phoneId +
                ", \"version\":\"" + version + '\"' +
                ", \"downurl\":\"" + downurl + '\"' +
                ", \"filesize\":" + filesize +
                ", \"md5sum\":\"" + md5sum + '\"' +
                ", \"oldver\":\"" + oldver + '\"' +
                ", \"upType\":" + upType +
                ", \"otaType\":" + otaType +
                ", \"releaseNote\":\"" + releaseNote + '\"' +
                '}';
    }
}
