package com.ilife.iliferobot_cn.holder;

import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ilife.iliferobot_cn.R;

/**
 * Created by chengjiaping on 2017/9/1.
 */

public class DeviceViewHolder extends RecyclerView.ViewHolder {
    public RelativeLayout content;
    public TextView delete;
    public LinearLayout layout;

    public ImageView image_product;
    public ImageView image_go;
    public ImageView image_delete;
    public TextView tv_name;
    public TextView tv_status;

    public DeviceViewHolder(View itemView) {
        super(itemView);
        content = (RelativeLayout) itemView.findViewById(R.id.item_content);
        delete = (TextView) itemView.findViewById(R.id.item_delete);
        layout = (LinearLayout) itemView.findViewById(R.id.item_layout);

        image_product = (ImageView) itemView.findViewById(R.id.image_product);
        image_go = (ImageView) itemView.findViewById(R.id.image_go);
        tv_name = (TextView) itemView.findViewById(R.id.tv_name);
        tv_status = (TextView) itemView.findViewById(R.id.tv_status1);
    }
}
