package com.ilife.iliferobot.presenter;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACAccountMgr;
import com.accloud.service.ACException;
import com.accloud.service.ACUserInfo;
import com.ilife.iliferobot.base.BasePresenter;
import com.ilife.iliferobot.utils.MyLogger;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.contract.SetPasswrodContract;
import com.ilife.iliferobot.utils.UserUtils;
import com.ilife.iliferobot.utils.Utils;

public class SetPasswordPresenter extends BasePresenter<SetPasswrodContract.View> implements SetPasswrodContract.Presenter {
    private ACAccountMgr accountMgr;
    private boolean isPwdOk = false;
    private boolean isPwdAgainOk = false;

    @Override
    public void checkPwd(String mobile) {
        isPwdOk = mobile.length() >=6;
        if (isPwdOk && isPwdAgainOk) {
            mView.reuseBtnLogin();
        } else {
            mView.unUsableBtnLogin();
        }
    }

    @Override
    public void checkPwdAgain(String password) {
        isPwdAgainOk = password.length() >=6;
        if (isPwdOk && isPwdAgainOk) {
            mView.reuseBtnLogin();
        } else {
            mView.unUsableBtnLogin();
        }
    }

    /**
     * 注册性质的登录
     */
    @Override
    public void login() {
        if (!UserUtils.checkPassword(mView.getPw())) {
            ToastUtils.showToast(Utils.getString(R.string.register2_aty_short_char));
            return;
        }
        if (!mView.getPw().equals(mView.getPwAgain())) {
            ToastUtils.showToast(Utils.getString(R.string.register2_aty_no_same));
            return;
        }
        //TODO login business logic
        if (accountMgr == null) {
            accountMgr = AC.accountMgr();
        }
        //emai和phone可以任选其一;nickName为可选项，没有时传空字符串
        boolean isPhone = UserUtils.isPhone(mView.getPhone());
        accountMgr.register(isPhone ? "" : mView.getPhone(), isPhone ? mView.getPhone() : "", mView.getPw(), "", mView.getVerificationCode(), new PayloadCallback<ACUserInfo>() {
            @Override
            public void success(ACUserInfo userInfo) {
                //获得用户userId和nickName，由此进入主页或设备管理
                mView.goMainActivity();
            }

            @Override
            public void error(ACException e) {
                ToastUtils.showErrorToast(e.getErrorCode());
                //网络错误或其他，根据e.getErrorCode()做不同的提示或处理
            }
        });
    }
}
