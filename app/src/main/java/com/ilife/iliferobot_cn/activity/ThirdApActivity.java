package com.ilife.iliferobot_cn.activity;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.ACDeviceActivator;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACDeviceBind;
import com.accloud.service.ACException;
import com.accloud.service.ACUserDevice;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.utils.AlertDialogUtils;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.MyLog;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.ToastUtils;
import com.ilife.iliferobot_cn.utils.UserUtils;
import com.ilife.iliferobot_cn.utils.WifiUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by chengjiaping on 2018/9/1.
 */

public class ThirdApActivity extends BaseActivity implements View.OnClickListener {
    final String TAG = ThirdApActivity.class.getSimpleName();
    private final int DELAYMILLIS = 2 * 1000;
    private final int TAG_START_BIND = 15 * 1000;
    private final int REQUEST_CODE_LOCATION = 0x11;
    final int STATUS_CONNECTING = 0x01;
    final int STATUS_NORMAL = 0x02;
    final int TAG_BIND_FAIL = 0x03;
    Context context;
    ACDeviceActivator activator;
    WifiManager wifiManager;
    LocationManager locationManager;
    String ssid;
    String pass;
    String ap_ssid;
    String subdomain;
    String physicalId;
    boolean isTimeOut, isFirst;
    boolean isCanceled, locationEnable;
    TextView tv_set;
    TextView tv_tip2;
    TextView tv_connect;
    Animation animation;
    ImageView image_cancel;
    ImageView image_back;
    ImageView imageView;
    RelativeLayout rl_tip1;
    LinearLayout ll_tip2;
    RelativeLayout rl_connect;
    EditText et_ssid;
    AlertDialog alertDialog;
    CountDownTimer timer = new CountDownTimer(80 * 1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            isTimeOut = true;
            handler.sendEmptyMessage(TAG_BIND_FAIL);
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TAG_START_BIND:
                    if (!isCanceled) {
                        timer.start();
                        if (WifiUtils.isWifiConnected(context)) {
                            bindDevice(physicalId);
                        } else {
                            MyLog.e(TAG, " WifiUtils.connectToAp_");
                            WifiUtils.connectToAp_(wifiManager, ssid, pass, getCipherType(ssid));
                            bindDevice(physicalId);
                        }
                    }
                    break;
                case TAG_BIND_FAIL:
                    if (!isTimeOut) {
                        bindDevice(physicalId);
                    } else {
                        setStatus(STATUS_NORMAL);
                        if (!isCanceled) {
                            ToastUtils.showToast(context, getString(R.string.personal_aty_bind_fail));
                        }
                    }
                    break;
            }
        }
    };

    private final ContentObserver mGpsMonitor = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            locationEnable = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_ap_third;
    }

    @Override
    protected void onStart() {
        super.onStart();
        getContentResolver()
                .registerContentObserver(
                        Settings.Secure
                                .getUriFor(Settings.System.LOCATION_PROVIDERS_ALLOWED),
                        false, mGpsMonitor);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (isFirst) {
                isFirst = false;
            } else {
                String ssid = WifiUtils.getSsid(context);
                et_ssid.setText(ssid);
            }
        }
    }

    private void initData() {
        animation = AnimationUtils.loadAnimation(context, R.anim.anims);
        animation.setInterpolator(new LinearInterpolator());
        subdomain = SpUtils.getSpString(context, SelectActivity_x.KEY_SUBDOMAIN);
        activator = AC.deviceActivator(Constants.DEVICE_TYPE_QCLTLINK);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        locationManager = (LocationManager) context.getApplicationContext().getSystemService(LOCATION_SERVICE);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            ssid = bundle.getString(FirstApActivity.EXTRA_SSID);
            pass = bundle.getString(FirstApActivity.EXTRA_PASS);
        }
        if (!subdomain.equals(Constants.subdomain_x800)) {
            tv_tip2.setText(getString(R.string.add_aty_tip22));
        }
    }

    public void initView() {
        isFirst = true;
        context = this;
        tv_set = (TextView) findViewById(R.id.tv_set);
        et_ssid = findViewById(R.id.et_ssid);
        tv_tip2 = (TextView) findViewById(R.id.tv_tip2);
        tv_connect = (TextView) findViewById(R.id.tv_connect);
        image_back = (ImageView) findViewById(R.id.image_back);
        image_cancel = (ImageView) findViewById(R.id.image_cancel);
        imageView = (ImageView) findViewById(R.id.image_map);
        rl_tip1 = (RelativeLayout) findViewById(R.id.rl_tip1);
        ll_tip2 = (LinearLayout) findViewById(R.id.ll_tip2);
        rl_connect = (RelativeLayout) findViewById(R.id.rl_connect);

        tv_set.setOnClickListener(this);
        image_cancel.setOnClickListener(this);
        image_back.setOnClickListener(this);
        rl_connect.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back:
                stop();
                unRegisterLocation();
                finish();
                break;
            case R.id.tv_set:
                Intent i = new Intent();
                i.setAction("android.net.wifi.PICK_WIFI_NETWORK");
                startActivity(i);
                break;
            case R.id.image_cancel:
                stop();
                setStatus(STATUS_NORMAL);
                break;
            case R.id.rl_connect:
                ap_ssid = et_ssid.getText().toString();
                if (TextUtils.isEmpty(ap_ssid) || !ap_ssid.startsWith("Robot")) {
                    ToastUtils.showToast(context, getString(R.string.third_ap_aty_port_));
                } else {
                    WifiInfo info = wifiManager.getConnectionInfo();
                    if (info != null) {
                        String bssid = info.getBSSID();
                        MyLog.e(TAG, "bssid = " + bssid);
                        if (!TextUtils.isEmpty(bssid)) {
                            if (bssid.startsWith("02")) {
                                showLocationDialog();
                            } else {
                                connect(bssid);
                            }
                        }
                    }
                }
                break;
        }
    }

    public void showLocationDialog() {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_open_location_dialog, null);

        v.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogUtils.hidden(alertDialog);
            }
        });
        v.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogUtils.hidden(alertDialog);
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, REQUEST_CODE_LOCATION);
            }
        });
        int width = (int) getResources().getDimension(R.dimen.dp_300);
        int height = (int) getResources().getDimension(R.dimen.dp_140);
        alertDialog = AlertDialogUtils.showDialog(context, v, width, height);
    }

    public void stop() {
        timer.cancel();
        isCanceled = true;
        isTimeOut = true;
        stopAbleLink();
    }

