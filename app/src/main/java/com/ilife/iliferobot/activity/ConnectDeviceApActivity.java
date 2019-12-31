package com.ilife.iliferobot.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.utils.Utils;
import com.ilife.iliferobot.utils.WifiUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 连接设备热点
 */
public class ConnectDeviceApActivity extends BackBaseActivity {
    @BindView(R.id.et_ssid)
    EditText et_ssid;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.bt_connect)
    Button bt_connect;
    @BindView(R.id.tv_ap_tip)
    TextView tv_ap_tip;
    private boolean isFirstOnresume = true;

    @Override
    public int getLayoutId() {
        return R.layout.activity_ap_third;
    }

    @Override
    public void initView() {
        tv_title.setText(R.string.guide_ap_prepare);
        tv_ap_tip.setText(matcherSearchText(Utils.getString(R.string.third_ap_aty_tip1), "123456789"));
    }

    @OnClick({R.id.bt_connect, R.id.tv_set})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.bt_connect:
                String ap_ssid = et_ssid.getText().toString();
                if (TextUtils.isEmpty(ap_ssid) || !ap_ssid.startsWith("Robot")) {
                    ToastUtils.showToast(this, getString(R.string.third_ap_aty_port_));
                } else {
                    Intent intent = new Intent(this, ApWifiActivity.class);
                    intent.putExtra(ApWifiActivity.EXTAR_ROBOT_SSID, ap_ssid);
                    startActivity(intent);
                    removeActivity();
                }
                break;
            case R.id.tv_set:
                Intent i = new Intent();
                i.setAction("android.net.wifi.PICK_WIFI_NETWORK");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                break;
        }
    }


    public SpannableString matcherSearchText(String text, String keyword) {
        SpannableString ss = new SpannableString(text);
        Pattern pattern = Pattern.compile(keyword);
        Matcher matcher = pattern.matcher(ss);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.color_f08300)), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);//new ForegroundColorSpan(color)
        }
        return ss;
    }


    @Override
    protected void onResume() {
        super.onResume();
        String ssid = WifiUtils.getSsid(this);
        if (ssid != null && !ssid.contains("unknown") && ssid.startsWith("Robot")) {
            et_ssid.setText(ssid);
            bt_connect.setClickable(true);
            bt_connect.setSelected(true);
        }
        if (!isFirstOnresume) {
            bt_connect.callOnClick();
        } else {
            isFirstOnresume = false;
        }
    }
}
