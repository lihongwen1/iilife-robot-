package com.ilife.iliferobot_cn.base;

import com.ilife.iliferobot_cn.R;

import butterknife.OnClick;

/**
 * back activity
 */
public abstract  class BackBaseActivity  <T extends BasePresenter>extends BaseActivity<T> {
    @OnClick(R.id.image_back)
    public void  clickBackBtn(){
        finish();
    }

    @Override
    protected boolean isChildPage() {
        return true;
    }

}
