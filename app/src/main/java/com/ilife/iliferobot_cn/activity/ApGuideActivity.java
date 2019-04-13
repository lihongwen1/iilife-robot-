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

import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.SpUtils;

import java.util.Locale;

/**
 * Created by chengjiaping on 2018/8/28.
 */
//DONE
public class ApGuideActivity extends BaseActivity implements View.OnClickListener {
    final String TAG = ApGuideActivity.class.getSimpleName();
    Context context;
    Button bt_next;
    String subdomain;
    TextView text_tip1, text_tip2;
    ImageView image_back, image_step1, image_step2;
    int res_id_start, res_id_light;
    int start, strId, iconId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ap_guide);
        initView();
    }

    private void initView() {
        context = this;
        String lan = Locale.getDefault().getLanguage();
        subdomain = SpUtils.getSpString(context, SelectActivity_x.KEY_SUBDOMAIN);
        bt_next = (Button) findViewById(R.id.bt_next);
        image_back = (ImageView) findViewById(R.id.image_back);
        text_tip1 = (TextView) findViewById(R.id.ap_guide_aty_tip1);
        text_tip2 = (TextView) findViewById(R.id.text_tip2);
        image_step1 = (ImageView) findViewById(R.id.image_step1);
        image_step2 = (ImageView) findViewById(R.id.image_step2);

        bt_next.setOnClickListener(this);
        image_back.setOnClickListener(this);
        if (subdomain.equals(Constants.subdomain_x800)) {
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_next:
                Intent i = new Intent(context, FirstApActivity.class);
                startActivity(i);
                break;
            case R.id.image_back:
                finish();
                break;
        }
    }
}
