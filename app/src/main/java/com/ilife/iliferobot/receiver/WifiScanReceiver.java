package com.ilife.iliferobot.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.ilife.iliferobot.app.MyApplication;
import com.ilife.iliferobot.utils.MyLogger;

import java.util.List;

import static android.content.Context.WIFI_SERVICE;

public class WifiScanReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
           WifiManager wifiManager= (WifiManager) MyApplication.getInstance().getApplicationContext().getSystemService(WIFI_SERVICE);
            List<ScanResult> results=wifiManager.getScanResults();
            if (results != null) {
                for (ScanResult scanResult:results) {
                    MyLogger.d("WifiScanReceiver","ssid:"+scanResult.SSID);
                }
                MyLogger.d("WifiScanReceiver", "results size: " + results.size());
            }
        }
    }
}
