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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BackBaseActivity;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.SpUtils;

import java.util.Locale;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;

/**
 * Created by chengjiaping on 2018/8/28.
 */
//DONE
public class ApGuideActivity extends BackBaseActivity {
    final String TAG = ApGuideActivity.class.getSimpleName();
    Context context;
    @BindView(R.id.bt_next)
    Button bt_next;
    String subdomain;
    @BindView(R.id.text_tip1)
    TextView text_tip1;
    @BindView(R.id.text_tip2)
    TextView text_tip2;
    @BindView(R.id.image_step1)
    ImageView image_step1;
    @BindView(R.id.image_step2)
    ImageView image_step2;
    @BindView(R.id.rb_next_tip)
    RadioButton rb_next_tip;
    @BindView(R.id.ll_ap_step1)
    LinearLayout ll_ap_step1;
    @BindView(R.id.ll_ap_step2)
    LinearLayout ll_ap_step2;
    @BindView(R.id.tv_top_title)
    TextView tv_title;

    private int res_id_start, res_id_light;
    int start, strId, iconId;
    private int curStep;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // TODO 根据机型选择不同的布局文件
    @Override
    public int getLayoutId() {
        return R.layout.activity_ap_guide_x900;
    }

    @Override
    public void initView() {
        context = this;
        subdomain = SpUtils.getSpString(context, SelectActivity_x.KEY_SUBDOMAIN);

        if (subdomain.equals(Constants.subdomain_x800)) {
            res_id_start = R.drawable.n_img_connect_start;
            res_id_light = R.drawable.n_img_connect_light;
        } else if (subdomain.equals(Constants.subdomain_x900)) {
            // TODO chage the picture to x900
            res_id_start = R.drawable.n_img_connect_start;
            res_id_light = R.drawable.n_img_connect_light;
        } else {
//            strId = R.string.ap_guide_aty_tip1_x7;
            res_id_start = R.drawable.n_img_connect_start_x7;
            res_id_light = R.drawable.n_img_connect_light_x7;
            text_tip2.setText(getString(R.string.ap_guide_aty_tip2_x7));
            start = 21;
            strId = R.string.ap_guide_aty_tip1_x7;
            iconId = R.drawable.n_icon_guide_wifi_x7;
            setStr(start, strId, iconId);
        }
        image_step1.setImageResource(res_id_start);
        image_step2.setImageResource(res_id_light);
        tv_title.setText(R.string.guide_ap_prepare);
        curStep = 1;
        bt_next.setSelected(false);
        bt_next.setClickable(false);
        rb_next_tip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                bt_next.setSelected(true);
                bt_next.setClickable(true);
            } else {
                bt_next.setSelected(false);
                bt_next.setClickable(false);
            }
        });
    }

    public void setStr(int start, int strId, int iconId) {
        String str_tip = getString(strId);
        SpannableString spannableString = new SpannableString(str_tip);
        Drawable drawable = getResources().getDrawable(iconId);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
        spannableString.setSpan(span, start,
                start + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        text_tip1.setText(spannableString);
//        text_tip2.setText(getString(R.string.ap_guide_aty_tip2_x7));
    }

    @OnClick({R.id.bt_next})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_next:
                if (curStep == 1) {
                    ll_ap_step1.setVisibility(View.GONE);
                    ll_ap_step2.setVisibility(View.VISIBLE);
                    rb_next_tip.setText(R.string.ap_guide_already_open_wifi);
                    rb_next_tip.setChecked(false);
                    curStep = 2;
                } else {
                    Intent i = new Intent(context, FirstApActivity.class);
                    startActivity(i);
                }
                break;
        }
    }
}
