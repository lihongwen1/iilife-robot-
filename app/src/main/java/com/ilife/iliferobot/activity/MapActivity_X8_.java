package com.ilife.iliferobot.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.view.View;

import com.ilife.iliferobot.R;
import com.ilife.iliferobot.able.ACSkills;
import com.ilife.iliferobot.able.MsgCodeUtils;
import com.ilife.iliferobot.utils.ToastUtils;

/**
 * Created by chengjiaping on 2018/8/15.
 */

public class MapActivity_X8_ extends BaseMapActivity {
    public static final String INTENT_ACTION = "com.example.MapActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initView() {
        super.initView();
        int rechargeModel = -1;
        switch (mPresenter.getRobotType()) {
            case "a9s":
            case "X800":
                rechargeModel = R.drawable.rechage_device_x800;
                break;
            case "X787":
                rechargeModel = R.drawable.rechage_device_x787;
                break;
            case "X785":
                rechargeModel = R.drawable.rechage_device_x785;
                break;
            case "a8s":
                rechargeModel = R.drawable.rechage_device_a8s;
                break;
        }
        if (rechargeModel != -1) {
            iv_recharge_model.setImageResource(rechargeModel);
        }
        tv_bottom_recharge_x8.setVisibility(View.VISIBLE);
        tv_wall.setVisibility(View.GONE);
        tv_appointment_x9.setVisibility(View.VISIBLE);
        tv_recharge_x9.setVisibility(View.GONE);
    }

    @Override
    public void showRemoteView() {
        if (mPresenter.isWork(mPresenter.getCurStatus()) || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_SLEEPING) {
            ToastUtils.showToast(context, getString(R.string.map_aty_can_not_execute));
        } else if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING_ || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING) {
            ToastUtils.showToast(context, getString(R.string.map_aty_charge));
        } else {
            USE_MODE = USE_MODE_REMOTE_CONTROL;
            mPresenter.sendToDeviceWithOption(ACSkills.get().enterWaitMode());
            showBottomView();
        }
    }

    @Override
    public void updateStartStatue(boolean isSelect, String value) {
        if (isSelect && mPresenter.getCurStatus() == MsgCodeUtils.STATUE_RECHARGE) {
            tv_start.setText(R.string.map_aty_start);
            tv_start.setText(value);
            tv_start.setTextColor(getResources().getColor(R.color.color_33));
            tv_wall.setTextColor(getResources().getColor(R.color.color_33));
            tv_bottom_recharge.setTextColor(getResources().getColor(R.color.color_33));
            tv_bottom_recharge_x8.setTextColor(getResources().getColor(R.color.color_33));
            tv_control_x9.setTextColor(getResources().getColor(R.color.color_33));
            tv_control_x9.setVisibility(View.VISIBLE);
            tv_bottom_recharge.setVisibility(View.GONE);
            fl_bottom_x9.setBackground(new ColorDrawable(getResources().getColor(R.color.bg_color_f5f7fa)));
        } else if (isSelect) {
            tv_start.setText(R.string.map_aty_stop);
            tv_start.setTextColor(getResources().getColor(R.color.white));
            tv_bottom_recharge_x8.setTextColor(getResources().getColor(R.color.white));
            tv_control_x9.setTextColor(getResources().getColor(R.color.white));
            tv_wall.setTextColor(getResources().getColor(R.color.white));
            fl_bottom_x9.setBackground(new ColorDrawable(Color.TRANSPARENT));

        } else {
            tv_start.setText(R.string.map_aty_start);
            tv_start.setText(value);
            tv_start.setTextColor(getResources().getColor(R.color.color_33));
            tv_wall.setTextColor(getResources().getColor(R.color.color_33));
            tv_bottom_recharge.setTextColor(getResources().getColor(R.color.color_33));
            tv_bottom_recharge_x8.setTextColor(getResources().getColor(R.color.color_33));
            tv_control_x9.setTextColor(getResources().getColor(R.color.color_33));
            tv_control_x9.setVisibility(View.VISIBLE);
            tv_bottom_recharge.setVisibility(View.GONE);
            fl_bottom_x9.setBackground(new ColorDrawable(getResources().getColor(R.color.bg_color_f5f7fa)));

        }
        if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_PLANNING) {
            setNavigationBarColor(R.color.color_ff1b92e2);
        } else {
            setNavigationBarColor(R.color.white);
        }
        tv_start.setSelected(isSelect);
        image_center.setSelected(isSelect);//remote control start button
    }
}
