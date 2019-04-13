package com.ilife.iliferobot_cn.contract;

import com.accloud.service.ACUserDevice;
import com.ilife.iliferobot_cn.base.BaseView;

import java.util.List;

public interface MainContract {
    interface Model {
    }

    interface View extends BaseView {
        void showButton();
        void showList();
        void setRefreshOver();
        void updateDeviceList(List<ACUserDevice> acUserDevices);
    }

    interface Presenter{
        void getDeviceList();
        boolean isDeviceOnLine(ACUserDevice acdvice);
    }
}
