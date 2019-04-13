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
 * Created by chengjiaping on 2018/8/28.
 */
//DONE
public class WifiGuideActivity extends BaseActivity implements View.OnClickListener {
    final String TAG = WifiGuideActivity.class.getSimpleName();
    Context context;
    ImageView image_back;
    Button bt_do;
    Button bt_done;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_wifi_guide;
    }

    public void initView() {
        context = this;
        image_back = (ImageView) findViewById(R.id.image_back);
        bt_do = (Button) findViewById(R.id.bt_do);
        bt_done = (Button) findViewById(R.id.bt_done);

        image_back.setOnClickListener(this);
        bt_do.setOnClickListener(this);
        bt_done.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back:
                finish();
                break;
            case R.id.bt_do:
                Intent i = new Intent();
                i.setAction("android.net.wifi.PICK_WIFI_NETWORK");
                startActivity(i);
                break;
            case R.id.bt_done:
                Intent intent = new Intent(context, AddActivity.class);
                startActivity(intent);
                break;
        }
    }
}
