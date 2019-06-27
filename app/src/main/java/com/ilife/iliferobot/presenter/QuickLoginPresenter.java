package com.ilife.iliferobot.presenter;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACAccountMgr;
import com.accloud.service.ACException;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.app.MyApplication;
import com.ilife.iliferobot.base.BasePresenter;
import com.ilife.iliferobot.contract.QuickLoginContract;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.utils.UserUtils;
import com.ilife.iliferobot.utils.Utils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class QuickLoginPresenter extends BasePresenter<QuickLoginContract.View> implements QuickLoginContract.Presenter {
    private boolean isPhoneUseful;
    private boolean isVerificationUseful;
    private Disposable verCodeDisposable, registerDisposable;
    private ACAccountMgr acAccountMgr;

    @Override
    public void attachView(QuickLoginContract.View view) {
        super.attachView(view);
        if (acAccountMgr == null) {
            acAccountMgr = AC.accountMgr();
        }
    }

    @Override
    public void sendVerification() {
        //send code by cloud
        if (!isPhoneUseful) {
            return;
        }
        verCodeDisposable = checkPhone().andThen(Completable.create(completableEmitter ->
                acAccountMgr.sendVerifyCode(mView.getPhone(), 1, new VoidCallback() {
                    @Override
                    public void success() {
                        completableEmitter.onComplete();
                    }

                    @Override
                    public void error(ACException e) {
                        completableEmitter.onError(new Exception(Utils.getString(R.string.login_aty_timeout)));
                        //发送验证码失败
                    }
                }))).andThen(countDown()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
            //发送验证码成功
        }, throwable -> {
            ToastUtils.showToast(throwable.getMessage());
            //发送验证码失败
        });


    }

    @Override
    public Completable countDown() {
        return Completable.fromPublisher(Flowable.intervalRange(1, 60, 0, 1, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread())
                .doOnNext(aLong -> mView.setCountDownValue(Long.toString(60 - aLong) + "s")).doOnComplete(() -> mView.onCountDownFinish()).doOnSubscribe(subscription -> mView.onStartCountDown()));
    }

    @Override
    public void isMobileUseful() {
        if (mView.getPhone().isEmpty()) {
            ToastUtils.showToast(Utils.getString(R.string.regist_wrong_account));
            isPhoneUseful = false;
            return;
        }
        if (!UserUtils.isPhone(mView.getPhone()) && !UserUtils.isEmail(mView.getPhone())) {
            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.regist_wrong_account));
            isPhoneUseful = false;
        } else {
            isPhoneUseful = true;
        }
    }

    @Override
    public Completable checkPhone() {
        return Completable.create(completableEmitter -> acAccountMgr.checkExist(mView.getPhone(), new PayloadCallback<Boolean>() {
            @Override
            public void success(Boolean isExit) {
                if (!isExit) {
                    completableEmitter.onComplete();
                } else {
                    completableEmitter.onError(new Exception(Utils.getString(R.string.register_aty_email_registered)));
                    //账号已经存在
                }
            }

            @Override
            public void error(ACException e) {
                completableEmitter.onError(e);
            }
        }));
    }

    @Override
    public void checkVerificationCode() {
        acAccountMgr.checkVerifyCode(mView.getPhone(), mView.getVerificationCode(), new PayloadCallback<Boolean>() {
            @Override
            public void success(Boolean result) {
                if (result) {
                    //注册
                    mView.goSetPassword();
                } else {
                    ToastUtils.showToast(Utils.getString(R.string.register2_aty_code_wrong));
                    //提示验证码错误
                }
            }

            @Override
            public void error(ACException e) {
                //网络错误或其他，根据e.getErrorCode()做不同的提示或处理
            }
        });
    }

    @Override
    public void isCodeEmpty() {
        //check code by cloud
        isVerificationUseful = mView.getVerificationCode().length() == 6;
        if (isPhoneUseful && isVerificationUseful) {
            mView.reUseQuickLogin();
        } else {
            mView.unUsableQuickLogin();
        }
    }

    @Override
    public void finishCountDown() {
        if (verCodeDisposable != null && !verCodeDisposable.isDisposed()) {
            verCodeDisposable.dispose();
        }
        if (registerDisposable != null && !registerDisposable.isDisposed()) {
            registerDisposable.dispose();
        }
        mView.onCountDownFinish();
    }

    @Override
    public void detachView() {
        super.detachView();
        if (verCodeDisposable != null && !verCodeDisposable.isDisposed()) {
            verCodeDisposable.dispose();
        }
        if (registerDisposable != null && !registerDisposable.isDisposed()) {
            registerDisposable.dispose();
        }
    }
}
