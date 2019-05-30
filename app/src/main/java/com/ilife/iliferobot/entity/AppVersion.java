package com.ilife.iliferobot.entity;

/**
 * Created by chengjiaping on 2017/7/24.
 */

public class AppVersion {
    private long ver_code;
    private String ver_name;

    public long getVer_code() {
        return ver_code;
    }

    public void setVer_code(long ver_code) {
        this.ver_code = ver_code;
    }

    public String getVer_name() {
        return ver_name;
    }

    public void setVer_name(String ver_name) {
        this.ver_name = ver_name;
    }

    @Override
    public String toString() {
        return "AppVersion{" +
                "ver_code=" + ver_code +
                ", ver_name='" + ver_name + '\'' +
                '}';
    }
}
