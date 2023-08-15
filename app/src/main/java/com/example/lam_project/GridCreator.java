package com.example.lam_project;


import android.util.Log;

import com.example.lam_project.managers.AcousticNoiseManager;
import com.example.lam_project.managers.DatabaseManager;
import com.example.lam_project.managers.SignalStrengthManager;
import com.example.lam_project.managers.WifiSignalManager;
import com.example.lam_project.model.Square;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

//is working so good cant belive its true, gotta replicate and add "meters" or smthing like that
//so i can re-use the code for 100m squares and 1km. as for now I get one done since this is
//at first look easy to complete for other use cases
public class GridCreator {
    private static double currentNoiseLevel = 0.0;
    private static double currentLTESignalStrength = 0.0; // Variable to store the current LTE signal strength
private static double currentWiFiSignalLevel = 0.0; // Store the wifi signal expressed in level.
private static double currentAcousticNoise = 0.0;
    //Create each unique ID for squares, needs for overlap prevention
    private static final List<Polygon> existingSquares = new ArrayList<>();
    public GridCreator() {
        // No need for any constructor parameters in this case
    }
    // Method to create and display the grid overlay
    public static List<Square> retrieveSquares(MapView map,  int mode, double squareSizeMeters) {
        DatabaseManager databaseManager = new DatabaseManager(map.getContext());
        List<Square> squares = databaseManager.getAllSquares();

        List<Square> filteredSquares = new ArrayList<>();
        for (Square square : squares) {
            if (square.getType() == mode && square.getSquareSize() == squareSizeMeters) {
                filteredSquares.add(square);
            }
        }
        databaseManager.close();
        return filteredSquares;
    }
    public static void createGridExistingSquares(MapView mapView, Square existingSquare) {
        if (mapView == null)
            return;

        double latitudeStart = existingSquare.getLatitudeStart();
        double longitudeStart = existingSquare.getLongitudeStart();
        double latitudeEnd = existingSquare.getLatitudeEnd();
        double longitudeEnd = existingSquare.getLongitudeEnd();

            // Create the square around the given coordinates
            List<GeoPoint> squarePoints = new ArrayList<>();
            squarePoints.add(new GeoPoint(latitudeStart, longitudeStart));
            squarePoints.add(new GeoPoint(latitudeEnd, longitudeStart));
            squarePoints.add(new GeoPoint(latitudeEnd, longitudeEnd));
            squarePoints.add(new GeoPoint(latitudeStart, longitudeEnd));

            Polygon square = new Polygon(mapView);
            square.setPoints(squarePoints);

            PainterExistingSquares.paintExistingSquares(mapView, square, existingSquare.getColor());
            // Store the current LTE signal strength from SignalStrengthManager

            mapView.invalidate();
    }
    public static void createSquare(MapView mapView, double latitude, double longitude,
                                    double squareSizeMeters, int mode) {
        if (mapView == null)
            return;

        List<Square> existingSquares = retrieveSquares(mapView, mode, squareSizeMeters);
        if (!doesSquareOverlap(existingSquares, latitude, longitude, squareSizeMeters,
                mode, mapView)) {

            double latitudeDiff = metersToLatitude(squareSizeMeters);
            double longitudeDiff = metersToLongitude(squareSizeMeters, latitude);

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
            //mapView.getOverlayManager().add(square);
            //existingSquares.add(square);
            //mapView.invalidate();
            if (mode == 1) {
                // Store the current LTE signal strength from SignalStrengthManager
                SignalStrengthManager signalStrengthManager = new
                        SignalStrengthManager(mapView.getContext());
                signalStrengthManager.requestSignalStrengthUpdates(signalStrength -> {

                    // Store the current LTE signal strength
                    currentLTESignalStrength = signalStrength;
                    signalStrengthManager.stopSignalStrengthUpdates();
                });

                // Paint the square based on the current LTE signal strength
                LTESignalPainter.paintSquareByLTESignalStrength(mapView, square,
                        currentLTESignalStrength, mode, squareSizeMeters);
            }
            else if(mode == 2) {
                WifiSignalManager wifiSignalManager = new WifiSignalManager(mapView.getContext());
                currentWiFiSignalLevel = wifiSignalManager.getWifiSignalStrength();
                WiFiSignalPainter.paintSquareByWiFiSignalStrength(mapView, square,
                        currentWiFiSignalLevel, mode, squareSizeMeters );
               // Log.d("Wifi log", "Wifi signal Dbm"+wifiSignalManager.getWifiSignalStrength());
            } else {
                    AcousticNoiseManager mNoiseManager = new AcousticNoiseManager();
                    mNoiseManager.startRecording(mapView.getContext(), noiseLevel -> {
                        currentNoiseLevel = noiseLevel;
                        Log.d("NoiseLevel", "Current noise level: " + noiseLevel + " dB");
                        mNoiseManager.stopRecording();
                    });

                    AcousticNoisePainter.paintSquareByAcousticNoise(mapView, square,
                            currentNoiseLevel, mode, squareSizeMeters);
                }
        }
    }

