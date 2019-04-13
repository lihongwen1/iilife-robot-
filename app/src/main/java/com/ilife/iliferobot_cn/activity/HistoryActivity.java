package com.ilife.iliferobot_cn.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACException;
import com.accloud.service.ACMsg;
import com.accloud.service.ACObject;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.adapter.HistoryAdapter_New;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.entity.HistoryRecord;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.DeviceUtils;
import com.ilife.iliferobot_cn.utils.DialogUtils;
import com.ilife.iliferobot_cn.utils.MyLog;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.ToastUtils;

import java.util.ArrayList;

/**
 * Created by chenjiaping on 2017/8/18.
 */

public class HistoryActivity extends BaseActivity implements View.OnClickListener {
    final String TAG = HistoryActivity.class.getSimpleName();
    int index;
    long deviceId;
    Context context;
    String subdomain;
    String serviceName;
    ArrayList<HistoryRecord> recordList;
    private Long[] startTimes;
    private HistoryRecord[] records;
    RecyclerView recyclerView;
    HistoryAdapter_New adapter;
    ImageView image_back;
    TextView tv_noRecord;
    Dialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        getHistoryRecord();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_history;
    }

    public void initView() {
        context = this;
        dialog = DialogUtils.createLoadingDialog(context);
        image_back = (ImageView) findViewById(R.id.image_back);
        image_back.setOnClickListener(this);
        tv_noRecord = (TextView) findViewById(R.id.tv_noRecord);
        recordList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new HistoryAdapter_New(context, recordList);
        recyclerView.setAdapter(adapter);
        adapter.setOnClickListener(new HistoryAdapter_New.OnClickListener() {
            @Override
            public void onContentClick(int position) {
                Intent intent = new Intent(HistoryActivity.this, HistoryDetailActivity.class);
                intent.putExtra("mapList", recordList.get(position).getHistoryData());
                startActivity(intent);
            }
        });
    }

    public void initData() {
        deviceId = SpUtils.getLong(context, MainActivity.KEY_DEVICEID);
        subdomain = SpUtils.getSpString(context, MainActivity.KEY_SUBDOMAIN);
        serviceName = DeviceUtils.getServiceName(subdomain);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back:
                finish();
                break;
        }
    }

    private void getHistoryRecord() {
        index++;
        MyLog.e(TAG, "getHistoryRecord " + index);
        ACMsg req = new ACMsg();
        req.put("record_index", index);
        req.setName("searchCleanHistory");
        req.put("device_id", deviceId);
        AC.sendToService("", serviceName, Constants.SERVICE_VERSION, req, new PayloadCallback<ACMsg>() {
            @Override
            public void success(ACMsg resp) {
                if (resp.get("data") != null) {
                    ArrayList<ACObject> data = resp.get("data");
                    ACObject obj = data.get(0);
                    String clean_data0 = obj.getString("clean_data");
                    HistoryRecord record = new HistoryRecord();
                    record.setLineSpace(String.valueOf(clean_data0.charAt(0)));
                    record.setWork_time(obj.getInt("work_time"));
                    record.setStart_time(obj.getLong("start_time"));
                    record.setClean_area(obj.getInt("clean_area"));
                    String clean_data;
                    ArrayList<String> mapList = new ArrayList<>();
                    for (int i = 0; i < data.size(); i++) {
                        clean_data = data.get(i).getString("clean_data");
                        mapList.add(clean_data);
                    }
                    record.setHistoryData(mapList);
                    recordList.add(record);
                    showList(recordList);
                }
                if (index < 7) {
                    getHistoryRecord();
                } else {
                    showList(recordList);
                }
            }

            @Override
            public void error(ACException e) {
                if (index < 7) {
                    getHistoryRecord();
                } else {
                    if (recordList.size() == 0) {
                        ToastUtils.showErrorToast(context, e.getErrorCode());
                    }
                    showList(recordList);
                }
            }
        });

    }

    public void showList(ArrayList<HistoryRecord> recordList) {
        DialogUtils.closeDialog(dialog);
        if (recordList.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            tv_noRecord.setVisibility(View.VISIBLE);
        } else {
            if (recyclerView.getVisibility() == View.GONE) {
                recyclerView.setVisibility(View.VISIBLE);
                tv_noRecord.setVisibility(View.GONE);
            }
            bubbleSort(recordList);
            adapter.notifyDataSetChanged();
        }
    }

    public void bubbleSort(ArrayList<HistoryRecord> recordList) {
        int size = recordList.size();
        startTimes = new Long[size];
        records = new HistoryRecord[size];
        for (int i = 0; i < size; i++) {
            startTimes[i] = recordList.get(i).getStart_time();
            records[i] = recordList.get(i);
        }
        for (int i = 0; i < startTimes.length - 1; i++) {
            for (int j = 0; j < startTimes.length - i - 1; j++) {
                if (startTimes[j] < startTimes[j + 1]) {
                    long temp = startTimes[j];
                    HistoryRecord tempRecord = records[j];
                    startTimes[j] = startTimes[j + 1];
                    records[j] = records[j + 1];
                    startTimes[j + 1] = temp;
                    records[j + 1] = tempRecord;
                }
            }
        }
        recordList.clear();
        for (int i = 0; i < size; i++) {
            recordList.add(records[i]);
        }
    }
}
