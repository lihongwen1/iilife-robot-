package com.ilife.iliferobot.presenter;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.ACDeviceActivator;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACDeviceBind;
import com.accloud.service.ACException;
import com.accloud.service.ACUserDevice;
import com.accloud.utils.LogUtil;
import com.ilife.iliferobot.app.MyApplication;
import com.ilife.iliferobot.base.BasePresenter;
import com.ilife.iliferobot.utils.Constants;
import com.ilife.iliferobot.utils.UserUtils;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.activity.SelectActivity_x;
import com.ilife.iliferobot.contract.ApWifiContract;
import com.ilife.iliferobot.utils.MyLog;
import com.ilife.iliferobot.utils.SpUtils;
import com.ilife.iliferobot.utils.Utils;
import com.ilife.iliferobot.utils.WifiUtils;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.WIFI_SERVICE;

/**
 *
 */
public class ApWifiPresenter extends BasePresenter<ApWifiContract.View> implements ApWifiContract.Presenter {
    private static final String TAG = ApWifiPresenter.class.getName();
    private ACDeviceActivator activator;
    private WifiManager wifiManager;
    private final String apWifiTarget = "Robot";
    private final String apPassWord = "123456789";
    private String physicalId = "";
    private String mApSsid;
    private Disposable apWifiDisposable, apProgressDsiposable;

    @Override
    public void attachView(ApWifiContract.View view) {
        super.attachView(view);
        activator = AC.deviceActivator(Constants.DEVICE_TYPE_QCLTLINK);
        wifiManager = (WifiManager) MyApplication.getInstance().getApplicationContext().getSystemService(WIFI_SERVICE);
    }

    @Override
    public Completable detectTargetWifi() {
        return Completable.create(e -> {
            mApSsid = WifiUtils.searchTargetWifi(apWifiTarget, wifiManager);
            int times = 0;
            try {
                while (mApSsid == null || mApSsid.isEmpty()) {
                    if (times >= 6) {
                        break;
                    }
                    wifiManager.startScan();
                    Thread.sleep(4000);
                    MyLog.d(TAG, "扫描目标wifi~~~~");
                    mApSsid = WifiUtils.searchTargetWifi(apWifiTarget, wifiManager);
                    times++;
                }
                MyLog.d(TAG, "扫描目标wifi结束" + mApSsid);
//            mView.updateBindProgress("扫描目标wifi结束" + mApSsid, 10);
                if (mApSsid == null || mApSsid.isEmpty()) {
                    e.onError(new Exception(Utils.getString(R.string.ap_wifi_connet_no_wifi)));
                } else {
                    e.onComplete();
                }
            } catch (Exception ex) {
                MyLog.d(TAG, "扫描目标wifi异常");
            }

        });
    }

    @Override
    public void connectToDevice() {
        apProgressDsiposable = Observable.intervalRange(1, 16, 1, 1, TimeUnit.SECONDS).subscribe(aLong -> {
            if (isViewAttached()) {
                Log.d(TAG, "update progress");
                mView.updateBindProgress("", (int) (aLong * 5));
            }
        });
        apWifiDisposable = detectTargetWifi().andThen(connectToAp(1)).delay(8, TimeUnit.SECONDS).
                andThen(broadCastWifi(mView.getSsid(), mView.getPassWord())).
                delay(18, TimeUnit.SECONDS)
                .andThen(connectToAp(2)).delay(6, TimeUnit.SECONDS)
                .andThen(bindDevice()).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(device ->
                        {
                            Log.d(TAG, "bind success");
                            if (isViewAttached()) {
                                mView.bindSuccess(device);
                            }
                        }, throwable ->
                        {
                            Log.d(TAG, "bind fail");
                            if (isViewAttached()) {
                                mView.bindFail(throwable.getMessage());
                            }
                        });
    }


    /**
     * 连接到家庭wifi或者设备热点
     *
     * @return
     */
    @Override
    public Completable connectToAp(int type) {

        return Completable.create(completableEmitter -> {
            // TODO apssid为null
            String connectSsid, connectPwd;
            if (type == 1) {
                connectSsid = mApSsid;
                connectPwd = apPassWord;
//                mView.updateBindProgress("连接设备热点成功" + connectSsid, 40);
            } else {
                connectSsid = mView.getSsid();
                connectPwd = mView.getPassWord();
//                mView.updateBindProgress("连接设备热点成功" + connectSsid, 70);
            }
            MyLog.d(TAG, "设备开始连接wifi：" + connectSsid + "是主线程：" + (Looper.getMainLooper() == Looper.myLooper()));
            boolean isSuccess = WifiUtils.getSsid(MyApplication.getInstance()).equals(connectSsid);
            if (!isSuccess) {
                isSuccess = WifiUtils.connectToAp_(wifiManager, connectSsid, connectPwd, WifiUtils.getCipherType(connectSsid, wifiManager));
            }
            if (isSuccess) {
                MyLog.d(TAG, "连接设备热点成功！");
                completableEmitter.onComplete();
            } else {
                MyLog.d(TAG, "连接到热点失败");
                completableEmitter.onError(new Exception("连接到" + connectSsid + "失败！"));
            }
        });
    }

