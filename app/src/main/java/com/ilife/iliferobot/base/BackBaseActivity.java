package com.ilife.iliferobot.base;

import com.ilife.iliferobot.R;

import butterknife.OnClick;

/**
 * back activity
 */
public abstract  class BackBaseActivity  <T extends BasePresenter>extends BaseActivity<T> {
    @OnClick(R.id.image_back)
    public void  clickBackBtn(){
        beforeFinish();
        finish();
    }

    @Override
    protected boolean isChildPage() {
        return true;
    }

}
