package com.ilife.iliferobot_cn.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AlertDialog;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACDeviceDataMgr;
import com.accloud.service.ACDeviceMsg;
import com.accloud.service.ACException;
import com.accloud.service.ACUserDevice;
import com.google.gson.Gson;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.entity.PropertyInfo;
import com.ilife.iliferobot_cn.listener.ReNameListener;
import com.ilife.iliferobot_cn.utils.AlertDialogUtils;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.DeviceUtils;
import com.ilife.iliferobot_cn.utils.DialogUtils;
import com.ilife.iliferobot_cn.utils.MsgCodeUtils;
import com.ilife.iliferobot_cn.utils.MyLog;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.ToastUtils;
import com.ilife.iliferobot_cn.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by chengjiaping on 2018/8/16.
 */

public class SettingActivity_a7 extends BaseActivity implements View.OnClickListener {
    final String TAG = SettingActivity_a7.class.getSimpleName();
    final int TAG_FIND_DONE = 0x01;
    public static final String KEY_MODE = "KEY_MODE";
    int mopForce, mode, index;
    boolean isMaxMode, voiceOpen;
    Context context;
    Intent intent;
    long deviceId, userId, ownerId;
    String devName, subdomain, physicalId, name;
    ImageView image_back, image_soft, image_standard, image_strong, image_max, image_voice,
            image_plan, image_random, image_product;
    TextView tv_name, tv_type, tv_soft, tv_standard, tv_strong, tv_water, tv_plan,
            tv_random, tv_mode;
    LayoutInflater inflater;
    AlertDialog alterDialog;
    Dialog dialog;
    Animation animation;
    RelativeLayout rl_water, rl_clock, rl_record, rl_consume,
            rl_mode, rl_suction, rl_find, rl_soft, rl_standard, rl_strong,
            rl_plan, rl_random, rl_facReset, rl_voice;
    LinearLayout ll_water, ll_mode;
    ACDeviceMsg acDeviceMsg;
    ImageView imageView;
    ReNameListener listener;
    ACDeviceDataMgr.PropertyReceiver propReceiver;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TAG_FIND_DONE:
                    rl_find.setClickable(true);
                    imageView.setVisibility(View.GONE);
                    imageView.clearAnimation();
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_setting_a7;
    }

    @Override
    protected void onResume() {
        super.onResume();
        propReceiver = new ACDeviceDataMgr.PropertyReceiver() {
            @Override
            public void onPropertyReceive(String s, long l, String s1) {
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
            }
        };
        registerMsg();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (propReceiver != null) {
            propReceiver = null;
        }
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
        imageView = (ImageView) findViewById(R.id.imageView);
        dialog = DialogUtils.createLoadingDialog_(context);
        inflater = LayoutInflater.from(context);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_type = (TextView) findViewById(R.id.tv_type);
        tv_water = (TextView) findViewById(R.id.tv_water);
        tv_soft = (TextView) findViewById(R.id.tv_soft);
        tv_standard = (TextView) findViewById(R.id.tv_standard);
        tv_strong = (TextView) findViewById(R.id.tv_strong);
        tv_random = (TextView) findViewById(R.id.tv_random);
        tv_mode = (TextView) findViewById(R.id.tv_mode);
        tv_plan = (TextView) findViewById(R.id.tv_plan);
        rl_plan = (RelativeLayout) findViewById(R.id.rl_plan);
        rl_random = (RelativeLayout) findViewById(R.id.rl_random);
        rl_voice = findViewById(R.id.rl_voice);
        rl_facReset = (RelativeLayout) findViewById(R.id.rl_facReset);
        rl_water = (RelativeLayout) findViewById(R.id.rl_water);
        rl_clock = (RelativeLayout) findViewById(R.id.rl_clock);
        rl_record = (RelativeLayout) findViewById(R.id.rl_record);
        rl_consume = (RelativeLayout) findViewById(R.id.rl_consume);
        rl_mode = (RelativeLayout) findViewById(R.id.rl_mode);
        rl_suction = (RelativeLayout) findViewById(R.id.rl_suction);
        rl_find = (RelativeLayout) findViewById(R.id.rl_find);
        rl_soft = (RelativeLayout) findViewById(R.id.rl_soft);
        rl_standard = (RelativeLayout) findViewById(R.id.rl_standard);
        rl_strong = (RelativeLayout) findViewById(R.id.rl_strong);
        ll_mode = (LinearLayout) findViewById(R.id.ll_mode);
        ll_water = (LinearLayout) findViewById(R.id.ll_water);
        image_plan = (ImageView) findViewById(R.id.image_plan);
        image_random = (ImageView) findViewById(R.id.image_random);
        image_back = (ImageView) findViewById(R.id.image_back);
        image_soft = (ImageView) findViewById(R.id.image_soft);
        image_standard = (ImageView) findViewById(R.id.image_standard);
        image_strong = (ImageView) findViewById(R.id.image_strong);
        image_product = (ImageView) findViewById(R.id.image_product);
        image_max = (ImageView) findViewById(R.id.image_max);
        image_voice = findViewById(R.id.image_voice);
        tv_name.setOnClickListener(this);
        rl_water.setOnClickListener(this);
        rl_clock.setOnClickListener(this);
        rl_record.setOnClickListener(this);
        rl_consume.setOnClickListener(this);
        rl_mode.setOnClickListener(this);
        rl_plan.setOnClickListener(this);
        rl_random.setOnClickListener(this);
        rl_voice.setOnClickListener(this);
        rl_facReset.setOnClickListener(this);
        rl_find.setOnClickListener(this);
        ll_water.setOnClickListener(this);
        image_back.setOnClickListener(this);
        rl_suction.setOnClickListener(new MyListener());
        rl_soft.setOnClickListener(new MyListener());
        rl_standard.setOnClickListener(new MyListener());
        rl_strong.setOnClickListener(new MyListener());
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
        mopForce = SpUtils.getInt(context, physicalId + MapActivity_X8_.KEY_MOP_FORCE);
        isMaxMode = SpUtils.getBoolean(context, physicalId + MapActivity_X8_.KEY_IS_MAX);
        voiceOpen = SpUtils.getBoolean(context, physicalId + MapActivity_X8_.KEY_VOICE_OPEN);
        setMode(mode);
        setStatus(1, mopForce, isMaxMode, voiceOpen);
        if (!TextUtils.isEmpty(devName)) {
            tv_name.setText(devName);
        } else {
            tv_name.setText(physicalId);
        }
        if (subdomain.equals(Constants.subdomain_x785)) {
            tv_type.setText(getString(R.string.setting_aty_type_x785));
            image_product.setImageResource(R.drawable.n_x785_1);
        } else if (subdomain.equals(Constants.subdomain_a7)) {
            tv_type.setText(getString(R.string.setting_aty_type_x787));
            image_product.setImageResource(R.drawable.n_x787_1);
        } else {
            rl_mode.setVisibility(View.GONE);
            tv_type.setText(getString(R.string.setting_aty_type_x800));
            image_product.setImageResource(R.drawable.n_x800_1);
        }

        listener = new ReNameListener() {
            @Override
            public void onSuccess() {
                ToastUtils.showToast(context, context.getString(R.string.bind_aty_reName_suc));
                SpUtils.saveString(context, "devName", name);
                tv_name.setText(name);
            }

            @Override
            public void onError(ACException e) {
                ToastUtils.showToast(context, context.getString(R.string.bind_aty_reName_fail));
            }
        };
    }

    public void setMode(int mode) {
        boolean isRandom = mode == 0x03 || mode == 100;
        tv_mode.setText(isRandom ? getString(R.string.map_aty_random)
                : getString(R.string.map_aty_plan_mode));
        image_plan.setSelected(!isRandom);
        image_random.setSelected(isRandom);
        tv_plan.setSelected(!isRandom);
        tv_random.setSelected(isRandom);
    }

    public void setStatus(int tag, int mopForce, boolean isMaxMode, boolean voiceOpen) {
        SpUtils.saveBoolean(context, physicalId + MapActivity_X7_.KEY_IS_MAX, isMaxMode);
        SpUtils.saveInt(context, physicalId + MapActivity_X7_.KEY_MOP_FORCE, mopForce);
        SpUtils.saveBoolean(context, physicalId + MapActivity_X7_.KEY_VOICE_OPEN, voiceOpen);
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

    @Override
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
                    ll_water.setVisibility(View.VISIBLE);
                } else {
                    ll_water.setVisibility(View.GONE);
                }
                break;
            case R.id.image_back:
                finish();
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
                } else {
                    ll_mode.setVisibility(View.GONE);
                }
                break;
            case R.id.rl_find:
                acDeviceMsg.setCode(MsgCodeUtils.WorkMode);
                acDeviceMsg.setContent(new byte[]{0x0B});
                sendToDeviceWithOption(acDeviceMsg, physicalId);
                rl_find.setClickable(false);
                imageView.setVisibility(View.VISIBLE);
                imageView.startAnimation(animation);
                break;
            case R.id.rl_plan:
                if (!image_plan.isSelected()) {
                    SpUtils.saveInt(context, physicalId + KEY_MODE, 0x06);
                    mode = 0x06;
                    setMode(mode);
                }
                break;
            case R.id.rl_random:
                if (!image_random.isSelected()) {
                    SpUtils.saveInt(context, physicalId + KEY_MODE, 0x03);
                    mode = 0x03;
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
        }
    }

    class MyListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            byte max;
            dialog.show();
            acDeviceMsg.setCode(MsgCodeUtils.CleanForce);
            switch (v.getId()) {
                case R.id.rl_suction:
                    max = (byte) (isMaxMode ? 0x00 : 0x01);
                    acDeviceMsg.setContent(new byte[]{max, (byte) mopForce});
                    break;
                case R.id.rl_soft:
                    max = (byte) (isMaxMode ? 0x01 : 0x00);
                    acDeviceMsg.setContent(new byte[]{max, 0x00});
                    break;
                case R.id.rl_standard:
                    max = (byte) (isMaxMode ? 0x01 : 0x00);
                    acDeviceMsg.setContent(new byte[]{max, 0x01});
                    break;
                case R.id.rl_strong:
                    max = (byte) (isMaxMode ? 0x01 : 0x00);
                    acDeviceMsg.setContent(new byte[]{max, 0x02});
                    break;
            }
            sendToDeviceWithOption(acDeviceMsg, physicalId);
        }
    }

    private void showRenameDialog() {
        View v = inflater.inflate(R.layout.layout_name_dialog, null);
        final EditText et_name = (EditText) v.findViewById(R.id.et_name);
        UserUtils.setInputFilter(et_name);
        name = tv_name.getText().toString();
        et_name.setText(name);
        et_name.setSelection(name.length());
        v.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogUtils.hidden(alterDialog);
            }
        });
        v.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = et_name.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    ToastUtils.showToast(context, getString(R.string.setting_aty_hit));
                    return;
                }
                if (!name.equals(devName)) {
                    AlertDialogUtils.hidden(alterDialog);
                    DeviceUtils.renameDevice(deviceId, name, subdomain, listener);
                }
            }
        });
        int width = (int) getResources().getDimension(R.dimen.dp_300);
        int height = (int) getResources().getDimension(R.dimen.dp_140);
        alterDialog = AlertDialogUtils.showDialog(context, v, width, height);
    }


    private void showResetDialog() {
        View v = inflater.inflate(R.layout.layout_reset_dialog, null);
        v.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogUtils.hidden(alterDialog);
            }
        });
        v.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogUtils.hidden(alterDialog);
                dialog.show();
                acDeviceMsg.setCode(MsgCodeUtils.FactoryReset);
                acDeviceMsg.setContent(new byte[]{0x01});
                sendToDeviceFactoryReset(acDeviceMsg, physicalId);
            }
        });
        int width = (int) getResources().getDimension(R.dimen.dp_300);
        int height = (int) getResources().getDimension(R.dimen.dp_140);
        alterDialog = AlertDialogUtils.showDialog(context, v, width, height);
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
                MyLog.e(TAG, "sendToDeviceWithOption error " + e.toString());
                if (imageView.getVisibility() == View.VISIBLE) {
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
        finish();
    }
}