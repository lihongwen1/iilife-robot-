package com.ilife.iliferobot.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import com.ilife.iliferobot.app.MyApplication;

public class MediumEditTextView extends EditText {

    public MediumEditTextView(Context context) {
        super(context);
        setTypeFace();
    }

    public MediumEditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeFace();
    }

    public MediumEditTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeFace();
    }

    public void setTypeFace() {
        setTypeface(MyApplication.getInstance().tf_medium);
    }
}
