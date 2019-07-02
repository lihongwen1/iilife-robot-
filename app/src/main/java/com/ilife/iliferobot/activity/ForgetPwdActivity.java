package com.ilife.iliferobot.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.view.SuperEditText;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.contract.ForgetPasswordContract;
import com.ilife.iliferobot.presenter.ForgetPasswordPresenter;
import com.ilife.iliferobot.utils.Utils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by chengjiaping on 2018/8/6.
 */
//DONE
public class ForgetPwdActivity extends BackBaseActivity<ForgetPasswordPresenter> implements ForgetPasswordContract.View,View.OnClickListener{
    private final String TAG = ForgetPwdActivity.class.getSimpleName();
    final int STATUS_GAIN_CODE = 0X01;
    final int STATUS_GAIN_DONE = 0X02;
    Context context;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.tv_gain)
    TextView tv_gain;
    @BindView(R.id.tv_count_down)
    TextView tv_count_down;
    @BindView(R.id.et_email)
    SuperEditText et_email;
    @BindView(R.id.et_code)
    SuperEditText et_code;
    @BindView(R.id.et_pw1)
    SuperEditText et_pw1;
    @BindView(R.id.et_pw2)
    SuperEditText et_pw2;
    @BindView(R.id.bt_confirm)
    Button bt_confirm;
    @BindView(R.id.image_show_1)
    ImageView image_show1;
    @BindView(R.id.image_show_2)
    ImageView image_show2;
    String str_email;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    public void initData(){
        Bundle bundle = getIntent().getExtras();
        if (bundle!=null){
            str_email = bundle.getString(LoginActivity.STR_EMAIL);
            if (!TextUtils.isEmpty(str_email)){
                et_email.setText(str_email);
                et_email.setEnabled(false);
            }
        }
    }



    @Override
    public void attachPresenter() {
        super.attachPresenter();
        mPresenter=new ForgetPasswordPresenter();
        mPresenter.attachView(this);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_forget_pwd;
    }

    @Override
    public void initView() {
        context = this;
        tv_title.setText(getString(R.string.register2_reset_pass));
        Utils.setTransformationMethod(et_pw1,false);
        Utils.setTransformationMethod(et_pw2,false);
        et_code.addOnInputEndListener(s -> mPresenter.checkVerificationCode(s));
        et_email.addOnInputEndListener(s -> mPresenter.checkAccount(s));
        et_pw1.addOnInputEndListener(s -> mPresenter.checkPwd1(s));
        et_pw2.addOnInputEndListener(s -> mPresenter.checkPwd2(s));

    }

      @OnClick({R.id.tv_gain, R.id.image_show_1, R.id.image_show_2, R.id.bt_confirm})
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_gain:
                mPresenter.sendVerificationCode();
                break;
            case R.id.bt_confirm:
                 mPresenter.confirm();
                break;
            case R.id.image_show_1:
                boolean isSelected = !image_show1.isSelected();
                int curIndex = et_pw1.getSelectionStart();
                image_show1.setSelected(isSelected);
                Utils.setTransformationMethod(et_pw1,isSelected);
                et_pw1.setSelection(curIndex);
                break;
            case R.id.image_show_2:
                boolean isSelected_ = !image_show2.isSelected();
                int curIndex_ = et_pw2.getSelectionStart();
                image_show2.setSelected(isSelected_);
                Utils.setTransformationMethod(et_pw2,isSelected_);
                et_pw2.setSelection(curIndex_);
                break;
        }
    }



    @Override
    public void registerSuccess() {
        Intent intent = new Intent(ForgetPwdActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void resetPwdSuccess() {
        Intent intent = new Intent(ForgetPwdActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void unusableBtnConfirm() {
        bt_confirm.setSelected(false);
        bt_confirm.setClickable(false);
    }

    @Override
    public void reuseBtnConfirm() {
        bt_confirm.setSelected(true);
        bt_confirm.setClickable(true);
    }

    @Override
    public void onStartCountDown() {
        tv_count_down.setVisibility(View.VISIBLE);
        tv_gain.setVisibility(View.GONE);
    }

    @Override
    public void setCountDownValue(String value) {
        tv_count_down.setText(value);
    }

    @Override
    public void onCountDownFinish() {
        tv_count_down.setVisibility(View.GONE);
        tv_gain.setVisibility(View.VISIBLE);
        tv_gain.setText(R.string.resend);
    }

    @Override
    public String getAccount() {
        return et_email.getText().toString().trim();
    }

    @Override
    public String getVerificationCode() {
        return et_code.getText().toString().trim();
    }

    @Override
    public String getPwd1() {
        return et_pw1.getText().toString().trim();
    }
    @Override
    public String getPwd2() {
        return et_pw2.getText().toString().trim();
    }
}
