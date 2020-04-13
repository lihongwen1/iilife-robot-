package com.ilife.iliferobot.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.badoo.mobile.util.WeakHandler;
import com.ilife.iliferobot.activity.fragment.UniversalDialog;
import com.ilife.iliferobot.base.BaseActivity;
import com.ilife.iliferobot.fragment.PrivacyDialogFragment;
import com.ilife.iliferobot.utils.SpUtils;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.utils.Utils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;


/**
 * Created by chenjiaping on 2017/7/20.
 */

public class FirstActivity extends BaseActivity {
    private final String TAG = FirstActivity.class.getSimpleName();
    private PrivacyDialogFragment protocolDialog;
    private final int GOTOMAIN = 0x11;
    @BindView(R.id.iv_launcher)
    ImageView iv_launcher;
    @BindView(R.id.tv_slogan)
    TextView tv_slogan;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!this.isTaskRoot()) {
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                removeActivity();
            }
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.avtivity_first;
    }

    @Override
    public void initView() {
        if (Utils.isChinaEnvironment()) {
            iv_launcher.setImageResource(R.drawable.logo);
        } else {
            tv_slogan.setVisibility(View.GONE);
            iv_launcher.setImageResource(R.drawable.logo);
        }
    }

    private WeakHandler handler = new WeakHandler(msg -> {
        switch (msg.what) {
            case GOTOMAIN:
                gotoMain();
                break;
        }
        return false;
    });

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.isChinaEnvironment() && !SpUtils.getBoolean(this, "key_agree_protocol")) {
            showProtocolDialog();
        } else {
            checkPermission();
        }
    }

    private void checkPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION).subscribe(aBoolean -> {
            if (aBoolean) {
                handler.sendEmptyMessageDelayed(GOTOMAIN, 1000);
            } else {
                ToastUtils.showToast(this, getString(R.string.access_location));
                //未授权处理
            }
        }).dispose();
    }

    public void gotoMain() {
        Intent i;
        if (AC.accountMgr().isLogin()) {
            i = new Intent(this, MainActivity.class);
        } else {
            i = new Intent(this, QuickLoginActivity.class);
            if (!Utils.isIlife()) {//Only ZACO Brand
                i.putExtra(QuickLoginActivity.QR_CODE_TIP, true);
            }
        }
        startActivity(i);
        removeActivity();
    }

    private void showProtocolDialog() {
        if (protocolDialog == null) {
            protocolDialog = new PrivacyDialogFragment();
            protocolDialog.setOnLeftButtonClck(() -> finish());
            protocolDialog.setOnRightButtonClck(() -> {
                SpUtils.saveBoolean(FirstActivity.this, "key_agree_protocol", true);
                checkPermission();
            });
        }
        if (!protocolDialog.isAdded()) {
            protocolDialog.show(getSupportFragmentManager(), "protocol");
        }
    }

    private class MyClickText extends ClickableSpan {
        private int type;

        public MyClickText(int type) {
            this.type = type;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            //设置文本的颜色
            ds.setColor(getResources().getColor(R.color.color_f08300));
            //超链接形式的下划线，false 表示不显示下划线，true表示显示下划线
            ds.setUnderlineText(false);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(FirstActivity.this, ProtocolActivity.class);
            intent.putExtra(ProtocolActivity.KEY_TYPE, type);
            startActivity(intent);
        }
    }
}
