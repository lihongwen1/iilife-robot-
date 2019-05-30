package com.ilife.iliferobot.contract;

import com.ilife.iliferobot.base.BaseView;

public interface LoginContract {
    interface Model {
    }

    interface View extends BaseView {
        void unUsableBtnLogin();
        void reuseBtnLogin();
    }

    interface Presenter {
         void checkMobile(String mobile);
         void chePassword(String password);
    }
}
