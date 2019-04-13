package com.ilife.iliferobot_cn.contract;

import com.ilife.iliferobot_cn.base.BaseView;

public interface SetPasswrodContract {
    interface Model {
    }

    interface View extends BaseView {
        void unUsableBtnLogin();
        void reuseBtnLogin();
        void goMainActivity();
        String getPw();
        String getPwAgain();
        String getPhone();
        String getVerificationCode();
    }

    interface Presenter {
        void checkPwd(String s);
        void checkPwdAgain(String s);
        void login();
    }
}
