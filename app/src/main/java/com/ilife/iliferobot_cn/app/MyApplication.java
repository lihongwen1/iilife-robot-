package com.ilife.iliferobot_cn.app;

import android.content.Context;
import android.graphics.Typeface;

import com.accloud.cloudservice.AC;
import com.ilife.iliferobot_cn.utils.Constants;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import androidx.multidex.MultiDexApplication;
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
    public Typeface tf_english_regular;
    public Typeface tf_medium;
    public Typeface tf_itca;

    @Override
    public void onCreate() {
        super.onCreate();
        Constants.CUR_APP_ENVIRONMENT = Constants.APP_ENVIRONMENT_PRODUCT;
        switch (Constants.CUR_APP_ENVIRONMENT) {
            case Constants.APP_ENVIRONMENT_TEST:
                 //        国内测试环境
                AC.init(this, Constants.MajorDomain, Constants.MajorDomainId, AC.TEST_MODE);
                break;
            case Constants.APP_ENVIRONMENT_PRODUCT:
               //国内生产
                AC.init(this, Constants.MajorDomain, Constants.MajorDomainId);
                break;

        }
        closeAndroidPDialog();
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
        tf_english_regular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
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


    private void closeAndroidPDialog() {
//        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.P){
//            return;
//        }
        try {
            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
