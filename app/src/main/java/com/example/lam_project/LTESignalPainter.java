package com.example.lam_project;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Path;

import com.example.lam_project.graphics.DiagonalPatternPolygon;
import com.example.lam_project.managers.DatabaseManager;
import com.example.lam_project.model.Square;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

public class LTESignalPainter {

    // Signal strength levels
    private static final int POOR_SIGNAL_STRENGTH = 1;
    private static final int AVERAGE_SIGNAL_STRENGTH = 2;

    // Colors for different signal strength levels
    private static final int COLOR_POOR = Color.RED;
    private static final int COLOR_AVERAGE = Color.YELLOW;
    private static final int COLOR_GOOD = Color.GREEN;

    private static final int ALPHA_TRANSPARENT = 100; // Adjust this value to control transparency



    // Method to paint the square based on the LTE signal strength
    public static void paintSquareByLTESignalStrength(MapView map, Polygon square,
                                                      int signalStrength, int mode,
                                                      double squareSizeMeters) {
        if (map == null || square == null)
            return;

        int color;

        // Determine the color based on the signal strength level
        if (signalStrength <= POOR_SIGNAL_STRENGTH) {
            color = Color.argb(ALPHA_TRANSPARENT, Color.red(COLOR_POOR), Color.green(COLOR_POOR), Color.blue(COLOR_POOR));
        } else if (signalStrength <= AVERAGE_SIGNAL_STRENGTH) {
            color = Color.argb(ALPHA_TRANSPARENT, Color.red(COLOR_AVERAGE), Color.green(COLOR_AVERAGE), Color.blue(COLOR_AVERAGE));
        } else {
            color = Color.argb(ALPHA_TRANSPARENT, Color.red(COLOR_GOOD), Color.green(COLOR_GOOD), Color.blue(COLOR_GOOD));
        }

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

        saveSquareToDatabase(square, color, mode, map, squareSizeMeters);
        map.getOverlayManager().add(diagonalPatternSquare);
        map.invalidate();
        // Save the square into the database
    }

    private static void saveSquareToDatabase(Polygon square, int color, int mode, MapView map,
                                             double squareSizeMeters) {
        // Get the latitude and longitude of the square
        double latitude = square.getPoints().get(0).getLatitude();
        double longitude = square.getPoints().get(0).getLongitude();

        // Create a new Square object
        Square squareObject = new Square(latitude, longitude, color, mode, squareSizeMeters);

        // Get a reference to the database helper
        Context context = map.getContext(); // Make sure you have access to the context where the map is displayed
        DatabaseManager dbHelper = new DatabaseManager(context);

        // Insert the square into the database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseManager.COLUMN_LATITUDE, squareObject.getLatitude());
        values.put(DatabaseManager.COLUMN_LONGITUDE, squareObject.getLongitude());
        values.put(DatabaseManager.COLUMN_COLOR, squareObject.getColor());
        values.put(DatabaseManager.COLUMN_TYPE, squareObject.getType());
        values.put(DatabaseManager.COLUMN_SIZE, squareObject.getSquareSize());
        long id = db.insert(DatabaseManager.TABLE_NAME, null, values);
        // Set the ID of the square object after insertion, maybe removing this later?
        squareObject.setId(id);
    }
}