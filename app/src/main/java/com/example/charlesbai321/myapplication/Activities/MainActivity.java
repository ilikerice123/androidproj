package com.example.charlesbai321.myapplication.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.charlesbai321.myapplication.Util.GPSService;
import com.example.charlesbai321.myapplication.Util.GPSTracker;
import com.example.charlesbai321.myapplication.Data.MonitoredLocation;
import com.example.charlesbai321.myapplication.Util.PlaceAdapter;
import com.example.charlesbai321.myapplication.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String PLACES_KEY = "places_key";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String USE_GPS = "use_gps";
    boolean useGPS;
    public static ArrayList<MonitoredLocation> places;
    TextView places_text;
    RecyclerView rv;
    RecyclerView.LayoutManager rvManager;
    RecyclerView.Adapter dataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //initializes the display

        if(savedInstanceState != null){
            this.places = savedInstanceState.getParcelableArrayList(PLACES_KEY);
        }

        places_text = findViewById(R.id.setofplaces);
        rv = findViewById(R.id.recyclerView); //obtains reference to rView
        rvManager = new LinearLayoutManager(this); //layout manager
        rv.setLayoutManager(rvManager);
        dataAdapter = new PlaceAdapter(); //data adapter
        rv.setAdapter(dataAdapter);

        places = new ArrayList<>();
    }

    @Override
    protected void onResume(){
        super.onResume();
        dataAdapter.notifyDataSetChanged();
        //places_text.setText(places.toString());
        if(!hasPermission(this)){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.
                    ACCESS_FINE_LOCATION}, GPSService.USE_GPS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case GPSTracker.REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    //TODO: Code here to initialize GPS service
                } else {
                    GPSTracker.noPermissionsAlert(this);
                    finish();
                }
            }
        }
    }

    public void addLocation(View view){
        Intent i = new Intent(this, Main2Activity.class);
        startActivity(i);
    }

    public void useGPSLocation(View view){
        Intent i = new Intent(this, DisplayMessageActivity.class);
        useGPS = true;
        i.putExtra(USE_GPS, useGPS);
        startActivity(i);
    }

    public void clearList(View view){
        places.clear();
        dataAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putParcelableArrayList(PLACES_KEY, places);
        super.onSaveInstanceState(outState);
    }

    public static boolean hasPermission(Context c){
        return ContextCompat.checkSelfPermission(c,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
