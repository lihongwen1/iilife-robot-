package com.ilife.iliferobot_cn.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;

/**
 * Created by chenjiaping on 2017/10/23.
 */
//DONE
public class SelectActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = SelectActivity.class.getSimpleName();
    Context context;
    ImageView image_back;
    RelativeLayout rl_x;
    RelativeLayout rl_w;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        initView();
    }

    private void initView() {
        context = this;
        image_back = (ImageView) findViewById(R.id.image_back);
        rl_x = (RelativeLayout) findViewById(R.id.rl_x);
        rl_w = (RelativeLayout) findViewById(R.id.rl_w);
        image_back.setOnClickListener(this);
        rl_x.setOnClickListener(this);
        rl_w.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.image_back:
                finish();
                break;
            case R.id.rl_x:
                i = new Intent(context, SelectActivity_x.class);
                startActivity(i);
                break;
            case R.id.rl_w:
                i = new Intent(context, SelectActivity_w.class);
                startActivity(i);
                break;
        }
    }
}
