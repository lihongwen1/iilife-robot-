package com.ilife.iliferobot_cn.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

/**
 * Created by chenjiaping on 2017/7/17.
 */

public class WifiUtils {
    public static final String TAG = WifiUtils.class.getSimpleName();
    public static final int WIFICIPHER_NOPASS = 1;
    public static final int WIFICIPHER_WEP = 3;
    public static final int WIFICIPHER_WPA = 2;

    public static List<ScanResult> getWifiList(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> results = manager.getScanResults();
//        ArrayList<String> ssidList = new ArrayList<>();
//        if (results!=null){
//            for (int i = 0; i <results.size(); i++) {
//                String ssid = results.get(i).SSID;
//                if (!TextUtils.isEmpty(ssid)){
//                    ssidList.add(ssid);
//                }
//            }
//        }
        return results;
    }

    public static WifiConfiguration isExist(WifiManager wifiManager, String ssid) {
        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        WifiConfiguration tempConfig = null;
        if (configs != null && configs.size() > 0) {
            for (WifiConfiguration config : configs) {
                if (config.SSID.equals("\"" + ssid + "\"")) {
                    tempConfig = config;
                    return tempConfig;
                }
            }
        }
        return null;
    }

    public static boolean connectToAp(WifiManager wifiManager, String ssid) {
        //can use
        boolean isConnSuc;
        WifiConfiguration tempConfig = WifiUtils.isExist(wifiManager, ssid);
        if (tempConfig != null) {
            isConnSuc = wifiManager.enableNetwork(tempConfig.networkId, true);
        } else {
            WifiConfiguration config = new WifiConfiguration();
            config.SSID = "\"" + ssid + "\"";
            config.preSharedKey = "\"" + "333666999QQQ" + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;

            int netId = wifiManager.addNetwork(config);
            wifiManager.disconnect();
            isConnSuc = wifiManager.enableNetwork(netId, true);
        }
        return isConnSuc;
    }

    public static void connectToAp_(WifiManager wifiManager, String ssid, String pass, int type) {
        createWifiConfig(wifiManager, ssid, pass, type);
    }

    public static void createWifiConfig(WifiManager mWifiManager, String ssid, String password, int type) {
        //初始化WifiConfiguration
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        //指定对应的SSID
        config.SSID = "\"" + ssid + "\"";

        //如果之前有类似的配置
        WifiConfiguration tempConfig = isExist(mWifiManager, ssid);
        if (tempConfig != null) {
            //则清除旧有配置
//            mWifiManager.removeNetwork(tempConfig.networkId);
            boolean isSucc = mWifiManager.enableNetwork(tempConfig.networkId, true);
            Log.e(TAG, "createWifiConfig: " + isSucc);
        } else {
            //不需要密码的场景
            if (type == WIFICIPHER_NOPASS) {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                //以WEP加密的场景
            } else if (type == WIFICIPHER_WEP) {
                config.hiddenSSID = true;
                config.wepKeys[0] = "\"" + password + "\"";
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.wepTxKeyIndex = 0;
                //以WPA加密的场景，自己测试时，发现热点以WPA2建立时，同样可以用这种配置连接
            } else if (type == WIFICIPHER_WPA) {
                config.preSharedKey = "\"" + password + "\"";
                config.hiddenSSID = true;
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                config.status = WifiConfiguration.Status.ENABLED;
            }
            int netId = mWifiManager.addNetwork(config);
            mWifiManager.disconnect();
            boolean isSucc = mWifiManager.enableNetwork(netId, true);
            Log.e(TAG, "createWifiConfig: " + isSucc);
        }
    }

//    private static WifiConfiguration isExist(String ssid) {
//        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
//
//        for (WifiConfiguration config : configs) {
//            if (config.SSID.equals("\""+ssid+"\"")) {
//                return config;
//            }
//        }
//        return null;
//    }

    public static String getSsid(Context context) {
        String ssid = "";
        if (isWifiConnected(context)) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                WifiInfo info = wifiManager.getConnectionInfo();
                int networkId = info.getNetworkId();
                List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
                for (WifiConfiguration wifiConfiguration : configuredNetworks) {
                    if (wifiConfiguration.networkId == networkId) {
                        ssid = wifiConfiguration.SSID;
                        break;
                    }
                }
                if (ssid.contains("\"")) {
                    return ssid.replace("\"", "");
                }
            }
        }
        return ssid;
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifiInfo != null && wifiInfo.isConnected()) {
                return true;
            }
        }
        return false;
    }
}
