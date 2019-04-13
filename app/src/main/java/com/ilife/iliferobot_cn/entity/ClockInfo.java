package com.ilife.iliferobot_cn.entity;

/**
 * Created by chenjiaping on 2017/7/27.
 */

public class ClockInfo {
    private byte number;        //编号
    private byte isOpen;        //预约使能
    private byte week;          //预约时间中的星期
    private byte hour;          //小时
    private byte minute;        //分钟

    public ClockInfo() {

    }

    public ClockInfo(byte number, byte minute, byte hour, byte week, byte isOpen) {
        this.number = number;
        this.minute = minute;
        this.hour = hour;
        this.week = week;
        this.isOpen = isOpen;
    }

    public byte getNumber() {
        return number;
    }

    public void setNumber(byte number) {
        this.number = number;
    }

    public byte getMinute() {
        return minute;
    }

    public void setMinute(byte minute) {
        this.minute = minute;
    }

    public byte getHour() {
        return hour;
    }

    public void setHour(byte hour) {
        this.hour = hour;
    }

    public byte getWeek() {
        return week;
    }

    public void setWeek(byte week) {
        this.week = week;
    }

    public byte getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(byte isOpen) {
        this.isOpen = isOpen;
    }

    @Override
    public String toString() {
        return "ClockInfo{" +
                "number=" + number +
                ", isOpen=" + isOpen +
                ", week=" + week +
                ", hour=" + hour +
                ", minute=" + minute +
                '}';
    }
}
