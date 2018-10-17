package com.example.nikolay.myspeedhud;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;


public class MainActivity extends AppCompatActivity implements MyLocationService.LocationServiceDisplay, View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private  SpeedmeterView speedmeter;
    private ToggleButton toggleButton;
    private double lastDistance = 0;
    private double lastSpeed = 0;
    private float screenBrightness;
    private Button btnChangeMod;
    private int orientation;
    public static final String dist = "LASR_DISTANCE";

    public  CompoundButton.OnCheckedChangeListener chekedChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if(b){
                hideSystemUI();
                speedmeter.setRotationY(180);
                WindowManager.LayoutParams layout = getWindow().getAttributes();
                screenBrightness = layout.screenBrightness;
                layout.screenBrightness = 1f;
                getWindow().setAttributes(layout);
            } else {
                showSystemUI();
                speedmeter.setRotationY(0);
                WindowManager.LayoutParams layout = getWindow().getAttributes();
                layout.screenBrightness = screenBrightness;
                getWindow().setAttributes(layout);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState != null){
            lastDistance = savedInstanceState.getDouble(dist, 0);
        }

        orientation = getResources().getConfiguration().orientation;

        //TODO Permissions check
        Intent intent = new Intent(this, MyLocationService.class);
        intent.putExtra(dist, lastDistance);
        if(checkPermissions()) {
            startService(intent);
            MyLocationService.setLocationServiceDisplay(this);
        } else {
            Toast.makeText(this, "Dont get permission", Toast.LENGTH_LONG).show();
        }

        speedmeter = findViewById(R.id.speedmeter);
        speedmeter.setdistance(lastDistance, "Km");
        toggleButton = findViewById(R.id.hudToggle);
        toggleButton.setOnCheckedChangeListener(chekedChangedListener);
        btnChangeMod = findViewById(R.id.btn_change_mod);
        btnChangeMod.setOnClickListener(this);
    }

    private boolean checkPermissions(){
        if(hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) && hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)){
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        if(orientation != getResources().getConfiguration().orientation) {
            Toast.makeText(this, "upside inside out", Toast.LENGTH_LONG).show();
        }else {
            stopService(new Intent(this, MyLocationService.class));
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble(dist, lastDistance);
    }

    @Override
    public void update(double speed, double distance) {

        speedmeter.moveToValue((float) (3.6f*speed));
        speedmeter.setdistance(distance, "Km");

        lastDistance = distance;
        lastSpeed = speed;

    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, DigitalSpeedActivity.class);
        intent.putExtra(DigitalSpeedActivity.distLast, lastDistance);
        intent.putExtra(DigitalSpeedActivity.speedLast, lastSpeed);
        startActivity(intent);
    }

    public static boolean hasPermission(Context context, String permission) {

        int res = context.checkCallingOrSelfPermission(permission);

        Log.v(TAG, "permission: " + permission + " = \t\t" +
                (res == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));

        return res == PackageManager.PERMISSION_GRANTED;

    }

}
