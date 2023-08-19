package com.example.lam_project;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lam_project.managers.ButtonManager;
import com.example.lam_project.managers.DatabaseManager;
import com.example.lam_project.managers.SignalStrengthManager;
import com.example.lam_project.model.Square;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;
import java.util.List;

public class MainActivity extends Activity {
    MapView map = null;

    private static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1001;
    private static double RANGE_SMALL = 10.0;
    private static double RANGE_MEDIUM = 100.0;
    private static double RANGE_BIG = 1000.0;
    private static double squareSizeMeters = 10.0;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private double latitude;
    private double longitude;
    private boolean isCameraFixed = true;
    private SignalStrengthManager signalStrengthManager;

    private static final int MODE_LTE = 1;
    private static final int MODE_WIFI = 2;
    private static final int MODE_SOUND = 3;

    private int currentMode = MODE_LTE;
    private boolean isButtonRangesClickable = true;
    private boolean isButtonModeClickable = true;

    private LocationManager locationManager;

    public void printDatabaseValues() {
        // Get a reference to the database helper
        Context context = map.getContext(); // Make sure you have access to the context where the map is displayed
        DatabaseManager dbHelper = new DatabaseManager(context);

        // Read the data from the database
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                DatabaseManager.COLUMN_ID,
                DatabaseManager.COLUMN_LATITUDE_START,
                DatabaseManager.COLUMN_LONGITUDE_START,
                DatabaseManager.COLUMN_LATITUDE_END,
                DatabaseManager.COLUMN_LONGITUDE_END,
                DatabaseManager.COLUMN_COLOR,
                DatabaseManager.COLUMN_TYPE,
                DatabaseManager.COLUMN_SIZE
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
                    double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_LATITUDE_START));
                    double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_LONGITUDE_START));
                    int color = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_COLOR));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_TYPE));
                    double size = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_SIZE));

                    Log.d("DatabaseValues", "ID: " + id + ", Latitude: " + latitude + "," +
                            " Longitude: " + longitude + ", Color: " + color + ", Type: " +
                            type + ", Size: " + size);
                }
            } finally {
                cursor.close();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Questo lo devo togliere
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
        ButtonManager buttonManager = new ButtonManager(map.getContext());
        Button toggleModeButton = findViewById(R.id.btn_toggle_mode);
        Button manualScanButton = findViewById(R.id.btn_manual_scan);
        //this name is dumb because it's not actually on Button change (in this instance);
        //but It does the same static thing aka printing the squares in db so no need to change
        printExistingSquaresOnButtonChange(buttonManager.getCurrentMode(), buttonManager.getCurrentSquareSizeMeters());
        toggleModeButton.setOnClickListener(this::toggleMode);

        if(buttonManager.getCurrentMode() == MODE_WIFI) {
            toggleModeButton.setBackgroundResource(R.drawable.ic_wifi);
        }
        else if(buttonManager.getCurrentMode() == MODE_SOUND) {
            Log.d("TEST", "test");
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
        map.setClickable(false);
        map.setMultiTouchControls(false);
        GridCreator.expiredSquares(map, buttonManager.getCurrentMode(), buttonManager.getCurrentSquareSizeMeters());
        startLocationUpdates();
        /*
        GeoPoint startPoint = new GeoPoint(44.494887, 11.3426163);
        mapController.setCenter(startPoint);*/

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
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            // Update the latitude and longitude variables
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            IMapController mapController = map.getController();
            // Create a GeoPoint using the latitude and longitude variables
            GeoPoint startPoint = new GeoPoint(latitude, longitude);
            // Use the GeoPoint as the fixed center of the map
            map.getController().setCenter(startPoint);
            // Set the desired fixed zoom level (e.g., 12.0)
            mapController.setZoom(21.0);
            ButtonManager buttonManager = new ButtonManager(map.getContext());
            GridCreator.createSquare(map, latitude, longitude,
                    buttonManager.getCurrentSquareSizeMeters(), buttonManager.getCurrentMode());

            // Do something with the updated coordinates
            // For example, display them on the UI or use them in your logic

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
        Log.d("TEST", "MODE: " +buttonManager.getCurrentMode());

        // Add a delay of 1 second before enabling button click again
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
                //printExistingSquaresOnButtonChange(currentMode, squareSizeMeters);
                break;
            case MODE_WIFI:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_REQUEST_CODE);
                } else {
                    Toast.makeText(this, "Acustic noise", Toast.LENGTH_SHORT).show();
                    map.getOverlays().clear();
                    buttonManager.setCurrentMode(MODE_SOUND);
                    toggleButton.setBackgroundResource(R.drawable.ic_sound);
                    //printExistingSquaresOnButtonChange(currentMode, squareSizeMeters);
                }
                break;
            case MODE_SOUND:
                Toast.makeText(this, "LTE signal", Toast.LENGTH_SHORT).show();
                map.getOverlays().clear();
                buttonManager.setCurrentMode(MODE_LTE);
                toggleButton.setBackgroundResource(R.drawable.ic_lte);
                //printExistingSquaresOnButtonChange(currentMode, squareSizeMeters);
                break;
        }
    }

    public void toggleDistance(View view) {
        Button toggleButton = (Button) view;
        if (!isButtonRangesClickable) {
            return;
        }

        // Disable button click temporarily
        isButtonRangesClickable = false;

        ButtonManager buttonManager = new ButtonManager(map.getContext());

        // Add a delay of 1 second before enabling button click again
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
                //printExistingSquaresOnButtonChange(currentMode, squareSizeMeters);
                break;
            case 100:
                Toast.makeText(this, "1 kilometer range", Toast.LENGTH_SHORT).show();
                map.getOverlays().clear();
                buttonManager.setCurrentSquareSizeMeters(RANGE_BIG);
                toggleButton.setText("1KM");
                //printExistingSquaresOnButtonChange(currentMode, squareSizeMeters);
                break;
            case 1000:
                Toast.makeText(this, "10 meters range", Toast.LENGTH_SHORT).show();
                map.getOverlays().clear();
                buttonManager.setCurrentSquareSizeMeters(RANGE_SMALL);
                toggleButton.setText("10M");
                //printExistingSquaresOnButtonChange(currentMode, squareSizeMeters);
                break;
        }
    }

    public void printExistingSquaresOnButtonChange(int currentMode, double squareSizeMeters){
        List<Square> squares = GridCreator.retrieveSquares(map, currentMode, squareSizeMeters);

        for (Square square : squares) {
        GridCreator.createGridExistingSquares(map, square);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        signalStrengthManager.stopSignalStrengthUpdates();
    }

    public void openSettingsActivity(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    private void startLocationUpdates() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ButtonManager buttonManager = new ButtonManager(this);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //printDatabaseValues();
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
                    GridCreator.createSquare(map, latitude, longitude,
                            buttonManager.getCurrentSquareSizeMeters(), buttonManager.getCurrentMode());
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
                1,   // Minimum distance between updates (e.g., 10 meters)
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