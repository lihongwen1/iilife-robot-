package com.ilife.iliferobot.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.service.ACUserDevice;
import com.ilife.iliferobot.BuildConfig;
import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.contract.ApWifiContract;
import com.ilife.iliferobot.presenter.ApWifiPresenter;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.utils.MyLogger;
import com.ilife.iliferobot.utils.SpUtils;
import com.tencent.bugly.crashreport.CrashReport;

import butterknife.BindView;

/**
 * 直接连wifi的ap配网模式
 */
public class ApWifiActivity extends BackBaseActivity<ApWifiPresenter> implements ApWifiContract.View {
    private final String TAG = ApWifiActivity.class.getSimpleName();
    public static final String EXTAR_DEVID = "EXTAR_DEVID";
    public static final String EXTAR_ROBOT_SSID = "EXTAR_ROBOT_SSID";
    Context context;
    @BindView(R.id.tv_bind_progress)
    TextView tv_bind_progress;
    @BindView(R.id.pb_bind_progress)
    ProgressBar pb_BindProgress;
    private String homeSsid;
    private String robot_ssid;
    private String homePassword;
    private boolean isStartBinding;
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        preCheckAndBindDevice();
    }

    private void preCheckAndBindDevice() {
        MyLogger.e("ConnectDeviceApActivity", "checkAndBindWifi");
        final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo inf = cm.getActiveNetworkInfo();
            MyLogger.e("ConnectDeviceApActivity", "test Wifi Type==:" + inf.getType() + "," + inf.isConnected() + "," + inf.isAvailable());
            if (inf.getType() == ConnectivityManager.TYPE_MOBILE) {//网络类型为4G,则需要绑定应用到特定WIFI网络
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    NetworkRequest.Builder builder = new NetworkRequest.Builder();
                    builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
                    builder.removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                    networkCallback = new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(Network network) {
                            MyLogger.d(TAG, "-----网络可用，bind wifi");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                MyLogger.d("NetworkRequest", "-----网络可用，bind wifi");
                                cm.bindProcessToNetwork(network);
                            }
                            if (!isStartBinding) {
                                bindDevice();
                                isStartBinding = true;
                            }
                        }
                    };

                    cm.requestNetwork(builder.build(), networkCallback);
                } else {
                    bindDevice();
                }
            } else {
                bindDevice();
            }
        } else {
            bindDevice();
        }
    }


    @Override
    public void attachPresenter() {
        mPresenter = new ApWifiPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected boolean isChildPage() {
        return true;
    }

    public void initData() {
        isStartBinding = false;
        context = this;
        robot_ssid = getIntent().getStringExtra(EXTAR_ROBOT_SSID);
        homeSsid = (String) SpUtils.get(this, ConnectHomeWifiActivity.EXTRA_SSID, "unknown");
        homePassword = (String) SpUtils.get(this, ConnectHomeWifiActivity.EXTRA_PASS, "unknown");
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_ap_wifi;
    }

    @Override
    public void initView() {
    }

    /**
     * 绑定设备
     */
    @Override
    public void bindDevice() {
        MyLogger.d(TAG,"开始绑定网络----");
        if (robot_ssid == null || !robot_ssid.contains("Robot")) {
            mPresenter.connectToDevice();
        } else {
            mPresenter.connectToDeviceWithSsid(robot_ssid);
        }

    }

    @Override
    public String getHomeSsid() {
        return homeSsid;
    }

    @Override
    public String getPassWord() {
        return homePassword;
    }

    @Override
    public void bindSuccess(ACUserDevice userDevice) {
        Intent i = new Intent(context, BindSucActivity.class);
        i.putExtra(EXTAR_DEVID, userDevice.deviceId);
        startActivity(i);
        removeActivity();
    }

    @Override
    public void bindFail(String message) {
        if (BuildConfig.Area != AC.REGIONAL_CENTRAL_EUROPE) {
            CrashReport.postCatchedException(new Exception(message));
        }
        MyLogger.d(TAG, "配网失败：    " + message);
        startActivity(new Intent(this, BindFailActivity.class));
        removeActivity();
    }

    @Override
    public void updateBindProgress(String tip, int progress) {
        if (tip == null) {
            return;
        }
        runOnUiThread(() -> {
            if (tv_bind_progress == null) {
                return;
            }
            tv_bind_progress.setText(progress + "%");
            pb_BindProgress.setProgress(progress);
        });

    }

    @Override
    protected void beforeFinish() {
        //取消配网，并结束页面
        mPresenter.cancelApWifi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null && networkCallback != null) {
                cm.bindProcessToNetwork(null);
                cm.unregisterNetworkCallback(networkCallback);
            }
        }
    }
}
