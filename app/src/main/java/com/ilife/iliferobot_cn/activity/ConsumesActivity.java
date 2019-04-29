package com.ilife.iliferobot_cn.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACDeviceMsg;
import com.accloud.service.ACException;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BackBaseActivity;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.utils.AlertDialogUtils;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.MsgCodeUtils;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.ToastUtils;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * Created by chenjiaping on 2017/7/28.
 */

public class ConsumesActivity extends BackBaseActivity implements View.OnLongClickListener{
    final String TAG = ConsumesActivity.class.getSimpleName();
    Context context;
    String physicalId;
    String subdomain;
    TextView tv_title, tv_cancel, tv_confirm;
    LayoutInflater inflater;
    ProgressBar pb_side, pb_roll, pb_filter;
    TextView tv_percent_side, tv_percent_roll, tv_percent_filter;
    LinearLayout rl_side;
    LinearLayout rl_roll;
    LinearLayout rl_filter;
    ACDeviceMsg acDeviceMsg;
    androidx.appcompat.app.AlertDialog alertDialog;
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
//        bytes = new byte[]{0x00,0x00,0x00,0x00,0x00};
        physicalId = SpUtils.getSpString(context, MainActivity.KEY_PHYCIALID);
        subdomain = SpUtils.getSpString(context, MainActivity.KEY_SUBDOMAIN);
        acDeviceMsg = new ACDeviceMsg();
        acDeviceMsg.setCode(MsgCodeUtils.MatConditions);
        acDeviceMsg.setContent(new byte[]{0x00});
        sendToDeviceWithOption(acDeviceMsg, physicalId);
    }

    public void showResetDialog(int tag) {
        if (alertDialog == null) {
            View contentView = inflater.inflate(R.layout.layout_reset_consume, null);
            tv_title = contentView.findViewById(R.id.tv_title);
            tv_cancel = contentView.findViewById(R.id.tv_cancel);
            tv_confirm = contentView.findViewById(R.id.tv_confirm);
            int width = (int) getResources().getDimension(R.dimen.dp_300);
            int height = (int) getResources().getDimension(R.dimen.dp_140);
            alertDialog = AlertDialogUtils.showDialog(context, contentView, width, height);
        } else {
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
        tv_cancel.setOnClickListener(new MyListener(tag));
        tv_confirm.setOnClickListener(new MyListener(tag));
        switch (tag) {
            case R.id.rl_side:
                tv_title.setText(getString(R.string.consume_aty_resetSide));
                break;
            case R.id.rl_roll:
                tv_title.setText(getString(R.string.consume_aty_resetRoll));
                break;
            case R.id.rl_filter:
                tv_title.setText(getString(R.string.consume_aty_resetFilter));
                break;
        }
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
//                        if (resp[0]!=0){
//                            pb_side.setProgress(100);
//                            tv_percent_side.setText(100+"%");
//                        } else if (resp[1]!=0){
//                            pb_roll.setProgress(100);
//                            tv_percent_roll.setText(100+"%");
//                        } else if (resp[2]!=0){
//                            pb_filter.setProgress(100);
//                            tv_percent_filter.setText(100+"%");
//                        }
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


    class MyListener implements View.OnClickListener {
        int tag;

        public MyListener(int tag) {
            this.tag = tag;
        }

        @Override
        public void onClick(View v) {
            acDeviceMsg.setCode(MsgCodeUtils.RestLifeTime);
            switch (v.getId()) {
                case R.id.tv_cancel:
                    alertDialog.dismiss();
                    break;
                case R.id.tv_confirm:
                    bytes = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00};
                    alertDialog.dismiss();
                    index = ids.indexOf(tag);
                    bytes[index] = 0x01;
                    acDeviceMsg.setContent(bytes);
                    sendToDeviceWithOption(acDeviceMsg, physicalId);
                    break;
            }
        }
    }
}