//    class SuccessConsumer implements Consumer<ACDeviceBind>{
//        @Override
//        public void accept(@NonNull ACDeviceBind acDeviceBind) throws Exception {
//            MyLog.e(TAG,"SuccessConsumer accept"+acDeviceBind.getPhysicalDeviceId());
//            String id = acDeviceBind.getPhysicalDeviceId();
//            if (isTheSameDevice(ap_ssid,id)){
//                bindDevice(id);
//            } else {
//                if (!isTimerStart){
//                    timer.start();
//                    isTimerStart = true;
//                }
//                if (isTimeOut){
//                    setStatus(STATUS_NORMAL);
//                } else {
//                    deviceActive();
//                }
//            }
//        }
//    }

//    class ErrorConsumer implements Consumer<Throwable>{
//        @Override
//        public void accept(@NonNull Throwable throwable) throws Exception {
//            MyLog.e(TAG,"ErrorConsumer accept "+throwable.toString());
//            ACException e = (ACException) throwable;
//            if (!isTimerStart){
//                timer.start();
//                isTimerStart = true;
//            }
//            if (isTimeOut){
//                setStatus(STATUS_NORMAL);
//            } else {
//                if (e.getErrorCode()==1999){
//                    connectWifi(ssid, pass, TimeUnit.SECONDS.toMillis(20))
//                            .delay(15,TimeUnit.SECONDS)
//                            .andThen(startAbleLink())
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe(new SuccessConsumer(),new ErrorConsumer());
//                } else {
//                    deviceActive();
//                }
//            }
//        }
//    }

