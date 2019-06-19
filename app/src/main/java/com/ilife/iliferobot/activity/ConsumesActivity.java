package com.ilife.iliferobot.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACDeviceMsg;
import com.accloud.service.ACException;
import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.able.MsgCodeUtils;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.utils.SpUtils;
import com.ilife.iliferobot.utils.Utils;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * Created by chenjiaping on 2017/7/28.
 */

public class ConsumesActivity extends BackBaseActivity implements View.OnLongClickListener {
    final String TAG = ConsumesActivity.class.getSimpleName();
    Context context;
    String physicalId;
    String subdomain;
    LayoutInflater inflater;
    ProgressBar pb_side, pb_roll, pb_filter;
    TextView tv_percent_side, tv_percent_roll, tv_percent_filter;
    LinearLayout rl_side;
    LinearLayout rl_roll;
    LinearLayout rl_filter;
    ACDeviceMsg acDeviceMsg;
    ArrayList<Integer> ids;
    byte[] bytes;
    int index;
    @BindView(R.id.tv_top_title)
    TextView tv_top_title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_consumes;
    }

    public void initView() {
        context = this;
        inflater = LayoutInflater.from(context);
        pb_side = (ProgressBar) findViewById(R.id.pb_side);
        pb_roll = (ProgressBar) findViewById(R.id.pb_roll);
        pb_filter = (ProgressBar) findViewById(R.id.pb_filter);

        rl_side = (LinearLayout) findViewById(R.id.rl_side);
        rl_roll = (LinearLayout) findViewById(R.id.rl_roll);
        rl_filter = (LinearLayout) findViewById(R.id.rl_filter);

        tv_percent_side = (TextView) findViewById(R.id.tv_percent_side);
        tv_percent_roll = (TextView) findViewById(R.id.tv_percent_roll);
        tv_percent_filter = (TextView) findViewById(R.id.tv_percent_filter);
        tv_top_title.setText(R.string.setting_aty_consume_detail);
        rl_side.setOnLongClickListener(this);
        rl_roll.setOnLongClickListener(this);
        rl_filter.setOnLongClickListener(this);
    }

    public void initData() {
        ids = new ArrayList<>();
        ids.add(R.id.rl_side);
        ids.add(R.id.rl_roll);
        ids.add(R.id.rl_filter);
        physicalId = SpUtils.getSpString(context, MainActivity.KEY_PHYCIALID);
        subdomain = SpUtils.getSpString(context, MainActivity.KEY_SUBDOMAIN);
        acDeviceMsg = new ACDeviceMsg();
        acDeviceMsg.setCode(MsgCodeUtils.MatConditions);
        acDeviceMsg.setContent(new byte[]{0x00});
        sendToDeviceWithOption(acDeviceMsg, physicalId);
    }

    public void showResetDialog(int tag) {
        String title = "";
        String hint = "";
        switch (tag) {
            case R.id.rl_side:
                title = Utils.getString(R.string.consume_aty_resetSide);
                hint = Utils.getString(R.string.consume_aty_resetSide_over);
                break;
            case R.id.rl_roll:
                title = Utils.getString(R.string.consume_aty_resetRoll);
                hint = Utils.getString(R.string.consume_aty_resetRoll_over);
                break;
            case R.id.rl_filter:
                title = Utils.getString(R.string.consume_aty_resetFilter);
                hint = Utils.getString(R.string.consume_aty_resetFilter_over);
                break;
        }
        UniversalDialog universalDialog = new UniversalDialog();
        universalDialog.setDialogType(UniversalDialog.TYPE_NORMAL).setTitle(title).setHintTip(hint).
                setOnRightButtonClck(() -> {
                    bytes = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00};
                    index = ids.indexOf(tag);
                    bytes[index] = 0x01;
                    acDeviceMsg.setContent(bytes);
                    acDeviceMsg.setCode(MsgCodeUtils.RestLifeTime);
                    sendToDeviceWithOption(acDeviceMsg, physicalId);
                }).show(getSupportFragmentManager(), "" + tag);
    }

    public void sendToDeviceWithOption(ACDeviceMsg deviceMsg, String physicalDeviceId) {
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalDeviceId, deviceMsg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void success(ACDeviceMsg deviceMsg) {
                byte[] resp = deviceMsg.getContent();
                switch (deviceMsg.getCode()) {
                    //下发重置生命周期
                    case MsgCodeUtils.RestLifeTime:
                        ToastUtils.showToast(context, getString(R.string.consume_aty_reset_suc));
                        switch (index) {
                            case 0:
                                pb_side.setProgress(100);
                                tv_percent_side.setText(100 + "%");
                                break;
                            case 1:
                                pb_roll.setProgress(100);
                                tv_percent_roll.setText(100 + "%");
                                break;
                            case 2:
                                pb_filter.setProgress(100);
                                tv_percent_filter.setText(100 + "%");
                                break;
                        }
//
                        break;
                    //查询耗材情况
                    case MsgCodeUtils.MatConditions:
                        pb_side.setProgress(resp[2]);
                        pb_roll.setProgress(resp[3]);
                        pb_filter.setProgress(resp[4]);

                        tv_percent_side.setText(String.valueOf(resp[2]) + "%");
                        tv_percent_roll.setText(String.valueOf(resp[3]) + "%");
                        tv_percent_filter.setText(String.valueOf(resp[4]) + "%");
                        break;
                }
            }

            @Override
            public void error(ACException e) {
                ToastUtils.showErrorToast(context, e.getErrorCode());
            }
        });
    }

    @Override
    public boolean onLongClick(View v) {
        showResetDialog(v.getId());
        return false;
    }


}
