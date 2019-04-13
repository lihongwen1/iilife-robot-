package com.ilife.iliferobot_cn.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACException;
import com.accloud.service.ACUserInfo;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.ToastUtils;
import com.ilife.iliferobot_cn.utils.UserUtils;
import com.ilife.iliferobot_cn.utils.Utils;

/**
 * Created by chengjiaping on 2018/8/6.
 */
//DONE
public class RegisterActivity2 extends BaseActivity implements View.OnClickListener {
    private final String TAG = RegisterActivity2.class.getSimpleName();
    final int STATUS_GAIN_CODE = 0X01;
    final int STATUS_GAIN_DONE = 0X02;
    Context context;
    TextView tv_title;
    TextView tv_gain;
    EditText et_email;
    EditText et_code;
    EditText et_pw1;
    EditText et_pw2;
    Button bt_confirm;

    ImageView image_show1;
    ImageView image_show2;
    ImageView image_back;

    String str_email;
    boolean isRegister;
    CountDownTimer timer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);
        initView();
        initData();
    }

    public void initData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            str_email = bundle.getString(RegisterActivity.STR_EMAIL);
            if (!TextUtils.isEmpty(str_email)) {
                et_email.setText(str_email);
                et_email.setEnabled(false);
            }
            isRegister = bundle.getBoolean(RegisterActivity.IS_REGISTER);
            tv_title.setText(isRegister ? getString(R.string.login_aty_register) : getString(R.string.register2_reset_pass));
            if (isRegister) {
                et_pw1.setHint(R.string.login_aty_pw);
                et_pw2.setHint(R.string.register_aty_confirm);
            }
        }
        timer = new CountDownTimer(60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                setGainTvStatus(STATUS_GAIN_CODE, millisUntilFinished);
            }

            @Override
            public void onFinish() {
                setGainTvStatus(STATUS_GAIN_DONE, 0);
            }
        };
    }

    public void setGainTvStatus(int status, long millisUntilFinished) {
        switch (status) {
            case STATUS_GAIN_CODE:
                if (tv_gain.isClickable()) {
                    tv_gain.setClickable(false);
//                    tv_gain.setBackgroundColor(getResources().getColor(R.color.color_ef8200));
                }
//                tv_gain.setText(context.getString(R.string.register2_gain_code_second,"("+millisUntilFinished/1000+")"));
                tv_gain.setText(String.valueOf(millisUntilFinished / 1000));
                break;
            case STATUS_GAIN_DONE:
                tv_gain.setClickable(true);
//                tv_gain.setBackgroundColor(getResources().getColor(R.color.color_81));
                tv_gain.setText(context.getString(R.string.register2_gain_code));
                break;
        }
    }

    private void initView() {
        context = this;
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_gain = (TextView) findViewById(R.id.tv_gain);
        et_email = (EditText) findViewById(R.id.et_email);
        et_code = (EditText) findViewById(R.id.et_code);
        et_pw1 = (EditText) findViewById(R.id.et_pw1);
        et_pw2 = (EditText) findViewById(R.id.et_pw2);
        image_show1 = (ImageView) findViewById(R.id.image_show_1);
        image_show2 = (ImageView) findViewById(R.id.image_show_2);
        image_back = (ImageView) findViewById(R.id.image_back);
        bt_confirm = (Button) findViewById(R.id.bt_confirm);

        image_show1.setOnClickListener(this);
        image_show2.setOnClickListener(this);
        image_back.setOnClickListener(this);
        bt_confirm.setOnClickListener(this);
        Utils.setTransformationMethod(et_pw1, false);
        Utils.setTransformationMethod(et_pw2, false);
        tv_gain.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back:
                finish();
                break;
            case R.id.tv_gain:
                str_email = et_email.getText().toString();
                if (TextUtils.isEmpty(str_email)) {
                    ToastUtils.showToast(context, getString(R.string.login_aty_input_email));
                    return;
                }
//                if (UserUtils.isEmail(str_email)){
                if (et_email.isEnabled()) {
                    AC.accountMgr().checkExist(str_email, new PayloadCallback<Boolean>() {
                        @Override
                        public void success(Boolean isExist) {
                            if (isExist) {
                                timer.start();
                                getVerifyCode(str_email);
                            } else {
                                ToastUtils.showToast(context, getString(R.string.register2_aty_not_register));
                            }
                        }

                        @Override
                        public void error(ACException e) {
                            ToastUtils.showErrorToast(context, e.getErrorCode());
                        }
                    });
                } else {
                    timer.start();
                    getVerifyCode(str_email);
                }
//                } else {
//                    ToastUtils.showToast(context,getString(R.string.login_aty_wrong_email));
//                }
                break;
            case R.id.bt_confirm:
                String email = et_email.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    ToastUtils.showToast(context, getString(R.string.login_aty_input_email));
                    return;
                }
                if (et_email.isEnabled()) {
                    AC.accountMgr().checkExist(email, new PayloadCallback<Boolean>() {
                        @Override
                        public void success(Boolean isExit) {
                            if (isExit) {
                                next();
                            } else {
                                ToastUtils.showToast(context, getString(R.string.register2_aty_not_register));
                            }
                        }

                        @Override
                        public void error(ACException e) {
//                        ToastUtils.showToast(context,getString(R.string.register2_aty_error_char));
//                    } else {
//                        ToastUtils.showToast(context,getString(R.string.register2_aty_short_char));
//                    }
                            ToastUtils.showErrorToast(context, e.getErrorCode());
                        }
                    });
                } else {
                    next();
                }


                break;
            case R.id.image_show_1:
                boolean isSelected = !image_show1.isSelected();
                int curIndex = et_pw1.getSelectionStart();
                image_show1.setSelected(isSelected);
                Utils.setTransformationMethod(et_pw1, isSelected);
                et_pw1.setSelection(curIndex);
                break;
            case R.id.image_show_2:
                boolean isSelected_ = !image_show2.isSelected();
                int curIndex_ = et_pw2.getSelectionStart();
                image_show2.setSelected(isSelected_);
                Utils.setTransformationMethod(et_pw2, isSelected_);
                et_pw2.setSelection(curIndex_);
                break;
        }
    }

    private void getVerifyCode(String str_email) {
        AC.accountMgr().sendVerifyCode(str_email, Constants.EMAIL_MODE_InLand_TEST, new VoidCallback() {
            @Override
            public void success() {
                ToastUtils.showToast(context, getString(R.string.register2_aty_obtain_suc));
            }

            @Override
            public void error(ACException e) {
                ToastUtils.showErrorToast(context, e.getErrorCode());
            }
        });
    }

    //检查验证码是否正确
    public void checkVerifyCode(final String str_code, final String str_pass) {
        AC.accountMgr().checkVerifyCode(str_email, str_code, new PayloadCallback<Boolean>() {
            @Override
            public void success(Boolean result) {
                if (result) {
                    if (isRegister) {
                        register(str_code, str_pass);
                    } else {
                        resetPass(str_code, str_pass);
                    }
                } else {
                    ToastUtils.showToast(context, getString(R.string.register2_aty_code_wrong));
                }
            }

            @Override
            public void error(ACException e) {

            }
        });
    }

    public void register(String str_code, String str_pass) {
        AC.accountMgr().register(str_email, "", str_pass, "", str_code, new PayloadCallback<ACUserInfo>() {
            @Override
            public void success(ACUserInfo userInfo) {
                ToastUtils.showToast(context, getString(R.string.register2_aty_register_suc));
                String email = userInfo.getEmail();
                SpUtils.saveString(context, LoginActivity.KEY_EMAIL, email);
                Intent intent = new Intent(RegisterActivity2.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void error(ACException e) {
                ToastUtils.showErrorToast(context, e.getErrorCode());
            }
        });
    }

    public void resetPass(String str_code, String str_pass) {
        AC.accountMgr().resetPassword(str_email, str_pass, str_code, new PayloadCallback<ACUserInfo>() {
            @Override
            public void success(ACUserInfo acUserInfo) {
                ToastUtils.showToast(context, getString(R.string.register2_aty_reset_suc));
                String email = acUserInfo.getEmail();
                SpUtils.saveString(context, LoginActivity.KEY_EMAIL, email);
                Intent intent = new Intent(RegisterActivity2.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void error(ACException e) {
                ToastUtils.showErrorToast(context, e.getErrorCode());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
    }

    public void next() {
        String code = et_code.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            ToastUtils.showToast(context, getString(R.string.register2_input_code));
            return;
        }
        String str_pw1 = et_pw1.getText().toString().trim();
        if (TextUtils.isEmpty(str_pw1)) {
            ToastUtils.showToast(context, getString(R.string.login_aty_input_pw));
            return;
        }
        if (!UserUtils.checkPassword(str_pw1)) {
            ToastUtils.showToast(context, getString(R.string.register2_aty_short_char));
            return;
        }
        String str_pw2 = et_pw2.getText().toString();
        if (TextUtils.isEmpty(str_pw2)) {
            ToastUtils.showToast(context, getString(R.string.register2_input_pw2));
            return;
        }
        if (!str_pw1.equals(str_pw2)) {
            ToastUtils.showToast(context, getString(R.string.register2_aty_no_same));
            return;
        }
        checkVerifyCode(code, str_pw1);
    }
}
