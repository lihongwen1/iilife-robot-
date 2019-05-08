package com.ilife.iliferobot_cn.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
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

import androidx.appcompat.app.AlertDialog;

import com.accloud.service.ACDeviceMsg;
import com.badoo.mobile.util.WeakHandler;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.app.MyApplication;
import com.ilife.iliferobot_cn.base.BackBaseActivity;
import com.ilife.iliferobot_cn.contract.MapX9Contract;
import com.ilife.iliferobot_cn.fragment.CancleDialogFragment;
import com.ilife.iliferobot_cn.presenter.MapX9Presenter;
import com.ilife.iliferobot_cn.ui.ControlPopupWindow;
import com.ilife.iliferobot_cn.utils.AlertDialogUtils;
import com.ilife.iliferobot_cn.utils.DeviceUtils;
import com.ilife.iliferobot_cn.utils.DialogUtils;
import com.ilife.iliferobot_cn.utils.MsgCodeUtils;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.ToastUtils;
import com.ilife.iliferobot_cn.utils.Utils;
import com.ilife.iliferobot_cn.view.MapView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by chengjiaping on 2018/8/15.
 */

public class MapActivity_X9_ extends BackBaseActivity<MapX9Presenter> implements View.OnClickListener, MapX9Contract.View {
    final String TAG = MapActivity_X9_.class.getSimpleName();
    public static final int VIRTUALWALL_MAXCOUNT = 0x12;
    public static final int SEND_VIRTUALDATA_SUCCESS = 0x15;
    public static final int SEND_VIRTUALDATA_FAILED = 0x16;
    public static final int QUERYVIRTUAL_SUCCESS_SHOWLINE = 0x17;
    public static final int START_AUTO_SCALE = 0x18;
    final int TAG_CONTROL = 0x01;
    final int TAG_NORMAL = 0x02;
    final int TAG_RECHAGRGE = 0x03;
    final int TAG_KEYPOINT = 0x04;
    final int TAG_ALONG = 0x05;
    static final int SEND_VIR = 1;//add virtual
    static final int EXIT_VIR = 2;// delete virtual
    Context context;
    boolean hasStart, hasStart_;
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
    @BindView(R.id.image_key_point)
    ImageView image_key_point;
    @BindView(R.id.image_edge)
    ImageView image_edge;
    @BindView(R.id.image_quan)
    ImageView image_quan;
    Animation animation, animation_alpha;
    @BindView(R.id.layout_recharge)
    View layout_recharge;
    @BindView(R.id.layout_key_point)
    View layout_key_point;
    @BindView(R.id.layout_along)
    View layout_along;
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
    ACDeviceMsg mAcDevMsg;
    PopupWindow errorPopup;
    ControlPopupWindow controlPopup;
    AnimationDrawable electricityDrawable;
    AlertDialog alertDialog;
    boolean isAutoScale = true;
    int index, length;
    ArrayList<Rect> rectLists = new ArrayList<>();
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
    private CancleDialogFragment dialogFragment;
    private Timer timer;
    private TimerTask timerTask;
    private int curentBottom = 1;//1-virtual wall and so on 2-along appoint and so on
    private boolean isTimerTaskRun;//标记发送方向指令的timer是否在运行
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
            case START_AUTO_SCALE:
                isAutoScale = true;
                break;
        }
        return false;
    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        initData();
        super.onCreate(savedInstanceState);
        mPresenter.queryVirtualWall();
        mPresenter.initTimer();
        mPresenter.getHistoryRoad();
        mPresenter.subscribeRealTimeMap();
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
        mAcDevMsg = new ACDeviceMsg();
        animation = AnimationUtils.loadAnimation(context, R.anim.anims_ni);
        animation.setInterpolator(new LinearInterpolator());
        animation_alpha = AnimationUtils.loadAnimation(context, R.anim.anim_alpha);
    }

    public void initView() {
        errorPopup = new PopupWindow();
        image_ele.setImageResource(R.drawable.map_aty_battery4_ing);
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
    public void updateQuanAnimation(boolean isStart) {
        if (isStart) {
            image_quan.startAnimation(animation);
        } else {
            image_quan.clearAnimation();
        }
    }

    @Override
    public void updateAlongAnimation(boolean isStart) {
        if (isStart) {
            image_edge.startAnimation(animation_alpha);
        } else {
            image_edge.clearAnimation();
        }
    }

    @Override
    public void setPointViewVisible(boolean isVisible) {
        layout_key_point.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setAlongViewVisible(boolean isVisible) {

        layout_along.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void updateStartStatue(boolean isSelect, String value) {
        if (isSelect) {
            tv_control_x9.setVisibility(View.GONE);
            tv_start.setText(R.string.map_aty_stop);
            tv_start.setTextColor(getResources().getColor(R.color.white));
            tv_wall.setTextColor(getResources().getColor(R.color.white));
            tv_bottom_recharge.setTextColor(getResources().getColor(R.color.white));
            tv_bottom_recharge.setVisibility(View.VISIBLE);
            fl_bottom_x9.setBackground(new ColorDrawable(Color.TRANSPARENT));
        } else {
            tv_start.setText(R.string.map_aty_start);
            tv_start.setText(value);
            tv_start.setTextColor(getResources().getColor(R.color.color_33));
            tv_wall.setTextColor(getResources().getColor(R.color.color_33));
            tv_bottom_recharge.setTextColor(getResources().getColor(R.color.color_33));
            tv_control_x9.setVisibility(View.VISIBLE);
            tv_bottom_recharge.setVisibility(View.GONE);
            fl_bottom_x9.setBackground(new ColorDrawable(getResources().getColor(R.color.bg_color_f5f7fa)));
        }
        tv_start.setSelected(isSelect);
        image_center.setSelected(isSelect);//remote control start button
    }

    @Override
    public void updateTvVirtualStatue(boolean isSelect) {
        tv_wall.setSelected(isSelect);
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
        mMapView.updateSlam(xMin, xMax, yMin, yMax);
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
                errorPopup.showAsDropDown(anchorView);
            }
        } else {
            if (errorPopup != null && !errorPopup.isShowing()) {
                errorPopup.dismiss();
            }
        }


    }

    /**
     * 选择进入虚拟墙编辑模式
     * 显示虚拟墙操作UI
     */
    private void showSetWallDialog() {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_set_wall_dialog, null);
        int width = (int) getResources().getDimension(R.dimen.dp_300);
        int height = (int) getResources().getDimension(R.dimen.dp_140);
        alertDialog = AlertDialogUtils.showDialog(context, v, width, height);
        v.findViewById(R.id.tv_confirm).setOnClickListener(view -> {
            DialogUtils.hideDialog(alertDialog);
            mPresenter.enterVirtualMode();
        });
        v.findViewById(R.id.tv_cancel).setOnClickListener(view -> DialogUtils.hideDialog(alertDialog));
    }

    /**
     * 清楚虚拟墙提示dialog
     */
    private void showClearWallDialog() {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_clear_wall_dialog, null);
        int width = (int) getResources().getDimension(R.dimen.dp_300);
        int height = (int) getResources().getDimension(R.dimen.dp_140);
        alertDialog = AlertDialogUtils.showDialog(context, v, width, height);
        v.findViewById(R.id.tv_confirm).setOnClickListener(view -> {
            DialogUtils.hideDialog(alertDialog);
            mMapView.undoAllOperation();
            /**
             * 退出虚拟墙编辑模式，相当于撤销所有操作，虚拟墙数据没有变化，无需发送数据到设备端
             */
            mPresenter.sendVirtualWallData(mMapView.getVirtualWallPointfs(), EXIT_VIR);
        });
        v.findViewById(R.id.tv_cancel).setOnClickListener(view -> DialogUtils.hideDialog(alertDialog));
    }

    /**
     * 进入添加虚拟墙模式提示dialog
     */
    private void showAddWallDialog() {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_add_wall_dialog, null);
        int width = (int) getResources().getDimension(R.dimen.dp_300);
        int height = (int) getResources().getDimension(R.dimen.dp_140);
        alertDialog = AlertDialogUtils.showDialog(context, v, width, height);
        v.findViewById(R.id.tv_confirm).setOnClickListener(view -> {
            DialogUtils.hideDialog(alertDialog);
            tv_add_virtual.setSelected(true);
            tv_delete_virtual.setSelected(false);
            mMapView.setMODE(MapView.MODE_ADD_VIRTUAL);
            ;
        });
        v.findViewById(R.id.tv_cancel).setOnClickListener(view -> DialogUtils.hideDialog(alertDialog));
    }

    /**
     * 删除虚拟墙提示dailog
     */
    private void showDeleteWallDialog() {//删除虚拟墙
        View v = LayoutInflater.from(context).inflate(R.layout.layout_delete_wall_dialog, null);
        int width = (int) getResources().getDimension(R.dimen.dp_300);
        int height = (int) getResources().getDimension(R.dimen.dp_140);
        alertDialog = AlertDialogUtils.showDialog(context, v, width, height);
        v.findViewById(R.id.tv_confirm).setOnClickListener(view -> {
            DialogUtils.hideDialog(alertDialog);
            tv_delete_virtual.setSelected(true);
            tv_add_virtual.setSelected(false);
            mMapView.setMODE(MapView.MODE_DELETE_VIRTUAL);
        });
        v.findViewById(R.id.tv_cancel).setOnClickListener(view -> DialogUtils.hideDialog(alertDialog));
    }


    /**
     * 下发虚拟墙信息到设备的提示dialog
     */
    private void showSaveWallDialog() {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_save_wall_dialog, null);
        int width = (int) getResources().getDimension(R.dimen.dp_300);
        int height = (int) getResources().getDimension(R.dimen.dp_140);
        alertDialog = AlertDialogUtils.showDialog(context, v, width, height);
        v.findViewById(R.id.tv_cancel).setOnClickListener(view -> DialogUtils.hideDialog(alertDialog));
        v.findViewById(R.id.tv_confirm).setOnClickListener(view -> {
            DialogUtils.hideDialog(alertDialog);
            mPresenter.sendVirtualWallData(mMapView.getVirtualWallPointfs(), SEND_VIR);
        });
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

    /**
     * 显示沿边重点,遥控等操作
     */
    public void showOperationView() {
        showRemoteControl();
        fl_bottom_x9.setVisibility(View.GONE);
        fl_control_x9.setVisibility(View.VISIBLE);
        updateOperationViewStatue(mPresenter.getCurStatus());
    }

    /**
     * 显示回冲，结束清扫等功能
     */
    @Override
    public void onBottomCancelClick() {
        if (dialogFragment == null) {
            dialogFragment = new CancleDialogFragment();
        }
        dialogFragment.show(getSupportFragmentManager(), "cancel");
    }

    @Override
    public void setCurrenrtBottom(int bottom) {
        this.curentBottom=bottom;
    }

    /**
     * 显示开始等操作按钮
     */
    public void showBottomView() {
        if (curentBottom == 1) {//需判断如果是从回冲状态到暂停状态，不需要显示实时地图。也就是开始回冲后，直接清空地图，不再接收绘制地图的数据
            layout_remote_control.setVisibility(View.GONE);
            showMap();
            fl_bottom_x9.setVisibility(View.VISIBLE);
            fl_control_x9.setVisibility(View.GONE);
            fl_virtual_wall.setVisibility(View.GONE);
        } else if (curentBottom == 2) {
            setMapViewVisible(false);
            showOperationView();
        }
    }


    /**
     * 显示遥控器控制UI，点击单次执行任务，长按每隔400ms执行一次任务
     */
    @Override
    public void showRemoteControl() {
        layout_remote_control.setVisibility(View.VISIBLE);
        image_max.setSelected(mPresenter.isMaxMode());
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


    /**
     * 清空所有布局
     *
     * @param curStatus
     */
    @Override
    public void clearAll(int curStatus) {
        if (curStatus != 0x5) {
            image_quan.clearAnimation();
            hasStart = false;
        }
        if (curStatus != 0x04) {
            image_edge.clearAnimation();
            hasStart_ = false;
        }
        if (curStatus != 0x08 && electricityDrawable.isRunning()) {
            electricityDrawable.stop();
        }
        tv_bottom_recharge.setSelected(false);
        hideVirtualEdit();
        layout_remote_control.setVisibility(View.GONE);
        layout_recharge.setVisibility(View.GONE);
        layout_key_point.setVisibility(View.GONE);
        layout_along.setVisibility(View.GONE);
        mMapView.setVisibility(View.INVISIBLE);
        tv_use_control.setVisibility(View.GONE);
    }

    @Override
    public void updateRecharge(boolean isRecharge) {
        if (layout_recharge.getVisibility()==View.VISIBLE&& isRecharge){//避免重复刷新UI导致异常
            return;
        }
        cleanMapView();
        showOperationView();
        layout_recharge.setVisibility(View.VISIBLE);
        tv_recharge_x9.setSelected(isRecharge);
        tv_bottom_recharge.setSelected(isRecharge);
        tv_point.setSelected(false);
        tv_along.setSelected(false);
        electricityDrawable.start();
        fl_bottom_x9.setBackground(new ColorDrawable(getResources().getColor(R.color.bg_color_f5f7fa)));
        setTvUseStatus(TAG_RECHAGRGE);
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
            } else if (batteryNo >= 75) {
                image_ele.setImageResource(R.drawable.map_aty_battery4_ing);   //满格
            }
        } else {
            if (batteryNo <= 6) {
                image_ele.setImageResource(R.drawable.map_aty_battery1);   //红色
            } else if (batteryNo < 35) {
                image_ele.setImageResource(R.drawable.map_aty_battery2);   //一格
            } else if (batteryNo < 75) {
                image_ele.setImageResource(R.drawable.map_aty_battery3);   //两格
            } else if (batteryNo >= 75) {
                image_ele.setImageResource(R.drawable.map_aty_battery4);   //满格
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back_recharge:
                mPresenter.enterRechargeMode();
                dialogFragment.dismiss();
                break;
            case R.id.tv_cancel_dialog:
                dialogFragment.dismiss();
                break;
            case R.id.tv_finish_cleaning://发送待机模式
                mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
                mAcDevMsg.setContent(new byte[]{0x02});//待机模式
                mPresenter.sendToDeviceWithOption_start(mAcDevMsg);
                dialogFragment.dismiss();
                break;
        }
    }

    @OnClick({R.id.image_center, R.id.tv_start_x9, R.id.tv_control_x9, R.id.image_top_menu, R.id.tv_recharge_x9, R.id.tv_along_x9,
            R.id.tv_point_x9, R.id.tv_virtual_wall_x9, R.id.tv_cancel_virtual_x9, R.id.tv_ensure_virtual_x9
            , R.id.tv_add_virtual_x9, R.id.tv_delete_virtual_x9, R.id.iv_control_close_x9, R.id.tv_bottom_recharge_x9, R.id.image_right, R.id.image_left, R.id.image_control_back, R.id.image_forward
    })
    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.image_center:
                image_center.setSelected(image_center.isSelected());
            case R.id.tv_start_x9: //done
                mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
                if (mPresenter.isWork(mPresenter.getCurStatus())) {
                    mAcDevMsg.setContent(new byte[]{0x02});//待机模式
                } else {
                    mAcDevMsg.setContent(new byte[]{0x06});
                }
                mPresenter.sendToDeviceWithOption_start(mAcDevMsg);
                break;
            case R.id.tv_bottom_recharge_x9:
                curentBottom = 2;
                mPresenter.enterRechargeMode();
                break;
            case R.id.tv_control_x9://显示沿边，遥控等操作UI
                if (mPresenter.isWork(mPresenter.getCurStatus()) || mPresenter.getCurStatus() == 0x01) {
                    ToastUtils.showToast(context, getString(R.string.map_aty_can_not_execute));
                } else if (mPresenter.getCurStatus() == 0x0B || mPresenter.getCurStatus() == 0x09) {
                    ToastUtils.showToast(context, getString(R.string.map_aty_charge));
                } else {

                    showOperationView();
                    curentBottom = 2;
                }
                break;
            case R.id.iv_control_close_x9:
                curentBottom = 1;
                showBottomView();
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
            case R.id.tv_virtual_wall_x9://虚拟墙编辑模式
                ToastUtils.showToast("虚拟墙");
                if (mPresenter.canEdit(mPresenter.getCurStatus()) && mPresenter.getCurStatus() == 0x06) {
                    showSetWallDialog();
                } else {
                    ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_can_not_execute));
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
                mAcDevMsg.setCode(MsgCodeUtils.Proceed);
                mAcDevMsg.setContent(new byte[]{0x03});
                mPresenter.sendToDeviceWithOption(mAcDevMsg);
                break;
            case R.id.image_right:
                mAcDevMsg.setCode(MsgCodeUtils.Proceed);
                mAcDevMsg.setContent(new byte[]{0x04});
                mPresenter.sendToDeviceWithOption(mAcDevMsg);
                break;
            case R.id.image_forward:
                mAcDevMsg.setCode(MsgCodeUtils.Proceed);
                mAcDevMsg.setContent(new byte[]{0x01});
                mPresenter.sendToDeviceWithOption(mAcDevMsg);
                break;
            case R.id.image_control_back://max吸力
                mPresenter.reverseMaxMode(mAcDevMsg);
                break;

        }
    }

