package com.ilife.iliferobot_cn.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACDeviceMsg;
import com.accloud.service.ACException;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.adapter.ClockAdapter;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.entity.NewClockInfo;
import com.ilife.iliferobot_cn.utils.AlertDialogUtils;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.DialogUtils;
import com.ilife.iliferobot_cn.utils.MsgCodeUtils;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.TimePickerUIUtil;
import com.ilife.iliferobot_cn.utils.TimeUtil;
import com.ilife.iliferobot_cn.utils.ToastUtils;

import java.util.ArrayList;


/**
 * Created by chenjiaping on 2017/7/25.
 */

public class ClockingActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {
    final String TAG = ClockingActivity.class.getSimpleName();
    final String UNDER_LINE = "_";
    final int TAG_REFRESH_OVER = 0x01;
    Context context;
    Dialog dialog;
    TextView tv_confirm;
    TextView tv_cancel;
    ImageView image_back;
    RecyclerView recyclerView;
    ClockAdapter adapter;
    AlertDialog alertDialog;
    LayoutInflater inflater;
    TimePicker timePicker;
    String subdomain;
    String physicalId;
    ACDeviceMsg acDeviceMsg;
    SwipeRefreshLayout refreshLayout;
    ArrayList<NewClockInfo> clockInfos;
    String[] weeks;
    byte[] bytes;
    String last, strTimeFormat;
//    ContentResolver cv;


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TAG_REFRESH_OVER:
                    if (refreshLayout != null && refreshLayout.isRefreshing()) {
                        refreshLayout.setRefreshing(false);
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);
        initView();
        intData();
        getClockInfo();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus){
//            strTimeFormat = android.provider.Settings.System.getString(cv,
//                    android.provider.Settings.System.TIME_12_24);
//        }
    }

    private void initView() {
        context = this;
        clockInfos = new ArrayList<>();
        dialog = DialogUtils.createLoadingDialog_(context);
        inflater = LayoutInflater.from(context);
        adapter = new ClockAdapter(context, clockInfos);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        refreshLayout.setColorSchemeColors(getResources().
                getColor(android.R.color.holo_blue_bright));
        refreshLayout.setOnRefreshListener(this);
        image_back = (ImageView) findViewById(R.id.image_back);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
        image_back.setOnClickListener(this);
        adapter.setListener(new ClockAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                showSetClockDialog(position);
            }

            @Override
            public void onSwitchClick(int position) {
                int startIndex_;
                if (position != 0) {
                    startIndex_ = (position - 1) * 5;
                } else {
                    startIndex_ = 30;
                }
                if (bytes[startIndex_ + 1] == 0) {
                    bytes[startIndex_ + 1] = 1;
                } else {
                    bytes[startIndex_ + 1] = 0;
                }
                acDeviceMsg.setContent(bytes);
                acDeviceMsg.setCode(MsgCodeUtils.Appointment);
                sendToDeviceWithOption(acDeviceMsg, physicalId, 1);
            }
        });
    }

    private void intData() {
//        cv = this.getContentResolver();
        bytes = new byte[50];
        acDeviceMsg = new ACDeviceMsg();
        subdomain = SpUtils.getSpString(context, MainActivity.KEY_SUBDOMAIN);
        physicalId = SpUtils.getSpString(context, MainActivity.KEY_PHYCIALID);
        weeks = getResources().getStringArray(R.array.array_week);
        for (int i = 0; i < 7; i++) {
            NewClockInfo info = new NewClockInfo();
            info.setWeek(weeks[i]);
            info.setHour((byte) 0);
            info.setMinute((byte) 0);
            info.setOpen((byte) 0);
            clockInfos.add(info);
        }
        adapter.notifyDataSetChanged();
    }

    public void showSetClockDialog(int position) {
        if (alertDialog == null) {
            View contentView = inflater.inflate(R.layout.layout_timepick_dialog, null);
            timePicker = (TimePicker) contentView.findViewById(R.id.timePicker);
//            timePicker.setIs24HourView(true);
            TimePickerUIUtil.set_timepicker_text_colour(timePicker, context);
            tv_confirm = (TextView) contentView.findViewById(R.id.tv_confirm);
            tv_cancel = (TextView) contentView.findViewById(R.id.tv_cancel);
            int width = (int) getResources().getDimension(R.dimen.dp_300);
            int height = (int) getResources().getDimension(R.dimen.dp_300);
            alertDialog = AlertDialogUtils.showDialog(context, contentView, width, height);
        } else {
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
        tv_cancel.setOnClickListener(new MyListener(position));
        tv_confirm.setOnClickListener(new MyListener(position));
        byte hour = clockInfos.get(position).getHour();
        byte minute = clockInfos.get(position).getMinute();
        last = String.valueOf(hour) + UNDER_LINE + String.valueOf(minute);
//        if (strTimeFormat.equals("24")){
//            timePicker.setIs24HourView(true);
//        } else {
//            timePicker.setIs24HourView(false);
//        }
        if (DateFormat.is24HourFormat(context)) {
            timePicker.setIs24HourView(true);
        } else {
            timePicker.setIs24HourView(false);
        }
        timePicker.setCurrentHour((int) hour);
        timePicker.setCurrentMinute((int) minute);
    }

    @Override
    public void onRefresh() {
        getClockInfo();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back:
                finish();
                break;
        }
    }

    class MyListener implements View.OnClickListener {
        int position;

        public MyListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            DialogUtils.hideDialog(alertDialog);
            switch (v.getId()) {
                case R.id.tv_cancel:

                    break;
                case R.id.tv_confirm:
                    int hour = timePicker.getCurrentHour();
                    int minute = timePicker.getCurrentMinute();
                    String current = String.valueOf(hour) + UNDER_LINE + String.valueOf(minute);
                    if (!current.equals(last)) {
                        int startIndex;
                        if (position != 0) {
                            startIndex = (position - 1) * 5;
                        } else {
                            startIndex = 30;
                        }
                        bytes[startIndex + 1] = 1;
                        bytes[startIndex + 2] = TimeUtil.getWeeks(position);
                        bytes[startIndex + 3] = (byte) hour;
                        bytes[startIndex + 4] = (byte) minute;
                        acDeviceMsg.setContent(bytes);
                        acDeviceMsg.setCode(MsgCodeUtils.Appointment);
                        sendToDeviceWithOption(acDeviceMsg, physicalId, 1);
                    }
                    break;
            }

        }
    }


    public void getClockInfo() {
        acDeviceMsg.setCode(MsgCodeUtils.ClockInfos);
        acDeviceMsg.setContent(new byte[]{0x00});
        sendToDeviceWithOption(acDeviceMsg, physicalId, 0);
    }

    public void sendToDeviceWithOption(ACDeviceMsg deviceMsg, String physicalDeviceId, int tag) {
        if (tag == 1) {
            dialog.show();
        }
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalDeviceId, deviceMsg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void success(ACDeviceMsg acDeviceMsg) {
                byte[] resp = acDeviceMsg.getContent();
                bytes = resp;
                for (int i = 0; i < 35; i++) {
                    if (i % 5 == 0) {
                        int index = 0;
                        if (i != 30) {
                            index = i / 5 + 1;
                        } else if (i == 30) {
                            index = 0;
                        }
                        clockInfos.get(index).setOpen(resp[i + 1]);
                        clockInfos.get(index).setHour(resp[i + 3]);
                        clockInfos.get(index).setMinute(resp[i + 4]);
                    }
                }
                DialogUtils.closeDialog(dialog);
                adapter.notifyDataSetChanged();
                handler.sendEmptyMessageDelayed(TAG_REFRESH_OVER, 1000);
            }

            @Override
            public void error(ACException e) {
                DialogUtils.closeDialog(dialog);
                ToastUtils.showErrorToast(context, e.getErrorCode());
                handler.sendEmptyMessageDelayed(TAG_REFRESH_OVER, 1000);
            }
        });
    }
}