//    class SuccessConsumer_ implements Consumer<ACUserDevice>{
//        @Override
//        public void accept(@NonNull ACUserDevice userDevice) throws Exception {
//            MyLog.e(TAG,"SuccessConsumer_ accept"+userDevice.toString());
//            if (!isDestroyed()){
////                    setStatus(STATUS_NORMAL);
//                stop();
//                Intent i = new Intent(context,BindSucActivity.class);
//                i.putExtra(AddActivity.EXTAR_DEVID,userDevice.deviceId);
//                startActivity(i);
//            }
//        }
//    }

    public void setStatus(int status) {
        switch (status) {
            case STATUS_CONNECTING:
                imageView.setVisibility(View.VISIBLE);
                imageView.startAnimation(animation);
                image_cancel.setVisibility(View.VISIBLE);
                rl_tip1.setVisibility(View.GONE);
                ll_tip2.setVisibility(View.VISIBLE);
                tv_connect.setText(getString(R.string.add_aty_connecting));
                break;
            case STATUS_NORMAL:
                imageView.setVisibility(View.GONE);
                imageView.clearAnimation();
                image_cancel.setVisibility(View.GONE);
                rl_tip1.setVisibility(View.VISIBLE);
                ll_tip2.setVisibility(View.GONE);
                tv_connect.setText(getString(R.string.add_aty_start_connect));
//                ToastUtils.showToast(context,getString(R.string.main_aty_bind_fail));
                String ssid = WifiUtils.getSsid(context);
                et_ssid.setText(ssid);
                break;
        }
    }

    private void setWifiToAP(final ACDeviceActivator activator, final String ssid, final String passWord) {
        AC.deviceActivator(ACDeviceActivator.QCLTLINK).startApLink(ssid, passWord, (int) TimeUnit.SECONDS.toMillis(20), new PayloadCallback<Boolean>() {
            @Override
            public void success(Boolean aBoolean) {
                MyLog.e(TAG, "setWifiToAP aBoolean = " + aBoolean);
            }

            @Override
            public void error(ACException e) {
                MyLog.e(TAG, "setWifiToAP e = " + e.toString());
//                setWifiToAP(activator_qc,ssid,passWord);
            }
        }, new PayloadCallback<ACDeviceBind>() {
            @Override
            public void success(ACDeviceBind acDeviceBind) {
                MyLog.e(TAG, "success acDeviceBind = " + acDeviceBind.toString());
            }

            @Override
            public void error(ACException e) {
                MyLog.e(TAG, "error e = " + e.toString());
            }
        });
    }

    public int getCipherType(String ssid) {
        int type = 0;
        List<ScanResult> list = wifiManager.getScanResults();
        for (ScanResult scResult : list) {
            if (!TextUtils.isEmpty(scResult.SSID) && scResult.SSID.equals(ssid)) {
                String capabilities = scResult.capabilities;
                if (!TextUtils.isEmpty(capabilities)) {
                    if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
                        type = 2;
                    } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
                        type = 3;
                    } else {
                        type = 1;
                    }
                }
            }
        }
        return type;
    }

    public boolean isTheSameDevice(String ap_ssid, String physicalId) {
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

    public void bindDevice(String physicalId) {
        AC.bindMgr().bindDevice(subdomain, physicalId, "", new PayloadCallback<ACUserDevice>() {
            @Override
            public void success(ACUserDevice userDevice) {
                MyLog.e(TAG, "bindDevice success " + userDevice.toString());
                if (!isDestroyed()) {
                    stop();
                    unRegisterLocation();
                    Intent i = new Intent(context, BindSucActivity.class);
                    i.putExtra(AddActivity.EXTAR_DEVID, userDevice.deviceId);
                    startActivity(i);
                }
            }

            @Override
            public void error(ACException e) {
                MyLog.e(TAG, "bindDevice errorCode " + e.toString());
                handler.sendEmptyMessageDelayed(TAG_BIND_FAIL, 500);
            }
        });
    }

    public void stopAbleLink() {
        if (activator != null && activator.isAbleLink()) {
            activator.stopAbleLink();
        }
    }

//    public void isDeviceBound(final String id){
//        AC.bindMgr().isDeviceBound(id, new PayloadCallback<Boolean>(){
//            @Override
//            public void success(Boolean aBoolean) {
//                MyLog.e(TAG,"isDeviceBound  success  "+aBoolean);
//                if (aBoolean||isTimeOut){
//                    handler.sendEmptyMessage(TAG_BIND_FAIL);
//                } else {
//                    bindDevice(id);
//                }
//            }
//
//            @Override
//            public void error(ACException e) {
//                MyLog.e(TAG,"isDeviceBound  errorCode  "+e.toString());
//                handler.sendEmptyMessage(TAG_BIND_FAIL);
//            }
//        });
//    }

    @Override
    public void onBackPressed() {
        stop();
        unRegisterLocation();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOCATION) {
            locationEnable = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (locationEnable) {
                ap_ssid = et_ssid.getText().toString();
                if (TextUtils.isEmpty(ap_ssid) || !ap_ssid.startsWith("Robot")) {
                    ToastUtils.showToast(context, getString(R.string.third_ap_aty_port_));
                } else {
                    WifiInfo info = wifiManager.getConnectionInfo();
                    if (info != null) {
                        String bssid = info.getBSSID();
                        if (!TextUtils.isEmpty(bssid)) {
                            connect(bssid);
                        }
                    }
                }
            } else {
                ToastUtils.showToast(context, getString(R.string.third_ap_aty_location));
            }
        }
    }

    public void connect(String bssid) {
        BigInteger id = new BigInteger(bssid.replace(":", ""), 16);
        long mac;
        if (!bssid.startsWith("84")) {
            mac = id.longValue() - 1;
        } else {
            mac = id.longValue();
        }
        physicalId = Long.toHexString(mac);
        MyLog.e(TAG, "physicalId = " + physicalId);
        if (TextUtils.isEmpty(physicalId) || !isTheSameDevice(ap_ssid, physicalId)) {
            ToastUtils.showToast(context, getString(R.string.third_ap_aty_port_));
        } else {
            setStatus(STATUS_CONNECTING);
            isCanceled = false;
            isTimeOut = false;
            setWifiToAP(null, ssid, pass);
            handler.sendEmptyMessageDelayed(TAG_START_BIND, 15 * 1000);
        }
    }

    public void unRegisterLocation() {
        getContentResolver().unregisterContentObserver(mGpsMonitor);
    }
}
