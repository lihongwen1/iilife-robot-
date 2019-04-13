package com.ilife.iliferobot_cn.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACException;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.utils.MyLog;
import com.ilife.iliferobot_cn.utils.ToastUtils;
import com.ilife.iliferobot_cn.utils.UserUtils;

import java.util.Locale;

/**
 * Created by chenjiaping on 2017/7/6.
 */
//DONE
public class RegisterActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = RegisterActivity.class.getSimpleName();
    public static final String IS_REGISTER = "IS_REGISTER";
    public static final String STR_EMAIL = "STR_EMAIL";
    public static Activity activity;
    Context context;
    TextView tv_agreement;
    TextView tv_email;
    EditText et_email;
    Button bt_agree;
    ImageView image_back, image_show_pw;

    SpannableString spannableString;
    String str_email;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_register;
    }

    public void initData() {
        String lan = Locale.getDefault().getLanguage();
        int index, endIndex;
        index = 20;
        endIndex = 29;
        spannableString = SpannableString.valueOf(getString(R.string.register_aty_tip));
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.color_f08300));
        spannableString.setSpan(colorSpan, index, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new MyClickableSpan(), index, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new UnderlineSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#f08300"));
                ds.setUnderlineText(false);// 去掉下划线
            }
        }, index, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tv_agreement.setMovementMethod(LinkMovementMethod.getInstance());
        tv_agreement.setText(spannableString);
    }

    class MyClickableSpan extends ClickableSpan {
        @Override
        public void onClick(@NonNull View widget) {
            Intent i = new Intent(RegisterActivity.this, ProtocolActivity.class);
            startActivity(i);
        }
    }

    public void initView() {
        context = this;
        activity = this;
        et_email = (EditText) findViewById(R.id.et_email);
        tv_agreement = (TextView) findViewById(R.id.tv_agreement);
        tv_email = (TextView) findViewById(R.id.image_emailAdd);
        image_back = (ImageView) findViewById(R.id.image_back);
        image_show_pw = (ImageView) findViewById(R.id.image_show_pw);
        bt_agree = (Button) findViewById(R.id.bt_agree);

        image_back.setOnClickListener(this);
        bt_agree.setOnClickListener(this);
        et_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if (UserUtils.isEmail(str)) {
                    image_show_pw.setVisibility(View.VISIBLE);
                } else {
                    image_show_pw.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back:
                finish();
                break;
            case R.id.bt_agree:
                str_email = et_email.getText().toString().trim();
                if (TextUtils.isEmpty(str_email)) {
                    ToastUtils.showToast(this, getString(R.string.login_aty_input_email));
                    return;
                }
                if (!UserUtils.isEmail(str_email)) {
                    ToastUtils.showToast(this, getString(R.string.login_aty_wrong_email));
                    return;
                }

                AC.accountMgr().checkExist(str_email, new PayloadCallback<Boolean>() {
                    @Override
                    public void success(Boolean isExist) {
                        if (isExist) {
                            ToastUtils.showToast(context, getString(R.string.register_aty_email_registered));
                        } else {
                            Intent i = new Intent(context, RegisterActivity2.class);
                            i.putExtra(IS_REGISTER, true);
                            i.putExtra(STR_EMAIL, str_email);
                            startActivity(i);
                        }
                    }

                    @Override
                    public void error(ACException e) {
                        MyLog.e(TAG, "R.id.bt_agree " + e.toString());
                    }
                });
                break;
        }
    }
}
