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
    private Disposable verCodeDisposable;
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
        if (!Utils.checkAccountUseful(mView.getPhone())) {
            return;
        }
        int templateId;
        if (Utils.isIlife()) {
            if (UserUtils.isPhone(mView.getPhone())) {
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
        verCodeDisposable = checkPhone().andThen(Completable.create(completableEmitter ->
                acAccountMgr.sendVerifyCode(mView.getPhone(), templateId, new VoidCallback() {
                    @Override
                    public void success() {
                        completableEmitter.onComplete();
                    }

                    @Override
                    public void error(ACException e) {
                        completableEmitter.onError(e);
                        //发送验证码失败
                    }
                }))).andThen(countDown()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
            //发送验证码成功
        }, throwable -> {
            if (throwable instanceof ACException) {
                ToastUtils.showToast(Utils.getString(R.string.login_aty_timeout));
            }
            //发送验证码失败
        });


    }

    @Override
    public Completable countDown() {
        return Completable.fromPublisher(Flowable.intervalRange(0, 59, 0, 1, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread())
                .doOnNext(aLong -> {
                    if (isViewAttached()) {
                        mView.setCountDownValue((60 - aLong) + "s");
                    }
                }).doOnComplete(() -> {
                    if (isViewAttached()) {
                        mView.onCountDownFinish();
                    }
                }).doOnSubscribe(subscription -> {
                    if (isViewAttached()) {
                        mView.onStartCountDown();
                    }
                }));
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
        if (!Utils.checkAccountUseful(mView.getPhone())) {//账户格式错误
            return;
        }
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
    public void isMobileEmpty() {
        String phone = mView.getPhone();
        if (phone != null && !phone.isEmpty()) {
            isPhoneUseful = true;
        }
        if (isPhoneUseful && isVerificationUseful) {
            mView.reUseQuickLogin();
        } else {
            mView.unUsableQuickLogin();
        }
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
        mView.onCountDownFinish();
    }

    @Override
    public void detachView() {
        super.detachView();
        if (verCodeDisposable != null && !verCodeDisposable.isDisposed()) {
            verCodeDisposable.dispose();
        }
    }
}
