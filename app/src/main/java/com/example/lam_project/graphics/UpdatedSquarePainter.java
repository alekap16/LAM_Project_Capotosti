package com.example.lam_project.graphics;

import android.graphics.Color;

public class UpdatedSquarePainter {
    private static final double POOR_SIGNAL_STRENGTH_LTE = 2.0;
    private static final double AVERAGE_SIGNAL_STRENGTH_LTE = 3.0;
    private static final double POOR_SIGNAL_STRENGTH_WIFI = 1.0;
    private static final double AVERAGE_SIGNAL_STRENGTH_WIFI = 2.0;
    private static final double VERY_LOUD = 10.0;
    private static final double AVERAGE_NOISE = -20.0;
    private static final int ALPHA_TRANSPARENT = 100;
    public static int paintSquare(int mode, double signalValue){
        int fillColor;
        if (mode == 1){
            // Determine the color based on the signal strength level
            if (signalValue <= POOR_SIGNAL_STRENGTH_LTE) {
                fillColor = Color.argb(ALPHA_TRANSPARENT, 255, 0, 0); // Red with 40% transparency
            } else if (signalValue >= POOR_SIGNAL_STRENGTH_LTE &&
                    signalValue <= AVERAGE_SIGNAL_STRENGTH_LTE) {
                fillColor = Color.argb(ALPHA_TRANSPARENT, 255, 255, 0); // Yellow with 40% transparency
            } else {
                fillColor = Color.argb(ALPHA_TRANSPARENT, 0, 255, 0); // Green with 40% transparency
            }
        return fillColor;

        } else if (mode == 2){
            //Wifi
            if (signalValue <= POOR_SIGNAL_STRENGTH_WIFI) {
                fillColor = Color.argb(ALPHA_TRANSPARENT, 255, 0, 0); // Red with 40% transparency
            } else if (signalValue >= POOR_SIGNAL_STRENGTH_WIFI &&
                    signalValue <= AVERAGE_SIGNAL_STRENGTH_WIFI) {
                fillColor = Color.argb(ALPHA_TRANSPARENT, 255, 255, 0); // Yellow with 40% transparency
            } else {
                fillColor = Color.argb(ALPHA_TRANSPARENT, 0, 255, 0); // Green with 40% transparency
            }
            return fillColor;
        } else {
            //sound
            if (signalValue <= VERY_LOUD) {
                fillColor = Color.argb(ALPHA_TRANSPARENT, 255, 0, 0); // Red with 40% transparency
            } else if (signalValue > VERY_LOUD && signalValue <= AVERAGE_NOISE) {
                fillColor = Color.argb(ALPHA_TRANSPARENT, 255, 255, 0); // Yellow with 40% transparency
            } else {
                fillColor = Color.argb(ALPHA_TRANSPARENT, 0, 255, 0); // Green with 40% transparency
            }
            return fillColor;
        }
    }
}
