package com.example.lam_project.managers;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiSignalManager {
    private Context context;

    public WifiSignalManager(Context context) {
        this.context = context;
    }

    public int getWifiSignalStrength() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if (wifiInfo != null) {
            return wifiInfo.getRssi(); // Returns the WiFi signal strength in dBm
        } else {
            return Integer.MIN_VALUE; // Signal strength value when WiFi information is unavailable
        }
    }
}
