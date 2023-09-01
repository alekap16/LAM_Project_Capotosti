package com.example.lam_project.logic;


import android.os.Handler;
import android.util.Log;

import com.example.lam_project.graphics.AcousticNoisePainter;
import com.example.lam_project.graphics.LTESignalPainter;
import com.example.lam_project.graphics.WiFiSignalPainter;
import com.example.lam_project.logic.PainterExistingSquares;
import com.example.lam_project.managers.AcousticNoiseManager;
import com.example.lam_project.managers.ButtonManager;
import com.example.lam_project.managers.DatabaseManager;
import com.example.lam_project.managers.NotificationsManager;
import com.example.lam_project.managers.SettingsManager;
import com.example.lam_project.managers.SignalStrengthManager;
import com.example.lam_project.managers.WifiSignalManager;
import com.example.lam_project.model.Square;
import com.example.lam_project.utils.Utils;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

public class SquareCreator {
    private static double currentNoiseLevel = 0.0; // Store noise in dB
    private static double currentLTESignalStrength = 0.0; // Variable to store the current LTE signal strength
    private static double currentWiFiSignalLevel = 0.0; // Store the wifi signal expressed in level.

    public static void createExistingSquares(MapView mapView, Square existingSquare) {
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

    }
    public static void createSquare(MapView mapView, double latitude, double longitude,
                                    boolean isManual) {

        if (mapView == null) return;

        NotificationsManager.createNotificationChannel(mapView.getContext());
        SettingsManager settingsManager = new SettingsManager(mapView.getContext());
        NotificationsManager notificationsManager = new NotificationsManager(mapView.getContext());
        ButtonManager buttonManager = new ButtonManager(mapView.getContext());

        List<Square> existingSquares = DatabaseManager.retrieveSquares(mapView);

        if (!doesSquareOverlap(existingSquares, latitude, longitude, mapView)) {

            if(settingsManager.isAutoScanEnabled() || isManual == true) {
                double latitudeDiff =
                        Utils.metersToLatitude(buttonManager.getCurrentSquareSizeMeters());
                double longitudeDiff =
                        Utils.metersToLongitude(buttonManager.getCurrentSquareSizeMeters(), latitude);

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

                if (buttonManager.getCurrentMode() == 1) {
                    // Store the current LTE signal strength from SignalStrengthManager
                    SignalStrengthManager signalStrengthManager = new
                            SignalStrengthManager(mapView.getContext());
                    signalStrengthManager.requestSignalStrengthUpdates(signalStrength -> {

                        currentLTESignalStrength = signalStrength;
                        signalStrengthManager.stopSignalStrengthUpdates();

                    });

                    // Paint the square based on the current LTE signal strength
                    LTESignalPainter.paintSquareByLTESignalStrength(mapView, square,
                            currentLTESignalStrength);
//                    Log.d("LTE log", "LTE signal strenght: "+currentLTESignalStrength);
                } else if (buttonManager.getCurrentMode() == 2) {
                    WifiSignalManager wifiSignalManager = new WifiSignalManager(mapView.getContext());
                    currentWiFiSignalLevel = wifiSignalManager.getWifiSignalStrength();
                    WiFiSignalPainter.paintSquareByWiFiSignalStrength(mapView, square,
                            currentWiFiSignalLevel);
//                    Log.d("Wifi log", "Wifi signal Dbm"+wifiSignalManager.getWifiSignalStrength());
                } else {

                    AcousticNoiseManager mNoiseManager = new AcousticNoiseManager();
                    mNoiseManager.startRecording(mapView.getContext(), new AcousticNoiseManager.NoiseLevelCallback() {
                        @Override
                        public void onNoiseLevelMeasured(double noiseLevelInDb) {
                            // Log the noise level for testing
                            Log.d("NoiseLevel", "Current noise level: " + noiseLevelInDb + " dB");
                            currentNoiseLevel = noiseLevelInDb;
                            mNoiseManager.stopRecording();
                        }
                    });

                    AcousticNoisePainter.paintSquareByAcousticNoise(mapView, square,
                            currentNoiseLevel);
//                    Log.d("input", "currentNoiseLevel. " + currentNoiseLevel);
                }
            } else {

                if (buttonManager.getCurrentSquareSizeMeters() == 10.0
                        && notificationsManager.is10MNotificationsEnabled()
                        && notificationsManager.areNotificationsEnabled())
                    NotificationsManager.show10MNotification(mapView.getContext());

                else if (buttonManager.getCurrentSquareSizeMeters() == 100.0
                        && notificationsManager.is100MNotificationsEnabled()
                        && notificationsManager.areNotificationsEnabled())
                    NotificationsManager.show100MNotification(mapView.getContext());

                else if (buttonManager.getCurrentSquareSizeMeters() == 1000.0
                        && notificationsManager.is1KMNotificationsEnabled()
                        && notificationsManager.areNotificationsEnabled())
                    NotificationsManager.show1KMNotification(mapView.getContext());
            }
        }
    }



