package com.ilife.iliferobot_cn.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;

/**
 * Created by chengjiaping on 2018/3/8.
 */
//DONE
public class SelectActivity_w extends BaseActivity implements View.OnClickListener {
    ImageView image_back;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_w);
        initView();
    }

    public void initView() {
        image_back = (ImageView) findViewById(R.id.image_back);
        image_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back:
                finish();
                break;
        }
    }
}
