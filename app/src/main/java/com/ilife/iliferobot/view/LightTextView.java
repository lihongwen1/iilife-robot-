package com.ilife.iliferobot.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ilife.iliferobot.app.MyApplication;

/**
 * Created by chengjiaping on 2018/8/30.
 */

public class LightTextView extends TextView {
    public LightTextView(Context context) {
        super(context);
        setTypeFace();
    }

    public LightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeFace();
    }

    public LightTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeFace();
    }

    public void setTypeFace() {
        if (!isInEditMode()) {
            setTypeface(MyApplication.getInstance().tf_light);
        }
    }
}