    // Helper method to calculate the latitude difference for a given distance in meters
    private static double metersToLatitude(double distance) {
        return distance / 111000.0;
    }

    // Helper method to calculate the longitude difference for a given distance in
    // meters at specific latitudes
    private static double metersToLongitude(double distance, double latitude) {
        double metersPerLongitudeDegree = 111320.0 * Math.cos(Math.toRadians(latitude));
        return distance / metersPerLongitudeDegree;
    }

    // Helper method that chjecks if overlaps are a thing
    private static boolean doesSquareOverlap(List<Square> existingSquares,
                                             double latitude, double longitude,
                                             double squareSizeMeters, int mode,
                                             MapView mapView) {

        for (Square square : existingSquares) {
            double latStart = square.getLatitudeStart();
            double latEnd = square.getLatitudeEnd();
            double lonStart = square.getLongitudeStart();
            double lonEnd = square.getLongitudeEnd();
            // Check if coordinates collides with the squares already painted for overlaps
            for (double lat = latitude - 0.5 * metersToLatitude(squareSizeMeters); lat <= latitude + 0.5 * metersToLatitude(squareSizeMeters); lat += 0.5 * metersToLatitude(squareSizeMeters)) {
                for (double lon = longitude - 0.5 * metersToLongitude(squareSizeMeters, latitude); lon <= longitude + 0.5 * metersToLongitude(squareSizeMeters, latitude); lon += 0.5 * metersToLongitude(squareSizeMeters, latitude)) {
                    if (lat >= latStart && lat <= latEnd && lon >= lonStart && lon <= lonEnd) {
                        if (mode == 1) {
                            // Store the current LTE signal strength from SignalStrengthManager
                            SignalStrengthManager signalStrengthManager = new
                                    SignalStrengthManager(mapView.getContext());
                            signalStrengthManager.requestSignalStrengthUpdates(signalStrength -> {

                                // Store the current LTE signal strength
                                currentLTESignalStrength = signalStrength;
                                signalStrengthManager.stopSignalStrengthUpdates();
                            });

                            // Paint the square based on the current LTE signal strength
                            DatabaseManager.updateSquare(square, currentLTESignalStrength, mapView);
                        }
                        else if(mode == 2) {
                            WifiSignalManager wifiSignalManager = new WifiSignalManager(mapView.getContext());
                            currentWiFiSignalLevel = wifiSignalManager.getWifiSignalStrength();
                            DatabaseManager.updateSquare(square, currentWiFiSignalLevel, mapView);
                            // Log.d("Wifi log", "Wifi signal Dbm"+wifiSignalManager.getWifiSignalStrength());
                        } else {
                            AcousticNoiseManager mNoiseManager = new AcousticNoiseManager();
                            mNoiseManager.startRecording(mapView.getContext(), noiseLevel -> {
                                currentNoiseLevel = noiseLevel;
                                mNoiseManager.stopRecording();
                            });
                            DatabaseManager.updateSquare(square, currentNoiseLevel, mapView);
                        }
                        return true;
//                        Log.d("lat", "lat: "+lat);
//                        Log.d("latStart", "latStart: "+latStart);
//                        Log.d("latEnd", "latEnd: "+latEnd);
//                        Log.d("lon", "lon: "+lon);
//                        Log.d("lonStart", "lonStart: "+lonStart);
//                        Log.d("lonEnd", "lonEnd: "+lonEnd);
                    }
                }
            }
        }
        return false; // no overlap
    }
}
