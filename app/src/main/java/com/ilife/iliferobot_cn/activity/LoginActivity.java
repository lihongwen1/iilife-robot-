package com.ilife.iliferobot_cn.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACException;
import com.accloud.service.ACUserInfo;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BackBaseActivity;
import com.ilife.iliferobot_cn.contract.LoginContract;
import com.ilife.iliferobot_cn.presenter.LoginPresenter;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.ToastUtils;
import com.ilife.iliferobot_cn.utils.UserUtils;
import com.ilife.iliferobot_cn.utils.Utils;
import com.ilife.iliferobot_cn.view.SuperEditText;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by chenjiaping on 2017/7/6.
 */
//DONE
public class LoginActivity extends BackBaseActivity<LoginPresenter> implements LoginContract.View {
    private final String TAG = LoginActivity.class.getSimpleName();
    public static final String KEY_EMAIL = "KEY_EMAIL";
    public static Activity activity;
    Context context;
    @BindView(R.id.image_show)
    ImageView image_show;
    @BindView(R.id.et_email)
    SuperEditText et_email;
    @BindView(R.id.et_pass)
    SuperEditText et_pass;
    @BindView(R.id.bt_login)
    Button bt_login;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void attachPresenter() {
        super.attachPresenter();
       mPresenter=new LoginPresenter();
       mPresenter.attachView(this);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    public void initView() {
        context = this;
        activity = this;
        Utils.setTransformationMethod(et_pass,false);
        et_email.addOnInputEndListener(s -> {
              mPresenter.checkMobile(s);
        });
        et_pass.addOnInputEndListener(s -> mPresenter.chePassword(s));
    }


    @OnClick({R.id.image_show, R.id.tv_forget, R.id.bt_login})
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image_show:
                boolean isSelected = !image_show.isSelected();
                int curIndex = et_pass.getSelectionStart();
                image_show.setSelected(isSelected);
                Utils.setTransformationMethod(et_pass,isSelected);
                et_pass.setSelection(curIndex);
                break;
            case R.id.tv_forget:
                Intent i = new Intent(LoginActivity.this, RegisterActivity2.class);
                i.putExtra(RegisterActivity.IS_REGISTER,false);
                startActivity(i);
                break;
            case R.id.bt_login:
                String str_account = et_email.getText().toString().trim();
                String str_pass = et_pass.getText().toString().trim();
                if (TextUtils.isEmpty(str_account)){
                    ToastUtils.showToast(context,getString(R.string.login_aty_input_email));
                    return;
                }
                if (TextUtils.isEmpty(str_pass)){
                    ToastUtils.showToast(context,getString(R.string.login_aty_input_pw));
                    return;
                }
                if (!UserUtils.isEmail(str_account)&&!UserUtils.isPhone(str_account)){
                    ToastUtils.showToast(context,getString(R.string.login_aty_wrong_email));
                    return;
                }
                login(str_account,str_pass);
                break;
        }
    }

    public void login(String account,String str_pass){
        AC.accountMgr().login(account, str_pass, new PayloadCallback<ACUserInfo>() {
            @Override
            public void success(ACUserInfo userInfo) {
                String email = userInfo.getEmail();
                SpUtils.saveString(context,KEY_EMAIL,email);
                Intent i = new Intent(context, MainActivity.class);
                startActivity(i);
                finish();
            }

            @Override
            public void error(ACException e) {
                ToastUtils.showErrorToast(context,e.getErrorCode());
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
