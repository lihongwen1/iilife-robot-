package com.ilife.iliferobot.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilife.iliferobot.R;
import com.ilife.iliferobot.base.BaseQuickAdapter;
import com.ilife.iliferobot.base.BaseViewHolder;
import com.ilife.iliferobot.entity.NewClockInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengjiaping on 2018/8/15.
 */

public class ClockAdapter extends BaseQuickAdapter<NewClockInfo, BaseViewHolder> {
    public ClockAdapter(int layoutId, @NonNull List<NewClockInfo> data) {
        super(layoutId, data);
    }


    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
        NewClockInfo info = data.get(position);
        byte open = info.getOpen();
        boolean isOpen = open == 1;
        String hour = info.getHour() < 10 ? "0" + info.getHour() : "" + info.getHour();
        String minute = info.getMinute() < 10 ? "0" + info.getMinute() : "" + info.getMinute();
        holder.setText(R.id.tv_time, hour + ":" + minute);
        holder.setSelect(R.id.tv_time, isOpen);

        holder.setText(R.id.tv_week, info.getWeek());
        holder.setSelect(R.id.tv_week, isOpen);

        holder.setSelect(R.id.image_status, isOpen);
        holder.addOnClickListener(R.id.image_status);
    }
}
