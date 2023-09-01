package com.example.lam_project.logic;

import android.graphics.Color;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

public class PainterExistingSquares {
    public static void paintExistingSquares(MapView map, Polygon square, int color) {

        if (map == null || square == null) return;

        square.setStrokeColor(color);
        square.setStrokeWidth(2f);
        square.setFillColor(color);

        map.getOverlayManager().add(square);
        map.invalidate();
    }
}
