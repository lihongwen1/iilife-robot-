package com.ilife.iliferobot_cn.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.accloud.service.ACUserDevice;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BackBaseActivity;
import com.ilife.iliferobot_cn.contract.ApWifiContract;
import com.ilife.iliferobot_cn.presenter.ApWifiPresenter;
import com.ilife.iliferobot_cn.utils.ToastUtils;

import butterknife.BindView;

/**
 * 直接连wifi的ap配网模式
 */
public class ApWifiActivity extends BackBaseActivity<ApWifiPresenter> implements ApWifiContract.View {
    private final String TAG = ApWifiActivity.class.getSimpleName();
    Context context;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.tv_bind_tip)
    TextView tv_bind_tip;
    @BindView(R.id.tv_bind_progress)
    TextView tv_bind_progress;
    @BindView(R.id.pb_bind_progress)
    ProgressBar pb_BindProgress;


    private ApWifiPresenter apWifiPresenter;
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
        apWifiPresenter = new ApWifiPresenter();
        apWifiPresenter.attachView(this);
    }

    public void initData() {
        context = this;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            ssid = bundle.getString(FirstApActivity.EXTRA_SSID);
            password = bundle.getString(FirstApActivity.EXTRA_PASS);
        }
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
        apWifiPresenter.connectToDevice();
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
        i.putExtra(AddActivity.EXTAR_DEVID, userDevice.deviceId);
        startActivity(i);
    }

    @Override
    public void bindFail(String message) {
        ToastUtils.showToast("绑定失败");
        updateBindProgress(message, 0);
    }

    @Override
    public void updateBindProgress(String tip, int progress) {
        if (tip == null) {
            return;
        }
        runOnUiThread(() -> {
            tv_bind_tip.setText(tip);
            tv_bind_progress.setText(progress + "%");
            pb_BindProgress.setProgress(progress);
        });

    }
}
