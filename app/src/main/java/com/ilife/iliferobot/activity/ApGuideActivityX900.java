package com.ilife.iliferobot.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.view.GifView;
import com.ilife.iliferobot.view.ToggleRadioButton;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.utils.SpUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by chengjiaping on 2018/8/28.
 */
//DONE
public class ApGuideActivityX900 extends BackBaseActivity {
    final String TAG = ApGuideActivityX900.class.getSimpleName();
    Context context;
    @BindView(R.id.bt_next)
    Button bt_next;
    String subdomain;
    @BindView(R.id.text_tip1)
    TextView text_tip1;
    @BindView(R.id.text_tip2)
    TextView text_tip2;
    @BindView(R.id.rb_next_tip)
    ToggleRadioButton rb_next_tip;
    @BindView(R.id.ll_ap_step1)
    LinearLayout ll_ap_step1;
    @BindView(R.id.ll_ap_step2)
    LinearLayout ll_ap_step2;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.gif_open_key)
    GifView gif_open_key;
    @BindView(R.id.gif_click_wifi)
    GifView gif_click_wifi;
    @BindView(R.id.tv_guide_tip4)
    TextView tv_guide_tip4;
    int start, strId, iconId;
    private int curStep;
    private int tip3_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // TODO 根据机型选择不同的布局文件
    @Override
    public int getLayoutId() {
        return R.layout.activity_ap_guide_x900;
    }

    @Override
    public void initView() {
        context = this;
        int open_key_id, click_wifi_id, tip1_id, tip2_id;

        subdomain = SpUtils.getSpString(context, SelectActivity_x.KEY_SUBDOMAIN);

        if (subdomain.equals(Constants.subdomain_x800)) {
            tip1_id = R.string.ap_guide_aty_tip1_x900;
            tip2_id = R.string.ap_guide_aty_tip2_x900;
            tip3_id = R.string.ap_guide_already_open_wifi;
            open_key_id = R.drawable.gif_open_key_800;
            click_wifi_id = R.drawable.gif_click_wifi_800;
        } else if (subdomain.equals(Constants.subdomain_x900)) {
            tip1_id = R.string.ap_guide_aty_tip1_x900;
            tip2_id = R.string.ap_guide_aty_tip2_x900;
            tip3_id = R.string.ap_guide_already_open_wifi;
            open_key_id = R.drawable.gif_open_key;
            click_wifi_id = R.drawable.gif_click_wifi;
        } else if (subdomain.equals(Constants.subdomain_a9s)) {
            tip1_id = R.string.ap_guide_aty_tip1_x900;
            tip2_id = R.string.ap_guide_aty_tip2_a9s;
            tip3_id = R.string.ap_guide_already_open_wifi_a9s;
            open_key_id = R.drawable.gif_open_key_800;
            click_wifi_id = R.drawable.gif_click_wifi_800;
        } else if (subdomain.equals(Constants.subdomain_a8s)) {
            tip1_id = R.string.ap_guide_aty_tip1_x900;
            tip2_id = R.string.ap_guide_aty_tip2_x7;
            tip3_id = R.string.ap_guide_have_heard_didi;
            open_key_id = R.drawable.gif_open_key_a8s;
            click_wifi_id = R.drawable.gif_click_wifi_a8s;
        } else {
            tip1_id = R.string.ap_guide_aty_tip1_x900;
            tip2_id = R.string.ap_guide_aty_tip2_x7;
            tip3_id = R.string.ap_guide_have_heard_didi;
            open_key_id = R.drawable.gif_open_key_787;
            click_wifi_id = R.drawable.gif_click_wifi_787;
        }
        text_tip1.setText(tip1_id);
        text_tip2.setText(tip2_id);
        gif_open_key.setMovieResource(open_key_id);
        gif_click_wifi.setMovieResource(click_wifi_id);
        tv_title.setText(R.string.guide_ap_prepare);
        curStep = 1;
        bt_next.setSelected(false);
        bt_next.setClickable(false);
        rb_next_tip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                bt_next.setSelected(true);
                bt_next.setClickable(true);
            } else {
                bt_next.setSelected(false);
                bt_next.setClickable(false);
            }
        });
    }


    @OnClick({R.id.bt_next})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_next:
                if (curStep == 1) {
                    ll_ap_step1.setVisibility(View.GONE);
                    ll_ap_step2.setVisibility(View.VISIBLE);
                    rb_next_tip.setText(tip3_id);
                    if (Constants.IS_FIRST_AP) {
                        bt_next.setText(R.string.add_aty_start_connect);
                    }
                    tv_guide_tip4.setVisibility(View.VISIBLE);
                    rb_next_tip.setChecked(false);
                    curStep = 2;
                } else {
                    if (Constants.IS_FIRST_AP) {
                        Intent i = new Intent(context, ApWifiActivity.class);
                        startActivity(i);
                    } else {
                        Intent i = new Intent(context, ConnectDeviceApActivity.class);
                        startActivity(i);
                    }
                }
                break;
        }
    }

    @Override
    public void clickBackBtn() {
        if (curStep == 2) {
            curStep = 1;
            ll_ap_step1.setVisibility(View.VISIBLE);
            ll_ap_step2.setVisibility(View.GONE);
            tv_guide_tip4.setVisibility(View.INVISIBLE);
            rb_next_tip.setText(R.string.ap_guide_already_open_device);
            bt_next.setText(R.string.guide_aty_next);
        } else {
            super.clickBackBtn();
        }
    }
}
