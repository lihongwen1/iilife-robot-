package com.ilife.iliferobot_cn.activity;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BackBaseActivity;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.ToastUtils;
import com.ilife.iliferobot_cn.utils.UserUtils;
import com.ilife.iliferobot_cn.utils.Utils;
import com.ilife.iliferobot_cn.utils.WifiUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by c on 2017/7/15.
 */
//Done
public class FirstApActivity extends BackBaseActivity {
    private final String TAG = FirstApActivity.class.getSimpleName();
    public static final String EXTRA_SSID = "EXTRA_SSID";
    public static final String EXTRA_PASS = "EXTRA_PASS";
    Context context;
    @BindView(R.id.image_show_pass)
    ImageView image_show;

    @BindView(R.id.tv_ssid)
    TextView tv_ssid;
    @BindView(R.id.et_pass)
    EditText et_pass;
    @BindView(R.id.tv_top_title)
    TextView tv_title;


    String ssid;
    String pass;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initData();
        checkGps();
    }

    public void initData() {
        context = this;
    }

    private void checkGps() {
        // 用户已经同意该权限
        String ssid = WifiUtils.getSsid(context);
        if (!TextUtils.isEmpty(ssid) && !ssid.contains("unknown")) {
            tv_ssid.setText(ssid);
        } else {
            if (!checkGpsIsOpen()) {
                goSetGps();
            }
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_ap_first;
    }

    @Override
    public void initView() {
        Utils.setTransformationMethod(et_pass, false);
        tv_title.setText(R.string.ap_wifi_guide);
    }

    @OnClick({R.id.image_show_pass, R.id.iv_set, R.id.bt_next})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_show_pass:
                boolean isSelected = !image_show.isSelected();
                int curIndex = et_pass.getSelectionStart();
                image_show.setSelected(isSelected);
                Utils.setTransformationMethod(et_pass, isSelected);
                et_pass.setSelection(curIndex);
                break;
            case R.id.iv_set:
                Intent i = new Intent();
                i.setAction("android.net.wifi.PICK_WIFI_NETWORK");
                startActivity(i);
                break;
            case R.id.bt_next:
                ssid = tv_ssid.getText().toString();
                if (TextUtils.isEmpty(ssid) || ssid.contains("unknown")) {
                    if (!checkGpsIsOpen()) {
                        goSetGps();
                    } else {
                        ToastUtils.showToast(context, getString(R.string.add_aty_no_wifi));
                    }
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
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                WifiInfo info = wifiManager.getConnectionInfo();
                if (info != null) {
                    String bssid = info.getBSSID();
                    if (!TextUtils.isEmpty(bssid)) {
                        if (bssid.startsWith("02")) {
                            goSetGps();
                        } else {
                            SpUtils.put(this, EXTRA_SSID, ssid);
                            SpUtils.put(this, EXTRA_PASS, pass);
                            Intent i_ap = new Intent(context, ApGuideActivityX900.class);
                            startActivity(i_ap);
                        }
                    }
                }

                break;
        }
    }


    private boolean checkGpsIsOpen() {
        boolean isOpen;
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        isOpen = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isOpen;
    }

    private void goSetGps() {
        UniversalDialog universalDialog = new UniversalDialog();
        universalDialog.setDialogType(UniversalDialog.TYPE_NORMAL_MID_BUTTON)
                .setMidText(Utils.getString(R.string.ap_aty_setting)).setOnMidButtonClck(() -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 1);
        });
        universalDialog.show(getSupportFragmentManager(), "gps");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 用户已经同意该权限
        String ssid = WifiUtils.getSsid(context);
        if (!TextUtils.isEmpty(ssid)) {
            tv_ssid.setText(ssid);
        }
    }
}
