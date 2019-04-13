package com.ilife.iliferobot_cn.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;


/**
 * Created by chengjiaping on 2018/8/4.
 */

public class Utils {
    public static Typeface getTypeFace(Context context) {
        Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/SourceHanSansCNRegular.ttf");
        return tf;
    }

    public static void setTransformationMethod(EditText editText, boolean isSelected) {
        editText.setTransformationMethod(isSelected ? HideReturnsTransformationMethod.getInstance()
                : PasswordTransformationMethod.getInstance());
    }
}
