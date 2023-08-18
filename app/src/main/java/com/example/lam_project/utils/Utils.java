package com.example.lam_project.utils;

import android.util.Log;

public class Utils {

    public static boolean hasTimeExpired(long timestampMillis, long minutes) {
        long currentTimeMillis = System.currentTimeMillis();
        Log.d("Current timestamp", "Current timestamp"+currentTimeMillis);
        long expirationTimeMillis = timestampMillis + (minutes * 60 * 1000); // Convert minutes to milliseconds
        Log.d("Expiration", "Expiration timestamp: "+expirationTimeMillis);
        return currentTimeMillis >= expirationTimeMillis;
    }
}
