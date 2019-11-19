package com.ilife.iliferobot.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACDeviceDataMgr;
import com.accloud.service.ACDeviceMsg;
import com.accloud.service.ACException;
import com.accloud.service.ACUserDevice;
import com.badoo.mobile.util.WeakHandler;
import com.google.gson.Gson;
import com.ilife.iliferobot.BuildConfig;
import com.ilife.iliferobot.activity.fragment.UniversalDialog;
import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.presenter.MapX9Presenter;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.able.DeviceUtils;
import com.ilife.iliferobot.able.MsgCodeUtils;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.entity.PropertyInfo;
import com.ilife.iliferobot.listener.ReNameListener;
import com.ilife.iliferobot.utils.AlertDialogUtils;
import com.ilife.iliferobot.utils.DialogUtils;
import com.ilife.iliferobot.utils.MyLogger;
import com.ilife.iliferobot.utils.SpUtils;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * Created by chengjiaping on 2018/8/16.
 */

public class SettingActivity extends BackBaseActivity {
    final String TAG = SettingActivity.class.getSimpleName();
    final int TAG_FIND_DONE = 0x01;
    public static final String KEY_MODE = "KEY_MODE";//工作模式（规划、随机）
    public static final String KEY_CUR_WORK_MODE = "KEY_MODE";//设备当前状态
    int mopForce, mode, index, curWorkMode;
    boolean isMaxMode, voiceOpen;
    Context context;
    Intent intent;
    long deviceId, userId, ownerId;
    String devName, subdomain, physicalId, name;
    @BindView(R.id.image_soft)
    ImageView image_soft;
    @BindView(R.id.image_standard)
    ImageView image_standard;
    @BindView(R.id.image_strong)
    ImageView image_strong;
    @BindView(R.id.image_max)
    ImageView image_max;
    @BindView(R.id.image_voice)
    ImageView image_voice;
    @BindView(R.id.image_plan)
    ImageView image_plan;
    @BindView(R.id.image_random)
    ImageView image_random;
    @BindView(R.id.image_product)
    ImageView image_product;
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.tv_type)
    TextView tv_type;
    @BindView(R.id.tv_soft)
    TextView tv_soft;
    @BindView(R.id.tv_standard)
    TextView tv_standard;
    @BindView(R.id.tv_strong)
    TextView tv_strong;
    @BindView(R.id.tv_water)
    TextView tv_water;
    @BindView(R.id.tv_plan)
    TextView tv_plan;
    @BindView(R.id.tv_random)
    TextView tv_random;
    @BindView(R.id.tv_mode)
    TextView tv_mode;
    @BindView(R.id.tv_top_title)
    TextView tv_top_title;
    @BindView(R.id.tv_ota_ver)
    TextView tv_ota_ver;
    @BindView(R.id.image_down_1)
    ImageView image_down_1;
    @BindView(R.id.image_down_2)
    ImageView image_down_2;
    @BindView(R.id.rl_water)
    RelativeLayout rl_water;
    @BindView(R.id.rl_clock)
    RelativeLayout rl_clock;
    @BindView(R.id.rl_record)
    RelativeLayout rl_record;
    @BindView(R.id.rl_consume)
    RelativeLayout rl_consume;
    @BindView(R.id.rl_mode)
    RelativeLayout rl_mode;
    @BindView(R.id.rl_suction)
    RelativeLayout rl_suction;
    @BindView(R.id.rl_find)
    RelativeLayout rl_find;
    @BindView(R.id.rl_soft)
    RelativeLayout rl_soft;
    @BindView(R.id.rl_standard)
    RelativeLayout rl_standard;
    @BindView(R.id.rl_strong)
    RelativeLayout rl_strong;
    @BindView(R.id.rl_plan)
    RelativeLayout rl_plan;
    @BindView(R.id.rl_random)
    RelativeLayout rl_random;
    @BindView(R.id.rl_facReset)
    RelativeLayout rl_facReset;
    @BindView(R.id.rl_voice)
    RelativeLayout rl_voice;
    @BindView(R.id.rl_update)
    RelativeLayout rl_update;
    @BindView(R.id.ll_water)
    LinearLayout ll_water;
    @BindView(R.id.ll_mode)
    LinearLayout ll_mode;
    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.iv_find_robot)
    ImageView iv_find_robot;
    LayoutInflater inflater;
    AlertDialog alterDialog;
    Dialog dialog;
    Animation animation;
    ACDeviceMsg acDeviceMsg;
    ACDeviceDataMgr.PropertyReceiver propReceiver;
    WeakHandler handler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == TAG_FIND_DONE) {
                if (rl_find != null) {
                    rl_find.setClickable(true);
                    imageView.setVisibility(View.GONE);
                    imageView.clearAnimation();
                    iv_find_robot.setVisibility(View.VISIBLE);
                }

            }
            return false;
        }
    });


    @Override
    public int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        propReceiver = (s, l, s1) -> {
            if (isDestroyed()) {
                return;
            }
            Gson gson = new Gson();
            PropertyInfo info = gson.fromJson(s1, PropertyInfo.class);
            if (info != null) {
                int cleanForce = info.getVacuum_cleaning();
                int voiceMode = info.getVoice_mode();
                isMaxMode = cleanForce == 0x01;
                voiceOpen = voiceMode == 0x01;
                mopForce = info.getCleaning_cleaning();
                setStatus(1, mopForce, isMaxMode, voiceOpen);
            }
        };
        registerMsg();
    }


    @Override
    protected void onDestroy() {
        if (propReceiver != null) {
            AC.deviceDataMgr().unregisterPropertyReceiver(propReceiver);
            AC.deviceDataMgr().unSubscribeAllProperty();
        }
        super.onDestroy();

    }


    public void registerMsg() {
        AC.deviceDataMgr().subscribeProperty(subdomain, deviceId,
                new VoidCallback() {
                    @Override
                    public void success() {
                        AC.deviceDataMgr().registerPropertyReceiver(propReceiver);
                    }

                    @Override
                    public void error(ACException e) {
                    }
                });
    }

    public void initView() {
        context = this;
        dialog = DialogUtils.createLoadingDialog_(context);
        inflater = LayoutInflater.from(context);
        tv_top_title.setText(R.string.ap_aty_setting);
        rl_suction.setOnClickListener(new MyListener());
        rl_soft.setOnClickListener(new MyListener());
        rl_standard.setOnClickListener(new MyListener());
        rl_strong.setOnClickListener(new MyListener());
        tv_ota_ver.setText("当前版本：0.0.1.10");

    }

    public void initData() {
        animation = AnimationUtils.loadAnimation(context, R.anim.anims);
        animation.setInterpolator(new LinearInterpolator());
        acDeviceMsg = new ACDeviceMsg();
        devName = SpUtils.getSpString(context, MainActivity.KEY_DEVNAME);
        deviceId = SpUtils.getLong(context, MainActivity.KEY_DEVICEID);
        subdomain = SpUtils.getSpString(context, MainActivity.KEY_SUBDOMAIN);
        physicalId = SpUtils.getSpString(context, MainActivity.KEY_PHYCIALID);
        ownerId = SpUtils.getLong(context, MainActivity.KEY_OWNER);
        userId = AC.accountMgr().getUserId();
        mode = SpUtils.getInt(context, physicalId + KEY_MODE);
        curWorkMode = SpUtils.getInt(context, physicalId + KEY_CUR_WORK_MODE);
        mopForce = SpUtils.getInt(context, physicalId + MapX9Presenter.KEY_MOP_FORCE);
        isMaxMode = SpUtils.getBoolean(context, physicalId + MapX9Presenter.KEY_IS_MAX);
        voiceOpen = SpUtils.getBoolean(context, physicalId + MapX9Presenter.KEY_VOICE_OPEN);
        setMode(mode);
        setStatus(1, mopForce, isMaxMode, voiceOpen);
        if (!TextUtils.isEmpty(devName)) {
            tv_name.setText(devName);
        } else {
            tv_name.setText(physicalId);
        }
        String robotType = DeviceUtils.getRobotType(subdomain);
        int product;
        switch (robotType) {
            case Constants.X785:
                product = R.drawable.n_x785;
                rl_voice.setVisibility(View.GONE);
                break;
            case Constants.X787:
                product = R.drawable.n_x787;
                rl_voice.setVisibility(View.GONE);
                break;
            case Constants.X800:
                if (SpUtils.getBoolean(this, MainActivity.KEY_DEV_WHITE)) {
                    product = R.drawable.n_x800_white;
                } else {
                    product = R.drawable.n_x800;
                }
                rl_mode.setVisibility(View.GONE);
                rl_update.setVisibility(View.VISIBLE);
                break;
            case Constants.X900:
                product = R.drawable.n_x900;
                rl_update.setVisibility(View.VISIBLE);
                rl_mode.setVisibility(View.GONE);
                break;
            case Constants.A8s:
                product = R.drawable.n_a8s;
                rl_mode.setVisibility(View.GONE);
                break;
            case Constants.A9s:
                if (Utils.isIlife()) {
                    product = R.drawable.n_x800;
                    rl_mode.setVisibility(View.GONE);
                } else {
                    product = R.drawable.n_a9s;
                    rl_mode.setVisibility(View.GONE);
                }
                rl_update.setVisibility(View.VISIBLE);
                break;
            case Constants.V85:
                product = R.drawable.n_v85;
                rl_record.setVisibility(View.GONE);
                rl_voice.setVisibility(View.GONE);
                break;
            case Constants.X910:
                product = R.drawable.n_x910;
                rl_update.setVisibility(View.VISIBLE);
                rl_mode.setVisibility(View.GONE);
                break;
            case Constants.V3x:
                product = R.drawable.n_v3x;
                rl_record.setVisibility(View.GONE);
                rl_voice.setVisibility(View.GONE);
                rl_mode.setVisibility(View.GONE);
                break;
            case Constants.V5x:
                product = R.drawable.n_v5x;
                rl_record.setVisibility(View.GONE);
                rl_voice.setVisibility(View.GONE);
                rl_mode.setVisibility(View.GONE);
                break;
            case Constants.A7:
                product = R.drawable.n_x787;
                rl_voice.setVisibility(View.GONE);
                rl_record.setVisibility(View.GONE);
                rl_water.setVisibility(View.GONE);
                break;
            case Constants.A9:
                if (BuildConfig.Area == AC.REGIONAL_SOUTHEAST_ASIA) {//日规A9有水量调节功能
                    rl_water.setVisibility(View.VISIBLE);
                    product = R.drawable.n_x800_white;
                } else {
                    rl_water.setVisibility(View.GONE);
                    product = R.drawable.n_x800;
                }
                rl_mode.setVisibility(View.GONE);
                rl_update.setVisibility(View.VISIBLE);
                break;
            default:
                product = R.drawable.n_x800;
                break;
        }
        if (BuildConfig.Area == AC.REGIONAL_CHINA || BuildConfig.BRAND.equals("ZACO")) {
            robotType = BuildConfig.BRAND + " " + robotType;
        }
        tv_type.setText(robotType);
        image_product.setImageResource(product);
    }

    public void setMode(int mode) {
        boolean isRandom = mode == MsgCodeUtils.STATUE_RANDOM;
        tv_mode.setText(isRandom ? getString(R.string.setting_aty_random_clean)
                : getString(R.string.setting_aty_nav_clean));
        image_plan.setSelected(!isRandom);
        image_random.setSelected(isRandom);
        tv_plan.setSelected(!isRandom);
        tv_random.setSelected(isRandom);
    }

    public void setStatus(int tag, int mopForce, boolean isMaxMode, boolean voiceOpen) {
        SpUtils.saveBoolean(context, physicalId + MapX9Presenter.KEY_IS_MAX, isMaxMode);
        SpUtils.saveInt(context, physicalId + MapX9Presenter.KEY_MOP_FORCE, mopForce);
        SpUtils.saveBoolean(context, physicalId + MapX9Presenter.KEY_VOICE_OPEN, voiceOpen);
        image_max.setSelected(isMaxMode);
        if (tag == 1) {
            image_voice.setSelected(voiceOpen);
        }
        clearAll();
        switch (mopForce) {
            case 100:
                tv_standard.setSelected(true);
                image_standard.setSelected(true);
                tv_water.setText(getString(R.string.setting_aty_standard));
                break;
            case 0:
                tv_soft.setSelected(true);
                image_soft.setSelected(true);
                tv_water.setText(getString(R.string.setting_aty_soft));
                break;
            case 1:
                tv_standard.setSelected(true);
                image_standard.setSelected(true);
                tv_water.setText(getString(R.string.setting_aty_standard));
                break;
            case 2:
                tv_strong.setSelected(true);
                image_strong.setSelected(true);
                tv_water.setText(getString(R.string.setting_aty_strong));
                break;
        }
    }

    public void clearAll() {
        tv_soft.setSelected(false);
        tv_standard.setSelected(false);
        tv_strong.setSelected(false);
        image_soft.setSelected(false);
        image_standard.setSelected(false);
        image_strong.setSelected(false);
    }

    @OnClick({R.id.tv_name, R.id.rl_water, R.id.rl_clock, R.id.rl_record, R.id.rl_consume, R.id.rl_mode, R.id.rl_find,
            R.id.rl_plan, R.id.rl_random, R.id.rl_facReset, R.id.rl_voice, R.id.rl_update})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_name:
                if (userId == ownerId) {
                    showRenameDialog();
                } else {
                    ToastUtils.showToast(context, getString(R.string.setting_aty_only_admin));
                }
                break;
            case R.id.rl_water:
                if (ll_water.getVisibility() == View.GONE) {
                    image_down_1.setRotation(-90);
                    ll_water.setVisibility(View.VISIBLE);
                } else {
                    image_down_1.setRotation(90);
                    ll_water.setVisibility(View.GONE);
                }
                break;
            case R.id.rl_clock:
                intent = new Intent(context, ClockingActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_record:
                intent = new Intent(context, HistoryActivity_x9.class);
                startActivity(intent);
                break;
            case R.id.rl_consume:
                intent = new Intent(context, ConsumesActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_mode:
                if (ll_mode.getVisibility() == View.GONE) {
                    ll_mode.setVisibility(View.VISIBLE);
                    image_down_2.setRotation(-90);
                } else {
                    image_down_2.setRotation(90);
                    ll_mode.setVisibility(View.GONE);
                }
                break;
            case R.id.rl_find:
                acDeviceMsg.setCode(MsgCodeUtils.WorkMode);
                acDeviceMsg.setContent(new byte[]{0x0B});
                sendToDeviceWithOption(acDeviceMsg, physicalId);
                rl_find.setClickable(false);
                imageView.setVisibility(View.VISIBLE);
                iv_find_robot.setVisibility(View.GONE);
                imageView.startAnimation(animation);
                break;
            case R.id.rl_plan:
                if (!image_plan.isSelected()) {
                    SpUtils.saveInt(context, physicalId + KEY_MODE, MsgCodeUtils.STATUE_PLANNING);
                    mode = MsgCodeUtils.STATUE_PLANNING;
                    setMode(mode);
                }
                break;
            case R.id.rl_random:
                if (!image_random.isSelected()) {
                    SpUtils.saveInt(context, physicalId + KEY_MODE, MsgCodeUtils.STATUE_RANDOM);
                    mode = MsgCodeUtils.STATUE_RANDOM;
                    setMode(mode);
                }
                break;
            case R.id.rl_facReset:
                if (userId == ownerId) {
                    showResetDialog();
                } else {
                    ToastUtils.showToast(context, getString(R.string.setting_aty_only_admin));
                }
                break;
            case R.id.rl_voice:
                acDeviceMsg.setCode(MsgCodeUtils.NoDisturbing);
                byte b = (byte) (voiceOpen ? 0x00 : 0x01);
                acDeviceMsg.setContent(new byte[]{b, 0x00});
                sendToDeviceWithOption(acDeviceMsg, physicalId);
                break;
            case R.id.rl_update:
                Intent intent = new Intent(context, OtaUpdateActivity.class);
                startActivity(intent);
                break;
        }
    }

    class MyListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            byte max;
            acDeviceMsg.setCode(MsgCodeUtils.CleanForce);
            switch (v.getId()) {
                case R.id.rl_suction:
                    if (canOperateSuction()) {
                        dialog.show();
                        max = (byte) (isMaxMode ? 0x00 : 0x01);
                        acDeviceMsg.setContent(new byte[]{max, (byte) mopForce});
                        sendToDeviceWithOption(acDeviceMsg, physicalId);
                    } else {
                        ToastUtils.showToast(getString(R.string.settiing_change_suction_tip));
                    }
                    break;
                case R.id.rl_soft:
                    dialog.show();
                    max = (byte) (isMaxMode ? 0x01 : 0x00);
                    acDeviceMsg.setContent(new byte[]{max, 0x00});
                    sendToDeviceWithOption(acDeviceMsg, physicalId);
                    break;
                case R.id.rl_standard:
                    dialog.show();
                    max = (byte) (isMaxMode ? 0x01 : 0x00);
                    acDeviceMsg.setContent(new byte[]{max, 0x01});
                    sendToDeviceWithOption(acDeviceMsg, physicalId);
                    break;
                case R.id.rl_strong:
                    dialog.show();
                    max = (byte) (isMaxMode ? 0x01 : 0x00);
                    acDeviceMsg.setContent(new byte[]{max, 0x02});
                    sendToDeviceWithOption(acDeviceMsg, physicalId);
                    break;
            }

        }
    }

    private boolean canOperateSuction() {
        if (curWorkMode == MsgCodeUtils.STATUE_POINT && (subdomain.equals(Constants.subdomain_x787) || subdomain.equals(Constants.subdomain_x785) || subdomain.equals(Constants.subdomain_a7) ||  subdomain.equals(Constants.subdomain_v5x) ||subdomain.equals(Constants.subdomain_V3x))) {
            return false;
        } else {
            return true;
        }
    }

    private void showRenameDialog() {
        name = tv_name.getText().toString();
        UniversalDialog universalDialog = new UniversalDialog();
        universalDialog.setDialogType(UniversalDialog.TYPE_NORMAL).setCanEdit(true).setTitle(name).setHintTip(Utils.getString(R.string.setting_aty_hit))
                .setOnRightButtonWithValueClck(value -> {
                    name = value;
                    if (TextUtils.isEmpty(name)) {
                        ToastUtils.showToast(context, getString(R.string.setting_aty_hit));
                        return;
                    }
                    int maxLength;
                    if (Utils.isChinaEnvironment()) {
                        maxLength = 12;
                    } else {
                        maxLength = 30;
                    }
                    if (name.length() > maxLength) {
                        ToastUtils.showToast(getResources().getString(R.string.name_max_length, maxLength + ""));
                        return;
                    }
                    universalDialog.dismiss();
                    if (SpUtils.getBoolean(this, MainActivity.KEY_DEV_WHITE)) {
                        name += Constants.ROBOT_WHITE_TAG;
                    }
                    DeviceUtils.renameDevice(deviceId, name, subdomain, new ReNameListener() {
                        @Override
                        public void onSuccess() {
                            ToastUtils.showToast(context, context.getString(R.string.bind_aty_reName_suc));
                            if (name.contains(Constants.ROBOT_WHITE_TAG)) {
                                name = name.replace(Constants.ROBOT_WHITE_TAG, "");
                            }
                            SpUtils.saveString(context, MainActivity.KEY_DEVNAME, name);
                            tv_name.setText(name);
                        }

                        @Override
                        public void onError(ACException e) {
                            ToastUtils.showToast(context, context.getString(R.string.bind_aty_reName_fail));
                        }
                    });
                }).show(getSupportFragmentManager(), "rename");
    }


    private void showResetDialog() {
        UniversalDialog universalDialog = new UniversalDialog();
        universalDialog.setDialogType(UniversalDialog.TYPE_NORMAL).setTitle(Utils.getString(R.string.setting_aty_confirm_reset))
                .setHintTip(Utils.getString(R.string.setting_aty_reset_hint)).setOnRightButtonClck(() -> {
            AlertDialogUtils.hidden(alterDialog);
            dialog.show();
            acDeviceMsg.setCode(MsgCodeUtils.FactoryReset);
            acDeviceMsg.setContent(new byte[]{0x01});
            sendToDeviceFactoryReset(acDeviceMsg, physicalId);
        }).show(getSupportFragmentManager(), "reset");
    }

    public void sendToDeviceWithOption(ACDeviceMsg deviceMsg, final String physicalDeviceId) {
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalDeviceId, deviceMsg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void success(ACDeviceMsg deviceMsg) {
                if (isDestroyed()) {
                    return;
                }
                DialogUtils.closeDialog(dialog);
                byte[] resp = deviceMsg.getContent();
                switch (deviceMsg.getCode()) {
                    case MsgCodeUtils.CleanForce:
                        isMaxMode = resp[0] == 0x01;
                        mopForce = resp[1];
                        setStatus(-1, mopForce, isMaxMode, voiceOpen);
                        break;
                    case MsgCodeUtils.WorkMode:
//                        if (subdomain.equals(Constants.subdomain_x800)){
//                            if (resp[0]==0x01){
//                                handler.sendEmptyMessageDelayed(TAG_FIND_DONE,10*1000);
//                            }
//                        } else {
//                            if (resp[0]==0x0B){
//                                handler.sendEmptyMessageDelayed(TAG_FIND_DONE,10*1000);
//                            }
//                        }
                        handler.sendEmptyMessageDelayed(TAG_FIND_DONE, 10 * 1000);
                        break;
                    case MsgCodeUtils.NoDisturbing:
                        voiceOpen = resp[0] == 0x01;
                        setStatus(1, mopForce, isMaxMode, voiceOpen);
                        break;
                }
            }

            @Override
            public void error(ACException e) {
                MyLogger.e(TAG, "sendToDeviceWithOption error " + e.toString());
                if (imageView != null && imageView.getVisibility() == View.VISIBLE) {
                    handler.sendEmptyMessage(TAG_FIND_DONE);
                }
                DialogUtils.closeDialog(dialog);
                ToastUtils.showErrorToast(context, e.getErrorCode());
            }
        });
    }

    public void sendToDeviceFactoryReset(ACDeviceMsg deviceMsg, final String physicalId) {
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, deviceMsg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void error(ACException e) {
                index = 0;
                listDevices();
            }

            @Override
            public void success(ACDeviceMsg acDeviceMsg) {
                if (!isDestroyed()) {
                    goToMain();
                }
            }
        });
    }

    public void listDevices() {
        index++;
        AC.bindMgr().listDevicesWithStatus(new PayloadCallback<List<ACUserDevice>>() {
            @Override
            public void success(List<ACUserDevice> acUserDevices) {
                if (acUserDevices != null) {
                    ArrayList<String> strList = new ArrayList<>();
                    for (int i = 0; i < acUserDevices.size(); i++) {
                        strList.add(acUserDevices.get(i).getPhysicalDeviceId());
                    }
                    if (strList.contains(physicalId)) {
                        ToastUtils.showToast(context, getString(R.string.login_aty_timeout));
                        DialogUtils.closeDialog(dialog);
                    } else {
                        goToMain();
                    }
                } else {
                    goToMain();
                }
            }

            @Override
            public void error(ACException e) {
                if (index < 2) {
                    listDevices();
                } else {
                    ToastUtils.showToast(context, getString(R.string.login_aty_timeout));
                    DialogUtils.closeDialog(dialog);
                }
            }
        });
    }

    public void goToMain() {
        DialogUtils.closeDialog(dialog);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        removeActivity();
    }
}
