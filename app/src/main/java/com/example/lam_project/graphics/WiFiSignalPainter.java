package com.example.lam_project.graphics;

import static com.example.lam_project.managers.DatabaseManager.saveSquareToDatabase;

import android.graphics.Color;

import com.example.lam_project.managers.ButtonManager;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

//I could abstract these, but they're just 3 cases and I'd rather write the same code in different
//classes rather than the same with switch cases logics and such
public class WiFiSignalPainter {

    private static final int MODE_WIFI = 2;
    // Signal strength levels 0 is poor 1 is average 3 is good. Consider 0 and no wifi are the same.
    private static final double POOR_SIGNAL_STRENGTH = 1.0;
    private static final double AVERAGE_SIGNAL_STRENGTH = 2.0;


    private static final int ALPHA_TRANSPARENT = 100;



    // Method to paint the square based on the LTE signal strength
    public static void paintSquareByWiFiSignalStrength(MapView map, Polygon square,
                                                      double signalStrength) {
        if (map == null || square == null)
            return;

        int fillColor;

        ButtonManager buttonManager = new ButtonManager(map.getContext());

        // Determine the color based on the signal strength level
        if (signalStrength <= POOR_SIGNAL_STRENGTH) {
            fillColor = Color.argb(ALPHA_TRANSPARENT, 255, 0, 0);
        } else if (signalStrength >= POOR_SIGNAL_STRENGTH &&
                signalStrength <= AVERAGE_SIGNAL_STRENGTH) {
            fillColor = Color.argb(ALPHA_TRANSPARENT, 255, 255, 0);
        } else {
            fillColor = Color.argb(ALPHA_TRANSPARENT, 0, 255, 0);
        }

        square.setStrokeColor(fillColor);
        square.setStrokeWidth(2f);
        square.setFillColor(fillColor);

        saveSquareToDatabase(square, fillColor, MODE_WIFI, map,
                buttonManager.getCurrentSquareSizeMeters(), signalStrength);
        map.getOverlayManager().add(square);
        map.invalidate();
    }
}

