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

public class MyLocationService extends Service implements LocationListener{

    private static final String TAG = MyLocationService.class.getSimpleName();

    private static LocationServiceDisplay locationServiceDisplay;
    private static final int FASTEST_INTERVAL = 2 * 1000;
    private static final float MINIMAL_DISTANCE = 10f;

    LocationManager locationManager;
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if(prevLoc != null && location != null){
                distance += prevLoc.distanceTo(location);
            }
            prevLoc = location;

            if(locationServiceDisplay != null && location != null){
                locationServiceDisplay.update(location, distance);
            }

            Log.i(TAG, "Location changed");
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            checkEnabled();
            Log.i(TAG, s + "//" + i + "Status changed");
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.i(TAG, "Enabled: " + s);
            @SuppressLint("MissingPermission")
            Location location = locationManager.getLastKnownLocation(s);
            if(location != null) {
                locationServiceDisplay.update(location, distance);
            }
            checkEnabled();
        }

        @Override
        public void onProviderDisabled(String s) {
            checkEnabled();
            Log.i(TAG, "Disabled: " + s);
        }
    };
    Location prevLoc;

    double distance = 0;

    public MyLocationService() {

    }

    @Override
    public void onCreate() {

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, FASTEST_INTERVAL, MINIMAL_DISTANCE, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, FASTEST_INTERVAL, MINIMAL_DISTANCE, locationListener);
        checkEnabled();

        Log.i(TAG, locationManager.getProviders(true).toString());


        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onLocationChanged(Location location) {

        if(prevLoc != null && location != null){
            distance += prevLoc.distanceTo(location);
        }
        prevLoc = location;

        if(locationServiceDisplay != null && location != null){
            locationServiceDisplay.update(location, distance);
        }

        Log.i(TAG, "Location changed");

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.i(TAG, s + "//" + i + "Status changed");
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onProviderEnabled(String s) {
        Log.i(TAG, "Enabled: " + s);
        Location location = locationManager.getLastKnownLocation(s);
        if(location != null) {
            locationServiceDisplay.update(location, distance);
        }

    }

    @Override
    public void onProviderDisabled(String s) {
        Log.i(TAG, "Disabled: " + s);
    }

    //Интерфейс для отоброжения обновленной информации о позиции устройства

    interface LocationServiceDisplay {
        public void update(Location location, double distance);
    }

    public static void setLocationServiceDisplay(LocationServiceDisplay _locationServiceDisplay){
        locationServiceDisplay = _locationServiceDisplay;
    }

    private void checkEnabled() {
        Log.i(TAG,"Enabled: "
                + locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER));
        Log.i(TAG, "Enabled: "
                + locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

}
