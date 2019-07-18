package com.ilife.iliferobot.adapter;

import android.view.View;

import androidx.annotation.NonNull;

import com.ilife.iliferobot.R;
import com.ilife.iliferobot.base.BaseQuickAdapter;
import com.ilife.iliferobot.base.BaseViewHolder;
import com.ilife.iliferobot.model.CleanningRobot;

import java.util.List;

public class XAdapter extends BaseQuickAdapter<CleanningRobot, BaseViewHolder> {
    public XAdapter(int layoutId, @NonNull List<CleanningRobot> data) {
        super(layoutId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
        holder.setImageResource(R.id.image_product, data.get(position).getImg());
        holder.setText(R.id.tv_product, data.get(position).getName());
    }
}
