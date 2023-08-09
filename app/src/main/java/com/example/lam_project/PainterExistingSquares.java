package com.example.lam_project;

import android.graphics.Color;
import android.graphics.Path;

import com.example.lam_project.graphics.DiagonalPatternPolygon;
import com.example.lam_project.model.Square;

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

        int colorExistingSquare = color;
        colorExistingSquare = Color.argb(ALPHA_TRANSPARENT, Color.red(COLOR_GOOD), Color.green(COLOR_GOOD), Color.blue(COLOR_GOOD));


        square.setStrokeColor(color);
        square.setFillColor(color);

        // Create a custom diagonal pattern for the square
        Path diagonalPattern = new Path();
        diagonalPattern.moveTo((float) square.getPoints().get(0).getLongitude(), (float) square.getPoints().get(0).getLatitude());
        diagonalPattern.lineTo((float) square.getPoints().get(2).getLongitude(), (float) square.getPoints().get(2).getLatitude());

        DiagonalPatternPolygon diagonalPatternSquare = new DiagonalPatternPolygon();
        diagonalPatternSquare.setPoints(square.getPoints());
        diagonalPatternSquare.setPatternPath(diagonalPattern);
        diagonalPatternSquare.setPatternScale(10); // Adjust the scale of the pattern
        diagonalPatternSquare.setStrokeColor(square.getStrokeColor());
        diagonalPatternSquare.setFillColor(square.getFillColor());

        map.getOverlayManager().add(diagonalPatternSquare);
        map.invalidate();
    }
}
