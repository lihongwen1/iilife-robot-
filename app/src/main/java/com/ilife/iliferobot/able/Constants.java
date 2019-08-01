package com.ilife.iliferobot.able;


import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.ACDeviceActivator;

/**
 * Created by chengjiaping on 2017/7/6.
 */

public class Constants {
    //子域
    public static final String subdomain = "zhiyitest";
    //430的子域
    public static final String subdomain_x430 = "zhiyitest";
    //780的子域
    public static final String subdomain_x780 = "ilifex780";
    //782的子域
    public static final String subdomain_x782 = "ilifex782";
    //785的子域
    public static final String subdomain_x785 = "ilifex785";
    //A7的子域
    public static final String subdomain_a7 = "ilifex786";
    //787的子域
    public static final String subdomain_x787 = "ilifex787";
    //800的子域
    public static final String subdomain_x800 = "ilifex800";
    //900的子域
    public static final String subdomain_x900 = "ilifex900";

    //    ZACO
    public static final String subdomain_a9s = "zacoa9s";
    public static final String subdomain_a8s = "zacoa8";
    public static final String subdomain_v85 = "zacov85";


    public static final long subdomainId_x785 = 6231;
    public static final long subdomainId_x787 = 6422;
    public static final long subdomainId_x800 = 6312;
    public static final long subdomainId_A7 = 6370;
    public static final long subdomainId_x900 = 6369;

    //    ZACO
    public static final long subdomaiId_a9s = 6746;
    public static final long subdomaiId_a8s = 6795;
    public static final long subdomaiId_v85 = 6809;

    //国内
    public static final String X900 = "X900";
    public static final String X800 = "X800";
    public static final String X787 = "X787";
    public static final String X785 = "X785";
    //ZACO
    public static final String A9s = "A9s";
    public static final String A8s = "A8s";
    public static final String V85 = "V85";
    //美洲
    public static final String A9 = "A9";
    public static final String A7 = "A7";


    public static final int DEVICE_TYPE_QCLTLINK = ACDeviceActivator.QCLTLINK;


    public static final int SERVICE_VERSION = 1;

    public static final int EMAIL_MODE_Europe = 2;

    public static final int EMAIL_MODE_InLand_TEST = 1;

    public static final int EMAIL_MODE_InLand_FORMAL = 3;

    public static final int LOCAL_FIRST = AC.LOCAL_FIRST;

    public static final int CLOUD_ONLY = AC.ONLY_CLOUD;

    public static final int OTATYPE = 1;
    public static boolean IS_FIRST_AP = true;//标记首次配网，可自动连接wifi，重试需要手动连接robot wifi

    public static final String BRAND_ILIFE = "ILIFE";
    public static final String BRAND_ZACO = "ZACO";
}
