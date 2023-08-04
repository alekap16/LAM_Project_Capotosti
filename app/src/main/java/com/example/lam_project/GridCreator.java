package com.example.lam_project;


import android.graphics.Color;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

//is working so good cant belive its true, gotta replicate and add "meters" or smthing like that
//so i can re-use the code for 100m squares and 1km. as for now I get one done since this is
//at first look easy to complete for other use cases
public class GridCreator {

    // Size of the squares in meters
    private static final double SQUARE_SIZE_METERS = 10.0;

    // Method to create and display the grid overlay
    public static void createGridOverlay(MapView mapView, double latitude, double longitude) {
        if (mapView == null)
            return;


        // Calculate the latitude and longitude differences for the square size
        double latitudeDiff = metersToLatitude(SQUARE_SIZE_METERS);
        double longitudeDiff = metersToLongitude(SQUARE_SIZE_METERS, latitude);

        // Calculate the boundaries of the square grid
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

        mapView.invalidate();
    }

    // Helper method to calculate the latitude difference for a given distance in meters
    private static double metersToLatitude(double distance) {
        return distance / 111000.0; // 1 degree of latitude is approximately 111 kilometers or 111000 meters
    }

    // Helper method to calculate the longitude difference for a given distance in meters at a specific latitude
    private static double metersToLongitude(double distance, double latitude) {
        double metersPerLongitudeDegree = 111320.0 * Math.cos(Math.toRadians(latitude));
        return distance / metersPerLongitudeDegree;
    }
}