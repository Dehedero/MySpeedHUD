package com.example.nikolay.myspeedhud;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements MyLocationService.LocationServiceDisplay {

    private TextView textView, tvNet;
    SpeedmeterView speedometer;
    double dist = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.tv_main);
        tvNet = findViewById(R.id.tv_net);

        //TODO Permissions check
        startService(new Intent(this, MyLocationService.class));
        MyLocationService.setLocationServiceDisplay(this);

        speedometer = (SpeedmeterView) findViewById(R.id.speedmeter);
        speedometer.setLabelConverter(new SpeedmeterView.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });
        speedometer.setSpeed(50);


    }

    private void checkPermissions(){

    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, MyLocationService.class));
        super.onDestroy();
    }

    private String formatLocation(Location location) {
        if (location == null)
            return "";
        return String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT, spd = %5$.4f, dst = %6$.4f %4$s",
                location.getLatitude(), location.getLongitude(), new Date(
                        location.getTime()), location.getProvider(), location.getSpeed(), dist);
    }



    @Override
    public void update(Location location, double distance) {
        dist = distance;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            textView.setText(formatLocation(location));
        } else if (location.getProvider().equals(
                LocationManager.NETWORK_PROVIDER)) {
            tvNet.setText(formatLocation(location));
        }

        speedometer.setSpeed(location.getSpeed());
    }

}
