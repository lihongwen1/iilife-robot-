package com.ilife.iliferobot.adapter;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.accloud.service.ACException;
import com.accloud.service.ACUserDevice;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.able.DeviceUtils;
import com.ilife.iliferobot.activity.BindSucActivity;
import com.ilife.iliferobot.activity.MainActivity;
import com.ilife.iliferobot.activity.SelectActivity_x;
import com.ilife.iliferobot.base.BaseQuickAdapter;
import com.ilife.iliferobot.base.BaseViewHolder;
import com.ilife.iliferobot.listener.ReNameListener;
import com.ilife.iliferobot.utils.SpUtils;
import com.ilife.iliferobot.utils.ToastUtils;

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
                case Constants.subdomain_a7://A7
                case Constants.subdomain_x787:
                    holder.setImageResource(context, R.id.image_product, R.drawable.n_x787);
                    break;
                case Constants.subdomain_x800:
                    if (data.get(position).getName().contains(Constants.ROBOT_WHITE_TAG)||SpUtils.getLong(context, BindSucActivity.KEY_BIND_WHITE_DEV_ID) == data.get(position).getDeviceId()) {
                        holder.setImageResource(context, R.id.image_product, R.drawable.n_x800_white);
                    } else {
                        holder.setImageResource(context, R.id.image_product, R.drawable.n_x800);
                    }
                    break;
                case Constants.subdomain_x910:
                    holder.setImageResource(context, R.id.image_product, R.drawable.n_x910);
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
                case Constants.subdomain_v5x:
                    holder.setImageResource(context, R.id.image_product, R.drawable.n_v5x);
                    break;
                case Constants.subdomain_V3x:
                    //TODO 修改为V3 Pro的图片
                    holder.setImageResource(context, R.id.image_product, R.drawable.n_v5x);
                    break;
                default:
                    holder.setImageResource(context, R.id.image_product, R.drawable.n_x800);
                    break;
            }

            String name = data.get(position).getName();
            if (TextUtils.isEmpty(name)) {
                name = data.get(position).physicalDeviceId;
                if (SpUtils.getLong(context, BindSucActivity.KEY_BIND_WHITE_DEV_ID) == data.get(position).getDeviceId()) {
                    name += Constants.ROBOT_WHITE_TAG;
                    DeviceUtils.renameDevice(data.get(position).getDeviceId(), name, subdomain, new ReNameListener() {
                        @Override
                        public void onSuccess() {
                           SpUtils.saveLong(context,BindSucActivity.KEY_BIND_WHITE_DEV_ID,-1);
                        }

                        @Override
                        public void onError(ACException e) {
                        }
                    });
                }
                data.get(position).setName(name);
            }
            if (name.contains(Constants.ROBOT_WHITE_TAG)) {
                name = name.replace(Constants.ROBOT_WHITE_TAG, "");
            }
            holder.setText(R.id.tv_name, name);
            int states = data.get(position).getStatus();
            holder.setText(R.id.tv_status2, states == 0 ? R.string.device_adapter_device_offline : R.string.device_adapter_device_online);
            holder.setTextColor(R.id.tv_status2, states == 0 ? context.getResources().getColor(R.color.color_81) :
                    context.getResources().getColor(R.color.color_f08300));
            holder.addOnClickListener(R.id.item_delete);
        }
    }
}
