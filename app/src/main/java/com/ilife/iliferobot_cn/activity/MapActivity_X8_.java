package com.ilife.iliferobot_cn.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACDeviceDataMgr;
import com.accloud.service.ACDeviceMsg;
import com.accloud.service.ACException;
import com.google.gson.Gson;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.entity.PropertyInfo;
import com.ilife.iliferobot_cn.ui.CanvasView;
import com.ilife.iliferobot_cn.ui.ControlPopupWindow;
import com.ilife.iliferobot_cn.utils.AlertDialogUtils;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.DeviceUtils;
import com.ilife.iliferobot_cn.utils.MsgCodeUtils;
import com.ilife.iliferobot_cn.utils.MyLog;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.TimeUtil;
import com.ilife.iliferobot_cn.utils.ToastUtils;

import java.util.ArrayList;

/**
 * Created by chengjiaping on 2018/8/15.
 */

public class MapActivity_X8_ extends BaseActivity implements View.OnClickListener {
    final String TAG = MapActivity_X8_.class.getSimpleName();
    public static final String INTENT_ACTION = "com.example.MapActivity";
    public static final String KEY_IS_MAX = "isMaxMode";
    public static final String KEY_MOP_FORCE = "mopForce";
    public static final String KEY_VOICE_OPEN = "voiceOpen";
    final int TAG_CONTROL = 0x01;
    final int TAG_NORMAL = 0x02;
    final int TAG_RECHAGRGE = 0x03;
    final int TAG_KEYPOINT = 0x04;
    final int TAG_ALONG = 0x05;
    Gson gson;
    Context context;
    View anchorView;
    CanvasView canvasView;
    RelativeLayout relativeLayout;
    RelativeLayout.LayoutParams params;
    long deviceId;
    int mopForce;
    byte sendByte;
    boolean isWork, hasAppoint, isMaxMode, hasStart, registerTag, hasStart_, voiceOpen, isX800;
    String physicalId, subdomain, robotType;
    int curStatus, errorCode, batteryNo, workTime, cleanArea;
    TextView tv_time, tv_area, tv_title, tv_start, tv_status, tv_use_control, tv_point, tv_recharge, tv_along, tv_clock;
    ImageView image_ele, image_back, image_start,
            image_control, image_setting, image_function, image_animation,
            image_clock, image_point, image_recharge, image_key_point, image_along, image_edge;
    Animation animation, animation_alpha;
    ImageView image_quan;
    View layout_recharge, layout_key_point, layout_along;
    ACDeviceMsg mAcDevMsg;
    PopupWindow errorPopup, menuPopup, controlPopup;
    AnimationDrawable drawable;
    ArrayList<Integer> pointList;
    ArrayList<String> pointStrList;
    private MyReceiver receiver;
    private IntentFilter filter;
    ACDeviceDataMgr.PropertyReceiver propertyReceiver;
    AlertDialog alertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_new);
        initData();
        initView();
        initReceiver();
    }

    public void initReceiver() {
        receiver = new MyReceiver();
        filter = new IntentFilter();
        filter.addAction(CanvasView.INTENT_ACTION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getClockInfo();
        getDevStatus();
        initPropReceiver();
        registerPropReceiver();
        if (!registerTag) {
            registerTag = true;
            registerReceiver(receiver, filter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (registerTag) {
            registerTag = false;
            unregisterReceiver(receiver);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    private void initView() {
        errorPopup = new PopupWindow();
        anchorView = findViewById(R.id.rl_status);
        tv_time = (TextView) findViewById(R.id.tv_time);
        tv_area = (TextView) findViewById(R.id.tv_area);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_start = (TextView) findViewById(R.id.tv_start);
        tv_status = (TextView) findViewById(R.id.tv_status);
        image_ele = (ImageView) findViewById(R.id.image_ele);
        image_ele.setImageResource(R.drawable.map_aty_battery4);
        image_back = (ImageView) findViewById(R.id.image_back);
        image_start = (ImageView) findViewById(R.id.image_start);
        image_control = (ImageView) findViewById(R.id.image_control);
        image_setting = (ImageView) findViewById(R.id.image_setting);
        tv_use_control = (TextView) findViewById(R.id.tv_use_control);
        image_edge = (ImageView) findViewById(R.id.image_edge);
        image_function = (ImageView) findViewById(R.id.image_function);
        image_animation = (ImageView) findViewById(R.id.image_animation);
        image_key_point = (ImageView) findViewById(R.id.image_key_point);
        image_quan = (ImageView) findViewById(R.id.image_quan);
        layout_along = findViewById(R.id.layout_along);
        layout_recharge = findViewById(R.id.layout_recharge);
        layout_key_point = findViewById(R.id.layout_key_point);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        drawable = (AnimationDrawable) image_animation.getBackground();
        image_back.setOnClickListener(this);
        image_start.setOnClickListener(this);
        image_control.setOnClickListener(this);
        image_setting.setOnClickListener(this);
        image_function.setOnClickListener(this);

        canvasView = new CanvasView(this, deviceId, physicalId, subdomain);
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        canvasView.setLayoutParams(params);
        relativeLayout.addView(canvasView);
        canvasView.setVisibility(View.GONE);
        tv_title.setText(getString(R.string.map_aty_title, robotType));
    }

    public void initData() {
        context = this;
        gson = new Gson();
        mAcDevMsg = new ACDeviceMsg();
        pointList = new ArrayList<>();
        pointStrList = new ArrayList<>();
        animation = AnimationUtils.loadAnimation(context, R.anim.anims_ni);
        animation.setInterpolator(new LinearInterpolator());
        animation_alpha = AnimationUtils.loadAnimation(context, R.anim.anim_alpha);
        deviceId = SpUtils.getLong(context, MainActivity.KEY_DEVICEID);
        subdomain = SpUtils.getSpString(context, MainActivity.KEY_SUBDOMAIN);
        physicalId = SpUtils.getSpString(context, MainActivity.KEY_PHYCIALID);
        if (subdomain.equals(Constants.subdomain_x800)) {
            robotType = "X800";
        } else if (subdomain.equals(Constants.subdomain_x787)) {
            robotType = "X787";
        } else {
            robotType = "X785";
        }
    }

    private void initPropReceiver() {
        propertyReceiver = new ACDeviceDataMgr.PropertyReceiver() {
            @Override
            public void onPropertyReceive(String s, long l, String s1) {
                MyLog.e(TAG, "onPropertyReceive ==== " + s1);
                if (!isDestroyed()) {
                    PropertyInfo info = gson.fromJson(s1, PropertyInfo.class);
                    MyLog.e(TAG, "initPropReceiver onPropertyReceive errorCode = " + info.getError_info());
                    errorCode = info.getError_info();
                    batteryNo = info.getBattery_level();
                    curStatus = info.getWork_pattern();
                    isMaxMode = info.getVacuum_cleaning() == 0x01;
                    mopForce = info.getCleaning_cleaning();
                    voiceOpen = info.getVoice_mode() == 0x01;
                    setStatus(curStatus, batteryNo, mopForce, isMaxMode, voiceOpen);
                    setTimeAndArea(curStatus);
                    if (errorCode != 0) {
                        if (errorPopup != null && !errorPopup.isShowing()) {
                            showErrorPopup(errorCode);
                        }
                        if (canvasView.getVisibility() == View.VISIBLE) {
                            canvasView.setVisibility(View.GONE);
                        }
                    } else {
                        if (errorPopup != null && errorPopup.isShowing()) {
                            errorPopup.dismiss();
                        }
                    }
                }
            }
        };
    }

    public void registerPropReceiver() {
        AC.deviceDataMgr().subscribeProperty(subdomain, deviceId,
                new VoidCallback() {
                    @Override
                    public void success() {
                        AC.deviceDataMgr().registerPropertyReceiver(propertyReceiver);
                    }

                    @Override
                    public void error(ACException e) {

                    }
                }
        );
    }

    public void setTimeAndArea(int curStatus) {
        if (curStatus != 0x06) {
            tv_time.setText(getString(R.string.map_aty_gang));
            tv_area.setText(getString(R.string.map_aty_gang));
        } else {
            tv_area.setText(cleanArea / 100.0 + "㎡");
            tv_time.setText(workTime / 60 + "min");
        }
    }

    public void showErrorPopup(int errorCode) {
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

    public void showSelectDialog() {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_select_mode_dialog, null);
        v.findViewById(R.id.tv_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogUtils.hidden(alertDialog);
                mAcDevMsg.setContent(new byte[]{0x0c});
                sendByte = mAcDevMsg.getContent()[0];
                sendToDeviceWithOption_start(mAcDevMsg);
            }
        });
        v.findViewById(R.id.tv_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogUtils.hidden(alertDialog);
                mAcDevMsg.setContent(new byte[]{0x02});
                sendByte = mAcDevMsg.getContent()[0];
                sendToDeviceWithOption_start(mAcDevMsg);

            }
        });
        int width = (int) getResources().getDimension(R.dimen.dp_300);
        int height = (int) getResources().getDimension(R.dimen.dp_140);
        alertDialog = AlertDialogUtils.showDialog(context, v, width, height);
    }

    private void initErrorPopup(int code, View contentView) {
        ImageView image_delete = (ImageView) contentView.findViewById(R.id.image_delete);
        TextView tv_error = (TextView) contentView.findViewById(R.id.tv_error);
        tv_error.setText(DeviceUtils.getErrorText(context, code));
        image_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyLog.e(TAG, "onClick ===");
                if (errorPopup != null) {
                    errorPopup.dismiss();
                }
            }
        });
    }

    public void showMenuPopup() {
        if (menuPopup == null) {
            View contentView = LayoutInflater.from(context).inflate(R.layout.layout_popup_menu, null);
            menuPopup = new PopupWindow(context);
            menuPopup.setBackgroundDrawable(new ColorDrawable());
            menuPopup.setContentView(contentView);
            initMenuView(contentView);
            menuPopup.setOutsideTouchable(true);
            menuPopup.setFocusable(true);
            menuPopup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
            menuPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        }
        if (!menuPopup.isShowing()) {
            menuPopup.showAtLocation(anchorView, Gravity.BOTTOM | Gravity.END,
                    (int) getResources().getDimension(R.dimen.dp_32),
                    (int) getResources().getDimension(R.dimen.dp_120));
        }
        image_point.setSelected(curStatus == 0x05);
        tv_point.setSelected(curStatus == 0x05);
        image_recharge.setSelected(curStatus == 0x08);
        tv_recharge.setSelected(curStatus == 0x08);
        image_along.setSelected(curStatus == 0x04);
        tv_along.setSelected(curStatus == 0x04);
        image_clock.setSelected(hasAppoint);
        tv_clock.setSelected(hasAppoint);
    }

    public void initMenuView(View contentView) {
        tv_clock = (TextView) contentView.findViewById(R.id.tv_clock);
        tv_along = (TextView) contentView.findViewById(R.id.tv_along);
        tv_point = (TextView) contentView.findViewById(R.id.tv_point);
        tv_recharge = (TextView) contentView.findViewById(R.id.tv_recharge);
        image_clock = (ImageView) contentView.findViewById(R.id.image_clock);
        image_along = (ImageView) contentView.findViewById(R.id.image_along);
        image_point = (ImageView) contentView.findViewById(R.id.image_point);
        image_recharge = (ImageView) contentView.findViewById(R.id.image_recharge);
        image_clock.setOnClickListener(this);
        image_along.setOnClickListener(this);
        image_point.setOnClickListener(this);
        image_recharge.setOnClickListener(this);
    }

    public void showControlPopup() {
        if (controlPopup == null) {
            controlPopup = new ControlPopupWindow(context, physicalId, subdomain);
            controlPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    setTextStatus(TAG_NORMAL);
                }
            });
        }
        if (!controlPopup.isShowing()) {
            controlPopup.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
            setTextStatus(TAG_CONTROL);
        }
    }

    public void setTextStatus(int tag) {
        switch (tag) {
            case TAG_NORMAL:
                tv_use_control.setVisibility(View.GONE);
                break;
            case TAG_CONTROL:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(getString(R.string.map_aty_use_control));
                break;
            case TAG_RECHAGRGE:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(getString(R.string.map_aty_use_recharging, robotType));
                break;
            case TAG_KEYPOINT:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(getString(R.string.map_aty_key_pointing, robotType));
                break;
            case TAG_ALONG:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(getString(R.string.map_aty_use_along));
                break;
        }
    }

    public void getClockInfo() {
        ACDeviceMsg msg_clockInfo = new ACDeviceMsg(MsgCodeUtils.ClockInfos, new byte[]{0x00});
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, msg_clockInfo,
                Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
                    @Override
                    public void success(ACDeviceMsg deviceMsg) {
                        adjustTime();
                        if (!isDestroyed()) {
                            byte[] resp = deviceMsg.getContent();
                            if (resp != null && resp.length == 50) {
                                hasAppoint = hasAppoint(resp);
                            }
                        }
                    }

                    @Override
                    public void error(ACException e) {
                        adjustTime();
                        hasAppoint = false;
                    }
                });
    }

    public void adjustTime() {
        ACDeviceMsg msg_adjustTime = new ACDeviceMsg(MsgCodeUtils.AdjustTime, TimeUtil.getTimeBytes());
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, msg_adjustTime, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void success(ACDeviceMsg acDeviceMsg) {

            }

            @Override
            public void error(ACException e) {

            }
        });
    }

    public void getDevStatus() {
        ACDeviceMsg msg_devStatus = new ACDeviceMsg(MsgCodeUtils.DevStatus, new byte[]{0x00});
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, msg_devStatus,
                Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
                    @Override
                    public void success(ACDeviceMsg deviceMsg) {
                        byte[] bytes = deviceMsg.getContent();
                        if (bytes != null) {
                            if (bytes.length == 10) {
                                isX800 = true;
                            }
                            errorCode = bytes[8];
                            curStatus = bytes[0];
                            batteryNo = bytes[5];
                            mopForce = bytes[4];
                            isMaxMode = bytes[3] == 0x01;
                            voiceOpen = bytes[6] == 0x01;
                            setStatus(curStatus, batteryNo, mopForce, isMaxMode, voiceOpen);
                            setTimeAndArea(curStatus);
                            if (errorCode != 0) {
                                if (errorPopup != null && !errorPopup.isShowing()) {
                                    showErrorPopup(errorCode);
                                }
                            } else {
                                if (errorPopup != null && errorPopup.isShowing()) {
                                    errorPopup.dismiss();
                                }
                            }
                        }
                    }

                    @Override
                    public void error(ACException e) {

                    }
                });
    }

    public void setStatus(int curStatus, int batteryNo, int mopForce, boolean isMaxMode, boolean voiceOpen) {
        isWork = isWork(curStatus);
        tv_status.setText(DeviceUtils.getStatusStr(context, curStatus, errorCode));
        image_start.setSelected(isWork);
        tv_start.setText(isWork ? getString(R.string.map_aty_stop) : getString(R.string.map_aty_start));
        if (batteryNo != -1) {
            setBatteryImage(curStatus, batteryNo);
            SpUtils.saveBoolean(context, physicalId + KEY_IS_MAX, isMaxMode);
            SpUtils.saveInt(context, physicalId + KEY_MOP_FORCE, mopForce);
            SpUtils.saveBoolean(context, physicalId + KEY_VOICE_OPEN, voiceOpen);
        }
        clearAll(curStatus);
        if (curStatus == 0x8) { //回充
            layout_recharge.setVisibility(View.VISIBLE);
            drawable.start();
            setTextStatus(TAG_RECHAGRGE);
//            image_quan.clearAnimation();
//            hasStart = false;
        } else if (curStatus == 0x05) { //重点
            layout_key_point.setVisibility(View.VISIBLE);
            if (!hasStart) {
                image_quan.startAnimation(animation);
                hasStart = true;
            }
            setTextStatus(TAG_KEYPOINT);
        } else if (curStatus == 0x0A) { //遥控
            setTextStatus(TAG_CONTROL);
        } else if (curStatus == 0x02) { //待机


        } else if (curStatus == 0x04) {
            layout_along.setVisibility(View.VISIBLE);
            if (!hasStart_) {
                image_edge.startAnimation(animation_alpha);
                hasStart_ = true;
            }
            setTextStatus(TAG_ALONG);
        } else if (curStatus == 0x06 || curStatus == 0x0C) {
            canvasView.setVisibility(View.VISIBLE);
            image_ele.bringToFront();
            setTextStatus(TAG_NORMAL);
//            image_quan.clearAnimation();
//            hasStart = false;
        }

        if (menuPopup != null && menuPopup.isShowing()) {
            image_point.setSelected(curStatus == 0x05);
            tv_point.setSelected(curStatus == 0x05);
            image_recharge.setSelected(curStatus == 0x08);
            tv_recharge.setSelected(curStatus == 0x08);
            image_along.setSelected(curStatus == 0x04);
            tv_along.setSelected(curStatus == 0x04);
        }
    }

    public void clearAll(int curStatus) {
        if (curStatus != 0x5) {
            image_quan.clearAnimation();
            hasStart = false;
        }
        if (curStatus != 0x04) {
            image_edge.clearAnimation();
            hasStart_ = false;
        }
        layout_recharge.setVisibility(View.GONE);
        layout_key_point.setVisibility(View.GONE);
        layout_along.setVisibility(View.GONE);
        canvasView.setVisibility(View.GONE);
        tv_use_control.setVisibility(View.GONE);
    }

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

    public boolean hasAppoint(byte[] resp) {
        for (int j = 1; j <= 31; j += 5) {
            if (resp[j] != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_start: //done
                mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
                if (curStatus == 0x06) {
                    if (isX800) {
                        showSelectDialog();
                        return;
                    } else {
                        mAcDevMsg.setContent(new byte[]{0x02});
                    }
                } else {
                    mAcDevMsg.setContent(new byte[]{0x06});
                }
                sendByte = mAcDevMsg.getContent()[0];
                sendToDeviceWithOption_start(mAcDevMsg);
                break;
            case R.id.image_control://done
                if (isWork(curStatus) || curStatus == 0x01) {
                    ToastUtils.showToast(context, getString(R.string.map_aty_can_not_execute));
                } else if (curStatus == 0x0B || curStatus == 0x09) {
                    ToastUtils.showToast(context, getString(R.string.map_aty_charge));
                } else {
                    showControlPopup();
                }
                break;
            case R.id.image_function:
                showMenuPopup();
                break;
            case R.id.image_back:
                finish();
                break;
            case R.id.image_setting:
                Intent i = new Intent(context, SettingActivity.class);
                startActivity(i);
                break;
            case R.id.image_clock: //done
                if (menuPopup != null && menuPopup.isShowing()) {
                    menuPopup.dismiss();
                }
                Intent i_clock = new Intent(context, ClockingActivity.class);
                startActivity(i_clock);
                break;
            case R.id.image_along:  //done
                mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
                if (curStatus == 0x02 || curStatus == 0x04 || curStatus == 0x0A) {
                    byte b = (byte) (curStatus == 0x04 ? 0x02 : 0x04);
                    mAcDevMsg.setContent(new byte[]{b});
                    sendToDeviceWithOption(mAcDevMsg);
                } else if (curStatus == 0x09 || curStatus == 0x0B) {
                    ToastUtils.showToast(context, getString(R.string.map_aty_charge));
                } else {
                    ToastUtils.showToast(context, getString(R.string.map_aty_can_not_execute));
                }
                break;
            case R.id.image_point:  //done
                if (curStatus == 0x09 || curStatus == 0x0B) {
                    ToastUtils.showToast(context, getString(R.string.map_aty_charge));
                } else if (curStatus == 0x08 || curStatus == 0x04) {
                    ToastUtils.showToast(context, getString(R.string.map_aty_can_not_execute));
                } else {
                    mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
                    if (curStatus == 0x05) {
                        mAcDevMsg.setContent(new byte[]{0x02});
                    } else {
                        mAcDevMsg.setContent(new byte[]{0x05});
                    }
                    sendToDeviceWithOption(mAcDevMsg);
                }
                break;
            case R.id.image_recharge:  //done
                if (curStatus == 0x09 || curStatus == 0x0B) {
                    ToastUtils.showToast(context, getString(R.string.map_aty_charge));
                } else if (curStatus == 0x05 || curStatus == 0x04) {
                    ToastUtils.showToast(context, getString(R.string.map_aty_can_not_execute));
                } else {
                    mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
                    if (curStatus == 0x08) {
                        mAcDevMsg.setContent(new byte[]{0x02});
                    } else {
                        mAcDevMsg.setContent(new byte[]{0x08});
                    }
                    sendToDeviceWithOption(mAcDevMsg);
                }
                break;
        }
    }


    public boolean isWork(int curStatus) {
        if (curStatus == 0x03 || curStatus == 0x04 ||
                curStatus == 0x05 || curStatus == 0x06 ||
                curStatus == 0x08) {
            return true;
        }
        return false;
    }

    public void sendToDeviceWithOption(ACDeviceMsg msg) {
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, msg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void error(ACException e) {
                ToastUtils.showErrorToast(context, e.getErrorCode());
            }

            @Override
            public void success(ACDeviceMsg deviceMsg) {
                byte[] bytes = deviceMsg.getContent();
                curStatus = bytes[0];
                setStatus(curStatus, -1, mopForce, isMaxMode, voiceOpen);
            }
        });
    }

    public void sendToDeviceWithOption_start(ACDeviceMsg msg) {
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, msg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void error(ACException e) {
                ToastUtils.showErrorToast(context, e.getErrorCode());
            }

            @Override
            public void success(ACDeviceMsg deviceMsg) {
                byte[] bytes = deviceMsg.getContent();
                MyLog.e(TAG, "sendToDeviceWithOption_start  success " + bytes[0]);
                curStatus = bytes[0];
                if (curStatus == sendByte) {
                    setStatus(curStatus, -1, mopForce, isMaxMode, voiceOpen);
                } else {
                    if (curStatus == 0x0B) {
                        ToastUtils.showToast(context, getString(R.string.map_aty_charge));
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (errorPopup != null && errorPopup.isShowing()) {
            errorPopup.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            workTime = intent.getIntExtra("workTime", 0);
            cleanArea = intent.getIntExtra("cleanArea", 0);
            MyLog.e(TAG, "onReceive workTime = " + workTime + " cleanArea = " + cleanArea);
            if (curStatus != 0x06) {
                tv_time.setText(getString(R.string.map_aty_gang));
                tv_area.setText(getString(R.string.map_aty_gang));
            } else {
                tv_area.setText(cleanArea / 100.0 + "㎡");
                tv_time.setText(workTime / 60 + "min");
            }
        }
    }
}
