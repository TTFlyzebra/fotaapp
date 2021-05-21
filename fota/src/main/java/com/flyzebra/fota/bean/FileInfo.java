package com.flyzebra.fota.bean;

public class FileInfo {
    public int type;
    public String fileName;
    public String fullName;
    public String otherInfo;
    public boolean isDirectory(){
        return type==0;
    }


}
