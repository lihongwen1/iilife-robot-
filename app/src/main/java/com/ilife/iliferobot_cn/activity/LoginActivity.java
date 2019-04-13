package com.ilife.iliferobot_cn.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACException;
import com.accloud.service.ACUserInfo;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.ToastUtils;
import com.ilife.iliferobot_cn.utils.UserUtils;
import com.ilife.iliferobot_cn.utils.Utils;

/**
 * Created by chenjiaping on 2017/7/6.
 */
//DONE
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = LoginActivity.class.getSimpleName();
    public static final String KEY_EMAIL = "KEY_EMAIL";
    public static Activity activity;
    Context context;
    ImageView image_back;
    ImageView image_show;
    TextView tv_forget;
    Button bt_login;
    EditText et_email;
    EditText et_pass;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    private void initView() {
        context = this;
        activity = this;
        image_back = (ImageView) findViewById(R.id.image_back);
        image_show = (ImageView) findViewById(R.id.image_show);
        tv_forget = (TextView) findViewById(R.id.tv_forget);
        bt_login = (Button) findViewById(R.id.bt_login);
        et_email = (EditText) findViewById(R.id.et_email);
        et_pass = (EditText) findViewById(R.id.et_pass);

        image_back.setOnClickListener(this);
        image_show.setOnClickListener(this);
        tv_forget.setOnClickListener(this);
        bt_login.setOnClickListener(this);

        Utils.setTransformationMethod(et_pass, false);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back:
                finish();
                break;
            case R.id.image_show:
                boolean isSelected = !image_show.isSelected();
                int curIndex = et_pass.getSelectionStart();
                image_show.setSelected(isSelected);
                Utils.setTransformationMethod(et_pass, isSelected);
                et_pass.setSelection(curIndex);
                break;
            case R.id.tv_forget:
                Intent i = new Intent(LoginActivity.this, RegisterActivity2.class);
                i.putExtra(RegisterActivity.IS_REGISTER, false);
                startActivity(i);
                break;
            case R.id.bt_login:
                String str_email = et_email.getText().toString().trim();
                String str_pass = et_pass.getText().toString().trim();
                if (TextUtils.isEmpty(str_email)) {
                    ToastUtils.showToast(context, getString(R.string.login_aty_input_email));
                    return;
                }
                if (TextUtils.isEmpty(str_pass)) {
                    ToastUtils.showToast(context, getString(R.string.login_aty_input_pw));
                    return;
                }
                if (!UserUtils.isEmail(str_email)) {
                    ToastUtils.showToast(context, getString(R.string.login_aty_wrong_email));
                    return;
                }

                login(str_email, str_pass);
                break;
        }
    }

    public void login(String str_email, String str_pass) {
        AC.accountMgr().login(str_email, str_pass, new PayloadCallback<ACUserInfo>() {
            @Override
            public void success(ACUserInfo userInfo) {
                String email = userInfo.getEmail();
                SpUtils.saveString(context, KEY_EMAIL, email);
                Intent i = new Intent(context, MainActivity.class);
                startActivity(i);
                finish();
            }

            @Override
            public void error(ACException e) {
                ToastUtils.showErrorToast(context, e.getErrorCode());
            }
        });
    }
}
