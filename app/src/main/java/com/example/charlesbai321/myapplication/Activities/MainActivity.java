package com.example.charlesbai321.myapplication.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.charlesbai321.myapplication.Data.MonitoredLocationDao;
import com.example.charlesbai321.myapplication.Data.MonitoredLocationsDatabase;
import com.example.charlesbai321.myapplication.Util.AlarmReciever;
import com.example.charlesbai321.myapplication.Util.GPSService;
import com.example.charlesbai321.myapplication.Util.GPSTracker;
import com.example.charlesbai321.myapplication.Data.MonitoredLocation;
import com.example.charlesbai321.myapplication.Util.PlaceAdapter;
import com.example.charlesbai321.myapplication.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String USE_GPS = "use_gps";
    public static final String PLACES_KEY = "places_key";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String LOG = "what_is_going_ONNNN";
    public static final String GPS_TOGGLE = "are_we_using_gps???";
    public static List<MonitoredLocation> places;
    TextView places_text;
    RecyclerView rv;
    RecyclerView.LayoutManager rvManager;
    RecyclerView.Adapter dataAdapter;
    //might as well just have a database here and try to get rid of it if it's taking up
    //too memory
    static MonitoredLocationsDatabase db;
    PendingIntent locationLogIntent;

    /**
     * creates a list from the database, initializes view, that's about all this does..
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //initializes the display

        places_text = findViewById(R.id.setofplaces);
        //initializes the list of places
        (new InitializeLocationsTask(this)).execute("dummy string");
        //allows it to run on main UI thread, which may or may not be a good thing
    }

    /**
     * refresh recyclerview if recyclerview isn't null
     * if list of places has been deleted, run task to fetch it again
     * check for gps permissions
     */
    @Override
    protected void onResume(){
        super.onResume();
        if(dataAdapter != null) {
            dataAdapter.notifyDataSetChanged();
        }
        //places_text.setText(places.toString());
        //whenever the list of places gets deleted (which it shouldn't), recreate it
        if(places == null) (new InitializeLocationsTask(this))
                .execute("dummy string");

        if(!hasPermission(this)){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.
                    ACCESS_FINE_LOCATION}, GPSService.USE_GPS);
        }
    }


    /**
     * callback for request permissions, this will check if location access has been
     * granted. If not, kill app (or at least this activity, which should lead to app kill
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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

    /**
     * poorly named method, actually configures alarm manager
     * @param view
     */
    public void startGPSService(View view){
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, AlarmReciever.class);
        locationLogIntent = PendingIntent.getBroadcast(this, 0,
                i, PendingIntent.FLAG_CANCEL_CURRENT);
        am.set(AlarmManager.ELAPSED_REALTIME,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES +
                        SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                );
    }

    public void stopGPSService(View view){

    }

    /**
     * method that executes when user presses add location button. launches new activity
     * @param view
     */
    public void addLocation(View view){
        Intent i = new Intent(this, Main2Activity.class);
        startActivity(i);
    }

    /**
     * clear the list of saved locations - deletes them not only from temporary list
     * created for recycler view, but also from the database
     * @param view
     */
    public void clearList(View view){
        if(db != null){
            for(MonitoredLocation ml : places) {
                db.monitoredLocationDao().deleteMonitoredLocations(ml);
            }
            places.clear();
            Toast.makeText(this, "Clear Success", Toast.LENGTH_SHORT).show();
            dataAdapter.notifyDataSetChanged();
        }
        else Toast.makeText(this,
                "Error occured, restart app and try again", Toast.LENGTH_SHORT).show();
    }

    /**
     * checks if we have GPS locations
     * @param c
     * @return
     */
    public static boolean hasPermission(Context c){
        return ContextCompat.checkSelfPermission(c,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * a few lines to initialize the recyclerView, the layout manager, and the data adapter
     */
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
    private class InitializeLocationsTask
            extends AsyncTask<String, Void, List<MonitoredLocation>>{
        private final WeakReference<Activity> weakActivity;

        protected List<MonitoredLocation> doInBackground(String... strings) {
            //the constructor is left as a string because I don't know how to deal with
            //void objects XD
            Activity a = weakActivity.get();
            if(a == null || a.isFinishing() || a.isDestroyed()){
                return null;
            }

            db = Room.databaseBuilder(a,
                    MonitoredLocationsDatabase.class, MonitoredLocation.DATABASE_KEY)
                    .allowMainThreadQueries().build();
            return db.monitoredLocationDao().getListOfLocations();
        }

        @Override
        protected void onPostExecute(List<MonitoredLocation> db){
            Activity a = weakActivity.get();
            if(a == null || a.isFinishing() || a.isDestroyed() || db == null){
                return;
            }
            places = db;
            setupRecyclerView();
            dataAdapter.notifyDataSetChanged();
            Toast.makeText(a.getApplication(), places.toString(), Toast.LENGTH_LONG).show();
        }

        public InitializeLocationsTask(Activity a){
            this.weakActivity = new WeakReference<>(a);
        }
    }
}
