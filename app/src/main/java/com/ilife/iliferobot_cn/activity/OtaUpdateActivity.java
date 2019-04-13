package com.ilife.iliferobot_cn.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.Nullable;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACDeviceMsg;
import com.accloud.service.ACException;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.DialogUtils;
import com.ilife.iliferobot_cn.utils.MsgCodeUtils;
import com.ilife.iliferobot_cn.utils.MyLog;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.ToastUtils;

import java.util.Timer;
import java.util.TimerTask;

public class OtaUpdateActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = OtaUpdateActivity.class.getSimpleName();
    private static final int SET_PROGRESSBAR = 1;
    private RelativeLayout rl_progress;
    private ProgressBar progressBar;
    private TextView tv_progress;
    private Context context;
    private String physicalId;
    private String subdomain;
    private TextView tv_curVersion;
    private TextView tv_tarVersion;
    private Button btn_update;
    private String curVersion;
    private String tarVersion;
    Timer otatimer;
    TimerTask otatask;
    Timer sendtimer;
    TimerTask sendtask;
    int current = 0;
    int sendp = 0;
    private boolean isNeedUpdate = true;
    private boolean isSuccess;
    private ImageView image_back;
    private Dialog dialog;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SET_PROGRESSBAR:
                    int progress = (int) msg.obj;
                    MyLog.e(TAG, "progerss progress==:" + progress);
                    if (progressBar != null) {
                        tv_progress.setText(progress + "%");
                        progressBar.setProgress(progress);
//                        progressBar.setProgressDrawable(getDrawable(R.drawable.progressbar_horizontal_long));//5.0以上
//                        progressBar.setProgressDrawable(ContextCompat.getDrawable(context,R.drawable.progressbar_horizontal_long));//不用管版本
                        current = progress;
                        if (current == 100) {
                            tv_curVersion.setText(getString(R.string.ota_aty_cur, tarVersion));
                            tv_tarVersion.setVisibility(View.GONE);
                            rl_progress.setVisibility(View.GONE);
                            btn_update.setVisibility(View.GONE);
//                            ToastUtils.showToast(context, getString(R.string.setting_aty_ota_upadta_success));
                            isSuccess = true;
                            isNeedUpdate = false;
                            image_back.setClickable(true);
                            current = 0;
                            sendp = 0;
                            otatimer.cancel();
                            sendtimer.cancel();
                        }
                    }
                    break;
            }
        }
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota_update);
        initView();
        showLoadingDialog();
        initData();
        startTimer();
    }

    public void showLoadingDialog() {
        dialog = DialogUtils.createLoadingDialog(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otatimer != null && sendtimer != null) {
            otatimer.cancel();
            sendtimer.cancel();
        }
    }

    private void initView() {
        context = this;
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tv_progress = (TextView) findViewById(R.id.tv_progress);
        rl_progress = (RelativeLayout) findViewById(R.id.rl_progress);
        tv_curVersion = (TextView) findViewById(R.id.tv_cur_version);
        tv_tarVersion = (TextView) findViewById(R.id.tv_tar_version);
        tv_curVersion.setText(getString(R.string.ota_aty_cur, ""));
        tv_tarVersion.setText(getString(R.string.ota_aty_tar, ""));
        btn_update = (Button) findViewById(R.id.btn_update);
        image_back = (ImageView) findViewById(R.id.image_back);
        btn_update.setOnClickListener(this);
        image_back.setOnClickListener(this);
    }

    private void initData() {
        physicalId = SpUtils.getSpString(context, MainActivity.KEY_PHYCIALID);
        subdomain = SpUtils.getSpString(context, MainActivity.KEY_SUBDOMAIN);
    }

    //检查升级
    public void checkOtaUpdate() {
        ACDeviceMsg deviceMsg = new ACDeviceMsg(MsgCodeUtils.CheckMachineInfo, new byte[]{0x00});
        sendToDevice_CheckUpdate(deviceMsg, physicalId);
    }

    public void sendToDevice_CheckUpdate(ACDeviceMsg deviceMsg, final String physicalDeviceId) {//检查OTA更新
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalDeviceId, deviceMsg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void success(ACDeviceMsg deviceMsg) {
                byte[] resp = deviceMsg.getContent();
                if (resp != null && resp.length > 0) {
                    byte firmwareStatus = resp[0];//固件更新状态
                    MyLog.e(TAG, "firmwareStatus==:" + firmwareStatus);
                    switch (firmwareStatus) {
                        case 0x00://无更新
                            DialogUtils.closeDialog(dialog);
                            curVersion = resp[2] + "." + resp[3] + "." + resp[4] + "." + resp[5];
                            tv_curVersion.setVisibility(View.VISIBLE);
                            tv_curVersion.setText(getString(R.string.ota_aty_cur, curVersion));
                            tv_tarVersion.setVisibility(View.GONE);
                            btn_update.setVisibility(View.GONE);
                            isNeedUpdate = false;
                            break;

                        case 0x01://有更新
                            DialogUtils.closeDialog(dialog);
                            tv_tarVersion.setVisibility(View.VISIBLE);
                            btn_update.setVisibility(View.VISIBLE);
                            curVersion = resp[2] + "." + resp[3] + "." + resp[4] + "." + resp[5];//当前版本
                            tarVersion = resp[6] + "." + resp[7] + "." + resp[8] + "." + resp[9];//目标版本
                            tv_curVersion.setText(getString(R.string.ota_aty_cur, curVersion));
                            tv_tarVersion.setText(getString(R.string.ota_aty_tar, tarVersion));
                            isNeedUpdate = false;
                            break;

                        case 0x02://正在更新
                            DialogUtils.closeDialog(dialog);
                            rl_progress.setVisibility(View.VISIBLE);//进入地图有更新时直接跳转至该界面
                            tv_curVersion.setVisibility(View.GONE);
                            tv_tarVersion.setVisibility(View.GONE);
//                            btn_update.setVisibility(View.GONE);
                            byte progress = resp[1];//更新进度
                            sendProgressTimer(progress);
                            break;

                    }
                } else {
                    MyLog.e(TAG, "resp is null");
                }

            }

            @Override
            public void error(ACException e) {
                DialogUtils.closeDialog(dialog);
                ToastUtils.showErrorToast(context, e.getErrorCode());
            }
        });
    }

    public void startTimer() {
        otatimer = new Timer();
        otatask = new TimerTask() {
            @Override
            public void run() {
                if (isNeedUpdate) {
                    checkOtaUpdate();
                }
            }
        };
        otatimer.schedule(otatask, 0, 5 * 1000);
    }

    public void sendProgressTimer(final int progress) {
        sendtimer = new Timer();
        sendtask = new TimerTask() {
            @Override
            public void run() {
                if (sendp <= progress) {
                    Message msg = new Message();
                    msg.what = SET_PROGRESSBAR;
                    msg.obj = sendp;
                    handler.sendMessage(msg);
                    sendp++;
                } else {
                    sendtimer.cancel();
                }
            }
        };
        sendtimer.schedule(sendtask, 0, 300);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back:
                if (isSuccess) {
                    finish();
                } else if (isNeedUpdate) {
                    image_back.setClickable(false);
                } else {
                    image_back.setClickable(true);
                    finish();
                }
                break;
            case R.id.btn_update:
                otaUpdate();
                break;
        }
    }

    //下发升级指令
    public void otaUpdate() {
        ACDeviceMsg deviceMsg = new ACDeviceMsg(MsgCodeUtils.DeviceUpdate, new byte[]{0x01});
        sendToDevice_OtaUptate(deviceMsg, physicalId);
    }

    public void sendToDevice_OtaUptate(ACDeviceMsg deviceMsg, final String physicalDeviceId) {//确认升级
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalDeviceId, deviceMsg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void success(ACDeviceMsg deviceMsg) {
                byte[] content = deviceMsg.getContent();
                if (content[1] == 0x01) {
                    rl_progress.setVisibility(View.VISIBLE);
                    isNeedUpdate = true;
                }
                MyLog.e(TAG, "sendToDevice_OtaUptate==:");
            }

            @Override
            public void error(ACException e) {
                MyLog.e(TAG, "otaUpate error!" + e.toString());
            }
        });
    }

    @Override
    public void onBackPressed() {
//        ;//注释掉为不退出当前Activity
        if (isSuccess) {
            finish();
        } else if (isNeedUpdate) {
            return;
        } else {
            finish();
        }
    }
}
