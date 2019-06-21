package com.ilife.iliferobot.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.Nullable;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACDeviceMsg;
import com.accloud.service.ACException;
import com.badoo.mobile.util.WeakHandler;
import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.able.MsgCodeUtils;
import com.ilife.iliferobot.utils.MyLogger;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.utils.SpUtils;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;

public class
OtaUpdateActivity extends BackBaseActivity {
    private final String TAG = OtaUpdateActivity.class.getSimpleName();
    private static final int SET_PROGRESSBAR = 1;
    private static final int UPDATE_FAILED = 2;
    public static final int UPDATE_SUCCESS = 3;
    public static final int AFTER_CHECK = 4;
    private Context context;
    private String physicalDeviceId;
    private String subdomain;
    @BindView(R.id.tv_cur_version)
    TextView tv_current;
    @BindView(R.id.tv_target_version)
    TextView tv_target;
    @BindView(R.id.btn_update)
    Button btn_update;
    @BindView(R.id.fl_version)
    LinearLayout fl_version;
    @BindView(R.id.ll_update)
    LinearLayout ll_update;
    @BindView(R.id.tv_top_title)
    TextView title;
    Timer otatimer;
    TimerTask otatask;
    Timer sendtimer;
    TimerTask sendtask;
    int index = 0;
    int sendp = 0;
    private boolean isUpdating = true;
    private boolean isSuccess = true;
    private boolean isPBFinished = false;

    @BindView(R.id.iv_updating_animate)
    ImageView iv_updating_animate;
    private WeakHandler handler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SET_PROGRESSBAR:
                    int progress = (int) msg.obj;
                    MyLogger.e(TAG, "moni progress==:" + progress);
                    if (progress == 100) {
                    }
                    break;
                case UPDATE_SUCCESS://更新成功
                    String version = (String) msg.obj;
                    ToastUtils.showToast(context, getString(R.string.setting_aty_ota_upadta_success));//弹更新成功的提示
                    isSuccess = true;
                    fl_version.setVisibility(View.VISIBLE);
                    ll_update.setVisibility(View.GONE);
                    tv_current.setText(getResources().getString(R.string.setting_ota_current_version, version));
                    tv_target.setText(getResources().getString(R.string.setting_ota_targat_version, version));
                    btn_update.setText(R.string.ota_aty_latest_ver);
                    sendp = 0;
                    if (otatimer != null) {
                        otatimer.cancel();
                    }
                    break;
                case UPDATE_FAILED://更新失败
                    if (!isSuccess) {//更新失败时可以退出界面
                        isSuccess = true;
                    }
                    index = 0;
                    break;
                case AFTER_CHECK://延时30秒后再次查询重启后的版本
                    isUpdating = true;
                    break;
            }
            return false;
        }
    });
    private boolean isFromSetting;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIntentValue();
        initView();
        showLoadingDialog();
        initData();
        startTimer();
        MyLogger.e(TAG, "onCreate==:");
    }

    @Override
    public int getLayoutId() {
        return R.layout.ota_activity;
    }

    private void getIntentValue() {
        isFromSetting = true;
    }


    public void initView() {
        context = this;
        btn_update.setClickable(false);
        title.setText(R.string.setting_aty_ota_update);
    }

    private void initData() {
        physicalDeviceId = SpUtils.getSpString(context, MainActivity.KEY_PHYCIALID);
        subdomain = SpUtils.getSpString(context, MainActivity.KEY_SUBDOMAIN);
    }

    public void startTimer() {
        otatimer = new Timer();
        otatask = new TimerTask() {
            @Override
            public void run() {
                if (isUpdating) {
                    checkOtaUpdate();
                    MyLogger.e(TAG, "cur_time:" + System.currentTimeMillis());
                }
            }
        };
        otatimer.schedule(otatask, 0, 3 * 1000);
    }

    //检查升级
    public void checkOtaUpdate() {
        ACDeviceMsg deviceMsg = new ACDeviceMsg(MsgCodeUtils.CheckMachineInfo, new byte[]{0x00});
        sendToDevice_CheckUptate(deviceMsg, physicalDeviceId);
    }

    public void sendToDevice_CheckUptate(ACDeviceMsg deviceMsg, final String physicalDeviceId) {//检查OTA更新
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalDeviceId, deviceMsg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void success(ACDeviceMsg deviceMsg) {
                hideLoadingDialog();
                byte[] resp = deviceMsg.getContent();
                MyLogger.e(TAG, "sendToDevice_CheckUptate success==:" + resp.length + "<--->" + "firmwareStatus:" + resp[0]);
                if (resp != null && resp.length > 0) {
                    byte firmwareStatus = resp[0];
                    if (isFromSetting) {//从设置界面进来
                        switch (firmwareStatus) {
                            case 0x00://无更新
                                String curVersion = resp[2] + "." + resp[3] + "." + resp[4] + "." + resp[5];
                                fl_version.setVisibility(View.VISIBLE);
                                tv_current.setText(getResources().getString(R.string.setting_ota_current_version, curVersion));
                                tv_target.setText(getResources().getString(R.string.setting_ota_targat_version, curVersion));
                                isUpdating = false;
                                break;
                            case 0x01://有更新
                                String currVersion = resp[2] + "." + resp[3] + "." + resp[4] + "." + resp[5];
                                String tarVersion = resp[6] + "." + resp[7] + "." + resp[8] + "." + resp[9];
                                fl_version.setVisibility(View.VISIBLE);
                                tv_current.setText(getResources().getString(R.string.setting_ota_current_version, currVersion));
                                tv_target.setText(getResources().getString(R.string.setting_ota_targat_version, tarVersion));
                                isUpdating = false;
                                btn_update.setSelected(true);
                                btn_update.setClickable(true);
                                break;
                        }
                        isFromSetting = false;
                    } else {//在更新中查询返回
                        switch (firmwareStatus) {
                            case 0x00://无更新
                                handler.sendEmptyMessage(UPDATE_FAILED);
                                isUpdating = false;
                                break;
                            case 0x01://有更新
                                handler.sendEmptyMessage(UPDATE_FAILED);
                                isUpdating = false;
                                break;
                            case 0x02://正在更新
                                byte progress = resp[1];//更新进度
                                MyLogger.e(TAG, "updating progress===:" + progress);
                                if (isPBFinished) {
                                    return;
                                }
                                sendProgressTimer(100);
                                isUpdating = false;
                                break;
                            case 0x03://更新成功
                                String newest_version = resp[6] + "." + resp[7] + "." + resp[8] + "." + resp[9];
                                byte suc_prg = resp[1];//更新进度
                                MyLogger.e(TAG, "update success progress===:" + suc_prg);
                                Message msg = new Message();
                                msg.obj = newest_version;
                                msg.what = UPDATE_SUCCESS;
                                handler.sendMessage(msg);
                                isUpdating = false;
                                break;
                            case 0x04://更新失败(退出当前界面到设备列表)
                                handler.sendEmptyMessage(UPDATE_FAILED);
                                isUpdating = false;
                                break;
                        }
                    }
                } else {
                    hideLoadingDialog();
                    MyLogger.e(TAG, "resp is null");
                }
            }

            @Override
            public void error(ACException e) {
                index++;
                MyLogger.e(TAG, "check update error Index==:" + index);
                MyLogger.e(TAG, "check update error cur_time:" + System.currentTimeMillis());
                if (isSuccess) {//第一次则不继续查询进入时如果失败，在更新中时如果失败则继续查询
                    isUpdating = false;
                }
                if (index >= 20) {
                    isUpdating = false;
                    handler.sendEmptyMessage(UPDATE_FAILED);
                }
                MyLogger.e(TAG, "sendToDevice_CheckUptate error==:" + e.toString());
            }
        });
    }

    public void sendProgressTimer(final int progress) {
        sendtimer = new Timer();
        sendtask = new TimerTask() {
            @Override
            public void run() {
                if (sendp <= progress) {
                    MyLogger.e(TAG, "moni==:" + sendp);
                    Message msg = new Message();
                    msg.what = SET_PROGRESSBAR;
                    msg.obj = sendp;
                    handler.sendMessage(msg);
                    sendp++;
                } else {
                    if (sendp > 100) {
                        sendtimer.cancel();
                        handler.sendEmptyMessageDelayed(AFTER_CHECK, 5 * 1000);
                        MyLogger.e(TAG, "progress finish==:" + sendp);
                    }
                }
            }
        };
        sendtimer.schedule(sendtask, 0, 50);
    }

    //下发升级指令
    public void otaUpdate() {
        ACDeviceMsg deviceMsg = new ACDeviceMsg(MsgCodeUtils.DeviceUpdate, new byte[]{0x01});
        sendToDevice_OtaUptate(deviceMsg, physicalDeviceId);
    }

    public void sendToDevice_OtaUptate(ACDeviceMsg deviceMsg, final String physicalDeviceId) {//确认升级
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalDeviceId, deviceMsg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void success(ACDeviceMsg deviceMsg) {
                byte[] content = deviceMsg.getContent();
                if (content[0] == 0x01) {
                    isUpdating = true;
                    isSuccess = false;
                    Animation rotate = AnimationUtils.loadAnimation(OtaUpdateActivity.this, R.anim.loading_rotate);
                    iv_updating_animate.setAnimation(rotate);
                    ll_update.setVisibility(View.VISIBLE);
                    fl_version.setVisibility(View.GONE);
                }
                MyLogger.e(TAG, "sendToDevice_OtaUptate success==:");
            }

            @Override
            public void error(ACException e) {
                ToastUtils.showErrorToast(context, e.getErrorCode());
                MyLogger.e(TAG, "sendToDevice_OtaUptate error==" + e.toString());
            }
        });
    }

    @Override
    public void clickBackBtn() {
        if (isSuccess && otatimer != null) {
            otatimer.cancel();
        }
        super.clickBackBtn();
    }

    @OnClick(R.id.btn_update)
    public void onClick(View v) {
        if (v.getId() == R.id.btn_update)
            otaUpdate();
        btn_update.setText(R.string.updating_btn);
        btn_update.setSelected(false);
        btn_update.setClickable(false);
    }

    @Override
    public void onBackPressed() {
        if (isSuccess && otatimer != null) {
            otatimer.cancel();
        }
        super.onBackPressed();
    }
}
