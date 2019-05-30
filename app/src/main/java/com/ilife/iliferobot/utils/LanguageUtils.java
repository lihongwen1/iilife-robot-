package com.ilife.iliferobot.utils;

import android.annotation.TargetApi;
import android.content.Context;

import java.util.Locale;

/**
 * Created by chengjiaping on 2018/3/2.
 */

public class LanguageUtils {
    @TargetApi(19)
    public static boolean isDe(Context context) {
        Locale locale = context.getResources().getConfiguration().getLocales().get(0);
        String language = locale.getLanguage();
        if (language.endsWith("DE") || language.endsWith("de"))
            return true;
        else
            return false;
    }
}
