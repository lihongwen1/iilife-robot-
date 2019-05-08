package com.ilife.iliferobot_cn.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.contract.QuickLoginContract;
import com.ilife.iliferobot_cn.presenter.QuickLoginPresenter;
import com.ilife.iliferobot_cn.utils.MyLog;
import com.ilife.iliferobot_cn.utils.ToastUtils;
import com.ilife.iliferobot_cn.utils.Utils;
import com.ilife.iliferobot_cn.view.SuperEditText;
import butterknife.BindView;
import butterknife.OnClick;


/**
 * Created by chenjiaping on 2017/7/5.
 */
//DONE
public class QuickLoginActivity extends BaseActivity<QuickLoginPresenter> implements View.OnClickListener, QuickLoginContract.View {
    private final String TAG = QuickLoginActivity.class.getSimpleName();
    public static final String PHONE = "phone";
    public static final String VER_CODE = "ver_code";
    long exitTime;
    Context context;
    public static Activity activity;
    private QuickLoginPresenter mPresenter;
    @BindView(R.id.et_verification_code)
    SuperEditText et_verification_code;
    @BindView(R.id.et_phone_number)
    SuperEditText et_phone_number;
    @BindView(R.id.bt_quick_login)
    TextView bt_quick_login;
    @BindView(R.id.tv_send_code)
    TextView tv_send_code;
    @BindView(R.id.tv_count_down)
    TextView tv_count_down;
    @BindView(R.id.tv_login)
    TextView tv_login;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_second;
    }

    @Override
    public void initView() {
        activity = this;
        context = this;
        et_verification_code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.isCodeEmpty();
            }
        });
        et_phone_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.isMobileUseful();
            }
        });
        String str = Utils.getString(R.string.have_account_and_login);
        SpannableString spannableString = new SpannableString(str);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.bt_bg_unpress_color)), 5, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_login.setText(spannableString);
    }

    @Override
    public void attachPresenter() {
        super.attachPresenter();
        mPresenter = new QuickLoginPresenter();
        mPresenter.attachView(this);
    }

    @OnClick({R.id.tv_login, R.id.bt_quick_login, R.id.tv_send_code})
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.tv_login:
                i = new Intent(this, LoginActivity.class);
                startActivity(i);
                break;
            case R.id.bt_quick_login:
                mPresenter.checkVerificationCode();
                break;
            case R.id.tv_send_code:
                mPresenter.sendVerification();
                break;
        }
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
    public String getPhone() {
        return et_phone_number.getText().toString().trim();
    }

    @Override
    public String getVerificationCode() {
        return et_verification_code.getText().toString().trim();
    }

    @Override
    public void unUsableQuickLogin() {
        bt_quick_login.setSelected(false);
        bt_quick_login.setClickable(false);
    }

    @Override
    public void reUseQuickLogin() {
        bt_quick_login.setSelected(true);
        bt_quick_login.setClickable(true);

    }

    @Override
    public void onStartCountDown() {
        tv_count_down.setVisibility(View.VISIBLE);
        tv_send_code.setVisibility(View.GONE);
    }

    @Override
    public void setCountDownValue(String value) {
        tv_count_down.setText(value);
    }

    @Override
    public void onCountDownFinish() {
        Log.d("QuickLogin", "是否是主线程：" + (Looper.getMainLooper() == Looper.myLooper()));
        tv_count_down.setVisibility(View.GONE);
        tv_send_code.setVisibility(View.VISIBLE);
    }

    @Override
    public void goSetPassword() {
        Intent intent = new Intent(QuickLoginActivity.this, SetPasswordActivity.class);
        intent.putExtra(PHONE, getPhone());
        intent.putExtra(VER_CODE, getVerificationCode());
        startActivity(intent);
    }
}
