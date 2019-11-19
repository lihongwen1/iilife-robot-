package com.ilife.iliferobot.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACException;
import com.accloud.service.ACUserInfo;
import com.ilife.iliferobot.app.MyApplication;
import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.utils.MyLogger;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.utils.UserUtils;
import com.ilife.iliferobot.view.SuperEditText;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.contract.LoginContract;
import com.ilife.iliferobot.presenter.LoginPresenter;
import com.ilife.iliferobot.utils.SpUtils;
import com.ilife.iliferobot.utils.Utils;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by chenjiaping on 2017/7/6.
 */
//DONE
public class LoginActivity extends BackBaseActivity<LoginPresenter> implements LoginContract.View {
    private final String TAG = LoginActivity.class.getSimpleName();
    public static final String KEY_EMAIL = "KEY_EMAIL";
    Context context;
    @BindView(R.id.image_show)
    ImageView image_show;
    @BindView(R.id.et_email)
    SuperEditText et_email;
    @BindView(R.id.et_pass)
    SuperEditText et_pass;
    @BindView(R.id.bt_login)
    Button bt_login;
    public static final String IS_REGISTER = "IS_REGISTER";
    public static final String STR_EMAIL = "STR_EMAIL";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void attachPresenter() {
        super.attachPresenter();
        mPresenter = new LoginPresenter();
        mPresenter.attachView(this);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    public void initView() {
        context = this;
        if (Utils.isSupportPhone()) {
            et_email.setHint(R.string.login_aty_email_phone);
        } else {
            et_email.setHint(R.string.login_aty_email);
        }
        Utils.setTransformationMethod(et_pass, false);
        et_email.addOnInputEndListener(s -> mPresenter.checkMobile(s));
        et_pass.addOnInputEndListener(s -> mPresenter.chePassword(s));
        bt_login.setClickable(false);
    }


    @OnClick({R.id.image_show, R.id.tv_forget, R.id.bt_login})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_show:
                boolean isSelected = !image_show.isSelected();
                int curIndex = et_pass.getSelectionStart();
                image_show.setSelected(isSelected);
                Utils.setTransformationMethod(et_pass, isSelected);
                et_pass.setSelection(curIndex);
                break;
            case R.id.tv_forget:
                Intent i = new Intent(LoginActivity.this, ForgetPwdActivity.class);
                i.putExtra(IS_REGISTER, false);
                startActivity(i);
                break;
            case R.id.bt_login:
                String str_account = et_email.getText().toString().trim();
                String str_pass = et_pass.getText().toString().trim();
                if (Utils.checkAccountUseful(str_account)) {
                    login(str_account, str_pass);
                }
                break;
        }
    }

    public void login(String account, String str_pass) {
        AC.accountMgr().login(account, str_pass, new PayloadCallback<ACUserInfo>() {
            @Override
            public void success(ACUserInfo userInfo) {
//                addAbleAlia(String.valueOf(userInfo.getUserId()));
                String email = userInfo.getEmail();
                SpUtils.saveString(context, KEY_EMAIL, email);
                Intent i = new Intent(context, MainActivity.class);
                startActivity(i);
                removeActivity();
            }


            @Override
            public void error(ACException e) {
                ToastUtils.showErrorToast(context, e.getErrorCode());
            }
        });
    }

    @Override
    public void addAbleAlia(String userId) {
        //userId为用户ID，通过AbleCloud登录接口返回的ACUserInfo可以获取到userId；第二个参数写死ablecloud即可。
        PushAgent.getInstance(this).addAlias(userId, "ablecloud", new UTrack.ICallBack() {
            @Override
            public void onMessage(boolean isSuccess, String message) {
                MyLogger.d(TAG, "able关联友盟  message:" + message);
            }
        });
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
}
