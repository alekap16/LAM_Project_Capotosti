package com.example.lam_project;

import android.graphics.Color;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

public class LTESignalPainter {

    // Signal strength levels
    private static final int POOR_SIGNAL_STRENGTH = 2;
    private static final int AVERAGE_SIGNAL_STRENGTH = 3;

    // Colors for different signal strength levels
    private static final int COLOR_POOR = Color.RED;
    private static final int COLOR_AVERAGE = Color.YELLOW;
    private static final int COLOR_GOOD = Color.GREEN;

    // Method to paint the square based on the LTE signal strength
    public static void paintSquareByLTESignalStrength(MapView map, Polygon square, int signalStrength) {
        if (map == null || square == null)
            return;

        int color;

        // Determine the color based on the signal strength level
        if (signalStrength <= POOR_SIGNAL_STRENGTH) {
            color = COLOR_POOR;
        } else if (signalStrength <= AVERAGE_SIGNAL_STRENGTH) {
            color = COLOR_AVERAGE;
        } else {
            color = COLOR_GOOD;
        }

        square.setStrokeColor(color);
        square.setFillColor(color);

        map.invalidate();
    }
}