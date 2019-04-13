package com.ilife.iliferobot_cn.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.accloud.service.ACException;
import com.ilife.iliferobot_cn.listener.ReNameListener;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.DeviceUtils;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.ToastUtils;
import com.ilife.iliferobot_cn.utils.UserUtils;

/**
 * Created by chengjiaping on 2018/9/3.
 */

public class BindSucActivity extends BaseActivity implements View.OnClickListener {
    final String TAG = BindSucActivity.class.getSimpleName();
    long deviceId;
    String name;
    Context context;
    String subdomain;
    Button bt_done;
    EditText et_devName;
    TextView tv_connected;
    ImageView image_back;
    ReNameListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_suc);
        initView();
        initData();
    }

    private void initView() {
        context = this;
        bt_done = (Button) findViewById(R.id.bt_done);
        et_devName = (EditText) findViewById(R.id.et_devName);
        et_devName.requestFocus();
        image_back = (ImageView) findViewById(R.id.image_back);
        tv_connected = (TextView) findViewById(R.id.tv_connected);

        bt_done.setOnClickListener(this);
        image_back.setOnClickListener(this);
    }

    public void initData() {
        Bundle bundle = getIntent().getExtras();
        String devName;
        if (bundle != null) {
            deviceId = bundle.getLong(AddActivity.EXTAR_DEVID);
        }
        subdomain = SpUtils.getSpString(context, SelectActivity_x.KEY_SUBDOMAIN);
        if (subdomain.equals(Constants.subdomain_x785)) {
            devName = "X785";
            tv_connected.setText(getString(R.string.bind_suc_aty_done_785));
        } else if (subdomain.equals(Constants.subdomain_x787)) {
            devName = "X787";
            tv_connected.setText(getString(R.string.bind_suc_aty_done_787));
        } else {
            devName = getString(R.string.bind_suc_sty_robot_name);
            tv_connected.setText(getString(R.string.bind_suc_aty_done_800));
        }
        et_devName.setText(devName);
        et_devName.setSelection(devName.length());
        UserUtils.setInputFilter(et_devName);

        listener = new ReNameListener() {
            @Override
            public void onSuccess() {
                ToastUtils.showToast(context, context.getString(R.string.bind_aty_reName_suc));
                SpUtils.saveString(context, "devName", name);
                Intent i = new Intent(context, MainActivity.class);
                startActivity(i);
                finish();
            }

            @Override
            public void onError(ACException e) {
                ToastUtils.showToast(context, getString(R.string.bind_aty_reName_fail));
                Intent i = new Intent(context, MainActivity.class);
                startActivity(i);
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back:
                finish();
                break;
            case R.id.bt_done:
                name = et_devName.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {

                } else {
                    DeviceUtils.renameDevice(deviceId, name, subdomain, listener);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}
