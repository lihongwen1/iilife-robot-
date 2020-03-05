package com.ilife.iliferobot.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACDeviceMsg;
import com.accloud.service.ACException;
import com.badoo.mobile.util.WeakHandler;
import com.ilife.iliferobot.able.DeviceUtils;
import com.ilife.iliferobot.app.MyApplication;
import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.able.MsgCodeUtils;
import com.ilife.iliferobot.fragment.ScheduleTipDialogFragment;
import com.ilife.iliferobot.utils.MyLogger;
import com.ilife.iliferobot.utils.TimePickerUIUtil;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.adapter.ClockAdapter;
import com.ilife.iliferobot.entity.NewClockInfo;
import com.ilife.iliferobot.utils.AlertDialogUtils;
import com.ilife.iliferobot.utils.DialogUtils;
import com.ilife.iliferobot.utils.SpUtils;
import com.ilife.iliferobot.utils.TimeUtil;
import com.ilife.iliferobot.utils.Utils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;

import java.util.ArrayList;

import butterknife.BindView;


/**
 * Created by chenjiaping on 2017/7/25.
 */

public class ClockingActivity extends BackBaseActivity {
    final String TAG = ClockingActivity.class.getSimpleName();
    final String UNDER_LINE = "_";
    public static final String KEY_DEVICE_TYPE="key_device_type";
    final int TAG_REFRESH_OVER = 0x01;
    Context context;
    Dialog dialog;
    TextView tv_confirm;
    TextView tv_cancel;
    RecyclerView recyclerView;
    ClockAdapter adapter;
    AlertDialog alertDialog;
    LayoutInflater inflater;
    TimePicker timePicker;
    String subdomain;
    String physicalId;
    ACDeviceMsg acDeviceMsg;
    SmartRefreshLayout refreshLayout;
    ArrayList<NewClockInfo> clockInfos;
    String[] weeks;
    byte[] bytes;
    String last, strTimeFormat;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    private ScheduleTipDialogFragment workTimeDialog;
    private int selectMinte, selectHour, selecPostion;
    private boolean isEveryDay;
    WeakHandler handler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case TAG_REFRESH_OVER:
                    if (refreshLayout != null) {
                        refreshLayout.finishRefresh();
                    }
                    break;
            }
            return false;
        }
    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adjustTime();
        getClockInfo();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_clock;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    public void initView() {
        context = this;
        tv_title.setText(R.string.clock_aty_appoint);
        clockInfos = new ArrayList<>();
        dialog = DialogUtils.createLoadingDialog_(context);
        inflater = LayoutInflater.from(context);
        refreshLayout = (SmartRefreshLayout) findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(refreshLayout -> getClockInfo());
        refreshLayout.setRefreshHeader(new ClassicsHeader(this));
        adapter = new ClockAdapter(R.layout.layout_clock_item, clockInfos);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            showSetClockDialog(position);
        });
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.image_status) {
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
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void initData() {
        bytes = new byte[50];
        acDeviceMsg = new ACDeviceMsg();
        subdomain = SpUtils.getSpString(context, MainActivity.KEY_SUBDOMAIN);
        physicalId = SpUtils.getSpString(context, MainActivity.KEY_PHYCIALID);
        int deviceType=SpUtils.getInt(MyApplication.getInstance(),KEY_DEVICE_TYPE);
        isEveryDay = DeviceUtils.getRobotType(subdomain).equals(Constants.V5x)&&deviceType!=0x31;//V5x是单条预约，每天触发,everyday
        if (adapter!=null){
            adapter.setEveryDaya(isEveryDay);
        }
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
            alertDialog = AlertDialogUtils.showDialogNoCancel(context, contentView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
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

    class MyListener implements View.OnClickListener {
        private int position;

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
                    selectHour = timePicker.getCurrentHour();
                    selectMinte = timePicker.getCurrentMinute();
                    selecPostion = position;
                    if (!isNoAtNight()) {
                        finishShedule();
                    } else if ((selectHour > 5 && selectHour < 20) || (selectHour == 5 && selectMinte > 0)) {//可用时间段
                        finishShedule();
                    } else {//不可用时间
                        if (workTimeDialog == null) {
                            workTimeDialog = new ScheduleTipDialogFragment();
                            workTimeDialog.setOnClickListener(new ScheduleTipDialogFragment.OnClickListener() {
                                @Override
                                public void onCancelClick() {
                                    workTimeDialog.dismiss();
                                }

                                @Override
                                public void onCommitClick() {
                                    workTimeDialog.dismiss();
                                    finishShedule();
                                }
                            });
                        }
                        workTimeDialog.show(getSupportFragmentManager(), "work_time");

                    }
                    break;
            }

        }
    }

    private boolean isNoAtNight() {
        String robotType = DeviceUtils.getRobotType(subdomain);
        return robotType.equals(Constants.A9) || robotType.equals(Constants.A9s) || robotType.equals(Constants.X800) || robotType.equals(Constants.A8s);
    }

    /**
     * ZACO V5X 预约改为单条，整周。
     */
    private void finishShedule() {
        String current = selectHour + UNDER_LINE + selectMinte;
        if (!current.equals(last)) {
            int startIndex;
            if (selecPostion != 0) {
                startIndex = (selecPostion - 1) * 5;
            } else {
                startIndex = 30;
            }
            bytes[startIndex + 1] = 1;
            if (isEveryDay) {//V5x只有一条预约，且预约为everyday
                bytes[startIndex + 2] = 0x7f;
            } else {
                bytes[startIndex + 2] = TimeUtil.getWeeks(selecPostion);
            }
            bytes[startIndex + 3] = (byte) selectHour;
            bytes[startIndex + 4] = (byte) selectMinte;

            acDeviceMsg.setContent(bytes);
            acDeviceMsg.setCode(MsgCodeUtils.Appointment);
            sendToDeviceWithOption(acDeviceMsg, physicalId, 1);
        }
    }

    private void adjustTime() {
        ACDeviceMsg msg_adjustTime = new ACDeviceMsg(MsgCodeUtils.AdjustTime, TimeUtil.getTimeBytes());
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, msg_adjustTime, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void success(ACDeviceMsg acDeviceMsg) {
                MyLogger.d(TAG, "adjust Time Success");
            }

            @Override
            public void error(ACException e) {
                MyLogger.d(TAG, "adjust Time Fail");
            }
        });
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
                handler.sendEmptyMessage(TAG_REFRESH_OVER);
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
