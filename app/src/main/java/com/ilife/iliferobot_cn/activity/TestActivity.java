package com.ilife.iliferobot_cn.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACException;
import com.accloud.service.ACUserDevice;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.MyLog;
import com.ilife.iliferobot_cn.utils.ToastUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created by chengjiaping on 2018/3/9.
 */

public class TestActivity extends BaseActivity implements View.OnClickListener {
    final String TAG = TestActivity.class.getSimpleName();
    EditText editText;
    Button bt_connect;
    Button bt_bind;
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initView();
    }

    private void initView() {
        context = this;
        editText = (EditText) findViewById(R.id.editText);
        bt_connect = (Button) findViewById(R.id.bt_connect);
        bt_bind = (Button) findViewById(R.id.bt_bind);

        bt_connect.setOnClickListener(this);
        bt_bind.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_connect:

//                AC.deviceLocalManager().setWifiToAP("ZhiYi_Test_plus1", "123456789", (int) TimeUnit.SECONDS.toMillis(10), new VoidCallback() {
//                    @Override
//                    public void success() {
//                        MyLog.e(TAG,"setWifiToAP success ");
//
//                    }
//
//                    @Override
//                    public void error(ACException e) {
//                        MyLog.e(TAG,"setWifiToAP errorCode "+e.toString());
//                    }
//                });
                break;

            case R.id.bt_bind:
                bindDevice(editText.getText().toString().trim());
                break;
        }
    }

    public void bindDevice(String physicalId) {
        AC.bindMgr().bindDevice(Constants.subdomain_x800, physicalId, "", new PayloadCallback<ACUserDevice>() {
            @Override
            public void success(ACUserDevice userDevice) {
                MyLog.e(TAG, "bindDevice success " + userDevice.toString());
                ToastUtils.showToast(context, "绑定成功");
            }

            @Override
            public void error(ACException e) {
                MyLog.e(TAG, "bindDevice errorCode " + e.toString());
                ToastUtils.showToast(context, "绑定失败 " + e.toString());
            }
        });
    }
}
