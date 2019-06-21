package com.ilife.iliferobot.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import androidx.core.util.Consumer;

import com.ilife.iliferobot.base.BaseActivity;
import com.ilife.iliferobot.presenter.QuickLoginPresenter;
import com.ilife.iliferobot.utils.MyLogger;
import com.ilife.iliferobot.view.SuperEditText;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.contract.QuickLoginContract;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.utils.Utils;

import java.util.Locale;

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
    @BindView(R.id.tv_slogan)
    TextView tv_slogan;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_quick_login;
    }

    @Override
    public void initView() {
        activity = this;
        context = this;
        et_verification_code.addOnInputEndListener(s -> mPresenter.isCodeEmpty());
        String str_login = Utils.getString(R.string.have_account_and_login);
        String lan = Locale.getDefault().getLanguage();
        int index, endIndex;
        String targetString;
        if (lan.equals("zh")) {
            targetString = "请登录";
        } else if (lan.equals("de")) {
            targetString = "Login now";
        } else {
            targetString = "Login now";
        }
        index = str_login.toString().indexOf(targetString);
        endIndex = str_login.toString().indexOf(targetString) + targetString.length();
        SpannableString spannableString = new SpannableString(str_login);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.bt_bg_unpress_color)), index, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_login.setText(spannableString);
        if (!Utils.isIlife()){
            tv_slogan.setVisibility(View.INVISIBLE);
        }
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
                mPresenter.isMobileUseful();
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
        MyLogger.e(TAG, "onBackPressed====");
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
        MyLogger.d("QuickLogin", "是否是主线程：" + (Looper.getMainLooper() == Looper.myLooper()));
        tv_count_down.setVisibility(View.GONE);
        tv_send_code.setVisibility(View.VISIBLE);
        tv_send_code.setText(R.string.resend);
    }

    @Override
    public void goSetPassword() {
        Intent intent = new Intent(QuickLoginActivity.this, SetPasswordActivity.class);
        intent.putExtra(PHONE, getPhone());
        intent.putExtra(VER_CODE, getVerificationCode());
        startActivity(intent);
    }
}
