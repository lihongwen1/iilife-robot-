package com.ilife.iliferobot.entity;


/**
 * Created by chengjiaping on 2017/7/4.
 */

public class DeviceInfo {
    String name;
    String status;
    int resId;

    public DeviceInfo(String name, String status, int resId) {
        this.name = name;
        this.status = status;
        this.resId = resId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", resId=" + resId +
                '}';
    }
}
