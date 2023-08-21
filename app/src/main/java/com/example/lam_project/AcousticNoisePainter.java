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

public class AcousticNoisePainter {

    // Acoustic Noise level, this is red when its louder and green when smoother.
    private static final double VERY_LOUD = 10.0;
    private static final double AVERAGE_NOISE = -20.0;


    private static final int ALPHA_TRANSPARENT = 100; // Adjust this value to control transparency



    // Method to paint the square based on the LTE signal strength
    public static void paintSquareByAcousticNoise(MapView map, Polygon square,
                                                       double acousticNoise, int mode,
                                                       double squareSizeMeters) {
        if (map == null || square == null)
            return;

        int fillColor;
        Log.d("NOISE LEVEL" ,"NOISE LEVEL: "+acousticNoise);
        // Determine the color based on the signal strength level
        if (acousticNoise <= AVERAGE_NOISE) {
            fillColor = Color.argb(ALPHA_TRANSPARENT, 0, 255, 0); // Red with 40% transparency
        } else if (acousticNoise < VERY_LOUD && acousticNoise > AVERAGE_NOISE) {
            fillColor = Color.argb(ALPHA_TRANSPARENT, 255, 255, 0); // Yellow with 40% transparency
        } else {
            fillColor = Color.argb(ALPHA_TRANSPARENT, 255, 0, 0); // Green with 40% transparency
        }

        square.setStrokeColor(fillColor);
        square.setStrokeWidth(2f);
        square.setFillColor(fillColor);

        saveSquareToDatabase(square, fillColor, mode, map, squareSizeMeters, acousticNoise);
        map.getOverlayManager().add(square);
        map.invalidate();
        // Save the square into the database
    }

}
