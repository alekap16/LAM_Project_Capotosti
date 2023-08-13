package com.example.lam_project;

import android.graphics.Color;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

public class PainterExistingSquares {
    private static final int COLOR_POOR = Color.RED;
    private static final int COLOR_AVERAGE = Color.YELLOW;
    private static final int COLOR_GOOD = Color.GREEN;

    private static final int ALPHA_TRANSPARENT = 100;
    //Passing both polygon and color for now
    public static void paintExistingSquares(MapView map, Polygon square, int color) {
        if (map == null || square == null)
            return;


        square.setStrokeColor(color);
        square.setStrokeWidth(2f);
        square.setFillColor(color);

        // Create a custom diagonal pattern for the square


        map.getOverlayManager().add(square);
        map.invalidate();
    }
}
