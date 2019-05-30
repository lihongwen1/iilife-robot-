package com.ilife.iliferobot.adapter;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilife.iliferobot.R;
import com.ilife.iliferobot.entity.NewClockInfo;

import java.util.ArrayList;

/**
 * Created by chengjiaping on 2018/8/15.
 */

public class ClockAdapter extends RecyclerView.Adapter<ClockAdapter.MyViewHolder> {
    Context context;
    String hour;
    String minute;
    ArrayList<NewClockInfo> clockInfos;
    LayoutInflater inflater;
    OnItemClickListener mListner;

    public ClockAdapter(Context context, ArrayList<NewClockInfo> clockInfos) {
        this.context = context;
        this.clockInfos = clockInfos;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(inflater.inflate(R.layout.layout_clock_item, null));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        NewClockInfo info = clockInfos.get(position);
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListner.onItemClick(position);
            }
        });
        holder.image_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListner.onSwitchClick(position);
            }
        });
        byte open = info.getOpen();
        boolean isOpen = open == 1;
        hour = info.getHour() < 10 ? "0" + info.getHour() : "" + info.getHour();
        minute = info.getMinute() < 10 ? "0" + info.getMinute() : "" + info.getMinute();
        holder.tv_time.setText(hour + ":" + minute);
        holder.tv_time.setSelected(isOpen);
        holder.tv_week.setText(info.getWeek());
        holder.tv_week.setSelected(isOpen);
        holder.image_status.setSelected(isOpen);
    }

    @Override
    public int getItemCount() {
        return clockInfos.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_week;
        TextView tv_time;
        ImageView image_status;
        LinearLayout rootView;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_week = (TextView) itemView.findViewById(R.id.tv_week);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            image_status = (ImageView) itemView.findViewById(R.id.image_status);
            rootView = (LinearLayout) itemView.findViewById(R.id.rootView);
        }
    }


    public interface OnItemClickListener {
        void onItemClick(int position);

        void onSwitchClick(int position);
    }

    public void setListener(OnItemClickListener listener) {
        mListner = listener;
    }
}
