package com.example.lam_project;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lam_project.managers.DatabaseManager;
import com.example.lam_project.managers.SignalStrengthManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;

import java.io.File;

public class MainActivity extends Activity {
    MapView map = null;
    private static double squareSizeMeters = 10.0;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private double latitude;
    private double longitude;
    private boolean isCameraFixed = true;
    private SignalStrengthManager signalStrengthManager;

    private static final int MODE_LTE = 0;
    private static final int MODE_WIFI = 1;
    private static final int MODE_SOUND = 2;

    private int currentMode = MODE_LTE;
    public void printDatabaseValues() {
        // Get a reference to the database helper
        Context context = map.getContext(); // Make sure you have access to the context where the map is displayed
        DatabaseManager dbHelper = new DatabaseManager(context);

        // Read the data from the database
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                DatabaseManager.COLUMN_ID,
                DatabaseManager.COLUMN_LATITUDE,
                DatabaseManager.COLUMN_LONGITUDE,
                DatabaseManager.COLUMN_COLOR,
                DatabaseManager.COLUMN_TYPE
        };

        Cursor cursor = db.query(
                DatabaseManager.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        // Loop through the cursor to log the data
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_ID));
                    double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_LATITUDE));
                    double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_LONGITUDE));
                    int color = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_COLOR));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_TYPE));

                    Log.d("DatabaseValues", "ID: " + id + ", Latitude: " + latitude + ", Longitude: " + longitude + ", Color: " + color + ", Type: " + type);
                }
            } finally {
                cursor.close();
            }
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        signalStrengthManager = new SignalStrengthManager(this);

        signalStrengthManager.requestSignalStrengthUpdates(new SignalStrengthManager.OnSignalStrengthChangeListener() {
            @Override
            public void onSignalStrengthChanged(int signalStrength) {
               // Log.d("SignalStrength", "LTE Signal Strength: " + signalStrength);
            }
        });
        // Initialize osmdroid configuration (needed for caching, etc.)
        String osmCachePath = getFilesDir().getAbsolutePath() + "/osmdroid";
        Configuration.getInstance().load(getApplicationContext(),
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setOsmdroidBasePath(new File(osmCachePath));
        Configuration.getInstance().setOsmdroidTileCache(new File(osmCachePath));

        // Check if the app has location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the permission if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // If the permission is granted, start getting the location
            startLocationUpdates();
        }


        // Initialize osmdroid configuration (needed for caching, etc.)

        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string

        //inflate and create the map
        setContentView(R.layout.activity_main);

        /*BoundingBox italyBoundingBox = new BoundingBox(35.5, 6.5, 47.1, 18.8);
        map.zoomToBoundingBox(italyBoundingBox, false);*/
        //map.setBuiltInZoomControls(false);
        //IMapController mapController = map.getController();

        map = (MapView) findViewById(R.id.map);

        map.setTileSource(TileSourceFactory.MAPNIK);
        Button toggleButton = findViewById(R.id.btn_toggle_mode);
        toggleButton.setOnClickListener(this::toggleMode);
        map.setClickable(false);
        map.setMultiTouchControls(false);

        startLocationUpdates();

        /*
        GeoPoint startPoint = new GeoPoint(44.494887, 11.3426163);
        mapController.setCenter(startPoint);*/

    }

    public void toggleMode(View view) {
        Button toggleButton = (Button) view;

        switch (currentMode) {
            case MODE_LTE:
                currentMode = MODE_WIFI;
                toggleButton.setBackgroundResource(R.drawable.ic_wifi);
                break;
            case MODE_WIFI:
                currentMode = MODE_SOUND;
                toggleButton.setBackgroundResource(R.drawable.ic_sound);
                break;
            case MODE_SOUND:
                currentMode = MODE_LTE;
                toggleButton.setBackgroundResource(R.drawable.ic_lte);
                break;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        signalStrengthManager.stopSignalStrengthUpdates();
    }
    private void startLocationUpdates() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                printDatabaseValues();
                // get coordinates from gps' location object (android documentation)
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                if (isCameraFixed) {

                    IMapController mapController = map.getController();
                    // Create a GeoPoint using the latitude and longitude variables
                    GeoPoint startPoint = new GeoPoint(latitude, longitude);
                    // Use the GeoPoint as the fixed center of the map
                    map.getController().setCenter(startPoint);
                    // Set the desired fixed zoom level (e.g., 12.0)
                    mapController.setZoom(21.0);
                    GridCreator.createGridOverlay(map, latitude, longitude, squareSizeMeters);

                }
                /*
                // Create a GeoPoint using the latitude and longitude variables
                GeoPoint startPoint = new GeoPoint(latitude, longitude);
                map.getController().setCenter(startPoint);*/
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
                10,   // Minimum distance between updates (e.g., 10 meters)
                locationListener);
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }
}