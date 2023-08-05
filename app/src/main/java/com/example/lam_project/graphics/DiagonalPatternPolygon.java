package com.example.lam_project.graphics;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

public class DiagonalPatternPolygon extends Polygon {

    private Path patternPath;
    private int patternColor;
    private float patternScale;

    public DiagonalPatternPolygon() {
        super();
        patternPath = new Path();
        patternColor = Color.BLACK; // Default pattern color (you can change it as needed)
        patternScale = 1.0f; // Default pattern scale (you can change it as needed)
    }

    public void setPatternPath(Path path) {
        patternPath = path;
    }

    public void setPatternColor(int color) {
        patternColor = color;
    }

    public void setPatternScale(float scale) {
        patternScale = scale;
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);

        if (!shadow) {
            // Draw the diagonal pattern on the canvas
            Paint paint = new Paint();
            paint.setColor(patternColor);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4);
            paint.setPathEffect(new DashPathEffect(new float[]{patternScale, patternScale}, 0));

            canvas.drawPath(patternPath, paint);
        }
    }
}
