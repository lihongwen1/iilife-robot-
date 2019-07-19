package com.ilife.iliferobot;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACDeviceDataMgr;
import com.accloud.service.ACException;
import com.ilife.iliferobot.able.MsgCodeUtils;
import com.ilife.iliferobot.entity.PropertyInfo;
import com.ilife.iliferobot.utils.MyLogger;

public class AcManager {
    private static AcManager acManager;
    private ACDeviceDataMgr.PropertyReceiver propertyReceiver;

    private AcManager() {
    }

    public static AcManager instance() {
        if (acManager == null) {
            synchronized (AcManager.class) {
                if (acManager == null) {
                    acManager = new AcManager();
                }
            }
        }
        return acManager;
    }

    public void registerPropReceiver(String subdomain, long deviceId) {
        AC.deviceDataMgr().subscribeProperty(subdomain, deviceId,
                new VoidCallback() {
                    @Override
                    public void success() {
                        if (propertyReceiver == null) {
                            propertyReceiver = (s, l, s1) -> {
                            };
                        }
                        AC.deviceDataMgr().registerPropertyReceiver(propertyReceiver);
                    }

                    @Override
                    public void error(ACException e) {

                    }
                }
        );
    }
}
