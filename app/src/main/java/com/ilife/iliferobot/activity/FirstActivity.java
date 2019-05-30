package com.ilife.iliferobot.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import com.accloud.cloudservice.AC;
import com.badoo.mobile.util.WeakHandler;
import com.ilife.iliferobot.base.BaseActivity;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.R;
import com.tbruyelle.rxpermissions2.RxPermissions;


/**
 * Created by chenjiaping on 2017/7/20.
 */

public class FirstActivity extends BaseActivity {
    private final String TAG = FirstActivity.class.getSimpleName();
    private final int GOTOMAIN = 0x11;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!this.isTaskRoot()) {
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                finish();
            }
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.avtivity_first;
    }

    @Override
    public void initView() {

    }

    private WeakHandler handler = new WeakHandler(msg -> {
        switch (msg.what) {
            case GOTOMAIN:
                gotoMain();
                break;
        }
        return false;
    });

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
    }

    private void checkPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION).subscribe(aBoolean -> {
            if (aBoolean) {
                handler.sendEmptyMessageDelayed(GOTOMAIN, 1000);
            } else {
                ToastUtils.showToast(this, getString(R.string.access_location));
                //未授权处理
            }
        }).dispose();
    }

    public void gotoMain() {
        Intent i;
//        i = new Intent(this,MapActivity_X9_.class);
        if (AC.accountMgr().isLogin()) {
            i = new Intent(this, MainActivity.class);
        } else {
            i = new Intent(this, QuickLoginActivity.class);
        }
        startActivity(i);
        finish();
    }

}
