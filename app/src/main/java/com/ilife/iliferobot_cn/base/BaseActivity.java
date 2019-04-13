package com.ilife.iliferobot_cn.base;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

/**
 * Created by chenjiaping on 2017/11/9.
 */

public class BaseActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= 21) {
//            View decorView = getWindow().getDecorView();
//            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
//            decorView.setSystemUiVisibility(option);
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//            getWindow().setNavigationBarColor(Color.TRANSPARENT);
//        }
//        if (Build.VERSION.SDK_INT >= 21) {
////            View decorView = getWindow().getDecorView();
////            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
////                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
////            decorView.setSystemUiVisibility(option);
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                getWindow().getDecorView().setSystemUiVisibility(
//                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//            }
//        }
//        getSupportActionBar().hide();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
