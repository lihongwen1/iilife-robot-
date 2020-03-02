package com.ilife.iliferobot.activity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ilife.iliferobot.R;
import com.ilife.iliferobot.base.BackBaseActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class PrepareFindNetActivity extends BackBaseActivity {
    @BindView(R.id.tv_top_title)
    TextView tv_top_title;

    @Override
    public int getLayoutId() {
        return R.layout.activity_prepare_find_net;
    }

    @Override
    public void initView() {
        tv_top_title.setText("Preparing to fnd a notwork");
    }

    @OnClick(R.id.bt_prepare_ok)
    public void onClick(View view) {
        //TODO GO next activity
        Intent intent = new Intent(PrepareFindNetActivity.this, ConnectHomeWifiActivity.class);
        startActivity(intent);
    }

}
