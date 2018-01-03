package com.example.charlesbai321.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String USE_GPS = "use_gps";
    boolean useGPS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //initializes the display

    }

    public void useInputLocation(View view){
        Double lat;
        Double lon;
        try {
            lat = Double.parseDouble(((EditText) findViewById(R.id.lat)).getText().toString());
            lon = Double.parseDouble(((EditText) findViewById(R.id.textlon)).getText().toString());
        }
        catch(Exception e){
            Toast.makeText(getApplicationContext(), "Error: extraneous input",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        useGPS = false;
        Intent i = new Intent(this, DisplayMessageActivity.class);
        i.putExtra(LATITUDE, lat);
        i.putExtra(LONGITUDE, lon);
        i.putExtra(USE_GPS, useGPS);
        startActivity(i);
    }

    public void useGPSLocation(View view){
        Intent i = new Intent(this, DisplayMessageActivity.class);
        useGPS = true;
        i.putExtra(USE_GPS, useGPS);
        startActivity(i);
    }
}
