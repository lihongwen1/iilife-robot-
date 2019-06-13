package com.ilife.iliferobot.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.badoo.mobile.util.WeakHandler;
import com.ilife.iliferobot.app.MyApplication;
import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.utils.DeviceUtils;
import com.ilife.iliferobot.view.MapView;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.contract.MapX9Contract;
import com.ilife.iliferobot.presenter.MapX9Presenter;
import com.ilife.iliferobot.utils.ACSkills;
import com.ilife.iliferobot.utils.MsgCodeUtils;
import com.ilife.iliferobot.utils.SpUtils;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public abstract class BaseMapActivity extends BackBaseActivity<MapX9Presenter> implements MapX9Contract.View {
    final String TAG = MapActivity_X9_.class.getSimpleName();
    public static final int VIRTUALWALL_MAXCOUNT = 0x12;
    public static final int SEND_VIRTUALDATA_SUCCESS = 0x15;
    public static final int SEND_VIRTUALDATA_FAILED = 0x16;
    public static final int QUERYVIRTUAL_SUCCESS_SHOWLINE = 0x17;
    public static final int TAG_CONTROL = 0x01;
    public static final int TAG_NORMAL = 0x02;
    public static final int TAG_RECHAGRGE = 0x03;
    public static final int TAG_KEYPOINT = 0x04;
    public static final int TAG_ALONG = 0x05;
    public static final int TAG_LEFT = 0x06;
    public static final int TAG_RIGHT = 0x07;
    public static final int TAG_FORWAD = 0x08;
    Context context;
    @BindView(R.id.rl_top)
    View rl_top;
    @BindView(R.id.rl_status)
    View anchorView;
    @BindView(R.id.relativeLayout)
    RelativeLayout relativeLayout;
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.tv_area)
    TextView tv_area;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.tv_start_x9)
    TextView tv_start;
    @BindView(R.id.tv_status)
    TextView tv_status;
    @BindView(R.id.tv_use_control)
    TextView tv_use_control;
    @BindView(R.id.tv_point_x9)
    TextView tv_point;
    @BindView(R.id.tv_along_x9)
    TextView tv_along;
    @BindView(R.id.tv_appointment_x9)
    TextView tv_appointment_x9;
    @BindView(R.id.image_ele)
    ImageView image_ele;//battery
    @BindView(R.id.tv_control_x9)
    TextView tv_control_x9;
    @BindView(R.id.tv_bottom_recharge_x9)
    TextView tv_bottom_recharge;
    @BindView(R.id.image_top_menu)
    ImageView image_setting;
    @BindView(R.id.image_animation)
    ImageView image_animation;
    Animation animation, animation_alpha;
    @BindView(R.id.layout_recharge)
    View layout_recharge;
    @BindView(R.id.layout_remote_control)
    View layout_remote_control;
    @BindView(R.id.tv_recharge_x9)
    TextView tv_recharge_x9;
    @BindView(R.id.v_map)
    MapView mMapView;
    @BindView(R.id.tv_virtual_wall_x9)
    TextView tv_wall;
    @BindView(R.id.fl_bottom_x9)
    FrameLayout fl_bottom_x9;
    @BindView(R.id.fl_control_x9)
    FrameLayout fl_control_x9;
    @BindView(R.id.fl_virtual_wall)
    FrameLayout fl_virtual_wall;
    @BindView(R.id.image_center)
    ImageView image_center;
    PopupWindow errorPopup;
    AnimationDrawable electricityDrawable;
    @BindView(R.id.tv_cancel_virtual_x9)
    TextView tv_cancle_virtual;
    @BindView(R.id.tv_add_virtual_x9)
    TextView tv_add_virtual;
    @BindView(R.id.tv_delete_virtual_x9)
    TextView tv_delete_virtual;
    @BindView(R.id.tv_ensure_virtual_x9)
    TextView tv_ensure_virtual;
    @BindView(R.id.image_control_back)
    ImageView image_max;
    @BindView(R.id.image_forward)
    ImageView image_forward;
    @BindView(R.id.image_left)
    ImageView image_left;
    @BindView(R.id.image_right)
    ImageView image_right;

    @BindView(R.id.tv_bottom_recharge_x8)
    TextView tv_bottom_recharge_x8;
    @BindView(R.id.iv_recharge_model)
    ImageView iv_recharge_model;
    public static final int USE_MODE_NORMAL = 1;
    public static final int USE_MODE_REMOTE_CONTROL = 2;
    protected int USE_MODE = USE_MODE_NORMAL;
    WeakHandler handler = new WeakHandler(msg -> {
        switch (msg.what) {
            case SEND_VIRTUALDATA_SUCCESS:
                ToastUtils.showToast(context, getString(R.string.map_aty_set_suc));
                break;
            case SEND_VIRTUALDATA_FAILED:
                ToastUtils.showToast(context, getString(R.string.map_aty_set_fail));
                break;
            case QUERYVIRTUAL_SUCCESS_SHOWLINE:
                //TODO 绘制虚拟墙
                break;
            case VIRTUALWALL_MAXCOUNT:
                ToastUtils.showToast(context, context.getString(R.string.map_aty_max_count));
                break;
        }
        return false;
    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        initData();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void attachPresenter() {
        super.attachPresenter();
        mPresenter = new MapX9Presenter();
        mPresenter.attachView(this);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_map_x9;
    }


    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.getAppointmentMsg();
        mPresenter.getDevStatus();
        mPresenter.initPropReceiver();
        mPresenter.registerPropReceiver();
        updateMaxButton(mPresenter.isMaxMode());
    }

    public void initData() {
        context = this;
        animation = AnimationUtils.loadAnimation(context, R.anim.anims_ni);
        animation.setInterpolator(new LinearInterpolator());
        animation_alpha = AnimationUtils.loadAnimation(context, R.anim.anim_alpha);
    }

    public void initView() {
        errorPopup = new PopupWindow();
        electricityDrawable = (AnimationDrawable) image_animation.getBackground();

        image_setting.setVisibility(View.VISIBLE);
        String devName = SpUtils.getSpString(context, MainActivity.KEY_DEVNAME);
        if (devName != null && !devName.isEmpty()) {
            tv_title.setText(devName);
        } else {
            tv_title.setText(getString(R.string.map_aty_title, mPresenter.getRobotType()));
        }

    }

    @Override
    public void sendHandler(int msgCode) {
        handler.sendEmptyMessage(msgCode);
    }


    @Override
    public void updateCleanArea(String value) {
        tv_area.setText(value);
    }

    @Override
    public void updateCleanTime(String value) {
        tv_time.setText(value);
    }

    @Override
    public void updateStatue(String value) {
        tv_status.setText(value);
    }

    @Override
    public void cleanMapView() {
        mMapView.clean();
    }

    @Override
    public void setMapViewVisible(boolean isViesible) {
        mMapView.setVisibility(isViesible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void updateSlam(int xMin, int xMax, int yMin, int yMax) {
        mMapView.updateSlam(xMin, xMax, yMin, yMax, 6);
    }

    @Override
    public void drawVirtualWall(List<int[]> existPointList) {
        mMapView.drawVirtualWall(existPointList);
    }

    @Override
    public void drawRoadMap(ArrayList<Integer> roadList, ArrayList<Integer> historyRoadList) {
        mMapView.drawRoadMap(roadList, historyRoadList);
    }

    @Override
    public void drawObstacle() {
        mMapView.drawObstacle();
    }

    @Override
    public void drawSlamMap(byte[] slamBytes) {
        mMapView.drawSlamMap(slamBytes);
    }

    /**
     * 显示组件异常
     *
     * @param errorCode
     */
    @Override
    public void showErrorPopup(int errorCode) {
        boolean isShow = errorCode != 0;
        if (isShow) {
            if (errorPopup != null && !errorPopup.isShowing()) {
                View contentView = LayoutInflater.from(context).inflate(R.layout.layout_popup_error, null);
                errorPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                errorPopup.setContentView(contentView);
                initErrorPopup(errorCode, contentView);
                errorPopup.setOutsideTouchable(false);
                errorPopup.setFocusable(false);
                errorPopup.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                errorPopup.setHeight((int) getResources().getDimension(R.dimen.dp_60));
                errorPopup.showAsDropDown(rl_top);
            }
        } else {
            if (errorPopup != null && errorPopup.isShowing()) {
                errorPopup.dismiss();
            }
        }


    }

    /**
     * 选择进入虚拟墙编辑模式
     * 显示虚拟墙操作UI
     */
    private void showSetWallDialog() {
        UniversalDialog universalDialog = new UniversalDialog();
        universalDialog.setDialogType(UniversalDialog.TYPE_NORMAL).setTitle(Utils.getString(R.string.map_aty_set_wall)).exchangeButtonColor()
                .setHintTIp(Utils.getString(R.string.map_aty_will_stop)).setOnRightButtonClck(() ->
                mPresenter.enterVirtualMode()).show(getSupportFragmentManager(), "add_wall");
    }

    /**
     * 清除虚拟墙提示dialog
     */
    private void showClearWallDialog() {
        UniversalDialog universalDialog = new UniversalDialog();
        universalDialog.setDialogType(UniversalDialog.TYPE_NORMAL).setTitle(Utils.getString(R.string.map_aty_clear_wall)).
                setHintTIp(Utils.getString(R.string.map_aty_clear_undo)).
                setOnRightButtonClck(() ->
                {
                    mMapView.undoAllOperation();
                    /**
                     * 退出虚拟墙编辑模式，相当于撤销所有操作，虚拟墙数据没有变化，无需发送数据到设备端
                     */
                    mPresenter.sendVirtualWallData(mMapView.getVirtualWallPointfs());
                }).show(getSupportFragmentManager(), "undo_wall");
    }


    /**
     * 下发虚拟墙信息到设备的提示dialog
     */
    private void showSaveWallDialog() {
        UniversalDialog universalDialog = new UniversalDialog();
        universalDialog.setDialogType(UniversalDialog.TYPE_NORMAL_MID_TITLE).setMidTitle(Utils.getString(R.string.map_aty_apply_wall)).exchangeButtonColor().setOnRightButtonClck(() ->
                mPresenter.sendVirtualWallData(mMapView.getVirtualWallPointfs())).show(getSupportFragmentManager(), "save_wall");
    }


    /**
     * 组件异常
     *
     * @param code
     * @param contentView
     */
    private void initErrorPopup(int code, View contentView) {
        ImageView image_delete = contentView.findViewById(R.id.image_delete);
        TextView tv_error = contentView.findViewById(R.id.tv_error);
        tv_error.setText(DeviceUtils.getErrorText(context, code));
        image_delete.setOnClickListener(v -> {
            if (errorPopup != null) {
                errorPopup.dismiss();
            }
        });
    }


    @Override
    public void setCurrentBottom(int bottom) {
        this.USE_MODE = bottom;
    }

    /**
     * 显示开始等操作按钮,包含地图
     */
    public void showBottomView() {
        switch (USE_MODE) {
            case USE_MODE_NORMAL:
                layout_remote_control.setVisibility(View.GONE);
                fl_control_x9.setVisibility(View.GONE);
                fl_virtual_wall.setVisibility(View.GONE);
                fl_bottom_x9.setVisibility(View.VISIBLE);
                setMapViewVisible(true);
                break;
            case USE_MODE_REMOTE_CONTROL:
                fl_virtual_wall.setVisibility(View.GONE);
                fl_bottom_x9.setVisibility(View.GONE);
                setMapViewVisible(false);
                fl_control_x9.setVisibility(View.VISIBLE);
                layout_remote_control.setVisibility(View.VISIBLE);
                image_max.setSelected(mPresenter.isMaxMode());
                updateOperationViewStatue(mPresenter.getCurStatus());
                break;
        }
    }


    /**
     * 设置当前操作提示文字
     *
     * @param tag
     */
    @Override
    public void setTvUseStatus(int tag) {
        tv_use_control.setTextColor(getResources().getColor(R.color.white));
        switch (tag) {
            case TAG_NORMAL:
                tv_use_control.setVisibility(View.GONE);
                break;
            case TAG_CONTROL:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(getString(R.string.map_aty_use_control));
                break;
            case TAG_LEFT:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(getString(R.string.map_aty_use_left));
                break;
            case TAG_FORWAD:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(getString(R.string.map_aty_use_forward));
                break;
            case TAG_RIGHT:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(getString(R.string.map_aty_use_right));
                break;
            case TAG_RECHAGRGE:
                tv_use_control.setTextColor(getResources().getColor(R.color.color_f08300));
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(R.string.map_aty_use_recharging_x9);
                break;
            case TAG_KEYPOINT:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(R.string.map_aty_key_pointing_x9);
                break;
            case TAG_ALONG:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(getString(R.string.map_aty_use_along));
                break;
        }
    }


    @Override
    public void setTvUseStatusVisible(boolean isVisible) {
        tv_use_control.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    /**
     * 清空不常显示的布局,虚拟墙编辑模式，回冲动画，沿边动画，重点动画
     *
     * @param curStatus
     */
    @Override
    public void clearAll(int curStatus) {
        if (curStatus != MsgCodeUtils.STATUE_VIRTUAL_EDIT) {
            mMapView.setMODE(MapView.MODE_NONE);
            mMapView.undoAllOperation();
        }
        if (curStatus != MsgCodeUtils.STATUE_VIRTUAL_EDIT) {
            hideVirtualEdit();
        }
        if (curStatus != MsgCodeUtils.STATUE_RECHARGE) {
            tv_bottom_recharge.setSelected(false);
            tv_bottom_recharge_x8.setSelected(false);
            layout_recharge.setVisibility(View.GONE);
            if (electricityDrawable.isRunning()) {
                electricityDrawable.stop();
            }
        }
        tv_use_control.setVisibility(View.GONE);
    }

    @Override
    public void updateRecharge(boolean isRecharge) {
        if (layout_recharge.getVisibility() == View.VISIBLE && isRecharge) {//避免重复刷新UI导致异常
            return;
        }
        cleanMapView();
        layout_recharge.setVisibility(View.VISIBLE);
        tv_bottom_recharge.setSelected(isRecharge);
        tv_bottom_recharge_x8.setSelected(isRecharge);
        electricityDrawable.start();
    }

    @Override
    /**
     * 设置电池图标
     */
    public void setBatteryImage(int curStatus, int batteryNo) {
        if (curStatus == 0x09 || curStatus == 0x0b) {
            if (batteryNo <= 6) {
                image_ele.setImageResource(R.drawable.map_aty_battery1_ing);   //红色
            } else if (batteryNo < 35) {
                image_ele.setImageResource(R.drawable.map_aty_battery2_ing);   //一格
            } else if (batteryNo < 75) {
                image_ele.setImageResource(R.drawable.map_aty_battery3_ing);   //两格
            } else {
                image_ele.setImageResource(R.drawable.map_aty_battery4_ing);   //满格
            }
        } else {
            if (batteryNo <= 6) {
                image_ele.setImageResource(R.drawable.map_aty_battery1);   //红色
            } else if (batteryNo < 35) {
                image_ele.setImageResource(R.drawable.map_aty_battery2);   //一格
            } else if (batteryNo < 75) {
                image_ele.setImageResource(R.drawable.map_aty_battery4);   //两格
            } else {
                image_ele.setImageResource(R.drawable.map_aty_battery3);   //满格
            }
        }

    }

    @OnClick({R.id.image_center, R.id.tv_start_x9, R.id.tv_control_x9, R.id.image_top_menu, R.id.tv_recharge_x9, R.id.tv_along_x9,
            R.id.tv_point_x9, R.id.tv_virtual_wall_x9, R.id.tv_cancel_virtual_x9, R.id.tv_ensure_virtual_x9
            , R.id.tv_add_virtual_x9, R.id.tv_delete_virtual_x9, R.id.iv_control_close_x9, R.id.tv_bottom_recharge_x9, R.id.tv_bottom_recharge_x8, R.id.image_right, R.id.image_left, R.id.image_control_back, R.id.image_forward
            , R.id.tv_appointment_x9
    })
    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.image_center:
                image_center.setSelected(image_center.isSelected());
            case R.id.tv_start_x9: //done
                if (mPresenter.isWork(mPresenter.getCurStatus())) {
                    mPresenter.sendToDeviceWithOption(ACSkills.get().enterWaitMode());
                } else if (mPresenter.isRandomMode()){
                    mPresenter.sendToDeviceWithOption(ACSkills.get().enterRandomMode());
                }else {
                    mPresenter.sendToDeviceWithOption(ACSkills.get().enterPlanningMode());
                }
                break;
            case R.id.tv_bottom_recharge_x8:
            case R.id.tv_bottom_recharge_x9://会跳转到遥控界面
                mPresenter.enterRechargeMode();
                break;
            case R.id.tv_control_x9://显示沿边，遥控等操作UI
                if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING_) {
                    ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_charge));
                } else {
                    showRemoteView();
                }
                break;
            case R.id.iv_control_close_x9:
                USE_MODE = USE_MODE_NORMAL;
                mPresenter.sendToDeviceWithOption(ACSkills.get().enterWaitMode());
                break;
            case R.id.image_top_menu:
                Intent i = new Intent(context, SettingActivity.class);
                startActivity(i);
                break;
            case R.id.tv_recharge_x9://回冲
                mPresenter.enterRechargeMode();
                break;
            case R.id.tv_along_x9:  //done
                mPresenter.enterAlongMode();
                break;
            case R.id.tv_point_x9:  //done
                mPresenter.enterPointMode();
                break;
            case R.id.tv_appointment_x9://预约
                Intent intent = new Intent(context, ClockingActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_virtual_wall_x9://虚拟墙编辑模式
                if (mPresenter.canEdit(mPresenter.getCurStatus()) && mPresenter.getCurStatus() == MsgCodeUtils.STATUE_PLANNING) {
                    showSetWallDialog();
                } else {
                    if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING_) {
                        ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_charge));
                    } else {
                        ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_can_not_execute));
                    }
                }
                break;
            case R.id.tv_add_virtual_x9://增加虚拟墙模式
                if (mMapView.isInMode(MapView.MODE_ADD_VIRTUAL)) {
                    tv_add_virtual.setSelected(false);
                    mMapView.setMODE(MapView.MODE_NONE);
                } else {
//                    showAddWallDialog();
                    tv_add_virtual.setSelected(true);
                    tv_delete_virtual.setSelected(false);
                    mMapView.setMODE(MapView.MODE_ADD_VIRTUAL);
                }
                break;
            case R.id.tv_delete_virtual_x9://删除虚拟墙模式
                if (mMapView.isInMode(MapView.MODE_DELETE_VIRTUAL)) {
                    tv_delete_virtual.setSelected(false);
                    mMapView.setMODE(MapView.MODE_NONE);
                } else {
//                    showDeleteWallDialog();
                    tv_delete_virtual.setSelected(true);
                    tv_add_virtual.setSelected(false);
                    mMapView.setMODE(MapView.MODE_DELETE_VIRTUAL);
                }
                break;
            case R.id.tv_ensure_virtual_x9://保存虚拟墙模式
                mMapView.setMODE(MapView.MODE_NONE);
                showSaveWallDialog();
                break;
            case R.id.tv_cancel_virtual_x9:// TODO 取消的
                mMapView.setMODE(MapView.MODE_NONE);
                showClearWallDialog();
                break;
            /* 遥控器方向键*/
            case R.id.image_left:
                mPresenter.sendToDeviceWithOption(ACSkills.get().turnLeft());
                break;
            case R.id.image_right:
                mPresenter.sendToDeviceWithOption(ACSkills.get().turnRight());
                break;
            case R.id.image_forward:
                mPresenter.sendToDeviceWithOption(ACSkills.get().turnForward());
                break;
            case R.id.image_control_back://max吸力
                mPresenter.reverseMaxMode();
                break;

        }
    }


    @Override
    public void updateMaxButton(boolean isMaXMode) {
        if (layout_remote_control.getVisibility() == View.VISIBLE) {
            image_max.setSelected(isMaXMode);
        }
    }

    @Override
    public void updateAlong(boolean isAlong) {
        layout_remote_control.setVisibility(View.GONE);
    }

    @Override
    public void updatePoint(boolean isPoint) {
        layout_remote_control.setVisibility(View.GONE);
    }

    private void updateDirectionUi(int selectId) {
        image_forward.setSelected(selectId == R.id.image_forward);
        image_max.setSelected(selectId == R.id.image_control_back);
        image_right.setSelected(selectId == R.id.image_right);
        image_left.setSelected(selectId == R.id.image_left);
    }

    @Override
    public void showVirtualEdit() {
        tv_add_virtual.setSelected(true);
        tv_delete_virtual.setSelected(false);
        mMapView.setMODE(MapView.MODE_ADD_VIRTUAL);
        fl_virtual_wall.setVisibility(View.VISIBLE);
        fl_control_x9.setVisibility(View.GONE);
        fl_bottom_x9.setVisibility(View.GONE);
    }

    @Override
    public void hideVirtualEdit() {
        fl_virtual_wall.setVisibility(View.GONE);
        tv_add_virtual.setSelected(false);
        tv_delete_virtual.setSelected(false);
    }


    @Override
    public void updateOperationViewStatue(int surStatues) {
        tv_point.setSelected(surStatues == 0x05);
        tv_along.setSelected(surStatues == 0x04);
        tv_recharge_x9.setSelected(surStatues == 0x08);
    }


    @Override
    public void drawBoxMapX8(ArrayList<Integer> pointList) {
        mMapView.drawBoxMapX8(pointList);
    }
}
