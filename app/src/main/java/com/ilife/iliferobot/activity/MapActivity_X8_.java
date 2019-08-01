package com.ilife.iliferobot.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.view.View;

import com.ilife.iliferobot.R;
import com.ilife.iliferobot.able.ACSkills;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.able.MsgCodeUtils;
import com.ilife.iliferobot.utils.ToastUtils;

/**
 * Created by chengjiaping on 2018/8/15.
 */

public class MapActivity_X8_ extends BaseMapActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initView() {
        super.initView();
        int rechargeModel = -1;
        switch (mPresenter.getRobotType()) {
            case Constants.A9:
            case Constants.A9s:
            case Constants.X800:
                rechargeModel = R.drawable.rechage_device_x800;
                break;
            case Constants.X787:
                rechargeModel = R.drawable.rechage_device_x787;
                break;
            case Constants.X785:
                rechargeModel = R.drawable.rechage_device_x785;
                break;
            case Constants.A8s:
                ll_map_container.setBackground(getResources().getDrawable(R.drawable.shape_gradient_map_bg_mokka));
                rechargeModel = R.drawable.rechage_device_a8s;
                break;
            case Constants.V85:
                rechargeModel = R.drawable.rechage_device_v85;
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
            ToastUtils.showToast(this, getString(R.string.map_aty_can_not_execute));
        } else if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING_ || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING) {
            ToastUtils.showToast(this, getString(R.string.map_aty_charge));
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
            if (mPresenter.getRobotType().equals(Constants.A8s)) {
                setNavigationBarColor(R.color.moka_color);
            } else {
                setNavigationBarColor(R.color.color_ff1b92e2);
            }
        } else {
            setNavigationBarColor(R.color.white);
        }
        tv_start.setSelected(isSelect);
        image_center.setSelected(isSelect);//remote control start button
    }

    @Override
    public void updateRecharge(boolean isRecharge) {
        setTvUseStatus(BaseMapActivity.TAG_RECHAGRGE);
        if (layout_recharge.getVisibility() == View.VISIBLE && isRecharge) {//避免重复刷新UI导致异常
            return;
        }
        layout_remote_control.setVisibility(View.GONE);
        tv_bottom_recharge.setSelected(isRecharge);
        tv_bottom_recharge_x8.setSelected(isRecharge);
        layout_recharge.setVisibility(View.VISIBLE);
        electricityDrawable.start();
    }

}
