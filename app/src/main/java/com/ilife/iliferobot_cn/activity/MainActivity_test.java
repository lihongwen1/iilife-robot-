package com.ilife.iliferobot_cn.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACException;
import com.accloud.service.ACUserDevice;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.adapter.DevListAdapter;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.utils.AlertDialogUtils;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.DialogUtils;
import com.ilife.iliferobot_cn.utils.MyLog;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;


public class MainActivity_test extends BaseActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = MainActivity_test.class.getSimpleName();
    public static List<ACUserDevice> mAcUserDevices;
    public static final String KEY_PHYCIALID = "KEY_PHYCIALID";
    public static final String KEY_SUBDOMAIN = "KEY_SUBDOMAIN";
    public static final String KEY_DEVICEID = "KEY_DEVICEID";
    public static final String KEY_DEVNAME = "KEY_DEVNAME";
    final int TAG_REFRESH_OVER = 0x01;
    Context context;
    Button bt_add;
    TextView tv_title;
    TextView tv_notice;
    long exitTime;
    ImageView addImage;
    ImageView image_add;
    ImageView image_personal;
    RecyclerView recyclerView;
    SwipeRefreshLayout refreshLayout;

    DevListAdapter adapter;
    AlertDialog alertDialog;
    Dialog dialog;
    LinearLayoutManager llm;
    RelativeLayout rootView;
    RelativeLayout container;
    RelativeLayout layout_no_device;
    RelativeLayout.LayoutParams params;
    RelativeLayout.LayoutParams lp_center;
    RelativeLayout.LayoutParams lp_below;
    RelativeLayout.LayoutParams lp_bottom;
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
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (QuickLoginActivity.activity != null) {
            QuickLoginActivity.activity.finish();
        }
        if (LoginActivity.activity != null) {
            LoginActivity.activity.finish();
        }
        if (RegisterActivity.activity != null) {
            RegisterActivity.activity.finish();
        }
        if (AC.accountMgr().isLogin()) {
            dialog.show();
            getDeviceList();
        } else {
            showButton();
        }
        AC.deviceDataMgr().unSubscribeAllProperty();
        AC.classDataMgr().unSubscribeAll();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
//        addImage.setLayoutParams(lp_center);
//        container.addView(addImage);
    }

    private void init() {
        context = this;
        initAddImage();
        llm = new LinearLayoutManager(context);
        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(RelativeLayout.BELOW, R.id.recyclerView);
        params.setMargins(0, (int) getResources().getDimension(R.dimen.dp_30), 0, 0);
        dialog = DialogUtils.createLoadingDialog_(context);

        mAcUserDevices = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(llm);
        adapter = new DevListAdapter(context, mAcUserDevices);
        recyclerView.setAdapter(adapter);

        bt_add = (Button) findViewById(R.id.bt_add);
        tv_notice = (TextView) findViewById(R.id.tv_notice);
        tv_title = (TextView) findViewById(R.id.tv_title);
        image_add = (ImageView) findViewById(R.id.image_add);
        image_personal = (ImageView) findViewById(R.id.image_personal);
        rootView = (RelativeLayout) findViewById(R.id.rootView);
        container = (RelativeLayout) findViewById(R.id.container);
        layout_no_device = (RelativeLayout) findViewById(R.id.layout_no_device);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        refreshLayout.setColorSchemeColors(getResources()
                .getColor(android.R.color.holo_blue_bright));
        bt_add.setOnClickListener(this);
        image_add.setOnClickListener(this);
        image_personal.setOnClickListener(this);
        refreshLayout.setOnRefreshListener(this);

        adapter.setOnClickListener(new DevListAdapter.OnClickListener() {
            @Override
            public void onMenuClick(final int position) {
                dialog.show();
                AC.bindMgr().unbindDevice(mAcUserDevices.get(position).getSubDomain(), mAcUserDevices.get(position).deviceId, new VoidCallback() {
                    @Override
                    public void success() {
                        mAcUserDevices.remove(position);
                        adapter.notifyDataSetChanged();
                        if (mAcUserDevices.size() == 0) {
                            showButton();
                        }
                        DialogUtils.closeDialog(dialog);
                    }

                    @Override
                    public void error(ACException e) {
                        ToastUtils.showToast(context, getString(R.string.main_aty_unbind_fail));
                        DialogUtils.closeDialog(dialog);
                    }
                });
            }

            @Override
            public void onContentClick(int position) {
                if (isDevOnline(mAcUserDevices, position)) {
                    Intent i;
                    String subdomain = mAcUserDevices.get(position).getSubDomain();
                    SpUtils.saveString(context, KEY_PHYCIALID, mAcUserDevices.get(position).getPhysicalDeviceId());
                    SpUtils.saveLong(context, KEY_DEVICEID, mAcUserDevices.get(position).getDeviceId());
                    SpUtils.saveString(context, KEY_DEVNAME, mAcUserDevices.get(position).getName());
                    SpUtils.saveLong(context, "owner", mAcUserDevices.get(position).getOwner());
                    SpUtils.saveString(context, KEY_SUBDOMAIN, subdomain);
                    if (subdomain.equals(Constants.subdomain_x800)) {
                        i = new Intent(context, MapActivity_X8.class);
                    } else {
                        i = new Intent(context, MapActivity_X7.class);
                    }
                    startActivity(i);
                } else {
                    showOfflineDialog();
                }
            }
        });

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int recyclerHeight = recyclerView.getHeight();
                int containerHeight = container.getHeight();
                if (recyclerHeight + 60 > containerHeight) {
                    addImage.setLayoutParams(lp_bottom);
                } else {
                    addImage.setLayoutParams(lp_below);
                }
                if (container.getChildCount() == 3) {
                    container.removeView(addImage);
                }
                container.addView(addImage);
            }
        });

