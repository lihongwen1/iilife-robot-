package com.ilife.iliferobot.presenter;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACException;
import com.accloud.service.ACUserInfo;
import com.ilife.iliferobot.app.MyApplication;
import com.ilife.iliferobot.base.BasePresenter;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.activity.LoginActivity;
import com.ilife.iliferobot.contract.ForgetPasswordContract;
import com.ilife.iliferobot.utils.SpUtils;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.utils.UserUtils;
import com.ilife.iliferobot.utils.Utils;

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
        isCodeOk = s.length() == 6;
        updateBtnConfirm();
    }

    @Override
    public void checkPwd1(String s) {
        isPw1Ok = s.length() >= 6;
        updateBtnConfirm();
    }

    @Override
    public void checkPwd2(String s) {
        isPw2Ok = s.length() >= 6;
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
        if (Utils.checkAccountUseful(str_email)) {
            AC.accountMgr().checkExist(str_email, new PayloadCallback<Boolean>() {
                @Override
                public void success(Boolean isExist) {
                    if (isExist) {
                        int templateId;
                        if (Utils.isIlife()) {
                            if (UserUtils.isPhone(str_email)) {
                                templateId = 1;
                            } else {
                                if (Utils.isChinaEnvironment()) {
                                    templateId = 3;
                                } else {
                                    templateId = 2;
                                }
                            }
                        } else {//ZACO
                            templateId = 1;
                        }
                        countDownDisposable = Flowable.intervalRange(0, 59, 0, 1, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).
                                observeOn(AndroidSchedulers.mainThread()).doOnNext(aLong ->
                                mView.setCountDownValue((60 - aLong) + "s")).doOnComplete(() -> mView.onCountDownFinish()).
                                doOnSubscribe(subscription -> mView.onStartCountDown()).subscribe();
                        AC.accountMgr().sendVerifyCode(str_email, templateId, new VoidCallback() {
                            @Override
                            public void success() {
//                                ToastUtils.showToast(Utils.getString(R.string.register2_aty_obtain_suc));
                            }

                            @Override
                            public void error(ACException e) {
                                ToastUtils.showErrorToast(e.getErrorCode());
                            }
                        });

                    } else {
                        ToastUtils.showToast(Utils.getString(R.string.login_aty_account_no));
                    }
                }

                @Override
                public void error(ACException e) {
                    ToastUtils.showErrorToast(e.getErrorCode());
                }
            });
        }
    }

    @Override
    public void confirm() {
        String str_email = mView.getAccount();
        if (Utils.checkAccountUseful(str_email)) {
            {
                AC.accountMgr().checkExist(str_email, new PayloadCallback<Boolean>() {
                    @Override
                    public void success(Boolean aBoolean) {
                        if (aBoolean) {
                            if (!UserUtils.checkPassword(mView.getPwd1())) {
                                ToastUtils.showToast(Utils.getString(R.string.register2_aty_short_char));
                                mView.resetPwdFail();
                                return;
                            }
                            if (!mView.getPwd1().equals(mView.getPwd2())) {
                                ToastUtils.showToast(Utils.getString(R.string.register2_aty_no_same));
                                mView.resetPwdFail();
                                return;
                            }
                            checkVerificationCode();
                        } else {
                            ToastUtils.showToast(Utils.getString(R.string.login_aty_account_no));
                            mView.resetPwdFail();
                        }
                    }

                    @Override
                    public void error(ACException e) {
                        ToastUtils.showToast(Utils.getString(R.string.login_aty_account_no));
                        mView.resetPwdFail();
                    }
                });
            }
        }
    }

    @Override
    public void checkVerificationCode() {
        if (!isViewAttached()) {
            mView.resetPwdFail();
            return;
        }
        AC.accountMgr().checkVerifyCode(mView.getAccount(), mView.getVerificationCode(), new PayloadCallback<Boolean>() {
            @Override
            public void success(Boolean result) {
                if (result) {
                    resetPassword();
                } else {
                    mView.resetPwdFail();
                    ToastUtils.showToast(Utils.getString(R.string.register2_aty_code_wrong));
                }
            }

            @Override
            public void error(ACException e) {
                mView.resetPwdFail();
            }
        });
    }

    @Override
    public void resetPassword() {
        if (!isViewAttached()) {
            mView.resetPwdFail();
            return;
        }
        AC.accountMgr().resetPassword(mView.getAccount(), mView.getPwd1(), mView.getVerificationCode(), new PayloadCallback<ACUserInfo>() {
            @Override
            public void success(ACUserInfo acUserInfo) {
                ToastUtils.showToast(Utils.getString(R.string.register2_aty_reset_suc));
                String email = acUserInfo.getEmail();
                SpUtils.saveString(MyApplication.getInstance(), LoginActivity.KEY_EMAIL, email);
                mView.resetPwdSuccess();
            }

            @Override
            public void error(ACException e) {
                mView.resetPwdFail();
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
