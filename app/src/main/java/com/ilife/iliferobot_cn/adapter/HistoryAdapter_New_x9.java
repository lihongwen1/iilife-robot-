package com.ilife.iliferobot_cn.adapter;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.entity.HistoryRecord_x9;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by chengjiaping on 2018/8/16.
 */

public class HistoryAdapter_New_x9 extends RecyclerView.Adapter<HistoryAdapter_New_x9.MyViewHolder> {
    Context context;
    LayoutInflater inflater;
    ArrayList<HistoryRecord_x9> records;

    public HistoryAdapter_New_x9(Context context, ArrayList<HistoryRecord_x9> records) {
        this.context = context;
        this.records = records;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(inflater.inflate(R.layout.layout_histroy_item, null));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        HistoryRecord_x9 historyRecord = records.get(position);
        holder.tv_duration.setText(historyRecord.getWork_time() / 60 + "min");
        holder.tv_area.setText(historyRecord.getClean_area() + "㎡");

        long time_ = historyRecord.getStart_time();
//        long time_ = Long.valueOf(time);
        String data = generateTime(time_, context.getString(R.string.history_adapter_month_day));
        String hour = generateTime(time_, context.getString(R.string.history_adapter_hour_minute));

        holder.tv_date.setText(data);
        holder.tv_time.setText(hour);

        holder.rl_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnClickListener.onContentClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_date;
        TextView tv_time;
        TextView tv_area;
        TextView tv_duration;
        RelativeLayout rl_content;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_date = (TextView) itemView.findViewById(R.id.tv_date);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            tv_area = (TextView) itemView.findViewById(R.id.tv_area);
            tv_duration = (TextView) itemView.findViewById(R.id.tv_duration);
            rl_content = (RelativeLayout) itemView.findViewById(R.id.rl_content);
        }
    }

    public String generateTime(long time, String strFormat) {
        SimpleDateFormat format = new SimpleDateFormat(strFormat);
        String str = format.format(new Date((time + 10) * 1000));
        return str;
    }

    public interface OnClickListener {
        void onContentClick(int position);
    }

    private OnClickListener mOnClickListener;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }
}
