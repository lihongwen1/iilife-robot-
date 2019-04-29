package com.ilife.iliferobot_cn.presenter;

import android.text.TextUtils;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACException;
import com.accloud.service.ACUserInfo;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.activity.LoginActivity;
import com.ilife.iliferobot_cn.app.MyApplication;
import com.ilife.iliferobot_cn.base.BasePresenter;
import com.ilife.iliferobot_cn.contract.ForgetPasswordContract;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.ToastUtils;
import com.ilife.iliferobot_cn.utils.UserUtils;
import com.ilife.iliferobot_cn.utils.Utils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ForgetPasswordPresenter extends BasePresenter<ForgetPasswordContract.View> implements ForgetPasswordContract.Presenter {
    private boolean isAccountOk, isCodeOk, isPw1Ok, isPw2Ok;
    private Disposable countDownDisposable;

    @Override
    public void checkAccount(String s) {
        isAccountOk = s.length() > 0;
        updateBtnConfirm();
    }

    @Override
    public void checkVerificationCode(String s) {
        isCodeOk = s.length() > 0;
        updateBtnConfirm();
    }

    @Override
    public void checkPwd1(String s) {
        isPw1Ok = s.length() > 0;
        updateBtnConfirm();
    }

    @Override
    public void checkPwd2(String s) {
        isPw2Ok = s.length() > 0;
        updateBtnConfirm();
    }

    @Override
    public void updateBtnConfirm() {
        if (isPw2Ok && isPw1Ok && isCodeOk && isAccountOk) {
            mView.reuseBtnConfirm();
        } else {
            mView.unusableBtnConfirm();
        }
    }

    @Override
    public void sendVerificationCode() {
        String str_email = mView.getAccount();
        if (TextUtils.isEmpty(str_email)) {
            ToastUtils.showToast(Utils.getString(R.string.login_aty_input_email));
            return;
        }
        if (UserUtils.isEmail(str_email) || UserUtils.isPhone(str_email)) {
            AC.accountMgr().checkExist(str_email, new PayloadCallback<Boolean>() {
                @Override
                public void success(Boolean isExist) {
                    if (isExist) {
                        countDownDisposable = Flowable.intervalRange(1, 60, 0, 1, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).
                                observeOn(AndroidSchedulers.mainThread()).doOnNext(aLong ->
                                mView.setCountDownValue(Long.toString(60 - aLong) + "s")).doOnComplete(() -> mView.onCountDownFinish()).
                                doOnSubscribe(subscription -> mView.onStartCountDown()).subscribe();
                        AC.accountMgr().sendVerifyCode(str_email, Constants.EMAIL_MODE_Europe, new VoidCallback() {
                            @Override
                            public void success() {
                                ToastUtils.showToast(Utils.getString(R.string.register2_aty_obtain_suc));
                            }

                            @Override
                            public void error(ACException e) {
                                ToastUtils.showErrorToast(e.getErrorCode());
                            }
                        });

                    } else {
                        ToastUtils.showToast(Utils.getString(R.string.register2_aty_not_register));
                    }
                }

                @Override
                public void error(ACException e) {
                    ToastUtils.showErrorToast(e.getErrorCode());
                }
            });
        } else {
            ToastUtils.showToast(Utils.getString(R.string.login_aty_wrong_email));
        }
    }

    @Override
    public void confirm(boolean isRegister) {
        if (!UserUtils.checkPassword(mView.getPwd1())) {
            ToastUtils.showToast(Utils.getString(R.string.register2_aty_short_char));
            return;
        }
        if (!mView.getPwd1().equals(mView.getPwd2())) {
            ToastUtils.showToast(Utils.getString(R.string.register2_aty_no_same));
            return;
        }
        checkVerificationCode(isRegister);
    }

    @Override
    public void checkVerificationCode(boolean isRegister) {
        AC.accountMgr().checkVerifyCode(mView.getAccount(), mView.getVerificationCode(), new PayloadCallback<Boolean>() {
            @Override
            public void success(Boolean result) {
                if (result) {
                    if (isRegister) {
                        register();
                    } else {
                        resetPassword();
                    }
                } else {
                    ToastUtils.showToast(Utils.getString(R.string.register2_aty_code_wrong));
                }
            }

            @Override
            public void error(ACException e) {

            }
        });
    }

    @Override
    public void register() {
        AC.accountMgr().register(mView.getAccount(), "", mView.getPwd1(), "", mView.getVerificationCode(), new PayloadCallback<ACUserInfo>() {
            @Override
            public void success(ACUserInfo userInfo) {
                ToastUtils.showToast(Utils.getString(R.string.register2_aty_register_suc));
                String email = userInfo.getEmail();
                SpUtils.saveString(MyApplication.getInstance(), LoginActivity.KEY_EMAIL, email);
            }

            @Override
            public void error(ACException e) {
                ToastUtils.showErrorToast(e.getErrorCode());
            }
        });
    }

    @Override
    public void resetPassword() {
        AC.accountMgr().resetPassword(mView.getAccount(), mView.getPwd1(), mView.getVerificationCode(), new PayloadCallback<ACUserInfo>() {
            @Override
            public void success(ACUserInfo acUserInfo) {
                ToastUtils.showToast(Utils.getString(R.string.register2_aty_reset_suc));
                String email = acUserInfo.getEmail();
                SpUtils.saveString(MyApplication.getInstance(), LoginActivity.KEY_EMAIL, email);

            }

            @Override
            public void error(ACException e) {
                ToastUtils.showErrorToast(e.getErrorCode());
            }
        });
    }

    @Override
    public void detachView() {
        if (countDownDisposable != null && !countDownDisposable.isDisposed()) {
            countDownDisposable.dispose();
        }
        super.detachView();
    }
}