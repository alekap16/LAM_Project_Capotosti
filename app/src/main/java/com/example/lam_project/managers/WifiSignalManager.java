package com.example.lam_project.managers;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiSignalManager {
    private Context context;

    public WifiSignalManager(Context context) {
        this.context = context;
    }

    public int getWifiSignalStrength() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        // This got tested in Android 8.0 on my physical device. The wifi manager sucks because
        //apparently there's no (easy, at least) way to get a simulation for the signal like we
        //did with LTE signal.
        //Log.d("RSSI", "RSSI: "+wifiInfo.getRssi());
        int numberOfLevels = 4;
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
        return level; // Returns the WiFi signal strength in dBm
    }
}
