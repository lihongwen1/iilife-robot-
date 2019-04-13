package com.ilife.iliferobot_cn.activity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BackBaseActivity;
import com.ilife.iliferobot_cn.contract.SetPasswrodContract;
import com.ilife.iliferobot_cn.presenter.SetPasswordPresenter;
import com.ilife.iliferobot_cn.utils.Utils;
import com.ilife.iliferobot_cn.view.SuperEditText;

import butterknife.BindView;
import butterknife.OnClick;

public class SetPasswordActivity extends BackBaseActivity<SetPasswordPresenter> implements SetPasswrodContract.View {
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.et_pw)
    SuperEditText et_pw;
    @BindView(R.id.et_pw_again)
    SuperEditText et_pw_again;
    @BindView(R.id.bt_login)
    Button bt_login;
    @BindView(R.id.iv_show_pw)
    ImageView iv_show_pw;
    @BindView(R.id.iv_show_pw_again)
    ImageView iv_show_pw_again;

    @Override
    public int getLayoutId() {
        return R.layout.activity_set_password;
    }

    @Override
    public void attachPresenter() {
        super.attachPresenter();
        mPresenter = new SetPasswordPresenter();
        mPresenter.attachView(this);
    }

    @Override
    public void initView() {
        tv_title.setText(getString(R.string.set_password));
        Utils.setTransformationMethod(et_pw, false);
        Utils.setTransformationMethod(et_pw_again, false);
        et_pw.addOnInputEndListener(s -> mPresenter.checkPwd(s));
        et_pw_again.addOnInputEndListener(s -> mPresenter.checkPwdAgain(s));
    }

    @OnClick({R.id.iv_show_pw, R.id.iv_show_pw_again, R.id.bt_login})
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_show_pw:
                boolean isSelected = !iv_show_pw.isSelected();
                int curIndex = et_pw.getSelectionStart();
                iv_show_pw.setSelected(isSelected);
                Utils.setTransformationMethod(et_pw, isSelected);
                et_pw.setSelection(curIndex);
                break;
            case R.id.iv_show_pw_again:
                boolean isSelected1 = !iv_show_pw_again.isSelected();
                int curIndex1 = et_pw_again.getSelectionStart();
                iv_show_pw_again.setSelected(isSelected1);
                Utils.setTransformationMethod(et_pw_again, isSelected1);
                et_pw_again.setSelection(curIndex1);
                break;
            case R.id.bt_login:
                //check pwd login
                mPresenter.login();
                break;


        }
    }

    @Override
    public void unUsableBtnLogin() {
        bt_login.setSelected(false);
        bt_login.setClickable(false);
    }

    @Override
    public void reuseBtnLogin() {
        bt_login.setSelected(true);
        bt_login.setClickable(true);
    }

    @Override
    public String getPw() {
        return et_pw.getText().toString().trim();
    }

    @Override
    public String getPwAgain() {
        return et_pw_again.getText().toString().trim();
    }

    @Override
    public String getPhone() {
     return    getIntent().getStringExtra(QuickLoginActivity.PHONE);

    }

    @Override
    public String getVerificationCode() {
        return    getIntent().getStringExtra(QuickLoginActivity.VER_CODE);
    }

    @Override
    public void goMainActivity() {
        Intent intent=new Intent(SetPasswordActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
