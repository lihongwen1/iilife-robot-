package com.ilife.iliferobot.adapter;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.accloud.service.ACUserDevice;
import com.bumptech.glide.Glide;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.base.BaseQuickAdapter;
import com.ilife.iliferobot.base.BaseViewHolder;
import com.ilife.iliferobot.view.SlideRecyclerView;

import java.util.List;

public class RobotListAdapter extends BaseQuickAdapter<ACUserDevice, BaseViewHolder> {
    private static int TYPE_ROBOT = 1;
    private static int TYPE_ADD = 2;
    private Context context;

    public RobotListAdapter(Context context, @NonNull List<ACUserDevice> data) {
        super(data);
        addItemType(TYPE_ADD, R.layout.layout_add_image);
        addItemType(TYPE_ROBOT, R.layout.device_list_item);
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return data.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < data.size()) {
            return TYPE_ROBOT;
        } else {
            return TYPE_ADD;
        }
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (type == TYPE_ADD) {//添加机器人按钮
            holder.addOnClickListener(R.id.iv_add_device);
        } else {
            String subdomain = data.get(position).getSubDomain();
            switch (subdomain) {
                case Constants.subdomain_x785:
                    holder.setImageResource(context, R.id.image_product, R.drawable.n_x785);
                    break;
                case Constants.subdomain_x787:
                    holder.setImageResource(context, R.id.image_product, R.drawable.n_x787);
                    break;
                case Constants.subdomain_x800:
                    holder.setImageResource(context, R.id.image_product, R.drawable.n_x800);
                    break;
                case Constants.subdomain_x900:
                    holder.setImageResource(context, R.id.image_product, R.drawable.n_x900);
                    break;
                case Constants.subdomain_a8s:
                    holder.setImageResource(context, R.id.image_product, R.drawable.n_a8s);
                    break;
                case Constants.subdomain_a9s:
                    holder.setImageResource(context, R.id.image_product, R.drawable.n_a9s);
                    break;
                case Constants.subdomain_v85:
                    holder.setImageResource(context, R.id.image_product, R.drawable.n_v85);
                    break;
                default:
                    holder.setImageResource(context, R.id.image_product,R.drawable.n_x800);
                    break;
            }
            holder.setText(R.id.tv_name, TextUtils.isEmpty(data.get(position).getName()) ? data.get(position).physicalDeviceId :
                    data.get(position).getName());
            int states = data.get(position).getStatus();
            holder.setText(R.id.tv_status2, states == 0 ? R.string.device_adapter_device_offline : R.string.device_adapter_device_online);
            holder.setTextColor(R.id.tv_status2, states == 0 ? context.getResources().getColor(R.color.color_81) :
                    context.getResources().getColor(R.color.color_f08300));
            holder.addOnClickListener(R.id.item_delete);
        }
    }
}
