package com.ilife.iliferobot_cn.contract;

import com.accloud.service.ACUserDevice;
import com.ilife.iliferobot_cn.base.BaseView;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface ApWifiContract {
    interface Model {
    }

    interface View extends BaseView {
        /**
         * get ssid from  text view
         * @return
         */
        String  getSsid();

        /**
         * get password from edit text view
         * @return
         */
        String getPassWord();
        void bindSuccess(ACUserDevice acUserDevice);
        void bindFail();
        void bindDevice();
        void updateBindProgress(String tip, int progress);
    }

    interface Presenter {
        void connectToDevice();
        /**
         * connect to device AP
         * @param apssid
         * @param apPassWord
         */
        Completable connectToAp(String apssid, String apPassWord);

        /**
         * broadcast wifi info to device
         * @param ssid
         * @param passWord
         */
        Completable broadCastWifi(String ssid, String passWord);

        Single<ACUserDevice> bindDevice();
    }
}
