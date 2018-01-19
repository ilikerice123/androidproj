package com.example.charlesbai321.myapplication.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.charlesbai321.myapplication.Util.GPSTracker;
import com.example.charlesbai321.myapplication.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.text.DecimalFormat;

public class DisplayMessageActivity extends AppCompatActivity {

    //https://www.mkyong.com/java/java-display-double-in-2-decimal-points/
    private static DecimalFormat df2 = new DecimalFormat(".##");

    private GPSTracker gps;
    //whether we are using GPS or using manually inputted decision
    boolean useGPS;
    //current location
    private Location location;
    //place selected by user
    Place thisplace;

    //lat and lon of current location/inputted location
    private double lat;
    private double lon;

    //https://stackoverflow.com/questions/6242268/repeat-a-task-with-a-time-delay/6242292#6242292
    private Handler mHandler;
    TextView result;

    Runnable getGPSlocation = new Runnable() {
        @Override
        public void run(){
            try{
                //TODO: Get it to display on the UI
                lat = gps.getLatitude();
                lon = gps.getLongitude();
                location = gps.getLocation();
                //location should never be null
                Location thisplacelocation = new Location("");
                thisplacelocation.setLatitude(thisplace.getLatLng().latitude);
                thisplacelocation.setLongitude(thisplace.getLatLng().longitude);
                setResult((String)thisplace.getName(), location.distanceTo(thisplacelocation));
                setLocationTextView();
            }
            catch(NullPointerException e){
                Log.e("GPS", "GPS not initalized??");
            }
            catch(Exception e){
                Log.e("ERROR", "ERROR");
            }
            finally{
                mHandler.postDelayed(getGPSlocation, 5000); //5 seconds
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        Intent i = getIntent();
        useGPS = i.getExtras().getBoolean(MainActivity.USE_GPS);
        initializeResult();

        PlaceAutocompleteFragment placeSuggestion = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        //on place selected listener
        placeSuggestion.setOnPlaceSelectedListener(new PlaceSelectionListener(){
            @Override
            public void onPlaceSelected(Place place) {
                //TODO: Do something about the place that has been selected
                thisplace = place;
            }
            @Override
            public void onError(Status status) {
                //TODO: How does an error even occur???
            }
        });

        if(useGPS){
            if(!GPSTracker.hasPermission(this)) {
                //if permissions aren't enabled, the user will be prompted to allow locations
                //the callback function that executes the result of this is: onRequestPermissionsResult
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.
                        ACCESS_FINE_LOCATION}, gps.REQUEST_LOCATION);
            }
            else{
                //if permissions is enabled, then start a new GPS
                gps = new GPSTracker(this, getApplicationContext());
                lat = gps.getLatitude();
                lon = gps.getLongitude();
                location = gps.getLocation();
                setLocationTextView();
            }
        }
        else{
            //statically set lat and lon once and display it.
            lat = i.getExtras().getDouble(MainActivity.LATITUDE);
            lon = i.getExtras().getDouble(MainActivity.LONGITUDE);
            setLocationTextView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch(requestCode){
            case GPSTracker.REQUEST_LOCATION: {
                if(grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    gps = new GPSTracker(this, getApplicationContext());
                    lat = gps.getLatitude();
                    lon = gps.getLongitude();
                    setLocationTextView();
                }
                else{
                    GPSTracker.noPermissionsAlert(this);
                    finish();
                }
            }
        }
    }

    //start location updates if we are using GPS
    @Override
    protected void onResume(){
        super.onResume();
        //check if the object still exists. If it does, start updating location, if not, create
        //a new one and start updating location.
        if(useGPS) {
            if (gps != null) {
                gps.startLocationUpdates();
            } else {
                gps = new GPSTracker(this, getApplicationContext());
                gps.startLocationUpdates();
            }
            //start a handler to periodically request GPS location
            mHandler = new Handler();
            //the runnable itself calls the handler so the GPS location is periodically updated
            getGPSlocation.run();
        }
    }

    //Stop the GPS if tabbed away from activity
    @Override
    protected void onPause(){
        super.onPause();
        if(useGPS) {
            if (gps != null) {
                gps.pauseLocationUpdates();
                Toast.makeText(this, "GPS stopped", Toast.LENGTH_SHORT);
            }
            if(mHandler != null) {
                mHandler.removeCallbacks(getGPSlocation);
            }
        }
    }

    //set the textview text
    private void setLocationTextView(){
        TextView textlat = findViewById(R.id.textlat);
        TextView textlon = findViewById(R.id.textlon);
        textlat.setText(Double.toString(lat));
        textlon.setText(Double.toString(lon));
    }

    private void initializeResult(){
        result = (TextView) findViewById(R.id.textView4);
        result.setText("Pick a place!");
    }

    private void setResult(String name, double distance){
        result.setText("You are " + df2.format(distance) + " meters away from " + name + "!");
    }

}
