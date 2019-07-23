package com.ilife.iliferobot.adapter;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.ilife.iliferobot.R;
import com.ilife.iliferobot.base.BaseQuickAdapter;
import com.ilife.iliferobot.base.BaseViewHolder;

import java.util.List;

public class HelpFeedImgAdapter extends BaseQuickAdapter<Bitmap, BaseViewHolder> {
    private Context context;
    public HelpFeedImgAdapter(Context context,int layoutId, @NonNull List<Bitmap> data) {
        super(layoutId, data);
        this.context=context;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
     holder.setImageResource(context, R.id.iv_feed_img,data.get(position));
     holder.addOnClickListener(R.id.iv_feed_img,R.id.iv_delete_img);
    }
}
