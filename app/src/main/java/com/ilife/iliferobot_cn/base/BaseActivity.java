package com.ilife.iliferobot_cn.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.utils.ToastUtils;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by chenjiaping on 2017/11/9.
 */

public abstract class BaseActivity<T extends BasePresenter> extends AppCompatActivity implements BaseView{
    protected T mPresenter;
    protected long exitTime;
    private Unbinder mUnBinder;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        mUnBinder =ButterKnife.bind(this);
        attachPresenter();
        initView();
        setAndroidNativeLightStatusBar(this,true);
    }
    private static void setAndroidNativeLightStatusBar(Activity activity, boolean dark) {
        View decor = activity.getWindow().getDecorView();
        if (dark) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    @Override
    public void attachPresenter() {

    }

    protected void backImageClick(){
        finish();
    }

    protected boolean isChildPage(){
        return false;
    };
    @Override
    public void onBackPressed() {
        if (!isChildPage()&&System.currentTimeMillis() - exitTime >= 2000) {
            ToastUtils.showToast(this, getString(R.string.main_aty_press_exit));
            exitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mUnBinder !=null){
            mUnBinder.unbind();
        }
        if (mPresenter!=null){
            mPresenter.detachView();
        }
        super.onDestroy();
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