    @Override
    public void generatePhysicalId() {
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info != null) {
            String bssid = info.getBSSID();
            MyLog.e(TAG, "bssid = " + bssid);
            if (!TextUtils.isEmpty(bssid)) {
                BigInteger id = new BigInteger(bssid.replace(":", ""), 16);
                long mac;
                if (!bssid.startsWith("84")) {
                    mac = id.longValue() - 1;
                } else {
                    mac = id.longValue();
                }
                physicalId = Long.toHexString(mac);
            }
        }
    }

    private boolean isTheSameDevice(String ap_ssid, String physicalId) {
        if (!TextUtils.isEmpty(ap_ssid) && !TextUtils.isEmpty(physicalId)) {
            if (ap_ssid.length() > 4 && physicalId.length() > 4) {
                String ap_ssid_ = UserUtils.exChange(ap_ssid);
                String physicalId_ = UserUtils.exChange(physicalId);
                if (ap_ssid_.substring(ap_ssid_.length() - 4)
                        .equals(physicalId_.substring(physicalId_.length() - 4))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 想设备广播ssid和password
     *
     * @param ssid
     * @param passWord
     * @return
     */
    @Override
    public Completable broadCastWifi(String ssid, String passWord) {

        return Completable.create(emitter -> {
            //AC.DEVICE_ACTIVATOR_DEFAULT_TIMEOUT：使用默认超时时间60s
            //第一个回调为配置SSID与Password成功与否的回调，建议传null；只用于调试阶段分析问题。
            //第二个回调为设备是否连云成功的回调。配置成功与否以第二个回调为主
//            mView.updateBindProgress("发送家庭WiFi", 50);
            generatePhysicalId();
            if (TextUtils.isEmpty(physicalId) || !isTheSameDevice(mApSsid, physicalId)) {
                emitter.onError(new Exception(Utils.getString(R.string.third_ap_aty_port_)));
            } else {

                activator.startApLink(ssid, passWord, (int) TimeUnit.SECONDS.toMillis(20), new PayloadCallback<Boolean>() {
                    @Override
                    public void success(Boolean aBoolean) {
                        //设备配置SSID与Password成功
//                        mView.updateBindProgress("设备配置SSID成功", 60);
                        MyLog.d(TAG, "设备配置ssid成功！");
                    }

                    @Override
                    public void error(ACException e) {
                        //设备配置SSID与Password失败
//                    emitter.onError(new Exception("设置ssid失败"));
                        MyLog.d(TAG, "设备配置ssid失败!" + e.getMessage() + "code:" + e.getErrorCode());
                    }
                }, new PayloadCallback<ACDeviceBind>() {
                    @Override
                    public void success(ACDeviceBind deviceBind) {
//                    physicalId = deviceBind.getPhysicalDeviceId();
                        //设备已成功连接，通过ACDeviceBind获取到物理ID进行绑定设备操作
                        MyLog.d(TAG, "设备连云成功");
//                        mView.updateBindProgress("设备连云成功", 70);
//                    emitter.onComplete();
                    }

                    @Override
                    public void error(ACException e) {
                        MyLog.d(TAG, "设备连云失败" + e.getMessage() + "code:" + e.getErrorCode());
//                    emitter.onError(e);
                        //此处一般为1993的超时错误，建议处理逻辑为页面上提示配网失败，提示用户检查自己输入的WIFI信息是否正确等，回到上述第一步骤，重新开始所有配网步骤。
                    }
                });
                emitter.onComplete();
            }
        });
    }


    /**
     * 绑定设备
     *
     * @return
     */
    @Override
    public Single<ACUserDevice> bindDevice() {
        return Single.create(emitter -> {
            LogUtil.d(TAG, "physicalId--" + physicalId);
            mView.updateBindProgress("开始绑定设备", 90);
            String bindPhysicalId = physicalId;
            AC.bindMgr().bindDevice(SpUtils.getSpString(MyApplication.getInstance(), SelectActivity_x.KEY_SUBDOMAIN), bindPhysicalId, "", new PayloadCallback<ACUserDevice>() {
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
            });
        });
    }

    @Override
    public void cancelApWifi() {
        if (apProgressDsiposable != null && apProgressDsiposable.isDisposed()) {
            apProgressDsiposable.dispose();
        }
        if (apWifiDisposable != null && !apWifiDisposable.isDisposed()) {
            apWifiDisposable.dispose();
        }
        activator.stopAbleLink();
    }
}
