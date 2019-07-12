package com.ilife.iliferobot.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ilife.iliferobot.R;
import com.ilife.iliferobot.utils.UserUtils;


public class UniversalDialog extends DialogFragment {
    private TextView tv_left, tv_mid, tv_right, tv_dialog_title, tv_mid_title;
    private EditText et_hint_tip;
    private LinearLayout ll_normal;
    private OnLeftButtonClck onLeftButtonClck;
    private OnMidButtonClck onMidButtonClck;
    private OnRightButtonClck onRightButtonClck;
    private OnRightButtonClckWithValue onRightButtonClckWithValue;
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_NORMAL_MID_BUTTON = 2;
    public static final int TYPE_NORMAL_MID_TITLE = 3;
    private String title, hintTip, midTitle, leftText, midText, rightText;
    private int hintColor = -1, hintGravity = -1;
    private int type;
    private int titleColor = -1;
    private boolean exchangeColor, canEdit;


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
        View v = inflater.inflate(R.layout.universal_dialog, container, false);
        initView(v);
        return v;
    }

    private void initView(View v) {
        ll_normal = v.findViewById(R.id.ll_normal);
        tv_mid_title = v.findViewById(R.id.tv_mid_title);
        tv_dialog_title = v.findViewById(R.id.tv_dialog_title);
        et_hint_tip = v.findViewById(R.id.et_dialog_hint_tip);
        tv_left = v.findViewById(R.id.tv_dialog_left);
        tv_mid = v.findViewById(R.id.tv_dialog_mid);
        tv_right = v.findViewById(R.id.tv_dialog_right);
        if (exchangeColor) {
            tv_left.setTextColor(getResources().getColor(R.color.color_595757));
            tv_right.setTextColor(getResources().getColor(R.color.color_f08300));
        }
        if (titleColor != -1) {
//            tv_dialog_title.setTextColor(titleColor);
        }
        if (title != null && !title.isEmpty()) {
            tv_dialog_title.setText(title);
        }
        if (midTitle != null && !midTitle.isEmpty()) {
            tv_mid_title.setText(midTitle);
        }
        if (hintTip != null && !hintTip.isEmpty()) {
            et_hint_tip.setHint(hintTip);
        }
        if (canEdit) {
            UserUtils.setEmojiFilter(et_hint_tip);
            et_hint_tip.setEnabled(true);
            et_hint_tip.setBackground(getResources().getDrawable(R.drawable.shape_edittext_bg));
        }
        if (hintGravity != -1) {
            et_hint_tip.setGravity(hintGravity);
        }
        if (hintColor != -1) {
            et_hint_tip.setTextColor(hintColor);
        }
        if (leftText != null && !leftText.isEmpty()) {
            tv_left.setText(leftText);
        }
        if (midText != null && !midText.isEmpty()) {
            tv_mid.setText(midText);
        }
        if (rightText != null && !rightText.isEmpty()) {
            tv_right.setText(rightText);
        }
        if (type == TYPE_NORMAL_MID_BUTTON) {
            ll_normal.setVisibility(View.GONE);
            tv_mid.setVisibility(View.VISIBLE);
        }
        if (type == TYPE_NORMAL_MID_TITLE) {
            tv_dialog_title.setVisibility(View.GONE);
            et_hint_tip.setVisibility(View.GONE);
            tv_mid_title.setVisibility(View.VISIBLE);
        }

        tv_left.setOnClickListener(v1 -> {
            dismiss();
            if (onLeftButtonClck != null) {
                onLeftButtonClck.onClick();
            }
        });
        tv_right.setOnClickListener(v1 -> {
            if (onRightButtonClck != null) {
                onRightButtonClck.onClick();
                dismiss();
            }
            if (onRightButtonClckWithValue != null) {
                onRightButtonClckWithValue.onClick(et_hint_tip.getText().toString());
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

    public interface OnRightButtonClckWithValue {
        void onClick(String value);
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

    public UniversalDialog setOnRightButtonWithValueClck(OnRightButtonClckWithValue onRightButtonClckWithValue) {
        this.onRightButtonClckWithValue = onRightButtonClckWithValue;
        return this;
    }

    public UniversalDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public UniversalDialog setHintTip(String tip) {
        this.hintTip = tip;
        return this;
    }

    public UniversalDialog setHintTip(String tip, int gravity, int color) {
        this.hintTip = tip;
        this.hintColor = color;
        this.hintGravity = gravity;
        return this;
    }

    public UniversalDialog setMidText(String midText) {
        this.midText = midText;
        return this;
    }

    public UniversalDialog setMidTitle(String midTitle) {
        this.midTitle = midTitle;
        return this;
    }

    public UniversalDialog setLeftText(String leftText) {
        this.leftText = leftText;
        return this;
    }

    public UniversalDialog setRightText(String rightText) {
        this.rightText = rightText;
        return this;
    }

    public UniversalDialog setDialogType(int type) {
        this.type = type;
        return this;
    }

    public UniversalDialog exchangeButtonColor() {
        exchangeColor = true;
        return this;
    }

    public UniversalDialog setTitleColor(int color) {
        titleColor = color;
        return this;
    }

    public UniversalDialog setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
        return this;
    }
}
