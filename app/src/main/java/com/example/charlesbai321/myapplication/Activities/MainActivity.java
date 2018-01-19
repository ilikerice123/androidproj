package com.example.charlesbai321.myapplication.Activities;

import android.Manifest;
import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.charlesbai321.myapplication.Data.MonitoredLocationDao;
import com.example.charlesbai321.myapplication.Data.MonitoredLocationsDatabase;
import com.example.charlesbai321.myapplication.Util.GPSService;
import com.example.charlesbai321.myapplication.Util.GPSTracker;
import com.example.charlesbai321.myapplication.Data.MonitoredLocation;
import com.example.charlesbai321.myapplication.Util.PlaceAdapter;
import com.example.charlesbai321.myapplication.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String PLACES_KEY = "places_key";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String USE_GPS = "use_gps";
    boolean useGPS;
    public static List<MonitoredLocation> places;
    TextView places_text;
    RecyclerView rv;
    RecyclerView.LayoutManager rvManager;
    RecyclerView.Adapter dataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //initializes the display

        setupRecyclerView();
        places_text = findViewById(R.id.setofplaces);

        //initializes the list of places
        (new InitializeLocationsTask(this, dataAdapter)).execute("dummy string");
        //allows it to run on main UI thread, which may or may not be a good thing
    }

    @Override
    protected void onResume(){
        super.onResume();
        dataAdapter.notifyDataSetChanged();
        //places_text.setText(places.toString());
        //whenever the list of places gets deleted (which it shouldn't), recreate it
        if(places == null) (new InitializeLocationsTask(this, dataAdapter))
                .execute("dummy string");

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

    public static boolean hasPermission(Context c){
        return ContextCompat.checkSelfPermission(c,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void setupRecyclerView(){
        rv = findViewById(R.id.recyclerView); //obtains reference to rView
        rvManager = new LinearLayoutManager(this); //layout manager
        rv.setLayoutManager(rvManager);
        dataAdapter = new PlaceAdapter(); //data adapter
        rv.setAdapter(dataAdapter);
    }

    //AsyncTask
    //this runs on another thread to create the lists of monitored locations from android's
    //Room

    //every time this finishes executing, it notifies the adapter of that

    //https://stackoverflow.com/questions/16920942/getting-context-in-asynctask
    //from here, because I need to use context to get the database, I'm obtaining a weak
    //reference to the database which will prevent memory leaks
    private static class InitializeLocationsTask
            extends AsyncTask<String, Void, List<MonitoredLocation>>{
        private final WeakReference<Activity> weakActivity;
        RecyclerView.Adapter adapter;

        protected List<MonitoredLocation> doInBackground(String... strings) {
            //the constructor is left as a string because I don't know how to deal with
            //void objects XD
            Activity a = weakActivity.get();
            if(a == null || a.isFinishing() || a.isDestroyed()){
                return null;
            }
            return Room.databaseBuilder(a,
                    MonitoredLocationsDatabase.class, "saved_places")
                    .build().monitoredLocationDao().getListOfLocations();
        }

        @Override
        protected void onPostExecute(List<MonitoredLocation> db){
            Activity a = weakActivity.get();
            if(a == null || a.isFinishing() || a.isDestroyed() || db == null){
                return;
            }
            places = db;
            adapter.notifyDataSetChanged();
        }

        public InitializeLocationsTask(Activity a, RecyclerView.Adapter adapter){
            this.weakActivity = new WeakReference<>(a);
            this.adapter = adapter;
        }
    }
}
