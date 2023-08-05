package com.example.lam_project;


import android.graphics.Color;
import android.util.Log;

import com.example.lam_project.managers.SignalStrengthManager;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//is working so good cant belive its true, gotta replicate and add "meters" or smthing like that
//so i can re-use the code for 100m squares and 1km. as for now I get one done since this is
//at first look easy to complete for other use cases
public class GridCreator {

    private static int currentLTESignalStrength = 0; // Variable to store the current LTE signal strength

    // Size of the squares in meters
    private static final double SQUARE_SIZE_METERS = 10.0;

    //Create each unique ID for squares, needs for overlap prevention
    private static final List<Polygon> existingSquares = new ArrayList<>();
    public GridCreator() {
        // No need for any constructor parameters in this case
    }
    // Method to create and display the grid overlay
    public static void createGridOverlay(MapView mapView, double latitude, double longitude) {
        if (mapView == null)
            return;


        if (!doesSquareOverlap(latitude, longitude)) {
            double latitudeDiff = metersToLatitude(SQUARE_SIZE_METERS);
            double longitudeDiff = metersToLongitude(SQUARE_SIZE_METERS, latitude);

            // Calculate the area of the square grid
            double latStart = latitude - 0.5 * latitudeDiff;
            double latEnd = latitude + 0.5 * latitudeDiff;
            double lonStart = longitude - 0.5 * longitudeDiff;
            double lonEnd = longitude + 0.5 * longitudeDiff;

            // Create the square around the given coordinates
            List<GeoPoint> squarePoints = new ArrayList<>();
            squarePoints.add(new GeoPoint(latStart, lonStart));
            squarePoints.add(new GeoPoint(latEnd, lonStart));
            squarePoints.add(new GeoPoint(latEnd, lonEnd));
            squarePoints.add(new GeoPoint(latStart, lonEnd));

            Polygon square = new Polygon(mapView);
            square.setPoints(squarePoints);
            square.setStrokeColor(Color.RED);
            square.setStrokeWidth(4f);
            mapView.getOverlayManager().add(square);
            // Store the current LTE signal strength from SignalStrengthManager
            SignalStrengthManager signalStrengthManager = new SignalStrengthManager(mapView.getContext());
            signalStrengthManager.requestSignalStrengthUpdates(new SignalStrengthManager.OnSignalStrengthChangeListener() {
                @Override
                public void onSignalStrengthChanged(int signalStrength) {
                    // Store the current LTE signal strength
                    currentLTESignalStrength = signalStrength;

                    //Log.d("SignalStrength", "LTE Signal Strength: " + signalStrength);
                    // Paint the square based on the current LTE signal strength
                    LTESignalPainter.paintSquareByLTESignalStrength(mapView, square, currentLTESignalStrength);
                    signalStrengthManager.stopSignalStrengthUpdates();
                }
            });
            existingSquares.add(square);
            mapView.invalidate();
        }
    }

    // Helper method to calculate the latitude difference for a given distance in meters
    private static double metersToLatitude(double distance) {
        return distance / 111000.0;
    }

    // Helper method to calculate the longitude difference for a given distance in meters at specific latitudes
    private static double metersToLongitude(double distance, double latitude) {
        double metersPerLongitudeDegree = 111320.0 * Math.cos(Math.toRadians(latitude));
        return distance / metersPerLongitudeDegree;
    }

    // Helper method that chjecks if overlaps are a thing
    private static boolean doesSquareOverlap(double latitude, double longitude) {
        for (Polygon square : existingSquares) {
            List<GeoPoint> points = square.getPoints();
            double latStart = points.get(0).getLatitude();
            double latEnd = points.get(2).getLatitude();
            double lonStart = points.get(0).getLongitude();
            double lonEnd = points.get(2).getLongitude();

            // Check if coordinates collides with the squares already painted for overlaps
            for (double lat = latitude - 0.5 * metersToLatitude(SQUARE_SIZE_METERS); lat <= latitude + 0.5 * metersToLatitude(SQUARE_SIZE_METERS); lat += 0.1 * metersToLatitude(SQUARE_SIZE_METERS)) {
                for (double lon = longitude - 0.5 * metersToLongitude(SQUARE_SIZE_METERS, latitude); lon <= longitude + 0.5 * metersToLongitude(SQUARE_SIZE_METERS, latitude); lon += 0.1 * metersToLongitude(SQUARE_SIZE_METERS, latitude)) {
                    if (lat >= latStart && lat <= latEnd && lon >= lonStart && lon <= lonEnd) {
                        return true; //overlap so don't print
                    }
                }
            }
        }
        return false; // no overlap
    }
}
