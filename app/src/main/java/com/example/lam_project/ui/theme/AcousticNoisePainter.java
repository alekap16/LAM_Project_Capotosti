package com.example.lam_project.ui.theme;

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
    private static final int POOR_SIGNAL_STRENGTH = 0;
    private static final int AVERAGE_SIGNAL_STRENGTH = 1;


    private static final int ALPHA_TRANSPARENT = 100; // Adjust this value to control transparency



    // Method to paint the square based on the LTE signal strength
    public static void paintSquareByWiFiSignalStrength(MapView map, Polygon square,
                                                       double acousticNoise, int mode,
                                                       double squareSizeMeters) {
        if (map == null || square == null)
            return;

        int fillColor;

        // Determine the color based on the signal strength level
        if (acousticNoise <= POOR_SIGNAL_STRENGTH) {
            fillColor = Color.argb(ALPHA_TRANSPARENT, 255, 0, 0); // Red with 40% transparency
        } else if (acousticNoise == AVERAGE_SIGNAL_STRENGTH) {
            fillColor = Color.argb(ALPHA_TRANSPARENT, 255, 255, 0); // Yellow with 40% transparency
        } else {
            fillColor = Color.argb(ALPHA_TRANSPARENT, 0, 255, 0); // Green with 40% transparency
        }

        square.setStrokeColor(fillColor);
        square.setStrokeWidth(2f);
        square.setFillColor(fillColor);

        saveSquareToDatabase(square, fillColor, mode, map, squareSizeMeters);
        map.getOverlayManager().add(square);
        map.invalidate();
        // Save the square into the database
    }

    // Detatch later
    private static void saveSquareToDatabase(Polygon square, int color, int mode, MapView map,
                                             double squareSizeMeters) {
        // Get the latitude and longitude of the square
        double latitudeStart = square.getPoints().get(0).getLatitude();
        double longitudeStart = square.getPoints().get(0).getLongitude();
        double latitudeEnd = square.getPoints().get(2).getLatitude();
        double longitudeEnd = square.getPoints().get(2).getLongitude();

        // Create a new Square object
        Square squareObject = new Square(latitudeStart, longitudeStart, latitudeEnd,
                longitudeEnd, color, mode, squareSizeMeters);

        // Get a reference to the database helper
        Context context = map.getContext(); // Make sure you have access to the context where the map is displayed
        DatabaseManager dbHelper = new DatabaseManager(context);

        // Insert the square into the database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(dbHelper.COLUMN_LATITUDE_START, squareObject.getLatitudeStart());
        values.put(dbHelper.COLUMN_LONGITUDE_START, squareObject.getLongitudeStart());
        values.put(dbHelper.COLUMN_LATITUDE_END, squareObject.getLatitudeEnd());
        values.put(dbHelper.COLUMN_LONGITUDE_END, squareObject.getLongitudeEnd());
        values.put(dbHelper.COLUMN_COLOR, squareObject.getColor());
        values.put(dbHelper.COLUMN_TYPE, squareObject.getType());
        values.put(dbHelper.COLUMN_SIZE, squareObject.getSquareSize());
        long id = db.insert(dbHelper.TABLE_NAME, null, values);
        // Set the ID of the square object after insertion, maybe removing this later?
        squareObject.setId(id);
        Log.d("DB check", "DB ENTRY CHECK");
        db.close();
        dbHelper.close();
    }
}
