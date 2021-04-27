package com.flyzebra.fota.bean;

public class OtaPackage {
    public String version;
    public String downurl;
    public String fileName;
    public long filesize;
    public String md5sum;
    public int otatype;
    public String oldversion;
    public String createtime;

    @Override
    public String toString() {
        return "{" +
                "\"version\":\"" + version + '\"' +
                ", \"downurl\":\"" + downurl + '\"' +
                ", \"md5sum\":\"" + md5sum + '\"' +
                ", \"pktype\":\"" + otatype + '\"' +
                ", \"oldver\":\"" + oldversion + '\"' +
                ", \"createtime\":\"" + createtime + '\"' +
                '}';
    }

}
