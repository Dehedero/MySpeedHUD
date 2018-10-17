package com.example.nikolay.myspeedhud;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity implements MyLocationService.LocationServiceDisplay {

    SpeedmeterView speedmeter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO Permissions check
        startService(new Intent(this, MyLocationService.class));
        MyLocationService.setLocationServiceDisplay(this);

        speedmeter = findViewById(R.id.speedmeter);

       // speedometer.setRotationY(180);


    }

    private void checkPermissions(){
        //todo Permissions check
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, MyLocationService.class));
        super.onDestroy();
    }

    @Override
    public void update(double speed, double distance) {
        speedmeter.moveToValue((float) (3.6f*speed));
        speedmeter.setdistance(distance, "Km");
    }
}
