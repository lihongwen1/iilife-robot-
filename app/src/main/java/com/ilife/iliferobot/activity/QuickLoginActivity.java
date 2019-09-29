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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.core.util.Consumer;

import com.ilife.iliferobot.base.BaseActivity;
import com.ilife.iliferobot.presenter.QuickLoginPresenter;
import com.ilife.iliferobot.utils.LanguageUtils;
import com.ilife.iliferobot.utils.MyLogger;
import com.ilife.iliferobot.view.SuperEditText;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.contract.QuickLoginContract;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.utils.Utils;
import com.ilife.iliferobot.view.ToggleRadioButton;

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
    public static final String QR_CODE_TIP = "qr_code_tip";
    Context context;
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
    @BindView(R.id.iv_logo)
    ImageView iv_logo;
    @BindView(R.id.tv_privacy_policy)
    TextView tv_privacy_policy;
    @BindView(R.id.rb_privacy_policy)
    ToggleRadioButton rb_privacy_policy;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent it = getIntent();
        if (it.getBooleanExtra(QR_CODE_TIP, false)) {//Only ZACO Brand
            showQRCodeTip();
        }
    }

    private void showQRCodeTip() {
        UniversalDialog universalDialog = new UniversalDialog();
        universalDialog.setDialogType(UniversalDialog.TYPE_NORMAL_MID_BUTTON_NO_TITLE).setHintTip("Please enter the email address here and then click on confirmation code. The code will then be sent automatically by email. Then enter the code and click on Quick Registration.")
                .setCanEdit(false).setMidText(Utils.getString(R.string.dialog_del_confirm));
        universalDialog.show(getSupportFragmentManager(), "qrcode");
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_quick_login;
    }

    @Override
    public void initView() {
        context = this;
        if (Utils.isSupportPhone()) {
            et_phone_number.setHint(R.string.login_aty_email_phone);
        } else {
            et_phone_number.setHint(R.string.login_aty_email);
        }
        et_phone_number.addOnInputEndListener(s -> mPresenter.isMobileEmpty());
        et_verification_code.addOnInputEndListener(s -> mPresenter.isCodeEmpty());
        if (!Utils.isIlife()) {
            tv_slogan.setVisibility(View.GONE);
        }

        if (Utils.isChinaEnvironment()){
            iv_logo.setBackground(getResources().getDrawable(R.drawable.logo_zh));
        }else {
            iv_logo.setBackground(getResources().getDrawable(R.drawable.logo));
        }

        unUsableQuickLogin();
    }

    @Override
    public void attachPresenter() {
        super.attachPresenter();
        mPresenter = new QuickLoginPresenter();
        mPresenter.attachView(this);
    }

    @OnClick({R.id.tv_login, R.id.bt_quick_login, R.id.tv_send_code, R.id.tv_privacy_policy})
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.tv_login:
                i = new Intent(this, LoginActivity.class);
                startActivity(i);
                break;
            case R.id.bt_quick_login:
                if (!rb_privacy_policy.isChecked()) {
                    ToastUtils.showToast(getResources().getString(R.string.please_agree_policy, " " + Utils.getString(R.string.personal_aty_protocol)));
                } else {
                    mPresenter.checkVerificationCode();
                }
                break;
            case R.id.tv_send_code:
                mPresenter.sendVerification();
                break;
            case R.id.tv_privacy_policy:
                startActivity(new Intent(QuickLoginActivity.this, Utils.isIlife() ? ProtocolActivity.class : ZacoProtocolActivity.class));
                break;

        }
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
        mPresenter.finishCountDown();
        Intent intent = new Intent(QuickLoginActivity.this, SetPasswordActivity.class);
        intent.putExtra(PHONE, getPhone());
        intent.putExtra(VER_CODE, getVerificationCode());
        startActivity(intent);
    }
}
