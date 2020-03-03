package com.ilife.iliferobot.activity;

import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACDeviceMsg;
import com.accloud.service.ACException;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.able.MsgCodeUtils;
import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.presenter.MapX9Presenter;
import com.ilife.iliferobot.utils.SpUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class VoiceVolumeActivity extends BackBaseActivity {
    private String physicalId;
    @BindView(R.id.bt_save_volume)
    TextView tv_save_volume;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.sk_voice_volume)
    SeekBar sk_voice_volume;
    @BindView(R.id.tv_voice_volume)
    TextView tv_voice_volume;
    @BindView(R.id.iv_volume_switch)
    ImageView iv_volume_switch;
    private ACDeviceMsg acDeviceMsg;
    private String subDomain;

    @Override
    public int getLayoutId() {
        return R.layout.activity_voice_volume;
    }

    @Override
    public void initView() {
        tv_title.setText(R.string.setting_aty_voice_mode);
        sk_voice_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_voice_volume.setText(progress + "%");
                tv_save_volume.setSelected(true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void initData() {
        super.initData();
        acDeviceMsg = new ACDeviceMsg();
        subDomain = SpUtils.getSpString(this, MainActivity.KEY_SUBDOMAIN);
        physicalId = SpUtils.getSpString(this, MainActivity.KEY_PHYCIALID);
        int voiceByte = SpUtils.getInt(this, physicalId + MapX9Presenter.KEY_VOICE_OPEN);
        sk_voice_volume.setProgress(parseVoiceByte(voiceByte));
        tv_voice_volume.setText(parseVoiceByte(voiceByte) + "%");
        boolean isOpen=voiceByte % 2 == 1;
        iv_volume_switch.setSelected(isOpen);
        sk_voice_volume.setEnabled(isOpen);
        tv_voice_volume.setEnabled(isOpen);
    }

    @OnClick({R.id.bt_save_volume, R.id.iv_volume_switch})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_save_volume:
                showLoadingDialog();
                acDeviceMsg.setCode(MsgCodeUtils.NoDisturbing);
                byte b = (byte) transformVolume();
                acDeviceMsg.setContent(new byte[]{b, 0x00});
                AC.bindMgr().sendToDeviceWithOption(subDomain, physicalId, acDeviceMsg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
                    @Override
                    public void success(ACDeviceMsg acDeviceMsg) {
                        int voiceByte = acDeviceMsg.getContent()[0];
                        SpUtils.saveInt(VoiceVolumeActivity.this, physicalId + MapX9Presenter.KEY_VOICE_OPEN, voiceByte);
                        hideLoadingDialog();
                        if (isDestroyed()) {
                            return;
                        }
                        finish();
                    }

                    @Override
                    public void error(ACException e) {
                        hideLoadingDialog();
                    }
                });
                break;
            case R.id.iv_volume_switch:
                boolean isOpen = !iv_volume_switch.isSelected();
                iv_volume_switch.setSelected(isOpen);
                sk_voice_volume.setEnabled(isOpen);
                tv_voice_volume.setEnabled(isOpen);
                break;


        }
    }

    private int parseVoiceByte(int voiceByte) {
        int volume = (voiceByte & 0xff) >> 1;
        return volume;
    }

    private int transformVolume() {
        int curVolume = sk_voice_volume.getProgress();
        return ((curVolume & 0xff) << 1) + (iv_volume_switch.isSelected() ? 1 : 0);
    }

}
