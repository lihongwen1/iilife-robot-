package com.ilife.iliferobot.contract;

import com.accloud.service.ACUserDevice;
import com.ilife.iliferobot.base.BaseView;

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
        void bindFail(String message);
        void bindDevice();
        void updateBindProgress(String tip, int progress);
    }

    interface Presenter {
        void connectToDevice();
        /**
         * connect to device AP
         */
        Completable connectToAp(int type);

        /**
         * broadcast wifi info to device
         * @param ssid
         * @param passWord
         */
        Completable broadCastWifi(String ssid, String passWord);

        void generatePhysicalId();

        Single<ACUserDevice> bindDevice();
        Completable detectTargetWifi();
    }
}
