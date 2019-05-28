package com.ilife.iliferobot_cn.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.utils.MsgCodeUtils;
import com.ilife.iliferobot_cn.utils.ToastUtils;

/**
 * Created by chengjiaping on 2018/8/15.
 */

public class MapActivity_X9_ extends BaseMapActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter.queryVirtualWall();
        mPresenter.initTimer();
        mPresenter.getHistoryRoad();
        mPresenter.subscribeRealTimeMap();
    }

    @Override
    public void initView() {
        super.initView();
        iv_recharge_model.setImageResource(R.drawable.rechage_device_x900);
    }

    @Override
    public void showRemoteView() {
        if (mPresenter.isWork(mPresenter.getCurStatus()) || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_SLEEPING) {
            ToastUtils.showToast(context, getString(R.string.map_aty_can_not_execute));
        } else if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING_|| mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING) {
            ToastUtils.showToast(context, getString(R.string.map_aty_charge));
        } else {
            USE_MODE = USE_MODE_REMOTE_CONTROL;
            showBottomView();
        }
    }

    @Override
    public void updateStartStatue(boolean isSelect, String value) {
        if (isSelect&&mPresenter.getCurStatus()==MsgCodeUtils.STATUE_RECHARGE){
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
            setNavigationBarColor(R.color.white);
        }else if (isSelect) {
            tv_control_x9.setVisibility(View.GONE);
            tv_bottom_recharge.setTextColor(getResources().getColor(R.color.white));
            tv_bottom_recharge.setVisibility(View.VISIBLE);
            tv_start.setText(R.string.map_aty_stop);
            tv_start.setTextColor(getResources().getColor(R.color.white));
            tv_bottom_recharge_x8.setTextColor(getResources().getColor(R.color.white));
            tv_control_x9.setTextColor(getResources().getColor(R.color.white));
            tv_wall.setTextColor(getResources().getColor(R.color.white));
            fl_bottom_x9.setBackground(new ColorDrawable(Color.TRANSPARENT));
            setNavigationBarColor(R.color.color_ff1b92e2);
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
            setNavigationBarColor(R.color.white);
        }
        tv_start.setSelected(isSelect);
        image_center.setSelected(isSelect);//remote control start button
    }
}
