package com.ilife.iliferobot.fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ilife.iliferobot.R;

/**
 * 显示返回充电座等操作
 */
public class CancleDialogFragment extends DialogFragment {
    private TextView tv_recharge, tv_finish_cleaning, tv_cancel;
    private View.OnClickListener onClickListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //DialogFragment.STYLE_NO_FRAME 没有边框，
        //R.style.dialogTheme 主要就是设置对话框内容区域外的背景色，
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.dialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialg_cancel, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onClickListener = (View.OnClickListener) context;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView(View view) {
        tv_recharge = view.findViewById(R.id.tv_back_recharge);
        tv_finish_cleaning = view.findViewById(R.id.tv_finish_cleaning);
        tv_cancel = view.findViewById(R.id.tv_cancel_dialog);
        tv_recharge.setOnClickListener(onClickListener);
        tv_finish_cleaning.setOnClickListener(onClickListener);
        tv_cancel.setOnClickListener(onClickListener);
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            //如果宽高都为MATCH_PARENT,内容外的背景色就会失效，所以只设置宽全屏
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);//全屏
            dialog.getWindow().setGravity(Gravity.BOTTOM);//内容设置在底部
            //内容的背景色.对于全屏很重要，系统的内容宽度是不全屏的，替换为自己的后宽度可以全屏
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}
