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
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BackBaseActivity;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.listener.ReNameListener;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.DeviceUtils;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.ToastUtils;
import com.ilife.iliferobot_cn.utils.UserUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by chengjiaping on 2018/9/3.
 */

public class BindSucActivity extends BackBaseActivity {
    final String TAG = BindSucActivity.class.getSimpleName();
    long deviceId;
    String name;
    Context context;
    String subdomain;
    @BindView(R.id.bt_done)
    Button bt_done;
    @BindView(R.id.et_devName)
    EditText et_devName;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    private ReNameListener listener;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_bind_suc;
    }

    @Override
    public void initView() {
        context = this;
        tv_title.setText(R.string.robot_connected);
        et_devName.requestFocus();
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
//            tv_connected.setText(getString(R.string.bind_suc_aty_done_785));
        } else if (subdomain.equals(Constants.subdomain_x787)) {
            devName = "X787";
//            tv_connected.setText(getString(R.string.bind_suc_aty_done_787));
        } else if (subdomain.equals(Constants.subdomain_x900)) {
            devName = "X900";
//            tv_connected.setText(getString(R.string.bind_suc_aty_done_787));
        } else {
            devName = getString(R.string.bind_suc_sty_robot_name);
//            tv_connected.setText(getString(R.string.bind_suc_aty_done_800));
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

    @OnClick({R.id.bt_done})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_done:
                name = et_devName.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {

                } else {
                    DeviceUtils.renameDevice(deviceId, name, subdomain, listener);
                }
                break;
        }
    }
}
