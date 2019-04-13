package com.ilife.iliferobot_cn.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;

import butterknife.OnClick;

/**
 * Created by chengjiaping on 2018/9/20.
 */

public class BindFailActivity extends BaseActivity {
    final String TAG = BindFailActivity.class.getSimpleName();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activty_bind_fail;
    }

    @Override
    public void initView(){
    }

    @OnClick({R.id.bt_ap, R.id.bt_try, R.id.image_back})
    public void onClick(View view) {
        switch (view.getId()){
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
