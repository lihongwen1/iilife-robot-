package com.ilife.iliferobot.able;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACException;
import com.accloud.service.ACUserDevice;
import com.ilife.iliferobot.BuildConfig;
import com.ilife.iliferobot.activity.MainActivity;
import com.ilife.iliferobot.activity.SelectActivity_x;
import com.ilife.iliferobot.app.MyApplication;
import com.ilife.iliferobot.listener.ReNameListener;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.utils.MyLogger;
import com.ilife.iliferobot.utils.SpUtils;
import com.ilife.iliferobot.utils.Utils;

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
        switch (subdomain) {
            case Constants.subdomain_x430:
                serviceName = BuildConfig.SERVICE_NAME_X430;
                break;
            case Constants.subdomain_x780:
                serviceName = BuildConfig.SERVICE_NAME_X780;
                break;
            case Constants.subdomain_V3x:
                serviceName = BuildConfig.SERVICE_NAME_X320;
                break;
            case Constants.subdomain_x782:
                serviceName = BuildConfig.SERVICE_NAME_X782;
                break;
            case Constants.subdomain_x785:
                serviceName = BuildConfig.SERVICE_NAME_X785;
                break;
            case Constants.subdomain_a7:
                serviceName = BuildConfig.SERVICE_NAME_X786;
                break;
            case Constants.subdomain_x800:
                serviceName = BuildConfig.SERVICE_NAME_X800;
                break;
            case Constants.subdomain_x900:
                serviceName = BuildConfig.SERVICE_NAME_X900;
                break;
            case Constants.subdomain_x787:
                serviceName = BuildConfig.SERVICE_NAME_X787;
                break;
            case Constants.subdomain_a9s:
                serviceName = BuildConfig.SERVICE_NAME_A9s;
                break;
            case Constants.subdomain_a8s:
                serviceName = BuildConfig.SERVICE_NAME_A8s;
                break;
            case Constants.subdomain_v85:
                serviceName = BuildConfig.SERVICE_NAME_V85;
                break;
            case Constants.subdomain_x910:
                serviceName = BuildConfig.SERVICE_NAME_X910;
                break;
            case Constants.subdomain_v5x:
                serviceName = BuildConfig.SERVICE_NAME_V5x;
                break;
            default:
                serviceName = BuildConfig.SERVICE_NAME_X430;
                break;
        }
        return serviceName;
    }

    public static String getRobotType(String subdomain) {
        String robotType = "";
        switch (BuildConfig.BRAND) {
            case Constants.BRAND_ILIFE:
                switch (subdomain) {
                    case Constants.subdomain_x785:
                        robotType = Constants.X785;
                        break;
                    case Constants.subdomain_x787:
                        robotType = Constants.X787;
                        break;
                    case Constants.subdomain_x800:
                        switch (BuildConfig.Area) {
                            case AC.REGIONAL_NORTH_AMERICA:
                                robotType = Constants.A9;
                                break;
                            case AC.REGIONAL_CENTRAL_EUROPE:
                                robotType = Constants.A9s;
                                break;
                            case AC.REGIONAL_SOUTHEAST_ASIA:
                                if (SpUtils.getBoolean(MyApplication.getInstance(), MainActivity.KEY_DEV_WHITE) || SpUtils.getBoolean(MyApplication.getInstance(), SelectActivity_x.KEY_BIND_WHITE)) {
                                    robotType = Constants.A9;
                                } else {
                                    robotType = Constants.A9s;
                                }
                                break;
                            case AC.REGIONAL_CHINA:
                                robotType = Constants.X800;
                                break;
                            default:
                                robotType = Constants.X800;
                        }
                        break;
                    case Constants.subdomain_x900:
                        robotType = Constants.X900;
                        break;

                    case Constants.subdomain_a7:
                        robotType = Constants.A7;
                        break;
                    case Constants.subdomain_V3x:
                        robotType = Constants.V3x;
                        break;
                }

                break;
            case Constants.BRAND_ZACO:
                switch (subdomain) {
                    case Constants.subdomain_a9s:
                        robotType = Constants.A9s;
                        break;
                    case Constants.subdomain_a8s:
                        robotType = Constants.A8s;
                        break;
                    case Constants.subdomain_v85:
                        robotType = Constants.V85;
                        break;
                    case Constants.subdomain_x910:
                        robotType = Constants.X910;
                        break;
                    case Constants.subdomain_v5x:
                        robotType = Constants.V5x;
                        break;
                }
                break;
        }
        MyLogger.i("ROBOT_TYPE", "-------" + robotType + "------------");
        return robotType;
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

    /**
     * get the image resource id of recharging
     *
     * @param robotType
     * @return
     */
    public static int getRechargeImageSrc(String robotType, boolean isWhite) {
        int src;
        switch (robotType) {
            case Constants.X910:
                src = R.drawable.rechage_device_x910;
                break;
            case Constants.X900:
                src = R.drawable.rechage_device_x900;
                break;
            case Constants.A9:
            case Constants.A9s:
            case Constants.X800:
                if (isWhite) {
                    src = R.drawable.rechage_device_x800w;
                } else {
                    src = R.drawable.rechage_device_x800;
                }
                break;
            case Constants.A7:
            case Constants.X787:
                src = R.drawable.rechage_device_x787;
                break;
            case Constants.X785:
                src = R.drawable.rechage_device_x785;
                break;
            case Constants.A8s:
                src = R.drawable.rechage_device_a8s;
                break;
            case Constants.V85:
                src = R.drawable.rechage_device_v85;
                break;
            case Constants.V3x:
            case Constants.V5x:
                src = R.drawable.rechage_device_v5x;
                break;
            default:
                src = R.drawable.rechage_device_x800;
                break;

        }
        return src;
    }


    public static String getErrorText(Context context, int code, String robotType) {
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
                if (robotType.equals(Constants.A9)) {
                    strError = context.getString(R.string.adapter_error_td_a9);
                } else {
                    strError = context.getString(R.string.adapter_error_td);
                }
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
            case 0x43:
                strError = context.getString(R.string.adapter_error_bs);
                break;
            case 0x51:
                if (robotType.equals(Constants.A9)) {
                    strError = context.getString(R.string.adapter_error_zbl_a9);
                } else {
                    strError = context.getString(R.string.adapter_error_zbl);
                }
                break;
            case 0x52:
                if (robotType.equals(Constants.A9)) {
                    strError = context.getString(R.string.adapter_error_ybl_a9);
                } else {
                    strError = context.getString(R.string.adapter_error_ybl);
                }
                break;
            case 0x61:
//                if (robotType.equals(Constants.A9)) {
//                    strError = context.getString(R.string.adapter_error_gs_a9);
//                } else {
//                    strError = context.getString(R.string.adapter_error_gs);
//                }
                strError = context.getString(R.string.adapter_error_gs_a9);
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
            default:
                strError = context.getString(R.string.adapter_error_qt);
        }
        return strError;
    }


    public static String getStatusStr(Context context, int b, int errCode) {
        String str = "";

        if (errCode != 0) {
            str = context.getString(R.string.map_aty_exception);
        } else {
            if (b == MsgCodeUtils.STATUE_OFF_LINE) {
                str = context.getString(R.string.device_adapter_device_offline);
            } else if (b == MsgCodeUtils.STATUE_SLEEPING) {
                str = context.getString(R.string.map_aty_sleep);
            } else if (b == MsgCodeUtils.STATUE_WAIT) {
                str = context.getString(R.string.map_aty_standby_mode);
            } else if (b == MsgCodeUtils.STATUE_RANDOM) {
                str = context.getString(R.string.map_aty_random);
            } else if (b == MsgCodeUtils.STATUE_ALONG) {
                str = context.getString(R.string.map_aty_along);
            } else if (b == MsgCodeUtils.STATUE_POINT) {
                str = context.getString(R.string.map_aty_point);
            } else if (b == MsgCodeUtils.STATUE_PLANNING) {
                str = context.getString(R.string.map_aty_plan_mode);
            } else if (b == MsgCodeUtils.STATUE_VIRTUAL_EDIT) {
                str = context.getString(R.string.map_aty_edit_mode);
            } else if (b == MsgCodeUtils.STATUE_RECHARGE) {
                str = context.getString(R.string.map_aty_recharge);
            } else if (b == MsgCodeUtils.STATUE_CHARGING) {
                str = context.getString(R.string.map_aty_charge);
            } else if (b == MsgCodeUtils.STATUE_REMOTE_CONTROL) {
                str = context.getString(R.string.map_aty_remote);
            } else if (b == MsgCodeUtils.STATUE_CHARGING_) {
                str = context.getString(R.string.map_aty_charge);
            } else if (b == MsgCodeUtils.STATUE_PAUSE) {
                str = context.getString(R.string.map_aty_pause);
            } else if (b == MsgCodeUtils.STATUE_TEMPORARY_POINT) {
                str = context.getString(R.string.map_aty_temp_keypoint);
            }
        }
        return str;
    }

    public static String[] getSupportDevices() {
        String[] types = null;
        if (Utils.isIlife()) {//BRAD ILIFE
            switch (BuildConfig.Area) {
                case AC.REGIONAL_CHINA://中国
                    types = MyApplication.getInstance().getResources().getStringArray(R.array.device_name_ilife_cn);
                    break;
                case AC.REGIONAL_SOUTHEAST_ASIA://东南亚
                    types = MyApplication.getInstance().getResources().getStringArray(R.array.device_name_ilife_as);
                    break;
                case AC.REGIONAL_NORTH_AMERICA://美洲
                    types = MyApplication.getInstance().getResources().getStringArray(R.array.device_name_ilife_us);
                    break;
                case AC.REGIONAL_CENTRAL_EUROPE://欧洲
                    types = MyApplication.getInstance().getResources().getStringArray(R.array.device_name_ilife_eu);
                    break;
                default://默认中国
                    types = MyApplication.getInstance().getResources().getStringArray(R.array.device_name_ilife_cn);
                    break;
            }
        } else {// BRAND ZACO
            types = MyApplication.getInstance().getResources().getStringArray(R.array.device_name_zaco);
        }
        return types;
    }


}
