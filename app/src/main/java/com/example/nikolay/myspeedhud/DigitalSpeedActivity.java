package com.example.nikolay.myspeedhud;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class DigitalSpeedActivity extends AppCompatActivity implements MyLocationService.LocationServiceDisplay, View.OnClickListener {

    private static TextView speedTV, distTV;

    public static final String distLast = "DISTANCE", speedLast = "SPEED";

    private Button btnChangeMod;
    private ToggleButton toggleButton;

    private double speed = 0, distance = 0;

    float screenBrightness = 1f;

    public  CompoundButton.OnCheckedChangeListener chekedChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            if(b){
                hideSystemUI();
                speedTV.setRotationY(180);
                distTV.setRotationY(180);
                WindowManager.LayoutParams layout = getWindow().getAttributes();
                screenBrightness = layout.screenBrightness;
                layout.screenBrightness = 1f;
                getWindow().setAttributes(layout);

            } else {

                showSystemUI();
                speedTV.setRotationY(0);
                distTV.setRotationY(0);
                WindowManager.LayoutParams layout = getWindow().getAttributes();
                layout.screenBrightness = screenBrightness;
                getWindow().setAttributes(layout);

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_speed);

        speedTV = findViewById(R.id.tv_speed);
        distTV = findViewById(R.id.tv_distance);
        btnChangeMod = findViewById(R.id.btn_change_mod);
        toggleButton = findViewById(R.id.hudToggle);
        toggleButton.setOnCheckedChangeListener(chekedChangedListener);

        btnChangeMod.setOnClickListener(this);

        MyLocationService.setSecondaryLocationServiceDisplay(this);

        if(savedInstanceState != null){
            speed = savedInstanceState.getDouble(speedLast, 0);
            distance = savedInstanceState.getDouble(distLast, 0);
            update(speed, distance);
        } else {
            Intent intent = getIntent();
            speed = intent.getDoubleExtra(speedLast, 0);
            distance = intent.getDoubleExtra(distLast, 0);
            update(speed, distance);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putDouble(distLast, distance);
        outState.putDouble(speedLast, speed);

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

    public static void setData(double speed, double distance){
        if (speedTV != null) {
            speedTV.setText(String.format("%1$.2f Km/h", speed));
            distTV.setText(String.format("%1$.2f Km", distance));
        }
    }


    @Override
    public void update(double speed, double distance) {
        setData(speed, distance);
        this.speed = speed;
       this.distance = distance;
    }

    @Override
    public void onClick(View view) {
        onBackPressed();
    }
}
