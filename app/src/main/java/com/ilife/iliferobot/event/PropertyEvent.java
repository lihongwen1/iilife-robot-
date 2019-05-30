package com.ilife.iliferobot.event;

/**
 * Created by chengjiaping on 2017/11/3.
 */

public class PropertyEvent {
    private String mMsg;

    public PropertyEvent(String msg) {
        mMsg = msg;
    }

    public String getMsg() {
        return mMsg;
    }
}
