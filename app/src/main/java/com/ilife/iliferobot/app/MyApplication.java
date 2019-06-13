package com.ilife.iliferobot.app;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import com.accloud.cloudservice.AC;
import com.ilife.iliferobot.BuildConfig;
import com.ilife.iliferobot.utils.Constants;
import com.ilife.iliferobot.utils.toast.Toasty;
import com.tencent.bugly.crashreport.CrashReport;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import androidx.multidex.MultiDexApplication;


/**
 * Created by chenjiaping on 2017/7/6.
 */

public class MyApplication extends MultiDexApplication {
    private final String TAG = MyApplication.class.getSimpleName();
    private static MyApplication instance;
    public Typeface tf_light;
    public Typeface tf_regular;
    public Typeface tf_english_regular;
    public Typeface avantGard;
    public Typeface tf_medium;
    public Typeface tf_itca;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MyApplication", getResources().getConfiguration().screenWidthDp + "----" + getResources().getConfiguration().screenHeightDp + "-----" + getResources().getConfiguration().densityDpi);
        Log.d("MyApplication",BuildConfig.Area+"---");
        instance = (MyApplication) getApplicationContext();
        if (BuildConfig.environment.equalsIgnoreCase("product")) {//生产环境
            AC.init(this, Constants.MajorDomain, Constants.MajorDomainId);
        } else { //测试环境
            AC.init(this, Constants.MajorDomain, Constants.MajorDomainId,AC.TEST_MODE);
        }
        switch (BuildConfig.Area) {
            case 0:
                AC.setRegional(AC.REGIONAL_CHINA);
                break;
            case 1:
                AC.setRegional(AC.REGIONAL_SOUTHEAST_ASIA);
                break;
            case 3:
                AC.setRegional(AC.REGIONAL_NORTH_AMERICA);
                break;
            case 4:
                AC.setRegional(AC.REGIONAL_CENTRAL_EUROPE);
                break;
        }
        closeAndroidPDialog();
        configToast();
        initTypeface();
        CrashReport.initCrashReport(getApplicationContext(), "76637b4e00", false);
    }

    private void initTypeface(){
        tf_regular = Typeface.createFromAsset(getAssets(), "fonts/SourceHanSansCNRegular.ttf");
        tf_light = Typeface.createFromAsset(getAssets(), "fonts/SourceHanSansCNLight.ttf");
        tf_medium = Typeface.createFromAsset(getAssets(), "fonts/SourceHanSansCNMedium.ttf");
        tf_itca = Typeface.createFromAsset(getAssets(), "fonts/ITCAvantGardeStd-Demi.ttf");
        tf_english_regular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        avantGard = Typeface.createFromAsset(getAssets(), "fonts/ITCAvantGardeStd-Demi.ttf");
    }
    private void configToast() {
        Toasty.Config.getInstance().tintIcon(true).tintIcon(false).
                setTextSize(16).allowQueue(false).apply();
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
