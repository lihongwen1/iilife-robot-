package com.ilife.iliferobot_cn.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ilife.iliferobot_cn.R;


public class UniversalDialog extends DialogFragment {
    private TextView tv_left, tv_mid, tv_right, tv_dialog_title, tv_hint_tip;
    private LinearLayout ll_normal;
    private OnLeftButtonClck onLeftButtonClck;
    private OnMidButtonClck onMidButtonClck;
    private OnRightButtonClck onRightButtonClck;
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_ONLY_MID = 2;
    private String title, hintTip,leftText,midText,rightText;
    private int type;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.universal_dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.universal_dialog, container, false);
        initView(v);
        return v;
    }

    private void initView(View v) {
        ll_normal = v.findViewById(R.id.ll_normal);
        tv_dialog_title = v.findViewById(R.id.tv_dialog_title);
        tv_hint_tip = v.findViewById(R.id.tv_dialog_hint_tip);
        tv_left = v.findViewById(R.id.tv_dialog_left);
        tv_mid = v.findViewById(R.id.tv_dialog_mid);
        tv_right = v.findViewById(R.id.tv_dialog_right);
        if (title != null && !title.isEmpty()) {
            tv_dialog_title.setText(title);
        }
        if (hintTip != null && !hintTip.isEmpty()) {
            tv_hint_tip.setText(hintTip);
        } if (leftText != null && !leftText.isEmpty()) {
            tv_left.setText(leftText);
        } if (midText != null && !midText.isEmpty()) {
            tv_mid.setText(midText);
        } if (rightText != null && !rightText.isEmpty()) {
            tv_right.setText(rightText);
        }
        if (type == TYPE_ONLY_MID) {
            ll_normal.setVisibility(View.GONE);
            tv_mid.setVisibility(View.VISIBLE);
        }
        tv_left.setOnClickListener(v1 -> {
            dismiss();
            if (onLeftButtonClck != null) {
                onLeftButtonClck.onClick();
            }
        });
        tv_right.setOnClickListener(v1 -> {
            dismiss();
            if (onRightButtonClck != null) {
                onRightButtonClck.onClick();
            }
        });
        tv_mid.setOnClickListener(v1 -> {
            dismiss();
            if (onMidButtonClck != null) {
                onMidButtonClck.onClick();
            }
        });
    }

    public interface OnLeftButtonClck {
        void onClick();
    }

    public interface OnMidButtonClck {
        void onClick();
    }

    public interface OnRightButtonClck {
        void onClick();
    }

    public UniversalDialog setOnLeftButtonClck(OnLeftButtonClck onLeftButtonClck) {
        this.onLeftButtonClck = onLeftButtonClck;
        return this;
    }

    public UniversalDialog setOnMidButtonClck(OnMidButtonClck onMidButtonClck) {
        this.onMidButtonClck = onMidButtonClck;
        return this;
    }

    public UniversalDialog setOnRightButtonClck(OnRightButtonClck onRightButtonClck) {
        this.onRightButtonClck = onRightButtonClck;
        return this;
    }

    public UniversalDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public UniversalDialog setHintTIp(String tip) {
        this.hintTip = tip;
        return this;
    }

    public UniversalDialog setMidText(String midText){
        this.midText=midText;
        return this;
    }public UniversalDialog setLeftText(String leftText){
        this.leftText=midText;
        return this;
    }

    public UniversalDialog setRightText(String rightText) {
        this.rightText=rightText;
        return this;
    }

    public UniversalDialog setDialogType(int type) {
        this.type = type;
        return this;
    }

}
