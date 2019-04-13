package com.ilife.iliferobot_cn.utils;

import android.content.Context;
import android.widget.Toast;

import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.app.MyApplication;

/**
 * Created by chenjiaping on 2017/7/4.
 */

public class ToastUtils {
    private static Toast toast;
    public static void showToast(Context context,String msg){
        if (toast==null){
            toast = Toast.makeText(context,msg,Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }
    public static void showToast(String msg){
        Toast.makeText(MyApplication.getInstance(),msg,Toast.LENGTH_SHORT).show();
    }

    public static void hiddenToast(){
        if (toast!=null){
            toast.cancel();
        }
    }
    public static void showErrorToast(int code){
        showErrorToast(MyApplication.getInstance(),code);
    }
    public static void showErrorToast(Context context,int code){
        //默认显示连接超时
        String msg = context.getString(R.string.login_aty_timeout);
        switch (code){
            case 1993:
                msg = context.getString(R.string.login_aty_timeout);
                break;
            case 1986:
                msg = context.getString(R.string.add_aty_no_wifi);
                break;
            case 3807:
                msg = context.getString(R.string.clock_aty_dev_offline);
                break;
            case 3501:
                msg = context.getString(R.string.login_aty_account_no);
                break;
            case 3502:
                msg = context.getString(R.string.register_aty_email_registered);
                break;
            case 3504:
                msg = context.getString(R.string.login_aty_pw_error);
                break;
            case 3505:
                msg = context.getString(R.string.register2_aty_code_wrong);
                break;
            case 3506:
                msg = context.getString(R.string.register2_aty_code_nouse);
                break;
            case 3507:
                msg = context.getString(R.string.register2_aty_illegal_email);
                break;
            case 3509:
                msg = context.getString(R.string.register2_aty_account_abnormal);
                break;
        }

        if (toast==null){
            toast = Toast.makeText(context,msg,Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }
}
