package com.example.lam_project;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private double latitude;
    private double longitude;
    private boolean isCameraFixed = true;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        // Initialize osmdroid for caching and stuff
        //I should wrap this into a class but to lazy will do at the end of project
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
            // when permission OK perform updates, this solves the control issue I tought about
            startLocationUpdates();
        }

        //Osmdroid stuff getting API
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string

        //inflate and create the map
        setContentView(R.layout.activity_main);

        // what is this it's not accomplish anything can't truly bound the map by default
        //or am i stupid that cant figure it out why but keeping this commented as for now
        /*BoundingBox italyBoundingBox = new BoundingBox(35.5, 6.5, 47.1, 18.8);
        map.zoomToBoundingBox(italyBoundingBox, false);*/
        //map.setBuiltInZoomControls(false);
        //IMapController mapController = map.getController();

        map = (MapView) findViewById(R.id.map);

        map.setTileSource(TileSourceFactory.MAPNIK);

        map.setClickable(false);
        map.setMultiTouchControls(false);

        startLocationUpdates();

        /*
        GeoPoint startPoint = new GeoPoint(44.494887, 11.3426163);
        mapController.setCenter(startPoint);*/

    }

    private void startLocationUpdates() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                // get coordinates from gps' location object (android documentation)
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                if (isCameraFixed) {

                    IMapController mapController = map.getController();
                    // Create a GeoPoint using the latitude and longitude variables
                    GeoPoint startPoint = new GeoPoint(latitude, longitude);
                    // Use the GeoPoint as the fixed center of the map
                    map.getController().setCenter(startPoint);
                    // Set zoom
                    mapController.setZoom(22.0);
                    GridCreator.createGridOverlay(map, latitude, longitude);

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
                1000, 10, locationListener);
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume();
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();
    }
}