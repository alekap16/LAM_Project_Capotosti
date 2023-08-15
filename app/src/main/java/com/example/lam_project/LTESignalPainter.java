package com.example.lam_project;

import static com.example.lam_project.managers.DatabaseManager.saveSquareToDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;

import com.example.lam_project.managers.DatabaseManager;
import com.example.lam_project.model.Square;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

public class LTESignalPainter {

    // Signal strength levels
    private static final double POOR_SIGNAL_STRENGTH = 1.0;
    private static final double AVERAGE_SIGNAL_STRENGTH = 3.0;


    private static final int ALPHA_TRANSPARENT = 100; // Adjust this value to control transparency



    // Method to paint the square based on the LTE signal strength
    public static void paintSquareByLTESignalStrength(MapView map, Polygon square,
                                                      double signalStrength, int mode,
                                                      double squareSizeMeters) {
        if (map == null || square == null)
            return;

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

        // Create a custom diagonal pattern for the square

        saveSquareToDatabase(square, fillColor, mode, map, squareSizeMeters, signalStrength);
        map.getOverlayManager().add(square);
        map.invalidate();
        // Save the square into the database
    }

}