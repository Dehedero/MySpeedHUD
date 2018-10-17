package com.example.nikolay.myspeedhud;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class MyLocationService extends Service {

    private static final String TAG = MyLocationService.class.getSimpleName();

    private static LocationServiceDisplay locationServiceDisplay;
    private static LocationServiceDisplay secondaryLocationServiceDisplay;
    private static final int FASTEST_INTERVAL = 2 * 1000;
    private static final float MINIMAL_DISTANCE = 10f;
    private static double speed = 0;
    private static double distance = 0;

    LocationManager locationManager;
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if(prevLoc != null && location != null){
                distance += (prevLoc.distanceTo(location) / 1000);
            }
            prevLoc = location;
            if(locationServiceDisplay != null && location != null){

                if(location.hasSpeed()){
                    speed = location.getSpeed();
                } else {
                    speed = 0;
                }

                locationServiceDisplay.update(speed, distance);
            }

            if(secondaryLocationServiceDisplay != null && location != null){

                if(location.hasSpeed()){
                    speed = location.getSpeed();
                } else {
                    speed = 0;
                }

                secondaryLocationServiceDisplay.update(speed, distance);
            }

            Log.d(TAG, "Location changed");
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d(TAG, s + " // " + i + " Status changed");
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.d(TAG, "Enabled: " + s);
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.d(TAG, "Disabled: " + s);
        }
    };
    Location prevLoc;

    public MyLocationService() {

    }

    @Override
    public void onCreate() {

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        distance = intent.getDoubleExtra(MainActivity.dist, 0);

        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, FASTEST_INTERVAL, MINIMAL_DISTANCE, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, FASTEST_INTERVAL, MINIMAL_DISTANCE, locationListener);

        Log.i(TAG, locationManager.getProviders(true).toString());

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    interface LocationServiceDisplay {
        public void update(double speed, double distance);
    }

    public static void setLocationServiceDisplay(LocationServiceDisplay _locationServiceDisplay){
        locationServiceDisplay = _locationServiceDisplay;
    }

    public static void setSecondaryLocationServiceDisplay(LocationServiceDisplay _locationServiceDisplay){
        secondaryLocationServiceDisplay = _locationServiceDisplay;
    }


}
