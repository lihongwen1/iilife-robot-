package com.ilife.iliferobot_cn.app;

import android.content.Context;
import android.graphics.Typeface;

import androidx.multidex.MultiDexApplication;

import com.accloud.cloudservice.AC;
import com.ilife.iliferobot_cn.utils.Constants;
//import com.tencent.bugly.crashreport.CrashReport;
//import com.tencent.bugly.crashreport.CrashReport;
//import com.umeng.message.IUmengRegisterCallback;
//import com.umeng.message.PushAgent;

/**
 * Created by chenjiaping on 2017/7/6.
 */

public class MyApplication extends MultiDexApplication {
    private final String TAG = MyApplication.class.getSimpleName();
    private static MyApplication instance;
    public Typeface tf_light;
    public Typeface tf_regular;
    public Typeface tf_medium;
    public Typeface tf_itca;

    @Override
    public void onCreate() {
        super.onCreate();
        //国内测试环境
        AC.init(this, Constants.MajorDomain, Constants.MajorDomainId, AC.TEST_MODE);
        //国内生产
//        AC.init(this, Constants.MajorDomain, Constants.MajorDomainId);
        //欧洲生产
//        AC.init(this, Constants.MajorDomain, Constants.MajorDomainId);
//        AC.setRegional(AC.REGIONAL_CENTRAL_EUROPE);
        //美洲生产
//        AC.init(this, Constants.MajorDomain, Constants.MajorDomainId);
//        AC.setRegional(AC.REGIONAL_NORTH_AMERICA);
        //东南亚生产
//        AC.init(this, Constants.MajorDomain, Constants.MajorDomainId);
//        AC.setRegional(AC.REGIONAL_SOUTHEAST_ASIA);
        //注册友盟
//        PushAgent mPushAgent = PushAgent.getInstance(this);
        //注册推送服务，每次调用register方法都会回调该接口
//        mPushAgent.register(new IUmengRegisterCallback() {
//
//            @Override
//            public void onSuccess(String deviceToken) {
////                Log.e(TAG, "onSuccess: "+deviceToken);
//            }
//
//            @Override
//            public void onFailure(String s, String s1) {
////                Log.e(TAG, "onFailure: "+s+" "+s1);
//            }
//        });
//        CrashReport.initCrashReport(getApplicationContext(), "53a14e2ea6", true);
//        LogcatHelper.getInstance(this).start();
        instance = (MyApplication) getApplicationContext();
        tf_regular = Typeface.createFromAsset(getAssets(), "fonts/SourceHanSansCNRegular.ttf");
        tf_light = Typeface.createFromAsset(getAssets(), "fonts/SourceHanSansCNLight.ttf");
        tf_medium = Typeface.createFromAsset(getAssets(), "fonts/SourceHanSansCNMedium.ttf");
        tf_itca = Typeface.createFromAsset(getAssets(), "fonts/ITCAvantGardeStd-Demi.ttf");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
//        LogcatHelper.getInstance(this).stop();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        MultiDex.install(base);
    }

    public static MyApplication getInstance() {
        return instance;
    }
}
