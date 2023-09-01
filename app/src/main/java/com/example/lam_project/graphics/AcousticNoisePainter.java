package com.example.lam_project.graphics;

import static com.example.lam_project.managers.DatabaseManager.saveSquareToDatabase;

import android.graphics.Color;
import android.util.Log;

import com.example.lam_project.managers.ButtonManager;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

public class AcousticNoisePainter {

    private static final int NOISE_MODE = 3;
    // Acoustic Noise level, this is red when its louder and green when smoother.
    private static final double VERY_LOUD = 10.0;
    private static final double AVERAGE_NOISE = -20.0;
    private static final int ALPHA_TRANSPARENT = 100; // Adjust this value to control transparency

    // Method to paint the square based on the LTE signal strength
    public static void paintSquareByAcousticNoise(MapView map, Polygon square,
                                                       double acousticNoise) {
        if (map == null || square == null)
            return;

        int fillColor;

        ButtonManager buttonManager = new ButtonManager(map.getContext());

        // Determine the color based on the signal strength level
        if (acousticNoise <= AVERAGE_NOISE) {
            fillColor = Color.argb(ALPHA_TRANSPARENT, 0, 255, 0);
        } else if (acousticNoise < VERY_LOUD && acousticNoise > AVERAGE_NOISE) {
            fillColor = Color.argb(ALPHA_TRANSPARENT, 255, 255, 0);
        } else {
            fillColor = Color.argb(ALPHA_TRANSPARENT, 255, 0, 0);
        }

        square.setStrokeColor(fillColor);
        square.setStrokeWidth(2f);
        square.setFillColor(fillColor);

        saveSquareToDatabase(square, fillColor, NOISE_MODE, map,
                buttonManager.getCurrentSquareSizeMeters(), acousticNoise);
        map.getOverlayManager().add(square);
        map.invalidate();
    }

}
