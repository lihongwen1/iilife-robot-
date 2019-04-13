package com.ilife.iliferobot_cn.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.ACDeviceActivator;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.WifiUtils;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by chengjiaping on 2018/8/27.
 */
//DONE
public class GuideActivity extends BaseActivity {
    final String TAG = GuideActivity.class.getSimpleName();
    int resId;
    Context context;
    @BindView(R.id.tv_tip2)
    TextView tv_tip2;
    @BindView(R.id.bt_next)
    Button bt_next;
    String subdomain;
    @BindView(R.id.image_back)
    ImageView image_back;
    @BindView(R.id.image_step2)
    ImageView image_step2;
    ACDeviceActivator activator;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_guide;
    }
    @Override
    public void initView() {
        context = this;
        subdomain = SpUtils.getSpString(context, SelectActivity_x.KEY_SUBDOMAIN);
        activator = AC.deviceActivator(Constants.DEVICE_TYPE_QCLTLINK);
        if (subdomain.equals(Constants.subdomain_x800)){
            resId = R.drawable.n_img_guide_control;
        } else {
            resId = R.drawable.n_img_connect_start_x7;
        }
        image_step2.setImageResource(resId);
        setStr();
    }

    public void setStr(){
        int start,iconId,strId;
        String str_tip;
        SpannableString spannableString;
        String lan = Locale.getDefault().getLanguage();
        if (subdomain.equals(Constants.subdomain_x800)){
            iconId = R.drawable.n_icon_guide_wifi;
            strId = R.string.guide_aty_tip2;
            start = 15;
        } else {
            iconId = R.drawable.n_icon_guide_wifi_x7;
            strId = R.string.guide_aty_tip2_x7;
            start = 13;
        }
        str_tip = getString(strId);
        spannableString = new SpannableString(str_tip);
        Drawable drawable = getResources().getDrawable(iconId);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        ImageSpan span = new ImageSpan(drawable,ImageSpan.ALIGN_BASELINE);
        spannableString.setSpan(span,start,
                start+1,Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tv_tip2.setText(spannableString);
    }

    @OnClick({R.id.bt_next, R.id.image_back})
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_next:
                String ssid = activator.getSSID();
                Intent i;
//                if (TextUtils.isEmpty(ssid)){
                if (!WifiUtils.isWifiConnected(context)){
                    i = new Intent(context, WifiGuideActivity.class);
                } else {
                    i = new Intent(context, AddActivity.class);
                }
                startActivity(i);
                break;
            case R.id.image_back:
                finish();
                break;
        }

    }
}
