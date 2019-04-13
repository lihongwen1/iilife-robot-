package com.ilife.iliferobot_cn.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.utils.MyLog;
import com.ilife.iliferobot_cn.utils.ToastUtils;


/**
 * Created by chenjiaping on 2017/7/5.
 */
//DONE
public class SecondActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = SecondActivity.class.getSimpleName();
    long exitTime;
    Context context;
    Button bt_login;
    TextView tv_register;
    public static Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        initView();
    }

    public void initView() {
        activity = this;
        context = this;
        bt_login = (Button) findViewById(R.id.bt_login);
        tv_register = (TextView) findViewById(R.id.tv_register);

        bt_login.setOnClickListener(this);
        tv_register.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.bt_login:
                i = new Intent(this, LoginActivity.class);
                startActivity(i);
                break;
            case R.id.tv_register:
                i = new Intent(this, RegisterActivity.class);
                startActivity(i);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - exitTime >= 2000) {
            ToastUtils.showToast(context, getString(R.string.main_aty_press_exit));
            exitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
        MyLog.e(TAG, "onBackPressed====");
    }
}
