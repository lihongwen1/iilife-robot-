package com.ilife.iliferobot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ilife.iliferobot.R;
import com.ilife.iliferobot.utils.Utils;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by chengjiaping on 2018/8/9.
 */
//DONE
public class X_seriesAdapter extends RecyclerView.Adapter<X_seriesAdapter.MyViewHolder> {
    OnItemClickListener mListener;
    LayoutInflater inflater;
    int[] drawables;
    String[] names;

    public X_seriesAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        if (Utils.isIlife()) {
            drawables = new int[]{R.drawable.n_x900, R.drawable.n_x800, R.drawable.n_x787, R.drawable.n_x785};
            names = new String[]{"ILIFE X900", "ILIFE X800", "ILIFE X787", "ILIFE X785"};
        }else {
            drawables = new int[]{R.drawable.n_a9s,R.drawable.n_a8s};
            names=new String[]{"ZACO A9s","ZACO A8s"};
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(inflater.inflate(R.layout.x_series_item, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(position);
            }
        });
        holder.image_product.setImageResource(drawables[position]);
        holder.tv_product.setText(names[position]);
    }

    @Override
    public int getItemCount() {
        return names.length;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView image_product;
        TextView tv_product;

        MyViewHolder(View itemView) {
            super(itemView);
            image_product = (ImageView) itemView.findViewById(R.id.image_product);
            tv_product = (TextView) itemView.findViewById(R.id.tv_product);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
}
