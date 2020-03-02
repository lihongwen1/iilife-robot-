package com.ilife.iliferobot.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.ilife.iliferobot.R;
import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.utils.Utils;

import butterknife.BindView;
import butterknife.OnClick;

public class BindFailActivity extends BackBaseActivity {
    @BindView(R.id.tv_top_title)
    TextView title;

    @Override
    protected boolean canGoBack() {
        return false;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_bind_fail;
    }

    @Override
    public void initView() {
        title.setText(Utils.getString(R.string.ap_wifi_timeout));
        findViewById(R.id.image_back).setVisibility(View.GONE);
    }

    @OnClick(R.id.bt_retry)
    public void onclick(View v) {
        if (v.getId() == R.id.bt_retry) {
            Constants.IS_FIRST_AP = false;
            Intent intent = new Intent(this, ConnectHomeWifiActivity.class);
            startActivity(intent);
            removeActivity();
        }
    }
}
