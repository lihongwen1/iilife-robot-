package com.ilife.iliferobot.view;

import android.content.Context;

import androidx.appcompat.widget.AppCompatTextView;

import android.util.AttributeSet;

import com.ilife.iliferobot.app.MyApplication;

/**
 * Created by chengjiaping on 2018/8/30.
 */

public class ItcaTextView extends AppCompatTextView {
    public ItcaTextView(Context context) {
        super(context);
        setTypeFace();
    }

    public ItcaTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeFace();
    }

    public ItcaTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeFace();
    }

    public void setTypeFace() {
        if (!isInEditMode()) {
            setTypeface(MyApplication.getInstance().tf_itca);
        }
    }
}
