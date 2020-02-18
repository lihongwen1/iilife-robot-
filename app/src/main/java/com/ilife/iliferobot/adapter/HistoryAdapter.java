package com.ilife.iliferobot.adapter;

import android.view.View;

import androidx.annotation.NonNull;

import com.ilife.iliferobot.BuildConfig;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.base.BaseQuickAdapter;
import com.ilife.iliferobot.base.BaseViewHolder;
import com.ilife.iliferobot.entity.HistoryRecord_x9;
import com.ilife.iliferobot.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HistoryAdapter extends BaseQuickAdapter<HistoryRecord_x9, BaseViewHolder> {
    public HistoryAdapter(int layoutId, @NonNull List<HistoryRecord_x9> data) {
        super(layoutId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
        HistoryRecord_x9 historyRecord = data.get(position);
        holder.setText(R.id.tv_duration, historyRecord.getWork_time() / 60 + "min");
        holder.setText(R.id.tv_area, historyRecord.getClean_area() + "㎡");
        long time_ = historyRecord.getStart_time();
        if (BuildConfig.BRAND.equals(Constants.BRAND_ZACO)) {
            holder.setText(R.id.tv_date, generateTime(time_,"dd/MM/yyyy"));
            holder.setText(R.id.tv_time, generateTime(time_, Utils.getString(R.string.history_adapter_hour_minute)));
        } else {
            holder.setText(R.id.tv_date, generateTime(time_, Utils.getString(R.string.history_adapter_month_day)));
            holder.setText(R.id.tv_time, generateTime(time_, Utils.getString(R.string.history_adapter_hour_minute)));
        }
    }


    public String generateTime(long time, String strFormat) {
        SimpleDateFormat format = new SimpleDateFormat(strFormat);
        String str = format.format(new Date((time + 10) * 1000));
        return str;
    }
}
