package com.ilife.iliferobot_cn.contract;

import com.ilife.iliferobot_cn.base.BaseView;

public interface ForgetPasswordContract {
    interface Model {
    }

    interface View extends BaseView {
        void unusableBtnConfirm();
        void reuseBtnConfirm();
        void onStartCountDown();
        void setCountDownValue(String value);
        void onCountDownFinish();
        String getAccount();
        String getVerificationCode();
        String getPwd1();
        String getPwd2();
        void registerSuccess();
        void resetPwdSuccess();

    }

    interface Presenter {
        void checkAccount(String s);
        void checkVerificationCode(String s);
        void checkPwd1(String s);
        void checkPwd2(String s);
        void updateBtnConfirm();
        void sendVerificationCode();
        void checkVerificationCode(boolean isRegist);
        void register();
        void resetPassword();
        void confirm(boolean isRegister);
    }
}
