package com.ilife.iliferobot_cn.entity;

/**
 * Created by chengjiaping on 2017/7/14.
 */

public class LightMsg {
    public static final int REQ_CODE = 68;
    public static final int RESP_CODE = 102;

    //0代表关，1代表开
    public static final byte ON = 1;
    public static final byte OFF = 0;

    private byte ledOnOff;

    public LightMsg(byte ledOnOff) {
        this.ledOnOff = ledOnOff;
    }

    public byte[] getLedOnOff() {
        return new byte[]{ledOnOff, 0, 0, 0};
    }

    public String getDescription() {
        if (ledOnOff == OFF)
            return "close light";
        else
            return "open light";
    }
}
