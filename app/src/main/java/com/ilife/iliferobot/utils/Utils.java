package com.ilife.iliferobot.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;

import com.ilife.iliferobot.BuildConfig;
import com.ilife.iliferobot.app.MyApplication;


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

    public static String getString(int id) {
        return MyApplication.getInstance().getString(id);
    }

    public static  boolean isIlife() {
        return BuildConfig.BRAND.equals("ILIFE");
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue （DisplayMetrics类中属性density）
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static byte[] concat_(byte[] a, byte[] b, byte type) {
        int offset = 0;
        switch (type) {
            case 1:
                offset = 7;
                break;
            case 2:
                offset = 2;
                break;
        }
        byte[] c = new byte[a.length + b.length - offset];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, offset, c, a.length, b.length - offset);
        return c;
    }
}
