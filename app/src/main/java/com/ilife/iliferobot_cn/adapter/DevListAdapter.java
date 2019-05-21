package com.ilife.iliferobot_cn.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.service.ACUserDevice;
import com.bumptech.glide.Glide;
import com.google.android.material.shadow.ShadowDrawableWrapper;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.activity.LoginActivity;
import com.ilife.iliferobot_cn.activity.SelectActivity_x;
import com.ilife.iliferobot_cn.ui.SlidingMenu;
import com.ilife.iliferobot_cn.utils.Constants;
import java.util.List;

/**
 * Created by chenjiaping on 2017/9/8.
 */

public class DevListAdapter extends RecyclerView.Adapter<DevListAdapter.MyViewHolder> {
    private List<ACUserDevice> deviceList;
    private LayoutInflater inflater;
    private Context context;

    private SlidingMenu mOpenMenu;
    private SlidingMenu mScrollingMenu;

    public SlidingMenu getScrollingMenu() {
        return mScrollingMenu;
    }

    public void setScrollingMenu(SlidingMenu scrollingMenu) {
        mScrollingMenu = scrollingMenu;
    }

    public void holdOpenMenu(SlidingMenu slidingMenu) {
        mOpenMenu = slidingMenu;
    }

    public void closeOpenMenu() {
        if (mOpenMenu != null && mOpenMenu.isOpen()) {
            mOpenMenu.closeMenu();
            mOpenMenu = null;
        }
    }

    public DevListAdapter(Context context, List<ACUserDevice> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        if (position < deviceList.size()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new MyViewHolder(inflater.inflate(R.layout.layout_add_image, parent, false), viewType);
        } else {
            return new MyViewHolder(inflater.inflate(R.layout.device_list_item, parent, false), viewType);
        }
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        int type = getItemViewType(position);
        if (type == 1) {//添加机器人按钮
            holder.iv_add_device.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO 进入选择activity
                    Intent i;
                    if (AC.accountMgr().isLogin()) {
                        i = new Intent(context, SelectActivity_x.class);
                        context.startActivity(i);
                    } else {
                        i = new Intent(context, LoginActivity.class);
                        context.startActivity(i);
                    }
                }
            });
        } else {
            String subdomain = deviceList.get(position).getSubDomain();
            if (subdomain.equals(Constants.subdomain_x785)) {
                Glide.with(context).load(R.drawable.n_x785).into(holder.image_product);
            } else if (subdomain.equals(Constants.subdomain_x787)) {
                Glide.with(context).load(R.drawable.n_x787).into(holder.image_product);
            }
            else if (subdomain.equals(Constants.subdomain_a7)) {
                Glide.with(context).load(R.drawable.n_x787).into(holder.image_product);
            } else if (subdomain.equals(Constants.subdomain_x900)) {
                Glide.with(context).load(R.drawable.n_x900).into(holder.image_product);
            } else if (subdomain.equals(Constants.subdomain_x800)) {
                Glide.with(context).load(R.drawable.n_x800).into(holder.image_product);
            } else {
                Glide.with(context).load(R.drawable.n_x800).into(holder.image_product);
            }
            String devName = deviceList.get(position).getName();
            if (TextUtils.isEmpty(devName)) {
                holder.tv_name.setText(deviceList.get(position).physicalDeviceId);
            } else {
                holder.tv_name.setText(deviceList.get(position).getName());
            }

            int states = deviceList.get(position).getStatus();
            if (states == 0) {
                holder.tv_status2.setText(context.getString(R.string.device_adapter_device_offline));
                holder.tv_status2.setTextColor(context.getResources().getColor(R.color.color_81));
            } else {
                holder.tv_status2.setText(context.getString(R.string.device_adapter_device_online));
                holder.tv_status2.setTextColor(context.getResources().getColor(R.color.color_f08300));
            }


            holder.item_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeOpenMenu();
                    if (mOnClickListener != null) {
                        mOnClickListener.onMenuClick(position);
                    }
                }
            });
            holder.slidingMenu.setCustomOnClickListener(new SlidingMenu.CustomOnClickListener() {
                @Override
                public void onClick() {
                    if (mOnClickListener != null) {
                        mOnClickListener.onContentClick(position);
                    }
                }
            });
        }
    }

    public interface OnClickListener {
        void onMenuClick(int position);

        void onContentClick(int position);
    }

    private OnClickListener mOnClickListener;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    @Override
    public int getItemCount() {
        return deviceList.size() + 1;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name;
        TextView tv_status2;
        TextView item_delete;
        ImageView image_product;
        SlidingMenu slidingMenu;
        ImageView iv_add_device;

        MyViewHolder(View itemView, int type) {
            super(itemView);
            if (type == 1) {
                iv_add_device = itemView.findViewById(R.id.iv_add_device);
            } else {
                tv_name = (TextView) itemView.findViewById(R.id.tv_name);
                tv_status2 = (TextView) itemView.findViewById(R.id.tv_status2);
                item_delete = (TextView) itemView.findViewById(R.id.item_delete);
                image_product = (ImageView) itemView.findViewById(R.id.image_product);
                slidingMenu = (SlidingMenu) itemView.findViewById(R.id.slidingMenu);

            }
        }
    }
}
