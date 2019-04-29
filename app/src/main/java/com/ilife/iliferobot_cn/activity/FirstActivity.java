package com.ilife.iliferobot_cn.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.accloud.cloudservice.AC;
import com.badoo.mobile.util.WeakHandler;
import com.ilife.iliferobot_cn.R;

import androidx.appcompat.app.AppCompatActivity;


/**
 * Created by chenjiaping on 2017/7/20.
 */

public class FirstActivity extends AppCompatActivity {
    private final String TAG = FirstActivity.class.getSimpleName();
    private final int GOTOMAIN = 0x11;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int option = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(option);
        setContentView(R.layout.avtivity_first);
        if (!this.isTaskRoot()) {
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                finish();
            }
        }
    }

    private WeakHandler handler = new WeakHandler(msg -> {
        switch (msg.what) {
            case GOTOMAIN:
                gotoMain();
                break;
        }
        return false;
    });

    @Override
    protected void onResume() {
        super.onResume();
        handler.sendEmptyMessageDelayed(GOTOMAIN, 1000);
    }

    public void gotoMain() {
        Intent i;
//        i = new Intent(this,MapActivity_X9_.class);
        if (AC.accountMgr().isLogin()){
            i = new Intent(this, MainActivity.class);
        } else {
            i = new Intent(this, QuickLoginActivity.class);
        }
        startActivity(i);
        finish();
    }
}