//    @OnTouch({R.id.image_right, R.id.image_left, R.id.image_control_back, R.id.image_forward})
//    public void onTouch(View v, MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN: //手指按下
//                distributeDirectOrder(v.getId());
//                updateDirectionUi(v.getId());
//                break;
//            case MotionEvent.ACTION_MOVE: //手指移动（从手指按下到抬起 move多次执行）
//                break;
//            case MotionEvent.ACTION_UP: //手指抬起
//                updateDirectionUi(-1);
//                if (timerTask != null) {
//                    timerTask.cancel();
//                    isTimerTaskRun = false;
//                }
//                if (v.getId() == R.id.image_control_back) {//没有返回功能，做max吸力功能
//                    mPresenter.reverseMaxMode(mAcDevMsg);
//                } else {
//                    mAcDevMsg.setCode(MsgCodeUtils.Proceed);
//                    mAcDevMsg.setContent(new byte[]{0x05});
//                    mPresenter.sendToDeviceWithOption(mAcDevMsg);
//                }
//                break;
//        }
//    }

    @Override
    public void updateMaxButton(boolean isMaXMode) {
        if (layout_remote_control.getVisibility() == View.VISIBLE) {
            image_max.setSelected(isMaXMode);
        }
    }

    @Override
    public void updateAlong(boolean isAlong) {
        layout_remote_control.setVisibility(View.GONE);
        setMapViewVisible(true);
        tv_along.setSelected(isAlong);
        tv_point.setSelected(false);
        tv_recharge_x9.setSelected(false);
    }

    @Override
    public void updatePoint(boolean isPoint) {
        setMapViewVisible(true);
        layout_remote_control.setVisibility(View.GONE);
        tv_point.setSelected(isPoint);
        tv_along.setSelected(false);
        tv_recharge_x9.setSelected(false);
    }

    private void updateDirectionUi(int selectId) {
        image_forward.setSelected(selectId == R.id.image_forward);
        image_max.setSelected(selectId == R.id.image_control_back);
        image_right.setSelected(selectId == R.id.image_right);
        image_left.setSelected(selectId == R.id.image_left);
    }

    //TODO 不能连续发送方向指令
    @Override
    public void distributeDirectOrder(int directId) {
        if (directId == R.id.image_control_back) {//max吸力功能不需要持续发送指令
            return;
        }
        if (timer == null) {
            timer = new Timer();
            timer.schedule(timerTask = new TimerTask() {
                @Override
                public void run() {
                    Log.d(TAG, "send direction order");
                    if (isTimerTaskRun) {
                        mPresenter.sendToDeviceWithOption(mAcDevMsg);
                    }
                }
            }, 0, 4000);
        }
        mAcDevMsg.setCode(MsgCodeUtils.Proceed);
        switch (directId) {
            case R.id.image_forward:
                mAcDevMsg.setContent(new byte[]{0x01});
                break;
            case R.id.image_left:
                mAcDevMsg.setContent(new byte[]{0x03});
                break;
            case R.id.image_right:
                mAcDevMsg.setContent(new byte[]{0x04});
                break;
        }
        if (!isTimerTaskRun) {
            timerTask.run();
            isTimerTaskRun = true;
        }
    }

    /**
     * 根据触摸点删除虚拟墙
     *
     * @param downX
     * @param downY
     */
    private void doDeleteVirtual(float downX, float downY) {//删除虚拟墙
        if (rectLists != null && rectLists.size() > 0) {
            for (int i = 0; i < rectLists.size(); i++) {
                Rect rect = rectLists.get(i);
                if (rect.contains((int) (downX), (int) (downY))) {
                    rectLists.remove(i);
//                    wallPointList.remove(i);
                }
            }
        }
    }


    @Override
    public void setVirtualWallStatus(boolean enable) {
        tv_wall.setSelected(enable);
        tv_wall.setClickable(enable);
    }

    @Override
    public void showVirtualEdit() {
        fl_virtual_wall.setVisibility(View.VISIBLE);
        fl_control_x9.setVisibility(View.GONE);
        fl_bottom_x9.setVisibility(View.GONE);
        tv_add_virtual.setSelected(false);
        tv_delete_virtual.setSelected(false);
    }

    @Override
    public void hideVirtualEdit() {
        fl_virtual_wall.setVisibility(View.GONE);
        tv_add_virtual.setSelected(false);
        tv_delete_virtual.setSelected(false);
    }


    /**
     * 回复常规画图界面
     */
    @Override
    public void showMap() {
        setMapViewVisible(true);
        image_ele.bringToFront();
        setTvUseStatus(TAG_NORMAL);
    }

    @Override
    public void updateOperationViewStatue(int surStatues) {
        tv_point.setSelected(surStatues == 0x05);
        tv_along.setSelected(surStatues == 0x04);
        tv_recharge_x9.setSelected(surStatues == 0x08);
    }
}
