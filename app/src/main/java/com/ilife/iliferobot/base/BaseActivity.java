package com.ilife.iliferobot.base;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.ilife.iliferobot.R;
import com.ilife.iliferobot.app.MyApplication;
import com.ilife.iliferobot.utils.DialogUtils;
import com.ilife.iliferobot.utils.MyLogger;
import com.ilife.iliferobot.utils.StatusBarUtil;
import com.ilife.iliferobot.utils.ToastUtils;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by chenjiaping on 2017/11/9.
 */

public abstract class BaseActivity<T extends BasePresenter> extends AppCompatActivity implements BaseView {
    protected T mPresenter;
    protected long exitTime;
    private Unbinder mUnBinder;
    private Dialog loadingDialog;
    private MyApplication application;
    private BaseActivity oContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Locale.getDefault().getLanguage().equals(MyApplication.getInstance().appInitLanguage)) {
            MyLogger.d("BaseActivity", "app language is change");
            MyApplication.getInstance().initTypeface();
        }
//        hideBottomUIMenu();
        setContentView(getLayoutId());
        mUnBinder = ButterKnife.bind(this);
        attachPresenter();
        initView();
        initData();
        setAndroidNativeLightStatusBar();
        if (application == null) {
            // 得到Application对象
            application = (MyApplication) getApplication();
        }
        oContext = this;
        addActivity();
    }

    /**
     * SYSTEM_UI_FLAG_LAYOUT_STABLE 白色图标
     * SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 黑色图标
     */
    private void setAndroidNativeLightStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
            StatusBarUtil.setColor(this, getResources().getColor(R.color.color_00));
        }
    }

    protected void setNavigationBarColor(int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(colorId));
        }
    }

    @Override
    public void attachPresenter() {

    }

    protected boolean isChildPage() {
        return false;
    }

    protected boolean canGoBack() {
        return true;
    }

    protected void beforeFinish() {

    }


    @Override
    public void onBackPressed() {
        if (!canGoBack()) {//拦截返回事件
            return;
        }
        if (!isChildPage() && System.currentTimeMillis() - exitTime >= 2000) {
            ToastUtils.showToast(this, getString(R.string.main_aty_press_exit));
            exitTime = System.currentTimeMillis();
        } else {
            beforeFinish();
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mUnBinder != null) {
            mUnBinder.unbind();
        }
        if (mPresenter != null) {
            mPresenter.detachView();
        }
        super.onDestroy();
    }

    /**
     * 隐藏虚拟按键，并且设置成全屏
     */
    protected void hideBottomUIMenu() {
        if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
            decorView.setSystemUiVisibility(uiOptions);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    protected void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = DialogUtils.createLoadingDialog_(this);
        }
        loadingDialog.show();
    }

    protected void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            DialogUtils.closeDialog(loadingDialog);
        }
    }

    /**
     * 设置布局
     *
     * @return
     */
    public abstract int getLayoutId();

    /**
     * 初始化视图
     */
    public abstract void initView();

    public void initData() {
    }


    // 添加Activity方法
    public void addActivity() {
        application.addActivity_(oContext);// 调用myApplication的添加Activity方法
    }

    //销毁当个Activity方法
    public void removeActivity() {
        application.removeActivity_(oContext);// 调用myApplication的销毁单个Activity方法
    }

    //销毁所有Activity方法
    public void removeALLActivity() {
        application.removeALLActivity_();// 调用myApplication的销毁所有Activity方法
    }

    public void removeAllActivityExclude() {
        application.removeALLActivityExclude(oContext);
    }

    /**
     * 1、获取main在窗体的可视区域
     *2、获取main在窗体的不可视区域高度
     *3、判断不可视区域高度，之前根据经验值，在有些手机上有点不大准，现改成屏幕整体高度的1/3
     *  1、大于屏幕整体高度的1/3：键盘显示  获取Scroll的窗体坐标
     * 算出main需要滚动的高度，使scroll显示   
     * 小于屏幕整体高度的1/3：键盘隐藏
     ** @param main 根布局 
     ** @param scroll 需要显示的最下方View
     */

    public static void addLayoutListener(final View main, final View scroll) {
        main.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect rect = new Rect();
            main.getWindowVisibleDisplayFrame(rect);
            int screenHeight = main.getRootView().getHeight();
            int mainInvisibleHeight = main.getRootView().getHeight() - rect.bottom;
            if (mainInvisibleHeight > screenHeight / 4) {
                int[] location = new int[2];
                scroll.getLocationInWindow(location);
                int scrollHeight = (location[1] + scroll.getHeight()) - rect.bottom;
                main.scrollTo(0, scrollHeight);
            } else {
                main.scrollTo(0, 0);
            }
        });
    }

}
