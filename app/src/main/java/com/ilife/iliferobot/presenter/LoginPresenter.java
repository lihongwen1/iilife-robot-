package com.ilife.iliferobot.presenter;

import com.ilife.iliferobot.base.BasePresenter;
import com.ilife.iliferobot.contract.LoginContract;

public class LoginPresenter extends BasePresenter<LoginContract.View> implements LoginContract.Presenter {
    private boolean isPhoneOk=false;
    private boolean isPasswordOk=false;
    @Override
    public void checkMobile(String mobile) {
        isPhoneOk=mobile.length()>0;
        if (isPhoneOk&&isPasswordOk){
            mView.reuseBtnLogin();
        }else {
            mView.unUsableBtnLogin();
        }
    }

    @Override
    public void chePassword(String password) {
        isPasswordOk=password.length()>0;
        if (isPhoneOk&&isPasswordOk){
            mView.reuseBtnLogin();
        }else {
            mView.unUsableBtnLogin();
        }
    }
}
