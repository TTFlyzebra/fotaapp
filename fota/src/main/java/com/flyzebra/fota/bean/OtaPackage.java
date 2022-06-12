package com.flyzebra.fota.bean;

public class OtaPackage {
    public int phoneId;
    public String version;
    public String downurl;
    public int filesize;
    public String filemd5;
    public String oldver;
    public int upType;
    public int otaType;
    public String releaseNote;
    public String remark;

    @Override
    public String toString() {
        return "{" +
                " \"phoneId\":" + phoneId +
                ", \"version\":\"" + version + '\"' +
                ", \"downurl\":\"" + downurl + '\"' +
                ", \"filesize\":" + filesize +
                ", \"md5sum\":\"" + filemd5 + '\"' +
                ", \"oldver\":\"" + oldver + '\"' +
                ", \"upType\":" + upType +
                ", \"otaType\":" + otaType +
                ", \"releaseNote\":\"" + releaseNote + '\"' +
                ", \"remark\":\"" + remark + '\"' +
                '}';
    }
}
