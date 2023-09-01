package com.example.lam_project.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lam_project.R;
import com.example.lam_project.logic.RefreshExpiryThread;
import com.example.lam_project.logic.SquareCreator;
import com.example.lam_project.managers.ButtonManager;
import com.example.lam_project.managers.NotificationsManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;


public class MainActivity extends Activity {
    MapView map = null;
    private static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1;
    private static double RANGE_SMALL = 10.0;
    private static double RANGE_MEDIUM = 100.0;
    private static double RANGE_BIG = 1000.0;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private double latitude;
    private double longitude;
    private static final int MODE_LTE = 1;
    private static final int MODE_WIFI = 2;
    private static final int MODE_SOUND = 3;
    private boolean isButtonRangesClickable = true;
    private boolean isButtonModeClickable = true;

    public void Caching(){
        // Initialize osmdroid configuration (needed for caching, etc.)
        String osmCachePath = getFilesDir().getAbsolutePath() + "/osmdroid";

        Configuration.getInstance().load(getApplicationContext(),
                androidx.preference.PreferenceManager.getDefaultSharedPreferences
                        (getApplicationContext()));
        Configuration.getInstance().setOsmdroidBasePath(new File(osmCachePath));
        Configuration.getInstance().setOsmdroidTileCache(new File(osmCachePath));

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Caching();
        // Check if the app has location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the permission if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            //Granted permission
            startLocationUpdates();
        }

        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        //inflate and create the map
        setContentView(R.layout.activity_main);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setClickable(true);
        map.setMultiTouchControls(true);

        ButtonManager buttonManager = new ButtonManager(map.getContext());

        Button toggleModeButton = findViewById(R.id.btn_toggle_mode);
        Button manualScanButton = findViewById(R.id.btn_manual_scan);
        toggleModeButton.setOnClickListener(this::toggleMode);

        if(buttonManager.getCurrentMode() == MODE_WIFI) {
            toggleModeButton.setBackgroundResource(R.drawable.ic_wifi);
        }
        else if(buttonManager.getCurrentMode() == MODE_SOUND) {
            toggleModeButton.setBackgroundResource(R.drawable.ic_sound);
        }

        Button toggleDistanceButton = findViewById(R.id.btn_toggle_distances);

        if(buttonManager.getCurrentSquareSizeMeters() == RANGE_MEDIUM) {
            toggleDistanceButton.setText("100M");
        }
        else if (buttonManager.getCurrentSquareSizeMeters() == RANGE_BIG){
            toggleDistanceButton.setText("1KM");
        }

        toggleDistanceButton.setOnClickListener(this::toggleDistance);
        manualScanButton.setOnClickListener(this::manualScan);

        NotificationsManager.createNotificationChannel(this);

        RefreshExpiryThread.expiredAndRefreshedSquares(map);

        startLocationUpdates();
    }

    private void manualScan(View view) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        new Handler().postDelayed(() -> isButtonRangesClickable = true, 2000);
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
    }

    private final LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(@NonNull Location location) {
            // Update the latitude and longitude variables
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            IMapController mapController = map.getController();
            mapController.setZoom(21.0); //Zoom function

            GeoPoint startPoint = new GeoPoint(latitude, longitude); //Fixed center
            map.getController().setCenter(startPoint);
            SquareCreator.createSquare(map, latitude, longitude, true);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };


    public void toggleMode(View view) {
        Button toggleButton = (Button) view;
        if (!isButtonModeClickable) {
            return;
        }

        // Disable button click temporarily
        isButtonModeClickable = false;
        ButtonManager buttonManager = new ButtonManager(map.getContext());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isButtonModeClickable = true;
            }
        }, 5000);
        //This switch case isn't broken as it may seems; it kinda works for the next iteration. If I
        //press the LTE signal it means i'm switching with a single 'tap' to the Wifi, so I'm
        //looking to trigger the Wi-Fi related stuff, same goes for the other two.
        switch (buttonManager.getCurrentMode()) {
            case MODE_LTE:
                Toast.makeText(this, "Wi-Fi signal", Toast.LENGTH_SHORT).show();
                map.getOverlays().clear();
                buttonManager.setCurrentMode(MODE_WIFI);
                toggleButton.setBackgroundResource(R.drawable.ic_wifi);
                break;
            case MODE_WIFI:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_REQUEST_CODE);
                } else {
                    Toast.makeText(this, "Acoustic noise", Toast.LENGTH_SHORT).show();
                    map.getOverlays().clear();
                    buttonManager.setCurrentMode(MODE_SOUND);
                    toggleButton.setBackgroundResource(R.drawable.ic_sound);
                }
                break;
            case MODE_SOUND:
                Toast.makeText(this, "LTE signal", Toast.LENGTH_SHORT).show();
                map.getOverlays().clear();
                buttonManager.setCurrentMode(MODE_LTE);
                toggleButton.setBackgroundResource(R.drawable.ic_lte);
                break;
        }
    }

    public void toggleDistance(View view) {
        Button toggleButton = (Button) view;
        if (!isButtonRangesClickable) {
            return;
        }

        isButtonRangesClickable = false;

        ButtonManager buttonManager = new ButtonManager(map.getContext());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isButtonRangesClickable = true;
            }
        }, 5000);
        //Same mechanism: the current selection triggers the next and so on
        switch ((int) buttonManager.getCurrentSquareSizeMeters()) {
            case 10:
                Toast.makeText(this, "100 meters range", Toast.LENGTH_SHORT).show();
                map.getOverlays().clear();
                buttonManager.setCurrentSquareSizeMeters(RANGE_MEDIUM);
                toggleButton.setText("100M");
                break;
            case 100:
                Toast.makeText(this, "1 kilometer range", Toast.LENGTH_SHORT).show();
                map.getOverlays().clear();
                buttonManager.setCurrentSquareSizeMeters(RANGE_BIG);
                toggleButton.setText("1KM");
                break;
            case 1000:
                Toast.makeText(this, "10 meters range", Toast.LENGTH_SHORT).show();
                map.getOverlays().clear();
                buttonManager.setCurrentSquareSizeMeters(RANGE_SMALL);
                toggleButton.setText("10M");
                break;
        }
    }


    public void openSettingsActivity(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    private void startLocationUpdates() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // get coordinates from gps' location object
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                IMapController mapController = map.getController();
                mapController.setZoom(21.0);

                GeoPoint startPoint = new GeoPoint(latitude, longitude);
                map.getController().setCenter(startPoint);

                SquareCreator.createSquare(map, latitude, longitude, false);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        // Request location updates with a minimum time interval (in milliseconds) and minimum distance (in meters)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000, // Minimum time interval between updates (e.g., 1000ms = 1 second)
                1,   // Minimum distance between updates (e.g., 10 meters)
                locationListener);
    }

    public void onResume(){
        super.onResume();
        map.onResume();
    }

    public void onPause(){
        super.onPause();
        map.onPause();
    }
}