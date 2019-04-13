package com.ilife.iliferobot_cn.activity;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
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
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.MyLog;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.ToastUtils;
import com.ilife.iliferobot_cn.utils.UserUtils;
import com.ilife.iliferobot_cn.utils.Utils;
import com.ilife.iliferobot_cn.utils.WifiUtils;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;


/**
 * Created by chenjiaping on 2017/7/4.
 */
//DONE
public class AddActivity extends BaseActivity implements View.OnClickListener {
    final String TAG = AddActivity.class.getSimpleName();
    public static final String EXTAR_DEVID = "EXTAR_DEVID";
    final long DELAYMILLIS = 2 * 1000;
    final int STATUS_CONNECTING = 0x01;
    final int STATUS_NORMAL = 0x02;
    final int TAG_BIND_FAIL = 0x03;
    Context context;
    TextView tv_title;
    TextView tv_ssid;
    TextView tv_tip1, tv_tip2;
    TextView tv_ap;
    TextView tv_connect;
    EditText et_pass;
    ImageView image_show;
    ImageView image_cancel;
    ImageView imageView;
    ImageView image_back;
    RelativeLayout rl_tip1;
    LinearLayout ll_tip2;
    RelativeLayout rl_connect;

    int index;
    int timeOut;
    String ssid;
    String pass;
    String subdomain;
    long subdomainId;
    boolean isTimeOut, isCanceled;
    Animation animation;
    CountDownTimer timer;
    ACDeviceActivator activator;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TAG_BIND_FAIL:
                    if (!isTimeOut) {
                        deviceActive(ssid, pass);
                    } else {
                        stopAbleLink();
                        setStatus(STATUS_NORMAL);
//                        ToastUtils.showToast(context,getString(R.string.personal_aty_bind_fail));
                        if (!isCanceled) {
                            Intent i = new Intent(AddActivity.this, BindFailActivity.class);
                            startActivity(i);
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        initView();
        initData();
    }

    private void initView() {
        context = this;
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_ssid = (TextView) findViewById(R.id.tv_ssid);
        tv_tip1 = (TextView) findViewById(R.id.tv_tip1);
        tv_tip2 = (TextView) findViewById(R.id.tv_tip2);
        tv_ap = (TextView) findViewById(R.id.tv_ap);
        tv_connect = (TextView) findViewById(R.id.tv_connect);
        et_pass = (EditText) findViewById(R.id.et_pass);
        Utils.setTransformationMethod(et_pass, false);
        rl_tip1 = (RelativeLayout) findViewById(R.id.rl_tip1);
        ll_tip2 = (LinearLayout) findViewById(R.id.ll_tip2);
        rl_connect = (RelativeLayout) findViewById(R.id.rl_connect);
        imageView = (ImageView) findViewById(R.id.imageView);
        image_show = (ImageView) findViewById(R.id.image_show_pass);
        image_cancel = (ImageView) findViewById(R.id.image_cancel);
        image_back = (ImageView) findViewById(R.id.image_back);
        tv_ap.setOnClickListener(this);
        image_show.setOnClickListener(this);
        rl_connect.setOnClickListener(this);
        image_cancel.setOnClickListener(this);
        image_back.setOnClickListener(this);
    }

    private void initData() {
        animation = AnimationUtils.loadAnimation(context, R.anim.anims);
        animation.setInterpolator(new LinearInterpolator());
        subdomain = SpUtils.getSpString(context, SelectActivity_x.KEY_SUBDOMAIN);
        subdomainId = SpUtils.getLong(context, SelectActivity_x.KEEY_SUBDOMAIN_ID);
        activator = AC.deviceActivator(Constants.DEVICE_TYPE_QCLTLINK);
//        ssid = activator.getSSID();
//        if (!TextUtils.isEmpty(ssid)){
//            tv_ssid.setText(ssid);
//        }
        timer = new CountDownTimer(60 * 1000, 1000) {
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
        if (!subdomain.equals(Constants.subdomain_x800)) {
            tv_tip2.setText(getString(R.string.add_aty_tip22));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_show_pass:
                boolean isSelected = !image_show.isSelected();
                int curIndex = et_pass.getSelectionStart();
                image_show.setSelected(isSelected);
                Utils.setTransformationMethod(et_pass, isSelected);
                et_pass.setSelection(curIndex);
                break;
            case R.id.rl_connect:
                ssid = tv_ssid.getText().toString();
                if (TextUtils.isEmpty(ssid)) {
                    ToastUtils.showToast(context, getString(R.string.add_aty_no_wifi));
                    return;
                }
                pass = et_pass.getText().toString().trim();
                if (TextUtils.isEmpty(pass)) {
                    ToastUtils.showToast(context, getString(R.string.ap_aty_input_pass));
                    return;
                }

                if (!UserUtils.rexCheckPassword(pass)) {
                    ToastUtils.showToast(context, getString(R.string.add_aty_wrong_wifi_pass));
                    return;
                }
                isCanceled = false;
                setStatus(STATUS_CONNECTING);
                timer.start();
                index = 0;
                timeOut = 5 * 1000;
                deviceActive(ssid, pass);
                break;
            case R.id.image_cancel:
                isCanceled = true;
                setStatus(STATUS_NORMAL);
                cancel();
                break;
            case R.id.tv_ap:
                Intent i = new Intent(context, ApGuideActivity.class);
                startActivity(i);
                break;
            case R.id.image_back:
                finish();
                break;
        }
    }

    public void cancel() {
        timer.cancel();
        isTimeOut = true;
        stopAbleLink();
    }

    public void setStatus(int status) {
        switch (status) {
            case STATUS_CONNECTING:
                imageView.setVisibility(View.VISIBLE);
                imageView.startAnimation(animation);
                image_cancel.setVisibility(View.VISIBLE);
                rl_tip1.setVisibility(View.GONE);
                ll_tip2.setVisibility(View.VISIBLE);
                et_pass.setEnabled(false);
                tv_connect.setText(getString(R.string.add_aty_connecting));
                break;
            case STATUS_NORMAL:
                imageView.setVisibility(View.GONE);
                imageView.clearAnimation();
                image_cancel.setVisibility(View.GONE);
                rl_tip1.setVisibility(View.VISIBLE);
                ll_tip2.setVisibility(View.GONE);
                et_pass.setEnabled(true);
                tv_connect.setText(getString(R.string.add_aty_start_connect));
                break;
        }
    }

    public void stopAbleLink() {
        MyLog.e(TAG, "stopAbleLink");
        if (activator != null && activator.isAbleLink()) {
            activator.stopAbleLink();
        }
    }

    public void deviceActive(String ssid, String pass) {
        index++;
        if (index <= 10) {
            timeOut = 5 * 1000;
        } else {
            timeOut = 15 * 1000;
        }
        activator.startAbleLink(ssid, pass, timeOut, new PayloadCallback<ACDeviceBind>() {
            @Override
            public void success(ACDeviceBind acDeviceBind) {
                long id = acDeviceBind.getSubDomainId();
                MyLog.e(TAG, "startAbleLink  success acDeviceBind.getPhysicalDeviceId() " + acDeviceBind.getPhysicalDeviceId());
                if (!isTimeOut && id == subdomainId) {
                    isDeviceBound(acDeviceBind);
                } else {
                    handler.sendEmptyMessage(TAG_BIND_FAIL);
                }
            }

            @Override
            public void error(ACException e) {
                MyLog.e(TAG, "startAbleLink  errorCode " + e.toString());
                if (e.getErrorCode() == 1999) {
                    handler.sendEmptyMessageDelayed(TAG_BIND_FAIL, DELAYMILLIS);
                } else {
                    handler.sendEmptyMessage(TAG_BIND_FAIL);
                }
            }
        });
    }

    public void isDeviceBound(final ACDeviceBind deviceBinds) {
        final String physicalId = deviceBinds.getPhysicalDeviceId();
        AC.bindMgr().isDeviceBound(physicalId, new PayloadCallback<Boolean>() {
            @Override
            public void error(ACException e) {
                MyLog.e(TAG, "isDeviceBound  errorCode  " + e.toString());
                handler.sendEmptyMessage(TAG_BIND_FAIL);
            }

            @Override
            public void success(Boolean aBoolean) {
                MyLog.e(TAG, "isDeviceBound  success  " + aBoolean);
                if (aBoolean || isTimeOut) {
                    handler.sendEmptyMessage(TAG_BIND_FAIL);
                } else {
                    bindDevice(physicalId);
                }
            }
        });
    }

    public void bindDevice(String physicalId) {
        AC.bindMgr().bindDevice(subdomain, physicalId, "", new PayloadCallback<ACUserDevice>() {
            @Override
            public void success(ACUserDevice acUserDevice) {
                MyLog.e(TAG, "bindDevice  success  " + acUserDevice.toString());
                if (!isDestroyed()) {
                    stopAbleLink();
                    Intent i = new Intent(context, BindSucActivity.class);
                    i.putExtra(EXTAR_DEVID, acUserDevice.deviceId);
                    startActivity(i);
                    finish();
                }
            }

            @Override
            public void error(ACException e) {
                MyLog.e(TAG, "bindDevice  errorCode  " + e.toString());
                handler.sendEmptyMessage(TAG_BIND_FAIL);
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        MyLog.e(TAG, "onWindowFocusChanged hasFocus = " + hasFocus);
        if (hasFocus) {
            new RxPermissions(this).requestEach(Manifest.permission.ACCESS_COARSE_LOCATION).subscribe(new Consumer<Permission>() {
                @Override
                public void accept(@NonNull Permission permission) throws Exception {
                    if (permission.granted) {
                        // 用户已经同意该权限
                        String ssid = WifiUtils.getSsid(context);
                        if (!TextUtils.isEmpty(ssid)) {
                            tv_ssid.setText(ssid);
                        }
                        MyLog.e(TAG, "onWindowFocusChanged permission.granted ");
                    } else {
                        // 用户拒绝了该权限，并且选中『不再询问』
                        ToastUtils.showToast(context, getString(R.string.access_location));
                        MyLog.e(TAG, "onWindowFocusChanged permission 拒绝了");
                    }
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
