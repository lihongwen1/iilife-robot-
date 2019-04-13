package com.ilife.iliferobot_cn.interface_;

import android.view.View;

/**
 * Created by chengjiaping on 2017/8/31.
 */

public interface OnItemClickListener {
    /**
     * item点击回调
     *
     * @param view
     * @param position
     */
    void onItemClick(View view, int position);

    /**
     * 删除按钮回调
     *
     * @param position
     */
    void onDeleteClick(int position);
}
