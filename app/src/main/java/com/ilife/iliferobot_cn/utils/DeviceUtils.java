package com.ilife.iliferobot_cn.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACException;
import com.accloud.service.ACUserDevice;
import com.ilife.iliferobot_cn.listener.ReNameListener;
import com.ilife.iliferobot_cn.R;

/**
 * Created by chenjiaping on 2017/8/3.
 */

public class DeviceUtils {
    public static String getPhysicalDeviceId(Activity activity) {
        Intent intent = activity.getIntent();
        String physicalDeviceId = null;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                physicalDeviceId = bundle.getString("physicalDeviceId");
            }
        }
        return physicalDeviceId;
    }

    public static String getServiceName(String subdomain) {
        String serviceName = "";
        if (subdomain.equals(Constants.subdomain_x430)) {
            serviceName = Constants.SERVICE_NAME_X430;
        } else if (subdomain.equals(Constants.subdomain_x780)) {
            serviceName = Constants.SERVICE_NAME_X780;
        } else if (subdomain.equals(Constants.subdomain_x782)) {
            serviceName = Constants.SERVICE_NAME_X782;
        } else if (subdomain.equals(Constants.subdomain_x785)) {
            serviceName = Constants.SERVICE_NAME_X785;
        } else if (subdomain.equals(Constants.subdomain_x800)) {
            serviceName = Constants.SERVICE_NAME_X800;
        } else if (subdomain.equals(Constants.subdomain_x900)) {
            serviceName = Constants.SERVICE_NAME_X900;
        } else if (subdomain.equals(Constants.subdomain_x787)) {
            serviceName = Constants.SERVICE_NAME_X787;
        } else {
            serviceName = Constants.SERVICE_NAME_X430;
        }
        return serviceName;
    }

    public static long getOwner(Activity activity) {
        Intent intent = activity.getIntent();
        long owner = 0;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                owner = bundle.getLong("owner");
            }
        }
        return owner;
    }

    public static long getDeviceId(Activity activity) {
        Intent intent = activity.getIntent();
        long deviceId = 0;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                deviceId = bundle.getLong("deviceId");
            }
        }
        return deviceId;
    }

    public static boolean getCanChange(Activity activity) {
        Intent intent = activity.getIntent();
        boolean canChange = false;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                canChange = bundle.getBoolean("canChange");
            }
        }
        return canChange;
    }

    public static void renameDevice(long deviceId, final String devName, String subdomain, final ReNameListener listener) {
        if (TextUtils.isEmpty(devName)) {
            return;
        }
        AC.bindMgr().changeName(subdomain, deviceId, devName, new VoidCallback() {
            @Override
            public void success() {
                listener.onSuccess();
            }

            @Override
            public void error(ACException e) {
                listener.onError(e);
            }
        });
    }

    public static String getDevName(Activity activity) {
        Intent intent = activity.getIntent();
        String devName = null;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                devName = bundle.getString("devName");
            }
        }
        return devName;
    }

    public static String getSubdomain(Activity activity) {
        Intent intent = activity.getIntent();
        String devName = null;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                devName = bundle.getString("subdomain");
            }
        }
        return devName;
    }

    public static boolean isOnline(ACUserDevice device) {
        int status = device.getStatus();
        if (status == 0) {
            return false;
        } else {
            return true;
        }
    }

    public static String getErrorText(Context context, int code) {
        String strError = "";
        switch (code) {
            case 0x00:
                strError = context.getString(R.string.adapter_error_no_error);
                break;
            case 0x01:
                strError = context.getString(R.string.adapter_error_bxg);
                break;
            case 0x11:
                strError = context.getString(R.string.adapter_error_obs);
                break;
            case 0x12:
                strError = context.getString(R.string.adapter_error_yq);
                break;
            case 0x21:
                strError = context.getString(R.string.adapter_error_td);
                break;
            case 0x22:
                strError = context.getString(R.string.dev_error_xuankong);
                break;
            case 0x31:
                strError = context.getString(R.string.adapter_error_wxl);
                break;
            case 0x41:
                strError = context.getString(R.string.adapter_error_zbs);
                break;
            case 0x42:
                strError = context.getString(R.string.adapter_error_ybs);
                break;
            case 0x51:
                strError = context.getString(R.string.adapter_error_zbl);
                break;
            case 0x52:
                strError = context.getString(R.string.adapter_error_ybl);
                break;
            case 0x61:
                strError = context.getString(R.string.adapter_error_gs);
                break;
            case 0x71:
                strError = context.getString(R.string.adapter_error_fs);
                break;
            case 0x81:
                strError = context.getString(R.string.adapter_error_sb);
                break;
            case 0x82:
                strError = context.getString(R.string.adapter_error_qb);
                break;
            case 0x91:
                strError = context.getString(R.string.adapter_error_ljx);
                break;
            case 0x92:
                strError = context.getString(R.string.adapter_error_sx);
                break;
            case 0x93:
                strError = context.getString(R.string.adapter_error_lw);
                break;
            case 0xA1:
                strError = context.getString(R.string.adapter_error_dc);
                break;
            case 0xB1:
                strError = context.getString(R.string.adapter_error_tly);
                break;
            case 0xC1:
                strError = context.getString(R.string.adapter_error_ld);
                break;
            case 0xC2:
                strError = context.getString(R.string.adapter_error_sxt);
                break;
            case 0xD1:
                strError = context.getString(R.string.adapter_error_xj);
                break;
            case 0xE1:
                strError = context.getString(R.string.adapter_error_qt);
                break;
            case 0xF1:
                strError = context.getString(R.string.adapter_error_qt);
                break;
        }
        return strError;
    }

    public static boolean canChange(String subdomain, int workPattern) {
        boolean canChange = false;
        if (subdomain.equals(Constants.subdomain_x430)) {
            if (workPattern == 0 || workPattern == 1 || workPattern == 2
                    || workPattern == 5 || workPattern == 8 || workPattern == 9
                    || workPattern == 10 || workPattern == 11) {
                canChange = false;
            } else {
                canChange = true;
            }
        } else {
            if (workPattern == 0 || workPattern == 5 || workPattern == 8) {
                canChange = false;
            } else {
                canChange = true;
            }
        }

        return canChange;
    }


    public static boolean is430Or780or800or900(String subdomain) {
        if (subdomain.equals(Constants.subdomain_x430) ||
                subdomain.equals(Constants.subdomain_x780) ||
                subdomain.equals(Constants.subdomain_x800) ||
                subdomain.equals(Constants.subdomain_x900)) {
            return true;
        }
        return false;
    }

    public static boolean is782oR785(String subdomain) {
        if (subdomain.equals(Constants.subdomain_x782) ||
                subdomain.equals(Constants.subdomain_x785) ||
                subdomain.equals(Constants.subdomain_x787)) {
            return true;
        }
        return false;
    }

    public static String getStatusStr(Context context, int b, int errCode) {
        String str = "";

        if (errCode != 0) {
            str = context.getString(R.string.map_aty_exception);
        } else {
            if (b == 0x00) {
                str = context.getString(R.string.device_adapter_device_offline);
            } else if (b == 0x01) {
                str = context.getString(R.string.map_aty_sleep);
            } else if (b == 0x02) {
                str = context.getString(R.string.map_aty_standby_mode);
            } else if (b == 0x03) {
                str = context.getString(R.string.map_aty_random);
            } else if (b == 0x04) {
                str = context.getString(R.string.map_aty_along);
            } else if (b == 0x05) {
                str = context.getString(R.string.map_aty_point);
            } else if (b == 0x06) {
                str = context.getString(R.string.map_aty_plan_mode);
            } else if (b == 0x07) {
                str = context.getString(R.string.map_aty_edit_mode);
            } else if (b == 0x08) {
                str = context.getString(R.string.map_aty_recharge);
            } else if (b == 0x09) {
                str = context.getString(R.string.map_aty_charge);
            } else if (b == 0x0A) {
                str = context.getString(R.string.map_aty_remote);
            } else if (b == 0x0B) {
                str = context.getString(R.string.map_aty_charge);
            } else if (b == 0x0C) {
                str = context.getString(R.string.map_aty_pause);
            } else if (b == 0x0D) {
                str = context.getString(R.string.map_aty_temp_keypoint);
            }
        }
        return str;
    }
}
