package com.example.lam_project.utils;

public class Utils {

    public static boolean hasTimeExpired(long timestampMillis, long minutes) {
        long currentTimeMillis = System.currentTimeMillis();
        long expirationTimeMillis = timestampMillis + (minutes * 60 * 1000); // Convert minutes to milliseconds

        return currentTimeMillis >= expirationTimeMillis;
    }
}
