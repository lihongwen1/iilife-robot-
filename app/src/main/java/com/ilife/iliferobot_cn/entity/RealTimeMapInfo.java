package com.ilife.iliferobot_cn.entity;

/**
 * Created by chengjiaping on 2017/8/25.
 */

public class RealTimeMapInfo {
    private String clean_data;
    private int device_id;
    private int package_id;
    private int package_num;
    private long start_time;

    private int real_clean_area;
    private int real_clean_time;

    public String getClean_data() {
        return clean_data;
    }

    public void setClean_data(String clean_data) {
        this.clean_data = clean_data;
    }

    public long getStart_time() {
        return start_time;
    }

    public void setStart_time(long start_time) {
        this.start_time = start_time;
    }

    public int getPackage_num() {
        return package_num;
    }

    public void setPackage_num(int package_num) {
        this.package_num = package_num;
    }

    public int getPackage_id() {
        return package_id;
    }

    public void setPackage_id(int package_id) {
        this.package_id = package_id;
    }

    public int getDevice_id() {
        return device_id;
    }

    public void setDevice_id(int device_id) {
        this.device_id = device_id;
    }


    public int getReal_clean_area() {
        return real_clean_area;
    }

    public void setReal_clean_area(int real_clean_area) {
        this.real_clean_area = real_clean_area;
    }

    public int getReal_clean_time() {
        return real_clean_time;
    }

    public void setReal_clean_time(int real_clean_time) {
        this.real_clean_time = real_clean_time;
    }

    @Override
    public String toString() {
        return "RealTimeMapInfo{" +
                "clean_data='" + clean_data + '\'' +
                ", device_id=" + device_id +
                ", package_id=" + package_id +
                ", package_num=" + package_num +
                ", start_time=" + start_time +
                '}';
    }
}
