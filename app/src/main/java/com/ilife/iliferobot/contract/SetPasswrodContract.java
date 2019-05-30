package com.ilife.iliferobot.contract;

import com.ilife.iliferobot.base.BaseView;

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
