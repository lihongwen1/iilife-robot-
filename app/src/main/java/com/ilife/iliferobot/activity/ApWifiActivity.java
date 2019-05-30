package com.ilife.iliferobot.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.accloud.service.ACUserDevice;
import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.contract.ApWifiContract;
import com.ilife.iliferobot.presenter.ApWifiPresenter;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.utils.SpUtils;

import butterknife.BindView;

/**
 * 直接连wifi的ap配网模式
 */
public class ApWifiActivity extends BackBaseActivity<ApWifiPresenter> implements ApWifiContract.View {
    private final String TAG = ApWifiActivity.class.getSimpleName();
    public static final String EXTAR_DEVID = "EXTAR_DEVID";
    Context context;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.tv_bind_progress)
    TextView tv_bind_progress;
    @BindView(R.id.pb_bind_progress)
    ProgressBar pb_BindProgress;


    private String ssid;
    private String password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initData();
        bindDevice();
    }

    @Override
    public void attachPresenter() {
        mPresenter = new ApWifiPresenter();
        mPresenter.attachView(this);
    }

    public void initData() {
        context = this;
        ssid = (String) SpUtils.get(this, FirstApActivity.EXTRA_SSID, "unknown");
        password = (String) SpUtils.get(this, FirstApActivity.EXTRA_PASS, "unknown");
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_ap_wifi;
    }

    @Override
    public void initView() {
        tv_title.setText("配网中");

    }

    /**
     * 绑定设备
     */
    @Override
    public void bindDevice() {
        mPresenter.connectToDevice();
    }

    @Override
    public String getSsid() {
        return ssid;
    }

    @Override
    public String getPassWord() {
        return password;
    }

    @Override
    public void bindSuccess(ACUserDevice userDevice) {
        Intent i = new Intent(context, BindSucActivity.class);
        i.putExtra(EXTAR_DEVID, userDevice.deviceId);
        startActivity(i);
    }

    @Override
    public void bindFail(String message) {
        Log.d(TAG,"绑定失败：    "+message);
        startActivity(new Intent(this, ConnectDeviceApActivity.class));
        finish();
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
}