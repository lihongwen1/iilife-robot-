package com.ilife.iliferobot.entity;

/**
 * Created by chengjiaping on 2017/8/5.
 */

public class MyPropertyRecord {
    private byte workMode;
    private byte roomMode;
    private byte cleanMode;
    private byte dustColForce;
    private byte mopForce;
    private byte elecQuantity;

    public byte getWorkMode() {
        return workMode;
    }

    public void setWorkMode(byte workMode) {
        this.workMode = workMode;
    }

    public byte getElecQuantity() {
        return elecQuantity;
    }

    public void setElecQuantity(byte elecQuantity) {
        this.elecQuantity = elecQuantity;
    }

    public byte getDustColForce() {
        return dustColForce;
    }

    public void setDustColForce(byte dustColForce) {
        this.dustColForce = dustColForce;
    }

    public byte getMopForce() {
        return mopForce;
    }

    public void setMopForce(byte mopForce) {
        this.mopForce = mopForce;
    }

    public byte getCleanMode() {
        return cleanMode;
    }

    public void setCleanMode(byte cleanMode) {
        this.cleanMode = cleanMode;
    }

    public byte getRoomMode() {
        return roomMode;
    }

    public void setRoomMode(byte roomMode) {
        this.roomMode = roomMode;
    }

    @Override
    public String toString() {
        return "MyPropertyRecord{" +
                "workMode=" + workMode +
                ", roomMode=" + roomMode +
                ", cleanMode=" + cleanMode +
                ", dustColForce=" + dustColForce +
                ", mopForce=" + mopForce +
                ", elecQuantity=" + elecQuantity +
                '}';
    }
}
