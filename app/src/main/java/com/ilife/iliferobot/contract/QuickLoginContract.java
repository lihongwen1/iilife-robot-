package com.ilife.iliferobot.contract;

import com.ilife.iliferobot.base.BaseView;

import io.reactivex.Completable;

public interface QuickLoginContract {
    interface Model {
    }

    interface View extends BaseView {
        String getPhone();
        String getVerificationCode();
        void reUseQuickLogin();
        void unUsableQuickLogin();
        void onStartCountDown();
        void setCountDownValue(String value);
        void onCountDownFinish();
        void goSetPassword();
    }

    interface Presenter {
        void sendVerification();
        void isCodeEmpty();
        void isMobileEmpty();
        Completable checkPhone();
        void checkVerificationCode();
        Completable countDown();
        void finishCountDown();
    }
}
