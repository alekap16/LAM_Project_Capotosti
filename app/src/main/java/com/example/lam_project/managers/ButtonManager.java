package com.example.lam_project.managers;

import android.content.Context;
import android.content.SharedPreferences;

public class ButtonManager {

    private static final String PREF_NAME = "buttons_preferences";
    private static final String CURRENT_MODE = "mode";
    private static final String CURRENT_SQUARE_SIZE_METERS = "square_size_meters";

    private SharedPreferences preferences;

    public ButtonManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public double getCurrentSquareSizeMeters() {
        return preferences.getFloat(CURRENT_SQUARE_SIZE_METERS, 10.0F);
    }

    public void setCurrentSquareSizeMeters(double currentSquareSizeMeters) {
        preferences.edit().putFloat(CURRENT_SQUARE_SIZE_METERS, (float) currentSquareSizeMeters).apply();
    }

    public int getCurrentMode() {
        return preferences.getInt(CURRENT_MODE, 1); // Default value 5
    }

    public void setCurrentMode(int currentMode) {
        preferences.edit().putInt(CURRENT_MODE, currentMode).apply();
    }

}
