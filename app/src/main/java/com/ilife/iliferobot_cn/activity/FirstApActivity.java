package com.ilife.iliferobot_cn.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BackBaseActivity;
import com.ilife.iliferobot_cn.utils.MyLog;
import com.ilife.iliferobot_cn.utils.ToastUtils;
import com.ilife.iliferobot_cn.utils.UserUtils;
import com.ilife.iliferobot_cn.utils.Utils;
import com.ilife.iliferobot_cn.utils.WifiUtils;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

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
    }

    public void initData(){
        context = this;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_ap_first;
    }

    @Override
    public void initView() {
        Utils.setTransformationMethod(et_pass,false);
        tv_title.setText(R.string.ap_wifi_guide);
    }

    @OnClick({R.id.image_show_pass, R.id.tv_set, R.id.bt_next})
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image_show_pass:
                boolean isSelected = !image_show.isSelected();
                int curIndex = et_pass.getSelectionStart();
                image_show.setSelected(isSelected);
                Utils.setTransformationMethod(et_pass,isSelected);
                et_pass.setSelection(curIndex);
                break;
            case R.id.tv_set:
                Intent i = new Intent();
                i.setAction("android.net.wifi.PICK_WIFI_NETWORK");
                startActivity(i);
                break;
            case R.id.bt_next:
                ssid = tv_ssid.getText().toString();
                if (TextUtils.isEmpty(ssid)){
                    ToastUtils.showToast(context,getString(R.string.add_aty_no_wifi));
                    return;
                }
                pass = et_pass.getText().toString().trim();
                if (TextUtils.isEmpty(pass)){
                    ToastUtils.showToast(context,getString(R.string.ap_aty_input_pass));
                    return;
                }

                if (!UserUtils.rexCheckPassword(pass)){
                    ToastUtils.showToast(context,getString(R.string.add_aty_wrong_wifi_pass));
                    return;
                }
                Intent i_ap = new Intent(context, ApWifiActivity.class);
                i_ap.putExtra(EXTRA_SSID,ssid);
                i_ap.putExtra(EXTRA_PASS,pass);
                startActivity(i_ap);
                break;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus){
            new RxPermissions(this).requestEach(Manifest.permission.ACCESS_COARSE_LOCATION).subscribe(new Consumer<Permission>() {
                @Override
                public void accept(@NonNull Permission permission) throws Exception {
                    if (permission.granted) {
                        // 用户已经同意该权限
                        String ssid = WifiUtils.getSsid(context);
                        if (!TextUtils.isEmpty(ssid)){
                            tv_ssid.setText(ssid);
                        }
                        MyLog.e(TAG,"onWindowFocusChanged permission.granted ");
                    } else {
                        // 用户拒绝了该权限，并且选中『不再询问』
                        ToastUtils.showToast(context,getString(R.string.access_location));
                        MyLog.e(TAG,"onWindowFocusChanged permission 拒绝了");
                    }
                }
            }).dispose();
        }
    }
}
