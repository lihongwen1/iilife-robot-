package com.ilife.iliferobot_cn.presenter;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACException;
import com.accloud.service.ACUserDevice;
import com.ilife.iliferobot_cn.base.BasePresenter;
import com.ilife.iliferobot_cn.contract.MainContract;

import java.util.List;

public class MainPresenter extends BasePresenter<MainContract.View> implements MainContract.Presenter {
    @Override
    public void getDeviceList() {
        AC.bindMgr().listDevicesWithStatus(new PayloadCallback<List<ACUserDevice>>() {
            @Override
            public void success(List<ACUserDevice> acUserDevices) {
                mView.updateDeviceList(acUserDevices);
                mView.setRefreshOver();
            }

            @Override
            public void error(ACException e) {
                mView.setRefreshOver();
            }
        });

    }

    @Override
    public boolean isDeviceOnLine(ACUserDevice acdvice) {
        return acdvice.getStatus() != 0;
    }
}
