package com.ilife.iliferobot.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACException;
import com.accloud.service.ACMsg;
import com.accloud.service.ACObject;
import com.ilife.iliferobot.adapter.HistoryAdapter;
import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.utils.MyLogger;
import com.ilife.iliferobot.utils.Utils;
import com.ilife.iliferobot.view.RecyclerViewDivider;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.entity.HistoryRecord_x9;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.able.DeviceUtils;
import com.ilife.iliferobot.utils.DialogUtils;
import com.ilife.iliferobot.utils.SpUtils;
import com.ilife.iliferobot.utils.ToastUtils;

import java.util.ArrayList;

/**
 * Created by chenjiaping on 2017/8/18.
 */

public class HistoryActivity_x9 extends BackBaseActivity implements View.OnClickListener {
    final String TAG = HistoryActivity_x9.class.getSimpleName();
    int index;
    long deviceId;
    Context context;
    String subdomain;
    String serviceName;
    ArrayList<HistoryRecord_x9> recordList;
    private Long[] startTimes;
    private HistoryRecord_x9[] records;
    RecyclerView recyclerView;
    HistoryAdapter adapter;
    FrameLayout fl_noRecord;
    TextView tv_title;
    Dialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getHistoryRecord();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_history;
    }

    public void initView() {
        context = this;
        dialog = DialogUtils.createLoadingDialog(context);
        dialog.dismiss();
        fl_noRecord = (FrameLayout) findViewById(R.id.fl_noRecord);
        tv_title = findViewById(R.id.tv_top_title);
        tv_title.setText(R.string.setting_aty_clean_record);
        recordList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new HistoryAdapter(Utils.isChineseLanguage() ? R.layout.layout_histroy_item : R.layout.layout_histroy_item_en, recordList);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            Intent intent = new Intent(HistoryActivity_x9.this, HistoryDetailActivity_x9.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("Record", recordList.get(position));
            intent.putExtras(bundle);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new RecyclerViewDivider(this, LinearLayoutManager.VERTICAL, 4,
                ContextCompat.getColor(this, R.color.bg_color_f5f7fa)));
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
                removeActivity();
                break;
        }
    }

    private void getHistoryRecord() {
        index++;
        MyLogger.e(TAG, "getHistoryRecord " + index);
        ACMsg req = new ACMsg();
        req.put("record_index", index);
        req.setName("searchCleanHistory");
        req.put("device_id", deviceId);
        AC.sendToService("", serviceName, Constants.SERVICE_VERSION, req, new PayloadCallback<ACMsg>() {
            @Override
            public void success(ACMsg resp) {
                if (subdomain.equals(Constants.subdomain_x900)) {
                    if (resp.get("data") != null) {
                        ArrayList<ACObject> data = resp.get("data");
                        ACObject acObject = data.get(0);
                        String slamData = acObject.getString("clean_slam");
                        String roadData = acObject.getString("clean_road");
                        ArrayList<String> maplists = new ArrayList<>();
                        maplists.add(slamData);
                        maplists.add(roadData);
                        HistoryRecord_x9 record = new HistoryRecord_x9();
                        record.setStart_time(acObject.getLong("start_time"));
                        record.setClean_area(acObject.getInt("clean_area"));
                        record.setWork_time(acObject.getInt("clean_time"));
                        record.setSlam_xMin(acObject.getInt("slam_x_min"));
                        record.setSlam_xMax(acObject.getInt("slam_x_max"));
                        record.setSlam_yMin(acObject.getInt("slam_y_min"));
                        record.setSlam_yMax(acObject.getInt("slam_y_max"));
                        record.setStart_reason(acObject.getInt("start_reason"));
                        record.setStop_reason(acObject.getInt("stop_reason"));
                        record.setHistoryData(maplists);
                        recordList.add(record);
                        showList(recordList);
                    }
                } else {
                    if (resp.get("data") != null) {
                        ArrayList<ACObject> data = resp.get("data");
                        ACObject obj = data.get(0);
                        String clean_data0 = obj.getString("clean_data");
                        HistoryRecord_x9 record = new HistoryRecord_x9();
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

    public void showList(ArrayList<HistoryRecord_x9> recordList) {
        if (recordList.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            fl_noRecord.setVisibility(View.VISIBLE);
        } else {
            if (recyclerView.getVisibility() == View.GONE) {
                recyclerView.setVisibility(View.VISIBLE);
            }
            fl_noRecord.setVisibility(View.GONE);
            bubbleSort(recordList);
            adapter.notifyDataSetChanged();
        }
    }

    public void bubbleSort(ArrayList<HistoryRecord_x9> recordList) {
        int size = recordList.size();
        startTimes = new Long[size];
        records = new HistoryRecord_x9[size];
        for (int i = 0; i < size; i++) {
            startTimes[i] = recordList.get(i).getStart_time();
            records[i] = recordList.get(i);
        }
        for (int i = 0; i < startTimes.length - 1; i++) {
            for (int j = 0; j < startTimes.length - i - 1; j++) {
                if (startTimes[j] < startTimes[j + 1]) {
                    long temp = startTimes[j];
                    HistoryRecord_x9 tempRecord = records[j];
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
