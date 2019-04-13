package com.ilife.iliferobot_cn.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.ACDeviceActivator;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.MyLog;
import com.ilife.iliferobot_cn.utils.ToastUtils;
import com.ilife.iliferobot_cn.utils.UserUtils;
import com.ilife.iliferobot_cn.utils.Utils;
import com.ilife.iliferobot_cn.utils.WifiUtils;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * Created by c on 2017/7/15.
 */
//Done
public class FirstApActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = FirstApActivity.class.getSimpleName();
    public static final String EXTRA_SSID = "EXTRA_SSID";
    public static final String EXTRA_PASS = "EXTRA_PASS";
    Context context;
    ImageView image_back, image_show;
    TextView tv_set;
    TextView tv_ssid;
    EditText et_pass;
    Button bt_next;

    ACDeviceActivator activator;
    String ssid;
    String pass;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_ap_first);
        initView();
        initData();
    }

    public void initData() {
        context = this;
        activator = AC.deviceActivator(Constants.DEVICE_TYPE_QCLTLINK);
//        ssid = activator.getSSID();
//        if (!TextUtils.isEmpty(ssid)){
//            tv_ssid.setText(ssid);
//        }
    }

    private void initView() {
        image_back = (ImageView) findViewById(R.id.image_back);
        image_show = (ImageView) findViewById(R.id.image_show_pass);
        tv_set = (TextView) findViewById(R.id.tv_set);
        tv_ssid = (TextView) findViewById(R.id.tv_ssid);
        bt_next = (Button) findViewById(R.id.bt_next);
        et_pass = (EditText) findViewById(R.id.et_pass);
        Utils.setTransformationMethod(et_pass, false);

        image_back.setOnClickListener(this);
        image_show.setOnClickListener(this);
        tv_set.setOnClickListener(this);
        bt_next.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back:
                finish();
                break;
            case R.id.image_show_pass:
                boolean isSelected = !image_show.isSelected();
                int curIndex = et_pass.getSelectionStart();
                image_show.setSelected(isSelected);
                Utils.setTransformationMethod(et_pass, isSelected);
                et_pass.setSelection(curIndex);
                break;
            case R.id.tv_set:
                Intent i = new Intent();
                i.setAction("android.net.wifi.PICK_WIFI_NETWORK");
                startActivity(i);
                break;
            case R.id.bt_next:
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
                Intent i_ap = new Intent(context, ThirdApActivity.class);
                i_ap.putExtra(EXTRA_SSID, ssid);
                i_ap.putExtra(EXTRA_PASS, pass);
                startActivity(i_ap);
                break;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
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
}
