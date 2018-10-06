package com.example.nikolay.myspeedhud;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements MyLocationService.LocationServiceDisplay {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.tv_main);

        //TODO Permissions check
        startService(new Intent(this, MyLocationService.class));
        MyLocationService.setLocationServiceDisplay(this);


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
                "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT, spd = %5$.4f %4$s",
                location.getLatitude(), location.getLongitude(), new Date(
                        location.getTime()), location.getProvider(), location.getSpeed());
    }



    @Override
    public void update(Location location) {
        textView.setText(formatLocation(location));
    }

}
