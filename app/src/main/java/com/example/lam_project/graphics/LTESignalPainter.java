package com.example.lam_project.graphics;

import static com.example.lam_project.managers.DatabaseManager.saveSquareToDatabase;

import android.graphics.Color;
import android.util.Log;

import com.example.lam_project.managers.ButtonManager;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

public class LTESignalPainter {

    private static final int MODE_LTE = 1;
    // Signal strength levels
    private static final double POOR_SIGNAL_STRENGTH = 2.0;
    private static final double AVERAGE_SIGNAL_STRENGTH = 3.0;
    private static final int ALPHA_TRANSPARENT = 100; // Adjust this value to control transparency
    // Method to paint the square based on the LTE signal strength
    public static void paintSquareByLTESignalStrength(MapView map, Polygon square,
                                                      double signalStrength) {
        if (map == null || square == null)
            return;

        ButtonManager buttonManager = new ButtonManager(map.getContext());

        int fillColor;

        // Determine the color based on the signal strength level
        if (signalStrength <= POOR_SIGNAL_STRENGTH) {
            fillColor = Color.argb(ALPHA_TRANSPARENT, 255, 0, 0); // Red with 40% transparency
        } else if (signalStrength >= POOR_SIGNAL_STRENGTH &&
                signalStrength <= AVERAGE_SIGNAL_STRENGTH) {
            fillColor = Color.argb(ALPHA_TRANSPARENT, 255, 255, 0); // Yellow with 40% transparency
        } else {
            fillColor = Color.argb(ALPHA_TRANSPARENT, 0, 255, 0); // Green with 40% transparency
        }

        square.setStrokeColor(fillColor);
        square.setStrokeWidth(2f);
        square.setFillColor(fillColor);


        saveSquareToDatabase(square, fillColor, MODE_LTE, map,
                buttonManager.getCurrentSquareSizeMeters(), signalStrength);
        map.getOverlayManager().add(square);
        map.invalidate();

    }

}