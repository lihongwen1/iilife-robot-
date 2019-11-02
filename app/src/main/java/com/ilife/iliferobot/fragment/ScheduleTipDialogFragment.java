package com.ilife.iliferobot.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ilife.iliferobot.R;

public class ScheduleTipDialogFragment extends DialogFragment {
    private OnClickListener onClickListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.universal_dialog);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window window = getDialog().getWindow();
        getDialog().setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = (int) getResources().getDimension(R.dimen.dp_315);
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_shedule_tip, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        view.findViewById(R.id.tv_schedule_cancel).setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onCancelClick();
            }
        });
        view.findViewById(R.id.tv_schedule_commit).setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onCommitClick();
            }
        });

    }


//    @Override
//    public void onStart() {
//        super.onStart();
//        Dialog dialog = getDialog();
//        if (dialog != null) {
//            //如果宽高都为MATCH_PARENT,内容外的背景色就会失效，所以只设置宽全屏
//            int width = ViewGroup.LayoutParams.MATCH_PARENT;
//            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
//            dialog.getWindow().setLayout(width, height);//全屏
//            dialog.getWindow().setGravity(Gravity.BOTTOM);//内容设置在底部
//            //内容的背景色.对于全屏很重要，系统的内容宽度是不全屏的，替换为自己的后宽度可以全屏
//            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        }
//    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onCancelClick();

        void onCommitClick();
    }
}
