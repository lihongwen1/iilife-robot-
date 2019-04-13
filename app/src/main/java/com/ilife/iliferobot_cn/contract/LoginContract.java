package com.ilife.iliferobot_cn.contract;

import com.ilife.iliferobot_cn.base.BaseView;

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
