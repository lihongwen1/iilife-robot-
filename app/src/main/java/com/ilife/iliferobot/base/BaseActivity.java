package com.ilife.iliferobot.base;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.ilife.iliferobot.R;
import com.ilife.iliferobot.app.MyApplication;
import com.ilife.iliferobot.utils.DialogUtils;
import com.ilife.iliferobot.utils.MyLogger;
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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Locale.getDefault().getLanguage().equals(MyApplication.getInstance().appInitLanguage)){
            MyLogger.d("BaseActivity","app language is change");
            MyApplication.getInstance().initTypeface();
        }
//        hideBottomUIMenu();
        setContentView(getLayoutId());
        mUnBinder = ButterKnife.bind(this);
        attachPresenter();
        initView();
        setAndroidNativeLightStatusBar();
    }

    private void setAndroidNativeLightStatusBar() {
        View decor = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setNavigationBarColor(R.color.white);
    }

    protected void setNavigationBarColor(int colorId){
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
    protected boolean canGoBack(){
        return true;
    }

    protected  void beforeFinish(){

    }
    @Override
    public void onBackPressed() {
        if (!canGoBack()){//拦截返回事件
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
    protected void showLoadingDialog(){
        if (loadingDialog==null){
            loadingDialog= DialogUtils.createLoadingDialog_(this);
        }
        loadingDialog.show();
    }
    protected void hideLoadingDialog(){
        if (loadingDialog!=null&&loadingDialog.isShowing()){
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
}
