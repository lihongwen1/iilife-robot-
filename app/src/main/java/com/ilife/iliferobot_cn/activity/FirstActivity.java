package com.ilife.iliferobot_cn.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.accloud.cloudservice.AC;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;


/**
 * Created by chenjiaping on 2017/7/20.
 */

public class FirstActivity extends BaseActivity {
    private final String TAG = FirstActivity.class.getSimpleName();
    private final int GOTOMAIN = 0x11;
    ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT>=21){
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        }
//        int option = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(option);
//        getSupportActionBar().hide();
        setContentView(R.layout.avtivity_first);
        if (!this.isTaskRoot()) {
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GOTOMAIN:
                    gotoMain();
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        handler.sendEmptyMessageDelayed(GOTOMAIN, 1000);
    }

    public void gotoMain() {
        Intent i;
        if (AC.accountMgr().isLogin()) {
            i = new Intent(this, MainActivity.class);
        } else {
            i = new Intent(this, SecondActivity.class);
        }
        startActivity(i);
        finish();
    }
}