//        recyclerView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()){
//                    case MotionEvent.ACTION_DOWN:
//                        MyLog.e(TAG,"recyclerView ACTION_DOWN");
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        MyLog.e(TAG,"recyclerView ACTION_UP");
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        MyLog.e(TAG,"recyclerView ACTION_MOVE");
//                        break;
//                }
//                return false;
//            }
//        });

//        refreshLayout.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()){
//                    case MotionEvent.ACTION_DOWN:
//                        MyLog.e(TAG,"refreshLayout ACTION_DOWN");
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        MyLog.e(TAG,"refreshLayout ACTION_UP");
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        MyLog.e(TAG,"refreshLayout ACTION_MOVE");
//                        break;
//                }
//                return false;
//            }
//        });
    }

    public void initAddImage() {
        addImage = new ImageView(context);
        addImage.setId(R.id.addImage);
        addImage.setImageResource(R.drawable.n_btn_add);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showToast(context, "测试");
            }
        });
        addImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        MyLog.e(TAG, "addImage ACTION_DOWN");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        MyLog.e(TAG, "addImage ACTION_MOVE");
                        break;
                    case MotionEvent.ACTION_UP:
                        MyLog.e(TAG, "addImage ACTION_UP");
                        break;
                }
//                return false;
                return true;
            }
        });

        int width_height = (int) getResources().getDimension(R.dimen.dp_40);
        int margin = (int) getResources().getDimension(R.dimen.dp_30);

        lp_below = new RelativeLayout.LayoutParams(width_height, width_height);
        lp_below.addRule(RelativeLayout.BELOW, R.id.recyclerView);
        lp_below.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp_below.topMargin = margin;

        lp_bottom = new RelativeLayout.LayoutParams(width_height, width_height);
        lp_bottom.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp_bottom.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp_bottom.bottomMargin = margin;

        lp_center = new RelativeLayout.LayoutParams(width_height, width_height);
        lp_center.addRule(RelativeLayout.CENTER_IN_PARENT);
    }

    public void getDeviceList() {
        AC.bindMgr().listDevicesWithStatus(new PayloadCallback<List<ACUserDevice>>() {
            @Override
            public void success(List<ACUserDevice> acUserDevices) {
                mAcUserDevices.clear();
                if (acUserDevices.size() == 0) {
                    showButton();
                } else {
                    for (ACUserDevice device : acUserDevices) {
                        mAcUserDevices.add(device);
                    }
                    showList();
                }
                handler.sendEmptyMessageDelayed(TAG_REFRESH_OVER, 1000);
            }

            @Override
            public void error(ACException e) {
                handler.sendEmptyMessageDelayed(TAG_REFRESH_OVER, 1000);
            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.bt_add:
                if (AC.accountMgr().isLogin()) {
                    i = new Intent(context, SelectActivity.class);
                    startActivity(i);
                } else {
                    i = new Intent(context, LoginActivity.class);
                    startActivity(i);
                }

                break;
            case R.id.image_add:
                i = new Intent(context, SelectActivity.class);
                startActivity(i);
                break;
//            case R.id.addImage:
//                i = new Intent(context,SelectActivity.class);
//                startActivity(i);
//                break;
            case R.id.image_personal:
                i = new Intent(context, PersonalActivity.class);
                startActivity(i);
                break;
            case R.id.rl_ok:
                AlertDialogUtils.hidden(alertDialog);
                break;
        }
    }

    public void showButton() {
        refreshLayout.setVisibility(View.INVISIBLE);
        layout_no_device.setVisibility(View.VISIBLE);
//        addImage.setVisibility(View.GONE);
        DialogUtils.closeDialog(dialog);
    }

    public void showList() {
        layout_no_device.setVisibility(View.GONE);
        refreshLayout.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
        DialogUtils.closeDialog(dialog);
    }

    @Override
    public void onRefresh() {
        getDeviceList();
        MyLog.e(TAG, "onRefresh");
//        if (mAcUserDevices.size()>0){
//            mAcUserDevices.add(mAcUserDevices.get(0));
//        }
//        adapter.notifyDataSetChanged();
//        refreshLayout.setRefreshing(false);
    }

    public boolean isDevOnline(List<ACUserDevice> mAcUserDevices, int position) {
        if (mAcUserDevices.get(position).getStatus() == 0) {
            return false;
        }
        return true;
    }

    private void showOfflineDialog() {
        View view = LayoutInflater.from(context).inflate(R.layout.offline_dialog, null);
        RelativeLayout rl_ok = (RelativeLayout) view.findViewById(R.id.rl_ok);
        rl_ok.setOnClickListener(this);
        int width = (int) getResources().getDimension(R.dimen.dp_300);
        int height = (int) getResources().getDimension(R.dimen.dp_150);
        alertDialog = AlertDialogUtils.showDialog(context, view, width, height);
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - exitTime >= 2000) {
            ToastUtils.showToast(context, getString(R.string.main_aty_press_exit));
            exitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
        MyLog.e(TAG, "onBackPressed====");
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main_test;
    }

    @Override
    public void initView() {

    }
}