    // Helper method that checks if overlaps are a thing
    private static boolean doesSquareOverlap(List<Square> existingSquares,
                                             double latitude, double longitude, MapView map) {

        ButtonManager buttonManager = new ButtonManager(map.getContext());
        for (Square square : existingSquares) {
                double latStart = square.getLatitudeStart();
                double latEnd = square.getLatitudeEnd();
                double lonStart = square.getLongitudeStart();
                double lonEnd = square.getLongitudeEnd();
                // Check if coordinates collides with the squares already painted for overlaps
                for (double lat = latitude - 0.5 * Utils.metersToLatitude(buttonManager.getCurrentSquareSizeMeters()); lat <= latitude + 0.5 * Utils.metersToLatitude(buttonManager.getCurrentSquareSizeMeters()); lat += 0.5 * Utils.metersToLatitude(buttonManager.getCurrentSquareSizeMeters())) {
                    for (double lon = longitude - 0.5 * Utils.metersToLongitude(buttonManager.getCurrentSquareSizeMeters(), latitude); lon <= longitude + 0.5 * Utils.metersToLongitude(buttonManager.getCurrentSquareSizeMeters(), latitude); lon += 0.5 * Utils.metersToLongitude(buttonManager.getCurrentSquareSizeMeters(), latitude)) {
                        if (lat >= latStart && lat <= latEnd && lon >= lonStart && lon <= lonEnd) {
                            if (buttonManager.getCurrentMode() == 1) {
                                // Store the current LTE signal strength from SignalStrengthManager
                                SignalStrengthManager signalStrengthManager = new
                                        SignalStrengthManager(map.getContext());
                                signalStrengthManager.requestSignalStrengthUpdates(signalStrength -> {

                                    // Store the current LTE signal strength
                                    currentLTESignalStrength = signalStrength;
                                    signalStrengthManager.stopSignalStrengthUpdates();
                                });

                                // Paint the square based on the current LTE signal strength
                                DatabaseManager.updateSquare(square, currentLTESignalStrength, map);
                            } else if (buttonManager.getCurrentMode()  == 2) {
                                WifiSignalManager wifiSignalManager = new WifiSignalManager(map.getContext());
                                currentWiFiSignalLevel = wifiSignalManager.getWifiSignalStrength();
                                DatabaseManager.updateSquare(square, currentWiFiSignalLevel, map);
                                // Log.d("Wifi log", "Wifi signal Dbm"+wifiSignalManager.getWifiSignalStrength());
                            } else {
                                AcousticNoiseManager mNoiseManager = new AcousticNoiseManager();

                                mNoiseManager.startRecording(map.getContext(), new AcousticNoiseManager.NoiseLevelCallback() {
                                    @Override
                                    public void onNoiseLevelMeasured(double noiseLevelInDb) {
                                        // Use the noise level value (noiseLevelInDb) as needed
                                        // For example, update UI elements with the noise level value
                                        // Remember to consider thread synchronization if updating UI

                                        // Log the noise level for testing
                                        Log.d("NoiseLevel", "Current noise level: " + noiseLevelInDb + " dB");
                                        currentNoiseLevel = noiseLevelInDb;
                                        mNoiseManager.stopRecording();
                                    }
                                });
                                DatabaseManager.updateSquare(square, currentNoiseLevel, map);
                            }
                            return true;
                        }
                        }
                    }

            }
            return false; // no overlap
        }

}
