package com.example.lam_project.managers;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {

    private static final String PREF_NAME = "settings_preferences";
    private static final String SWITCH_AUTO_SCAN = "auto_scan_enabled";
    private static final String KEY_SELECTED_MINUTES = "selected_minutes";
    private static final String KEY_SELECTED_MEASUREMENTS = "selected_measurements";

    private SharedPreferences preferences;

    public SettingsManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isAutoScanEnabled() {
        return preferences.getBoolean(SWITCH_AUTO_SCAN, false);
    }

    public void setAutoScanEnabled(boolean isEnabled) {
        preferences.edit().putBoolean(SWITCH_AUTO_SCAN, isEnabled).apply();
    }

    public long getSelectedMinutes() {
        return preferences.getLong(KEY_SELECTED_MINUTES, 5); // Default value 5
    }

    public void setSelectedMinutes(long minutes) {
        preferences.edit().putLong(KEY_SELECTED_MINUTES, minutes).apply();
    }

    public int getSelectedMeasurements() {
        return preferences.getInt(KEY_SELECTED_MEASUREMENTS, 1); // Default value 1
    }

    public void setSelectedMeasurements(int measurements) {
        preferences.edit().putInt(KEY_SELECTED_MEASUREMENTS, measurements).apply();
    }
}

