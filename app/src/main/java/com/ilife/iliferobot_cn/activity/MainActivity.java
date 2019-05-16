package com.ilife.iliferobot_cn.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACException;
import com.accloud.service.ACUserDevice;
import com.badoo.mobile.util.WeakHandler;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.adapter.DevListAdapter;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.contract.MainContract;
import com.ilife.iliferobot_cn.presenter.MainPresenter;
import com.ilife.iliferobot_cn.ui.MyRelativeLayout;
import com.ilife.iliferobot_cn.utils.AlertDialogUtils;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.DialogUtils;
import com.ilife.iliferobot_cn.utils.MyLog;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.ToastUtils;
import com.ilife.iliferobot_cn.utils.Utils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;


public class MainActivity extends BaseActivity<MainPresenter> implements View.OnClickListener, MainContract.View {
    private final String TAG = MainActivity.class.getSimpleName();
    public static List<ACUserDevice> mAcUserDevices;
    public static final String KEY_PHYCIALID = "KEY_PHYCIALID";
    public static final String KEY_SUBDOMAIN = "KEY_SUBDOMAIN";
    public static final String KEY_DEVICEID = "KEY_DEVICEID";
    public static final String KEY_DEVNAME = "KEY_DEVNAME";
    public static final String KEY_OWNER = "KEY_OWNER";
    final int TAG_REFRESH_OVER = 0x01;
    public static Activity activity;
    Context context;
    @BindView(R.id.bt_add)
    Button bt_add;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.tv_notice)
    TextView tv_notice;
    @BindView(R.id.image_personal)
    ImageView image_personal;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    DevListAdapter adapter;
    AlertDialog alertDialog;
    Dialog loadingDialog;
    LinearLayoutManager llm;
    @BindView(R.id.rootView)
    LinearLayout rootView;
    @BindView(R.id.layout_no_device)
    RelativeLayout layout_no_device;
    Rect rect;
    private WeakHandler handler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case TAG_REFRESH_OVER:
                    if (refreshLayout != null) {
                       refreshLayout.finishRefresh();
                    }
                    break;
            }
            return true;
        }
    });

    @Override
    public void setRefreshOver() {
        handler.sendEmptyMessage(TAG_REFRESH_OVER);
    }


    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void attachPresenter() {
        mPresenter = new MainPresenter();
        mPresenter.attachView(this);
    }

    @Override
    public void initView() {
        activity=this;
        context=this;
        rect = new Rect();
        llm = new LinearLayoutManager(context);
        loadingDialog = DialogUtils.createLoadingDialog_(context);
        mAcUserDevices = new ArrayList<>();
        recyclerView.setLayoutManager(llm);
        adapter = new DevListAdapter(context, mAcUserDevices);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                adapter.setScrollingMenu(null);
            }
        });

        bt_add.setOnClickListener(this);
        image_personal.setOnClickListener(this);
        refreshLayout.setOnRefreshListener(refreshLayout -> mPresenter.getDeviceList());
        adapter.setOnClickListener(new DevListAdapter.OnClickListener() {
            @Override
            public void onMenuClick(final int position) {
                loadingDialog.show();
                AC.bindMgr().unbindDevice(mAcUserDevices.get(position).getSubDomain(), mAcUserDevices.get(position).deviceId, new VoidCallback() {
                    @Override
                    public void success() {
                        mAcUserDevices.remove(position);
                        adapter.notifyDataSetChanged();
                        if (mAcUserDevices.size() == 0) {
                            showButton();
                        }
                        DialogUtils.closeDialog(loadingDialog);
                    }

                    @Override
                    public void error(ACException e) {
                        ToastUtils.showToast(context, getString(R.string.main_aty_unbind_fail));
                        DialogUtils.closeDialog(loadingDialog);
                    }
                });
            }

            @Override
            public void onContentClick(int position) {
                if (mPresenter.isDeviceOnLine(mAcUserDevices.get(position))) {
                    Intent i;
                    String subdomain = mAcUserDevices.get(position).getSubDomain();
                    SpUtils.saveString(context, KEY_PHYCIALID, mAcUserDevices.get(position).getPhysicalDeviceId());
                    SpUtils.saveLong(context, KEY_DEVICEID, mAcUserDevices.get(position).getDeviceId());
                    SpUtils.saveString(context, KEY_DEVNAME, mAcUserDevices.get(position).getName());
                    SpUtils.saveLong(context, KEY_OWNER, mAcUserDevices.get(position).getOwner());
                    SpUtils.saveString(context, KEY_SUBDOMAIN, subdomain);
//                    if (subdomain.equals(Constants.subdomain_x800)) {
//                        i = new Intent(context, MapActivity_X8_.class);
//                    } else if (subdomain.equals(Constants.subdomain_x900)) {
                        i = new Intent(context, MapActivity_X9_.class);
//                    } else {
//                        i = new Intent(context, MapActivity_X7_.class);
//                    }
                    startActivity(i);
                } else {
                    showOfflineDialog();
                }
            }
        });
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
            loadingDialog.show();
            mPresenter.getDeviceList();
        } else {
            showButton();
        }
        AC.deviceDataMgr().unSubscribeAllProperty();
        AC.classDataMgr().unSubscribeAll();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }



    @Override
    public void updateDeviceList(List<ACUserDevice> acUserDevices) {
        mAcUserDevices.clear();
        if (acUserDevices.size() == 0) {
            showButton();
        } else {
            MyLog.e(TAG, "getDeviceList success " + acUserDevices.get(0).getPhysicalDeviceId());
            mAcUserDevices.addAll(acUserDevices);
            showList();
        }
    }



    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.bt_add:
                //TODO 进入选择activity
                if (AC.accountMgr().isLogin()) {
                    i = new Intent(context, SelectActivity_x.class);
                    startActivity(i);
                } else {
                    i = new Intent(context, LoginActivity.class);
                    startActivity(i);
                }

                break;
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
        refreshLayout.setVisibility(View.GONE);
        layout_no_device.setVisibility(View.VISIBLE);
//        addImage.setVisibility(View.GONE);
        DialogUtils.closeDialog(loadingDialog);
    }

    public void showList() {
        layout_no_device.setVisibility(View.GONE);
        refreshLayout.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
        DialogUtils.closeDialog(loadingDialog);
    }

    private void showOfflineDialog() {
        View view = LayoutInflater.from(context).inflate(R.layout.offline_dialog, null);
        RelativeLayout rl_ok = (RelativeLayout) view.findViewById(R.id.rl_ok);
        rl_ok.setOnClickListener(this);
        int width = (int) getResources().getDimension(R.dimen.dp_280);
        int height = (int) getResources().getDimension(R.dimen.dp_150);
        alertDialog = AlertDialogUtils.showDialog(context, view, width, height);
    }


}
