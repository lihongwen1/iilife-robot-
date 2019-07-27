package com.ilife.iliferobot.presenter;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACException;
import com.accloud.service.ACUserDevice;
import com.accloud.service.ACUserInfo;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.app.MyApplication;
import com.ilife.iliferobot.base.BasePresenter;
import com.ilife.iliferobot.contract.MainContract;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.utils.Utils;

import java.util.List;

public class MainPresenter extends BasePresenter<MainContract.View> implements MainContract.Presenter {
    /**
     * 请求设备列表，刷新设备状态
     */
    @Override
    public void getDeviceList() {
        AC.bindMgr().listDevicesWithStatus(new PayloadCallback<List<ACUserDevice>>() {
            @Override
            public void success(List<ACUserDevice> acUserDevices) {
                if (isViewAttached()) {
                    mView.updateDeviceList(acUserDevices);
                    mView.setRefreshOver();
                }
            }

            @Override
            public void error(ACException e) {
                if (!isViewAttached()) {
                    return;
                }
                if (e.getErrorCode() == 1993) {
                    ToastUtils.showToast(Utils.getString(R.string.login_aty_timeout));
                    mView.setRefreshOver();
                } else {
                    AC.accountMgr().forceUpdateRefreshToken(new PayloadCallback<ACUserInfo>() {
                        @Override
                        public void success(ACUserInfo acUserInfo) {
                            getDeviceList();
                        }

                        @Override
                        public void error(ACException e) {
                            mView.loginInvalid();
                            mView.setRefreshOver();
                        }
                    });
                }
            }
        });

    }

    @Override
    public boolean isDeviceOnLine(ACUserDevice acdvice) {
        return acdvice.getStatus() != 0;
    }
}
