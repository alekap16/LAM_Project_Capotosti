package com.example.lam_project.utils;

import android.content.Context;
import android.util.Log;

import org.osmdroid.config.Configuration;

import java.io.File;

public class Utils {

    //Helper method that converts in minutes
    public static boolean hasTimeExpired(long timestampMillis, long minutes) {
        long currentTimeMillis = System.currentTimeMillis();
        long expirationTimeMillis = timestampMillis + (minutes * 60 * 1000); // Convert minutes to milliseconds
        return currentTimeMillis >= expirationTimeMillis;
    }

    // Helper method to calculate the latitude difference for a given distance in meters
    public static double metersToLatitude(double distance) {
        return distance / 111000.0;
    }

    // Helper method to calculate the longitude difference for a given distance in
    // meters at specific latitudes
    public static double metersToLongitude(double distance, double latitude) {
        double metersPerLongitudeDegree = 111320.0 * Math.cos(Math.toRadians(latitude));
        return distance / metersPerLongitudeDegree;
    }
}

