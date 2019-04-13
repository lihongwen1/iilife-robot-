package com.ilife.iliferobot_cn.presenter;

import android.net.wifi.WifiManager;
import android.os.CountDownTimer;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.ACDeviceActivator;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACDeviceBind;
import com.accloud.service.ACException;
import com.accloud.service.ACUserDevice;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.activity.SelectActivity_x;
import com.ilife.iliferobot_cn.app.MyApplication;
import com.ilife.iliferobot_cn.base.BasePresenter;
import com.ilife.iliferobot_cn.contract.ApWifiContract;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.MyLog;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.ToastUtils;
import com.ilife.iliferobot_cn.utils.Utils;
import com.ilife.iliferobot_cn.utils.WifiUtils;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.WIFI_SERVICE;

public class ApWifiPresenter extends BasePresenter<ApWifiContract.View> implements ApWifiContract.Presenter {
    private static final String TAG = ApWifiPresenter.class.getName();
    private ACDeviceActivator activator;
    private WifiManager wifiManager;
    private final String apWifiTarget = "flylin";
    private final String apPassWord = "123456789";
    private String physicalId = "";
    boolean isTimeOut;
    boolean isTimerStart;
    private Disposable apWifiDisposable;
    private CountDownTimer timer = new CountDownTimer(80 * 1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            if (isTimeOut) {
                isTimeOut = false;
            }
        }

        @Override
        public void onFinish() {
            isTimeOut = true;
        }
    };

    @Override
    public void attachView(ApWifiContract.View view) {
        super.attachView(view);
        activator = AC.deviceActivator(Constants.DEVICE_TYPE_QCLTLINK);
        wifiManager = (WifiManager) MyApplication.getInstance().getApplicationContext().getSystemService(WIFI_SERVICE);
    }

    @Override
    public void connectToDevice() {
        final String apssid = WifiUtils.searchTargetWifi(apWifiTarget, wifiManager);
        if (apssid == null || apssid.isEmpty()) {
            ToastUtils.showToast(Utils.getString(R.string.ap_wifi_connet_no_wifi));
        } else {
            apWifiDisposable = connectToAp(apssid, apPassWord).andThen(broadCastWifi(mView.getSsid(), mView.getPassWord())).andThen(connectToAp(mView.getSsid(), mView.getPassWord()))
                    .andThen(bindDevice()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(device -> {
                        mView.bindSuccess(device);
                    }, throwable -> mView.bindFail());
        }
    }


    /**
     * 连接到家庭wifi或者设备热点
     *
     * @param apssid
     * @param apPassWord
     * @return
     */
    @Override
    public Completable connectToAp(final String apssid, final String apPassWord) {
        return Completable.create(completableEmitter -> {
            boolean isSuccess = WifiUtils.connectToAp_(wifiManager, apssid, apPassWord, WifiUtils.getCipherType(apssid, wifiManager));
            String ssid="";
            if (isSuccess) {
                boolean isMatch = false;
                int times = 0;
                while (!isMatch && times < 5) {
                    Thread.sleep(1000);
                    ssid = WifiUtils.getSsid(MyApplication.getInstance());
                    isMatch = ssid != null && ssid.equals(apssid);
                    times++;
                }
                if (isMatch) {
                    MyLog.d(TAG, "连接设备热点成功！");
                    mView.updateBindProgress("连接设备热点成功" + ssid, 30);
                    completableEmitter.onComplete();
                } else {
                    completableEmitter.onError(new Exception("连接到" + apssid + "失败！"));
                }
            } else {
                MyLog.d(TAG, "连接到热点失败");
                completableEmitter.onError(new Exception("连接到" + apssid + "失败！"));
            }
        });
    }

    /**
     * 想设备广播ssid和password
     *
     * @param ssid
     * @param passWord
     * @return
     */
    @Override
    public Completable broadCastWifi(final String ssid, final String passWord) {
        return Completable.create(emitter -> {
            //AC.DEVICE_ACTIVATOR_DEFAULT_TIMEOUT：使用默认超时时间60s
            //第一个回调为配置SSID与Password成功与否的回调，建议传null；只用于调试阶段分析问题。
            //第二个回调为设备是否连云成功的回调。配置成功与否以第二个回调为主
            activator.startApLink(ssid, passWord, AC.DEVICE_ACTIVATOR_DEFAULT_TIMEOUT, new PayloadCallback<Boolean>() {
                @Override
                public void success(Boolean aBoolean) {
                    //设备配置SSID与Password成功
                    mView.updateBindProgress("设备配置SSID成功", 60);
                    MyLog.d(TAG, "设备配置ssid成功！");
                }

                @Override
                public void error(ACException e) {
                    //设备配置SSID与Password失败
                    emitter.onError(new Exception("设置ssid失败"));
                    MyLog.d(TAG, "设备配置ssid失败!" + e.getMessage() + "code:" + e.getErrorCode());
                }
            }, new PayloadCallback<ACDeviceBind>() {
                @Override
                public void success(ACDeviceBind deviceBind) {
                    physicalId = deviceBind.getPhysicalDeviceId();
                    //设备已成功连接，通过ACDeviceBind获取到物理ID进行绑定设备操作
                    MyLog.d(TAG, "设备连云成功");
                    mView.updateBindProgress("设备连云成功", 90);
                    emitter.onComplete();
                }

                @Override
                public void error(ACException e) {
                    MyLog.d(TAG, "设备连云失败" + e.getMessage() + "code:" + e.getErrorCode());
                    emitter.onError(e);
                    //此处一般为1993的超时错误，建议处理逻辑为页面上提示配网失败，提示用户检查自己输入的WIFI信息是否正确等，回到上述第一步骤，重新开始所有配网步骤。
                }
            });
        });
    }


    /**
     * 绑定设备
     *
     * @return
     */
    @Override
    public Single<ACUserDevice> bindDevice() {
        if (!isTimerStart) {
            timer.start();
            isTimerStart = true;
        }
        return Single.create(emitter -> AC.bindMgr().bindDevice(SpUtils.getSpString(MyApplication.getInstance(), SelectActivity_x.KEY_SUBDOMAIN), physicalId, "", new PayloadCallback<ACUserDevice>() {
            @Override
            public void success(ACUserDevice userDevice) {
                MyLog.e(TAG, "设备绑定成功！ " + userDevice.toString());
                mView.updateBindProgress("设备绑定成功", 100);
                emitter.onSuccess(userDevice);
            }

            @Override
            public void error(ACException e) {
                MyLog.e(TAG, "绑定设备失败" + e.toString() + e.getMessage() + "code:" + e.getErrorCode());
                emitter.onError(e);
            }
        }));
    }

    @Override
    public void detachView() {
        super.detachView();
        if (apWifiDisposable != null && !apWifiDisposable.isDisposed()) {
            apWifiDisposable.dispose();
        }
    }
}
