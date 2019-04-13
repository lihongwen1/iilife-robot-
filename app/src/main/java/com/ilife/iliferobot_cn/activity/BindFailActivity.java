package com.ilife.iliferobot_cn.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;

/**
 * Created by chengjiaping on 2018/9/20.
 */

public class BindFailActivity extends BaseActivity implements View.OnClickListener {
    final String TAG = BindFailActivity.class.getSimpleName();
    ImageView image_back;
    Context context;
    Button bt_ap;
    Button bt_try;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_bind_fail);
        init();
    }

    public void init() {
        context = this;
        image_back = (ImageView) findViewById(R.id.image_back);
        bt_ap = (Button) findViewById(R.id.bt_ap);
        bt_try = (Button) findViewById(R.id.bt_try);

        bt_ap.setOnClickListener(this);
        bt_try.setOnClickListener(this);
        image_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_ap:
                Intent intent = new Intent(BindFailActivity.this, ApGuideActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.bt_try:
                finish();
                break;
            case R.id.image_back:
                finish();
                break;
        }
    }
}
