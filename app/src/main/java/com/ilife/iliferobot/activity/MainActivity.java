package com.ilife.iliferobot.activity;

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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACException;
import com.accloud.service.ACUserDevice;
import com.badoo.mobile.util.WeakHandler;
import com.ilife.iliferobot.base.BaseActivity;
import com.ilife.iliferobot.contract.MainContract;
import com.ilife.iliferobot.presenter.MainPresenter;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.utils.MyLogger;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.adapter.DevListAdapter;
import com.ilife.iliferobot.utils.AlertDialogUtils;
import com.ilife.iliferobot.utils.DialogUtils;
import com.ilife.iliferobot.utils.SpUtils;
import com.ilife.iliferobot.utils.Utils;
import com.ilife.iliferobot.view.SlideRecyclerView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    FrameLayout image_personal;
    @BindView(R.id.recyclerView)
    SlideRecyclerView recyclerView;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    DevListAdapter adapter;
    Dialog loadingDialog;
    @BindView(R.id.rootView)
    LinearLayout rootView;
    @BindView(R.id.layout_no_device)
    RelativeLayout layout_no_device;
    Rect rect;
    UniversalDialog unbindDialog;
    private WeakHandler handler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case TAG_REFRESH_OVER:
                    if (refreshLayout != null) {
                        refreshLayout.finishRefresh();
                    }
                    DialogUtils.closeDialog(loadingDialog);
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
        activity = this;
        context = this;
        rect = new Rect();
        loadingDialog = DialogUtils.createLoadingDialog_(context);
        mAcUserDevices = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        adapter = new DevListAdapter(context, mAcUserDevices,recyclerView);
        recyclerView.setAdapter(adapter);
        bt_add.setOnClickListener(this);
        image_personal.setOnClickListener(this);
        refreshLayout.setOnRefreshListener(refreshLayout -> mPresenter.getDeviceList());
        adapter.setOnClickListener(new DevListAdapter.OnClickListener() {
            @Override
            public void onMenuClick(final int position) {
                if (unbindDialog == null) {
                    unbindDialog = new UniversalDialog();
                    unbindDialog.setDialogType(UniversalDialog.TYPE_NORMAL).setTitle(Utils.getString(R.string.main_aty_unbind_device))
                            .setHintTip(Utils.getString(R.string.main_aty_unbind_device_tip)).setOnRightButtonClck(() -> {
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
                    });
                }
                unbindDialog.show(getSupportFragmentManager(), "unbind");
            }

            @Override
            public void onContentClick(int position) {
                if (mPresenter.isDeviceOnLine(mAcUserDevices.get(position))) {
                    String subdomain = mAcUserDevices.get(position).getSubDomain();
                    SpUtils.saveString(context, KEY_PHYCIALID, mAcUserDevices.get(position).getPhysicalDeviceId());
                    SpUtils.saveLong(context, KEY_DEVICEID, mAcUserDevices.get(position).getDeviceId());
                    SpUtils.saveString(context, KEY_DEVNAME, mAcUserDevices.get(position).getName());
                    SpUtils.saveLong(context, KEY_OWNER, mAcUserDevices.get(position).getOwner());
                    SpUtils.saveString(context, KEY_SUBDOMAIN, subdomain);
                    if (subdomain.equals(Constants.subdomain_x900)) {
                        Intent i = new Intent(context, MapActivity_X9_.class);
                        startActivity(i);
                    } else {
                        Intent i = new Intent(context, MapActivity_X8_.class);
                        startActivity(i);
                    }
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
    public void loginInvalid() {
        ToastUtils.showToast(Utils.getString(R.string.login_invalid));
        startActivity(new Intent(MainActivity.this, QuickLoginActivity.class));
        finish();
    }

    @Override
    public void updateDeviceList(List<ACUserDevice> acUserDevices) {
        mAcUserDevices.clear();
        if (acUserDevices.size() == 0) {
            showButton();
        } else {
            MyLogger.e(TAG, "getDeviceList success " + acUserDevices.get(0).getPhysicalDeviceId());
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
        }
    }

    public void showButton() {
        refreshLayout.setVisibility(View.GONE);
        layout_no_device.setVisibility(View.VISIBLE);
    }

    public void showList() {
        layout_no_device.setVisibility(View.GONE);
        refreshLayout.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
    }

    private void showOfflineDialog() {
        UniversalDialog offLineDialog = new UniversalDialog();
        offLineDialog.setDialogType(UniversalDialog.TYPE_NORMAL_MID_BUTTON).setTitle(Utils.getString(R.string.dev_frag_offline))
                .setHintTip(Utils.getString(R.string.dev_frag_try)).setMidText(Utils.getString(R.string.dialog_del_confirm))
                .show(getSupportFragmentManager(), "offline");
    }


}
