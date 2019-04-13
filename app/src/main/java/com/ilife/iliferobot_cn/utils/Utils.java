package com.ilife.iliferobot_cn.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;

import com.ilife.iliferobot_cn.app.MyApplication;


/**
 * Created by chengjiaping on 2018/8/4.
 */

public class Utils {
    public static  Typeface getTypeFace(Context context){
        Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/SourceHanSansCNRegular.ttf");
        return tf;
    }

    public static void setTransformationMethod(EditText editText, boolean isSelected){
        editText.setTransformationMethod(isSelected? HideReturnsTransformationMethod.getInstance()
                : PasswordTransformationMethod.getInstance());
    }
    public static String getString(int id){
        return MyApplication.getInstance().getString(id);
    }


    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue
     *            （DisplayMetrics类中属性density）
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
