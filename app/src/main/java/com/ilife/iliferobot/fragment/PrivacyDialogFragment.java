package com.ilife.iliferobot.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ilife.iliferobot.R;
import com.ilife.iliferobot.activity.ProtocolActivity;

import java.util.Locale;

public class PrivacyDialogFragment extends DialogFragment {
    private TextView title;
    private TextView tip;
    private View.OnClickListener onClickListener;
    private TextView tv_left, tv_right;
    private OnLeftButtonClck onLeftButtonClck;
    private OnRightButtonClck onRightButtonClck;

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
        View view = inflater.inflate(R.layout.dialog_fragment_privacy, container, false);
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
        tv_left = view.findViewById(R.id.tv_privacy_left);
        tv_left.setOnClickListener(v -> {
            if (onLeftButtonClck != null) {
                dismiss();
                onLeftButtonClck.onClick();
            }
        });
        tv_right = view.findViewById(R.id.tv_privacy_right);
        tv_right.setOnClickListener(v -> {
            if (onRightButtonClck != null) {
                dismiss();
                onRightButtonClck.onClick();
            }
        });
        title = view.findViewById(R.id.tv_privacy_title);
        title.setText(R.string.dialog_privacy_agreement_title);
        tip = view.findViewById(R.id.tv_privacy_tip);
        String text = getString(R.string.dialog_privacy_agreement_content);
        SpannableString sb = new SpannableString(text);
        String lan = Locale.getDefault().getLanguage();
        String contry=Locale.getDefault().getCountry();
        int start1, end1, start2, end2;
        String text1, text2;
        if (lan.equals("zh")) {
            if (contry.equals("CN")){
            text1 = "《服务协议》";
            text2 = "《隐私政策》";
            }else {
                text1="《服務協議》";
                text2="《隱私政策》";
            }
        }else {
            text1="\"Service Agreement\"";
            text2="\"Privacy Policy\"";
        }
        start1=text.indexOf(text1);
        end1=start1+text1.length();
        start2=text.indexOf(text2);
        end2=start2+text2.length();
        sb.setSpan(new MyClickText(1), start1, end1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(new MyClickText(2), start2, end2, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        tip.setText(sb);
        tip.setHighlightColor(getResources().getColor(R.color.transparent));
        tip.setMovementMethod(LinkMovementMethod.getInstance());
    }


    public void setOnLeftButtonClck(OnLeftButtonClck onLeftButtonClck) {
        this.onLeftButtonClck = onLeftButtonClck;
    }


    public void setOnRightButtonClck(OnRightButtonClck onRightButtonClck) {
        this.onRightButtonClck = onRightButtonClck;
    }

    private class MyClickText extends ClickableSpan {
        private int type;

        public MyClickText(int type) {
            this.type = type;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            //设置文本的颜色
            ds.setColor(getResources().getColor(R.color.color_f08300));
            //超链接形式的下划线，false 表示不显示下划线，true表示显示下划线
            ds.setUnderlineText(false);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), ProtocolActivity.class);
            intent.putExtra(ProtocolActivity.KEY_TYPE, type);
            startActivity(intent);
        }
    }

    public interface OnLeftButtonClck {
        void onClick();
    }

    public interface OnRightButtonClck {
        void onClick();
    }
}
